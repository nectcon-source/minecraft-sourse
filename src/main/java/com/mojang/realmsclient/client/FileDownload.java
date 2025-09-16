//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileDownload {
   private static final Logger LOGGER = LogManager.getLogger();
   private volatile boolean cancelled;
   private volatile boolean finished;
   private volatile boolean error;
   private volatile boolean extracting;
   private volatile File tempFile;
   private volatile File resourcePackPath;
   private volatile HttpGet request;
   private Thread currentThread;
   private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public FileDownload() {
   }

   public long contentLength(String var1) {
      CloseableHttpClient var2 = null;
      HttpGet var3 = null;

      long var5;
      try {
         var3 = new HttpGet(var1);
         var2 = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
         CloseableHttpResponse var4 = var2.execute(var3);
         var5 = Long.parseLong(var4.getFirstHeader("Content-Length").getValue());
         return var5;
      } catch (Throwable var16) {
         LOGGER.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (var3 != null) {
            var3.releaseConnection();
         }

         if (var2 != null) {
            try {
               var2.close();
            } catch (IOException var9_1) {
               LOGGER.error("Could not close http client", var9_1);
            }
         }

      }

      return var5;
   }

   public void download(WorldDownload var1, String var2, RealmsDownloadLatestWorldScreen.DownloadStatus var3, LevelStorageSource var4) {
      if (this.currentThread == null) {
         this.currentThread = new Thread(() -> {
            CloseableHttpClient var5 = null;

            try {
               this.tempFile = File.createTempFile("backup", ".tar.gz");
               this.request = new HttpGet(var1.downloadLink);
               var5 = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
               HttpResponse var6 = var5.execute(this.request);
               var3.totalBytes = Long.parseLong(var6.getFirstHeader("Content-Length").getValue());
               if (var6.getStatusLine().getStatusCode() == 200) {
                  OutputStream var7 = new FileOutputStream(this.tempFile);
                  ProgressListener var8 = new ProgressListener(var2.trim(), this.tempFile, var4, var3);
                  DownloadCountingOutputStream var9 = new DownloadCountingOutputStream(var7);
                  var9.setListener(var8);
                  IOUtils.copy(var6.getEntity().getContent(), var9);
                  return;
               }

               this.error = true;
               this.request.abort();
            } catch (Exception var6_1) {
               LOGGER.error("Caught exception while downloading: " + var6_1.getMessage());
               this.error = true;
               return;
            } finally {
               this.request.releaseConnection();
               if (this.tempFile != null) {
                  this.tempFile.delete();
               }

               if (!this.error) {
                  if (!var1.resourcePackUrl.isEmpty() && !var1.resourcePackHash.isEmpty()) {
                     try {
                        this.tempFile = File.createTempFile("resources", ".tar.gz");
                        this.request = new HttpGet(var1.resourcePackUrl);
                        HttpResponse var15 = var5.execute(this.request);
                        var3.totalBytes = Long.parseLong(var15.getFirstHeader("Content-Length").getValue());
                        if (var15.getStatusLine().getStatusCode() != 200) {
                           this.error = true;
                           this.request.abort();
                           return;
                        }

                        OutputStream var16 = new FileOutputStream(this.tempFile);
                        ResourcePackProgressListener var17 = new ResourcePackProgressListener(this.tempFile, var3, var1);
                        DownloadCountingOutputStream var18 = new DownloadCountingOutputStream(var16);
                        var18.setListener(var17);
                        IOUtils.copy(var15.getEntity().getContent(), var18);
                     } catch (Exception var15_1) {
                        LOGGER.error("Caught exception while downloading: " + var15_1.getMessage());
                        this.error = true;
                     } finally {
                        this.request.releaseConnection();
                        if (this.tempFile != null) {
                           this.tempFile.delete();
                        }

                     }
                  } else {
                     this.finished = true;
                  }
               }

               if (var5 != null) {
                  try {
                     var5.close();
                  } catch (IOException var90) {
                     LOGGER.error("Failed to close Realms download client");
                  }
               }

            }

         });
         this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
         this.currentThread.start();
      }
   }

   public void cancel() {
      if (this.request != null) {
         this.request.abort();
      }

      if (this.tempFile != null) {
         this.tempFile.delete();
      }

      this.cancelled = true;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public boolean isError() {
      return this.error;
   }

   public boolean isExtracting() {
      return this.extracting;
   }

   public static String findAvailableFolderName(String var0) {
      var0 = var0.replaceAll("[\\./\"]", "_");

      for(String var4 : INVALID_FILE_NAMES) {
         if (var0.equalsIgnoreCase(var4)) {
            var0 = "_" + var0 + "_";
         }
      }

      return var0;
   }

   private void untarGzipArchive(String var1, File var2, LevelStorageSource var3) throws IOException {
      Pattern var4 = Pattern.compile(".*-([0-9]+)$");
      int var6 = 1;

      for(char var10 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
         var1 = var1.replace(var10, '_');
      }

      if (StringUtils.isEmpty(var1)) {
         var1 = "Realm";
      }

      var1 = findAvailableFolderName(var1);

      try {
         for(LevelSummary var133 : var3.getLevelList()) {
            if (var133.getLevelId().toLowerCase(Locale.ROOT).startsWith(var1.toLowerCase(Locale.ROOT))) {
               Matcher var135 = var4.matcher(var133.getLevelId());
               if (var135.matches()) {
                  if (Integer.valueOf(var135.group(1)) > var6) {
                     var6 = Integer.valueOf(var135.group(1));
                  }
               } else {
                  ++var6;
               }
            }
         }
      } catch (Exception var7_4) {
         LOGGER.error("Error getting level list", var7_4);
         this.error = true;
         return;
      }

      String var5;
      if (var3.isNewLevelIdAcceptable(var1) && var6 <= 1) {
         var5 = var1;
      } else {
         var5 = var1 + (var6 == 1 ? "" : "-" + var6);
         if (!var3.isNewLevelIdAcceptable(var5)) {
            boolean var131 = false;

            while(!var131) {
               ++var6;
               var5 = var1 + (var6 == 1 ? "" : "-" + var6);
               if (var3.isNewLevelIdAcceptable(var5)) {
                  var131 = true;
               }
            }
         }
      }

      TarArchiveInputStream var132 = null;
      File var134 = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "saves");

      try {
         var134.mkdir();
         var132 = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(var2))));

         for(TarArchiveEntry var136 = var132.getNextTarEntry(); var136 != null; var136 = var132.getNextTarEntry()) {
            File var137 = new File(var134, var136.getName().replace("world", var5));
            if (var136.isDirectory()) {
               var137.mkdirs();
            } else {
               var137.createNewFile();
               FileOutputStream var11 = new FileOutputStream(var137);
               Throwable var12 = null;

               try {
                  IOUtils.copy(var132, var11);
               } catch (Throwable var122) {
                  var12 = var122;
                  throw var122;
               } finally {
                  if (var11 != null) {
                     if (var12 != null) {
                        try {
                           var11.close();
                        } catch (Throwable var121) {
                           var12.addSuppressed(var121);
                        }
                     } else {
                        var11.close();
                     }
                  }

               }
            }
         }
      } catch (Exception var9_10) {
         LOGGER.error("Error extracting world", var9_10);
         this.error = true;
      } finally {
         if (var132 != null) {
            var132.close();
         }

         if (var2 != null) {
            var2.delete();
         }

         try (LevelStorageSource.LevelStorageAccess var21 = var3.createAccess(var5)) {
            var21.renameLevel(var5.trim());
            Path var23 = var21.getLevelPath(LevelResource.LEVEL_DATA_FILE);
            deletePlayerTag(var23.toFile());
         } catch (IOException var21_1) {
            LOGGER.error("Failed to rename unpacked realms level {}", var5, var21_1);
         }

         this.resourcePackPath = new File(var134, var5 + File.separator + "resources.zip");
      }

   }

   private static void deletePlayerTag(File var0) {
      if (var0.exists()) {
         try {
            CompoundTag var1 = NbtIo.readCompressed(var0);
            CompoundTag var2 = var1.getCompound("Data");
            var2.remove("Player");
            NbtIo.writeCompressed(var1, var0);
         } catch (Exception var1_1) {
            var1_1.printStackTrace();
         }
      }

   }

   class ProgressListener implements ActionListener {
      private final String worldName;
      private final File tempFile;
      private final LevelStorageSource levelStorageSource;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

      private ProgressListener(String var2, File var3, LevelStorageSource var4, RealmsDownloadLatestWorldScreen.DownloadStatus var5) {
         this.worldName = var2;
         this.tempFile = var3;
         this.levelStorageSource = var4;
         this.downloadStatus = var5;
      }

      public void actionPerformed(ActionEvent var1) {
         this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)var1.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
            try {
               FileDownload.this.extracting = true;
               FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
            } catch (IOException var2_1) {
               FileDownload.LOGGER.error("Error extracting archive", var2_1);
               FileDownload.this.error = true;
            }
         }

      }
   }

   class ResourcePackProgressListener implements ActionListener {
      private final File tempFile;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
      private final WorldDownload worldDownload;

      private ResourcePackProgressListener(File var2, RealmsDownloadLatestWorldScreen.DownloadStatus var3, WorldDownload var4) {
         this.tempFile = var2;
         this.downloadStatus = var3;
         this.worldDownload = var4;
      }

      public void actionPerformed(ActionEvent var1) {
         this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)var1.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
            try {
               String var2 = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
               if (var2.equals(this.worldDownload.resourcePackHash)) {
                  FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                  FileDownload.this.finished = true;
               } else {
                  FileDownload.LOGGER.error("Resourcepack had wrong hash (expected " + this.worldDownload.resourcePackHash + ", found " + var2 + "). Deleting it.");
                  FileUtils.deleteQuietly(this.tempFile);
                  FileDownload.this.error = true;
               }
            } catch (IOException var2_1) {
               FileDownload.LOGGER.error("Error copying resourcepack file", var2_1.getMessage());
               FileDownload.this.error = true;
            }
         }

      }
   }

   class DownloadCountingOutputStream extends CountingOutputStream {
      private ActionListener listener;

      public DownloadCountingOutputStream(OutputStream var2) {
         super(var2);
      }

      public void setListener(ActionListener var1) {
         this.listener = var1;
      }

      protected void afterWrite(int var1) throws IOException {
         super.afterWrite(var1);
         if (this.listener != null) {
            this.listener.actionPerformed(new ActionEvent(this, 0, null));
         }

      }
   }
}
