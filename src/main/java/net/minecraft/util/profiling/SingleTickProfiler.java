package net.minecraft.util.profiling;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingleTickProfiler {
   private static final Logger LOGGER = LogManager.getLogger();
   private final LongSupplier realTime = null;
   private final long saveThreshold = 0L;
   private int tick;
   private final File location = null;
   private ProfileCollector profiler;

   public ProfilerFiller startTick() {
      this.profiler = new ActiveProfiler(this.realTime, () -> this.tick, false);
      ++this.tick;
      return this.profiler;
   }

   public void endTick() {
      if (this.profiler != InactiveProfiler.INSTANCE) {
         ProfileResults var1 = this.profiler.getResults();
         this.profiler = InactiveProfiler.INSTANCE;
         if (var1.getNanoDuration() >= this.saveThreshold) {
            File var2x = new File(this.location, "tick-results-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt");
            var1.saveResults(var2x);
            LOGGER.info("Recorded long tick -- wrote info to: {}", var2x.getAbsolutePath());
         }
      }
   }

   @Nullable
   public static SingleTickProfiler createTickProfiler(String var0) {
      return null;
   }

   public static ProfilerFiller decorateFiller(ProfilerFiller var0, @Nullable SingleTickProfiler var1) {
      return var1 != null ? ProfilerFiller.tee(var1.startTick(), var0) : var0;
   }
}
