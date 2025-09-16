package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue<T> {
   public static final int PRIORITY_LEVEL_COUNT = ChunkMap.MAX_CHUNK_DISTANCE + 2;
//   private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = IntStream.range(0, PRIORITY_LEVEL_COUNT)
//      .mapToObj(var0 -> new Long2ObjectLinkedOpenHashMap()).collect(Collectors.toList());
private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = IntStream.range(0, PRIORITY_LEVEL_COUNT)
        .<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>>mapToObj(var0 -> new Long2ObjectLinkedOpenHashMap<>())
        .collect(Collectors.toList());
   private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
   private final String name;
   private final LongSet acquired = new LongOpenHashSet();
   private final int maxTasks;

   public ChunkTaskPriorityQueue(String var1, int var2) {
      this.name = var1;
      this.maxTasks = var2;
   }

   protected void resortChunkTasks(int var1, ChunkPos var2, int var3) {
      if (var1 < PRIORITY_LEVEL_COUNT) {
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> var4 = this.taskQueue.get(var1);
         List<Optional<T>> var5 = var4.remove(var2.toLong());
         if (var1 == this.firstQueue) {
            while(this.firstQueue < PRIORITY_LEVEL_COUNT && (this.taskQueue.get(this.firstQueue)).isEmpty()) {
               ++this.firstQueue;
            }
         }

         if (var5 != null && !var5.isEmpty()) {
            ((List)((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(var3)).computeIfAbsent(var2.toLong(), (var0) -> Lists.newArrayList())).addAll(var5);
            this.firstQueue = Math.min(this.firstQueue, var3);
         }
      }
   }

   protected void submit(Optional<T> var1, long var2, int var4) {
      this.taskQueue.get(var4).computeIfAbsent(var2, (var0) -> Lists.newArrayList()).add(var1);
      this.firstQueue = Math.min(this.firstQueue, var4);
   }

   protected void release(long var1, boolean var3) {
      for(Long2ObjectLinkedOpenHashMap<List<Optional<T>>> var5 : this.taskQueue) {
         List<Optional<T>> var6 = (List)var5.get(var1);
         if (var6 != null) {
            if (var3) {
               var6.clear();
            } else {
               var6.removeIf((var0) -> !var0.isPresent());
            }

            if (var6.isEmpty()) {
               var5.remove(var1);
            }
         }
      }

      while(this.firstQueue < PRIORITY_LEVEL_COUNT && ((Long2ObjectLinkedOpenHashMap)this.taskQueue.get(this.firstQueue)).isEmpty()) {
         ++this.firstQueue;
      }

      this.acquired.remove(var1);
   }

   private Runnable acquire(long var1) {
      return () -> this.acquired.add(var1);
   }

   @Nullable
   public Stream<Either<T, Runnable>> pop() {
      if (this.acquired.size() >= this.maxTasks) {
         return null;
      } else if (this.firstQueue >= PRIORITY_LEVEL_COUNT) {
         return null;
      } else {
         int var1 = this.firstQueue;
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> var2 = this.taskQueue.get(var1);
         long var3 = var2.firstLongKey();

         List<Optional<T>> var5;
         for(var5 = var2.removeFirst(); this.firstQueue < PRIORITY_LEVEL_COUNT && this.taskQueue.get(this.firstQueue).isEmpty(); ++this.firstQueue) {
         }

         return var5.stream().map((var3x) -> (Either)var3x.map(Either::left).orElseGet(() -> Either.right(this.acquire(var3))));
      }
   }

   @Override
   public String toString() {
      return this.name + " " + this.firstQueue + "...";
   }

   @VisibleForTesting
   LongSet getAcquired() {
      return new LongOpenHashSet(this.acquired);
   }
}
