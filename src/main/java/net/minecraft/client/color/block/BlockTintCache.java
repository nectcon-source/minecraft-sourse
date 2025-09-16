package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class BlockTintCache {
   private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(() -> new BlockTintCache.LatestCacheInfo());
   private final Long2ObjectLinkedOpenHashMap<int[]> cache = new Long2ObjectLinkedOpenHashMap(256, 0.25F);
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   public int getColor(BlockPos var1, IntSupplier var2) {
      int var3 = var1.getX() >> 4;
      int var4x = var1.getZ() >> 4;
      BlockTintCache.LatestCacheInfo var5xx = this.latestChunkOnThread.get();
      if (var5xx.x != var3 || var5xx.z != var4x) {
         var5xx.x = var3;
         var5xx.z = var4x;
         var5xx.cache = this.findOrCreateChunkCache(var3, var4x);
      }

      int var6 = var1.getX() & 15;
      int var7x = var1.getZ() & 15;
      int var8xx = var7x << 4 | var6;
      int var9xxx = var5xx.cache[var8xx];
      if (var9xxx != -1) {
         return var9xxx;
      } else {
         int var10 = var2.getAsInt();
         var5xx.cache[var8xx] = var10;
         return var10;
      }
   }

   public void invalidateForChunk(int var1, int var2) {
      try {
         this.lock.writeLock().lock();

         for(int var3 = -1; var3 <= 1; ++var3) {
            for(int var4x = -1; var4x <= 1; ++var4x) {
               long var5xx = ChunkPos.asLong(var1 + var3, var2 + var4x);
               this.cache.remove(var5xx);
            }
         }
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   public void invalidateAll() {
      try {
         this.lock.writeLock().lock();
         this.cache.clear();
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   private int[] findOrCreateChunkCache(int var1, int var2) {
      long var3 = ChunkPos.asLong(var1, var2);
      this.lock.readLock().lock();

      int[] var5;
      try {
         var5 = (int[])this.cache.get(var3);
      } finally {
         this.lock.readLock().unlock();
      }

      if (var5 != null) {
         return var5;
      } else {
         int[] var6x = new int[256];
         Arrays.fill(var6x, -1);

         try {
            this.lock.writeLock().lock();
            if (this.cache.size() >= 256) {
               this.cache.removeFirst();
            }

            this.cache.put(var3, var6x);
         } finally {
            this.lock.writeLock().unlock();
         }

         return var6x;
      }
   }

   static class LatestCacheInfo {
      public int x = Integer.MIN_VALUE;
      public int z = Integer.MIN_VALUE;
      public int[] cache;

      private LatestCacheInfo() {
      }
   }
}
