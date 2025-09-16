package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpgrader {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
   private final ImmutableSet<ResourceKey<Level>> levels;
   private final boolean eraseCache;
   private final LevelStorageSource.LevelStorageAccess levelStorage;
   private final Thread thread;
   private final DataFixer dataFixer;
   private volatile boolean running = true;
   private volatile boolean finished;
   private volatile float progress;
   private volatile int totalChunks;
   private volatile int converted;
   private volatile int skipped;
   private final Object2FloatMap<ResourceKey<Level>> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityStrategy()));
   private volatile Component status = new TranslatableComponent("optimizeWorld.stage.counting");
   private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
   private final DimensionDataStorage overworldDataStorage;

   public WorldUpgrader(LevelStorageSource.LevelStorageAccess var1, DataFixer var2, ImmutableSet<ResourceKey<Level>> var3, boolean var4) {
      this.levels = var3;
      this.eraseCache = var4;
      this.dataFixer = var2;
      this.levelStorage = var1;
      this.overworldDataStorage = new DimensionDataStorage(new File(this.levelStorage.getDimensionPath(Level.OVERWORLD), "data"), var2);
      this.thread = THREAD_FACTORY.newThread(this::work);
      this.thread.setUncaughtExceptionHandler((var1x, var2x) -> {
         LOGGER.error("Error upgrading world", var2x);
         this.status = new TranslatableComponent("optimizeWorld.stage.failed");
         this.finished = true;
      });
      this.thread.start();
   }

   public void cancel() {
      this.running = false;

      try {
         this.thread.join();
      } catch (InterruptedException var2) {
      }
   }

   private void work() {
      this.totalChunks = 0;
      ImmutableMap.Builder<ResourceKey<Level>, ListIterator<ChunkPos>> var1 = ImmutableMap.builder();

      List<ChunkPos> var4;
      for(UnmodifiableIterator var2 = this.levels.iterator(); var2.hasNext(); this.totalChunks += var4.size()) {
         ResourceKey<Level> var3 = (ResourceKey)var2.next();
         var4 = this.getAllChunkPos(var3);
         var1.put(var3, var4.listIterator());
      }

      if (this.totalChunks == 0) {
         this.finished = true;
      } else {
         float var25 = (float)this.totalChunks;
         ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> var26 = var1.build();
         ImmutableMap.Builder<ResourceKey<Level>, ChunkStorage> var27 = ImmutableMap.builder();
         UnmodifiableIterator var5 = this.levels.iterator();

         while(var5.hasNext()) {
            ResourceKey<Level> var6 = (ResourceKey)var5.next();
            File var7 = this.levelStorage.getDimensionPath(var6);
            var27.put(var6, new ChunkStorage(new File(var7, "region"), this.dataFixer, true));
         }

         ImmutableMap<ResourceKey<Level>, ChunkStorage> var28 = var27.build();
         long var29 = Util.getMillis();
         this.status = new TranslatableComponent("optimizeWorld.stage.upgrading");

         while(this.running) {
            boolean var8 = false;
            float var9 = 0.0F;

            float var33;
            for(UnmodifiableIterator var10 = this.levels.iterator(); var10.hasNext(); var9 += var33) {
               ResourceKey<Level> var11 = (ResourceKey)var10.next();
               ListIterator<ChunkPos> var12 = (ListIterator)var26.get(var11);
               ChunkStorage var13 = (ChunkStorage)var28.get(var11);
               if (var12.hasNext()) {
                  ChunkPos var14 = (ChunkPos)var12.next();
                  boolean var15 = false;

                  try {
                     CompoundTag var16 = var13.read(var14);
                     if (var16 != null) {
                        int var34 = ChunkStorage.getVersion(var16);
                        CompoundTag var18 = var13.upgradeChunkTag(var11, () -> this.overworldDataStorage, var16);
                        CompoundTag var19 = var18.getCompound("Level");
                        ChunkPos var20 = new ChunkPos(var19.getInt("xPos"), var19.getInt("zPos"));
                        if (!var20.equals(var14)) {
                           LOGGER.warn("Chunk {} has invalid position {}", var14, var20);
                        }

                        boolean var21 = var34 < SharedConstants.getCurrentVersion().getWorldVersion();
                        if (this.eraseCache) {
                           var21 = var21 || var19.contains("Heightmaps");
                           var19.remove("Heightmaps");
                           var21 = var21 || var19.contains("isLightOn");
                           var19.remove("isLightOn");
                        }

                        if (var21) {
                           var13.write(var14, var18);
                           var15 = true;
                        }
                     }
                  } catch (ReportedException var16_3) {
                     Throwable var17 = var16_3.getCause();
                     if (!(var17 instanceof IOException)) {
                        throw var16_3;
                     }

                     LOGGER.error("Error upgrading chunk {}", var14, var17);
                  } catch (IOException var16_1) {
                     LOGGER.error("Error upgrading chunk {}", var14, var16_1);
                  }

                  if (var15) {
                     ++this.converted;
                  } else {
                     ++this.skipped;
                  }

                  var8 = true;
               }

               var33 = (float)var12.nextIndex() / var25;
               this.progressMap.put(var11, var33);
            }

            this.progress = var9;
            if (!var8) {
               this.running = false;
            }
         }

         this.status = new TranslatableComponent("optimizeWorld.stage.finished");
         UnmodifiableIterator var31 = var28.values().iterator();

         while(var31.hasNext()) {
            ChunkStorage var32 = (ChunkStorage)var31.next();

            try {
               var32.close();
            } catch (IOException var10_4) {
               LOGGER.error("Error upgrading chunk", var10_4);
            }
         }

         this.overworldDataStorage.save();
         var29 = Util.getMillis() - var29;
         LOGGER.info("World optimizaton finished after {} ms", var29);
         this.finished = true;
      }
   }

   private List<ChunkPos> getAllChunkPos(ResourceKey<Level> var1) {
      File var2 = this.levelStorage.getDimensionPath(var1);
      File var3 = new File(var2, "region");
      File[] var4 = var3.listFiles((var0, var1x) -> var1x.endsWith(".mca"));
      if (var4 == null) {
         return ImmutableList.of();
      } else {
         List<ChunkPos> var5 = Lists.newArrayList();

         for(File var9 : var4) {
            Matcher var10 = REGEX.matcher(var9.getName());
            if (var10.matches()) {
               int var11 = Integer.parseInt(var10.group(1)) << 5;
               int var12 = Integer.parseInt(var10.group(2)) << 5;

               try (RegionFile var13 = new RegionFile(var9, var3, true)) {
                  for(int var15 = 0; var15 < 32; ++var15) {
                     for(int var16 = 0; var16 < 32; ++var16) {
                        ChunkPos var17 = new ChunkPos(var15 + var11, var16 + var12);
                        if (var13.doesChunkExist(var17)) {
                           var5.add(var17);
                        }
                     }
                  }
               } catch (Throwable var28) {
               }
            }
         }

         return var5;
      }
   }

   public boolean isFinished() {
      return this.finished;
   }

   public ImmutableSet<ResourceKey<Level>> levels() {
      return this.levels;
   }

   public float dimensionProgress(ResourceKey<Level> var1) {
      return this.progressMap.getFloat(var1);
   }

   public float getProgress() {
      return this.progress;
   }

   public int getTotalChunks() {
      return this.totalChunks;
   }

   public int getConverted() {
      return this.converted;
   }

   public int getSkipped() {
      return this.skipped;
   }

   public Component getStatus() {
      return this.status;
   }
}
