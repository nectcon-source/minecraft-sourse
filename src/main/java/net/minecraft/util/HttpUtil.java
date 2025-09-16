package net.minecraft.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpUtil {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ListeningExecutorService DOWNLOAD_EXECUTOR = MoreExecutors.listeningDecorator(
      Executors.newCachedThreadPool(
         new ThreadFactoryBuilder()
            .setDaemon(true)
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
            .setNameFormat("Downloader %d")
            .build()
      )
   );

   public static CompletableFuture<?> downloadTo(File var0, String var1, Map<String, String> var2, int var3, @Nullable ProgressListener var4, Proxy var5) {
      return CompletableFuture.supplyAsync(() -> {
         HttpURLConnection var6 = null;
         InputStream var7 = null;
         OutputStream var8 = null;
         if (var4 != null) {
            var4.progressStart(new TranslatableComponent("resourcepack.downloading"));
            var4.progressStage(new TranslatableComponent("resourcepack.requesting"));
         }

         try {
            try {
               byte[] var9 = new byte[4096];
               URL var25 = new URL(var1);
               var6 = (HttpURLConnection)var25.openConnection(var5);
               var6.setInstanceFollowRedirects(true);
               float var11 = 0.0F;
               float var12 = (float)var2.entrySet().size();

               for(Map.Entry<String, String> var14 : var2.entrySet()) {
                  var6.setRequestProperty((String)var14.getKey(), (String)var14.getValue());
                  if (var4 != null) {
                     var4.progressStagePercentage((int)(++var11 / var12 * 100.0F));
                  }
               }

               var7 = var6.getInputStream();
               var12 = (float)var6.getContentLength();
               int var27 = var6.getContentLength();
               if (var4 != null) {
                  var4.progressStage(new TranslatableComponent("resourcepack.progress", new Object[]{String.format(Locale.ROOT, "%.2f", var12 / 1000.0F / 1000.0F)}));
               }

               if (var0.exists()) {
                  long var28 = var0.length();
                  if (var28 == (long)var27) {
                     if (var4 != null) {
                        var4.stop();
                     }

                     Object var16 = null;
                     return var16;
                  }

                  LOGGER.warn("Deleting {} as it does not match what we currently have ({} vs our {}).", var0, var27, var28);
                  FileUtils.deleteQuietly(var0);
               } else if (var0.getParentFile() != null) {
                  var0.getParentFile().mkdirs();
               }

               var8 = new DataOutputStream(new FileOutputStream(var0));
               if (var3 > 0 && var12 > (float)var3) {
                  if (var4 != null) {
                     var4.stop();
                  }

                  throw new IOException("Filesize is bigger than maximum allowed (file is " + var11 + ", limit is " + var3 + ")");
               }

               int var29;
               while((var29 = var7.read(var9)) >= 0) {
                  var11 += (float)var29;
                  if (var4 != null) {
                     var4.progressStagePercentage((int)(var11 / var12 * 100.0F));
                  }

                  if (var3 > 0 && var11 > (float)var3) {
                     if (var4 != null) {
                        var4.stop();
                     }

                     throw new IOException("Filesize was bigger than maximum allowed (got >= " + var11 + ", limit was " + var3 + ")");
                  }

                  if (Thread.interrupted()) {
                     LOGGER.error("INTERRUPTED");
                     if (var4 != null) {
                        var4.stop();
                     }

                     Object var15 = null;
                     return var15;
                  }

                  var8.write(var9, 0, var29);
               }

               if (var4 != null) {
                  var4.stop();
                  return null;
               }
            } catch (Throwable var9_1) {
               var9_1.printStackTrace();
               if (var6 != null) {
                  InputStream var10 = var6.getErrorStream();

                  try {
                     LOGGER.error(IOUtils.toString(var10));
                  } catch (IOException var11_1) {
                     var11_1.printStackTrace();
                  }
               }

               if (var4 != null) {
                  var4.stop();
                  return null;
               }
            }

            return null;
         } finally {
            IOUtils.closeQuietly(var7);
            IOUtils.closeQuietly(var8);
         }
      }, DOWNLOAD_EXECUTOR);
   }

   public static int getAvailablePort() {
      try (ServerSocket var0 = new ServerSocket(0)) {
         return var0.getLocalPort();
      } catch (IOException var14) {
         return 25564;
      }
   }
}
