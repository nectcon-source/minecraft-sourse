package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActiveProfiler implements ProfileCollector {
   private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<String> paths = Lists.newArrayList();
   private final LongList startTimes = new LongArrayList();
   private final Map<String, ActiveProfiler.PathEntry> entries = Maps.newHashMap();
   private final IntSupplier getTickTime;
   private final LongSupplier getRealTime;
   private final long startTimeNano;
   private final int startTimeTicks;
   private String path = "";
   private boolean started;
   @Nullable
   private ActiveProfiler.PathEntry currentEntry;
   private final boolean warn;

   public ActiveProfiler(LongSupplier var1, IntSupplier var2, boolean var3) {
      this.startTimeNano = var1.getAsLong();
      this.getRealTime = var1;
      this.startTimeTicks = var2.getAsInt();
      this.getTickTime = var2;
      this.warn = var3;
   }

   @Override
   public void startTick() {
      if (this.started) {
         LOGGER.error("Profiler tick already started - missing endTick()?");
      } else {
         this.started = true;
         this.path = "";
         this.paths.clear();
         this.push("root");
      }
   }

   @Override
   public void endTick() {
      if (!this.started) {
         LOGGER.error("Profiler tick already ended - missing startTick()?");
      } else {
         this.pop();
         this.started = false;
         if (!this.path.isEmpty()) {
            LOGGER.error(
               "Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?",
               new org.apache.logging.log4j.util.Supplier[]{() -> ProfileResults.demanglePath(this.path)}
            );
         }
      }
   }

   @Override
   public void push(String var1) {
      if (!this.started) {
         LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", var1);
      } else {
         if (!this.path.isEmpty()) {
            this.path = this.path + '\u001e';
         }

         this.path = this.path + var1;
         this.paths.add(this.path);
         this.startTimes.add(Util.getNanos());
         this.currentEntry = null;
      }
   }

   @Override
   public void push(Supplier<String> var1) {
      this.push(var1.get());
   }

   @Override
   public void pop() {
      if (!this.started) {
         LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
      } else if (this.startTimes.isEmpty()) {
         LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
      } else {
         long var1 = Util.getNanos();
         long var3x = this.startTimes.removeLong(this.startTimes.size() - 1);
         this.paths.remove(this.paths.size() - 1);
         long var5xx = var1 - var3x;
         ActiveProfiler.PathEntry var7xxx = this.getCurrentEntry();
         var7xxx.duration = var7xxx.duration + var5xx;
         var7xxx.count = var7xxx.count + 1L;
         if (this.warn && var5xx > WARNING_TIME_NANOS) {
            LOGGER.warn(
               "Something's taking too long! '{}' took aprox {} ms",
               new org.apache.logging.log4j.util.Supplier[]{() -> ProfileResults.demanglePath(this.path), () -> (double)var5xx / 1000000.0}
            );
         }

         this.path = this.paths.isEmpty() ? "" : this.paths.get(this.paths.size() - 1);
         this.currentEntry = null;
      }
   }

   @Override
   public void popPush(String var1) {
      this.pop();
      this.push(var1);
   }

   @Override
   public void popPush(Supplier<String> var1) {
      this.pop();
      this.push(var1);
   }

   private ActiveProfiler.PathEntry getCurrentEntry() {
      if (this.currentEntry == null) {
         this.currentEntry = this.entries.computeIfAbsent(this.path, var0 -> new ActiveProfiler.PathEntry());
      }

      return this.currentEntry;
   }

   @Override
   public void incrementCounter(String var1) {
      this.getCurrentEntry().counters.addTo(var1, 1L);
   }

   @Override
   public void incrementCounter(Supplier<String> var1) {
      this.getCurrentEntry().counters.addTo(var1.get(), 1L);
   }

   @Override
   public ProfileResults getResults() {
      return new FilledProfileResults(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
   }

   static class PathEntry implements ProfilerPathEntry {
      private long duration;
      private long count;
      private Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap();

      private PathEntry() {
      }

      @Override
      public long getDuration() {
         return this.duration;
      }

      @Override
      public long getCount() {
         return this.count;
      }

      @Override
      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.unmodifiable(this.counters);
      }
   }
}
