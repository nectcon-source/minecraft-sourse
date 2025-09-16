package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProfiledReloadInstance extends SimpleReloadInstance<ProfiledReloadInstance.State> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Stopwatch total = Stopwatch.createUnstarted();

   public ProfiledReloadInstance(ResourceManager var1, List<PreparableReloadListener> var2, Executor var3, Executor var4, CompletableFuture<Unit> var5) {
      super(var3, var4, var1, var2, (var1x, var2x, var3x, var4x, var5x) -> {
         AtomicLong var6 = new AtomicLong();
         AtomicLong var7x = new AtomicLong();
         ActiveProfiler var8xx = new ActiveProfiler(Util.timeSource, () -> 0, false);
         ActiveProfiler var9xxx = new ActiveProfiler(Util.timeSource, () -> 0, false);
         CompletableFuture<Void> var10xxxx = var3x.reload(var1x, var2x, var8xx, var9xxx, var2xx -> var4x.execute(() -> {
               long var2xxx = Util.getNanos();
               var2xx.run();
            var6.addAndGet(Util.getNanos() - var2xxx);
            }), var2xx -> var5x.execute(() -> {
               long var2xxx = Util.getNanos();
               var2xx.run();
            var7x.addAndGet(Util.getNanos() - var2xxx);
            }));
         return var10xxxx.thenApplyAsync(var5xx -> new ProfiledReloadInstance.State(var3x.getName(), var8xx.getResults(), var9xxx.getResults(), var6, var7x), var4);
      }, var5);
      this.total.start();
      this.allDone.thenAcceptAsync(this::finish, var4);
   }

   private void finish(List<ProfiledReloadInstance.State> var1) {
      this.total.stop();
      int var2 = 0;
      LOGGER.info("Resource reload finished after " + this.total.elapsed(TimeUnit.MILLISECONDS) + " ms");

      for(ProfiledReloadInstance.State var4 : var1) {
//         ProfileResults var5 = var4.preparationResult;
//         ProfileResults var6 = var4.reloadResult;
         int var7 = (int)((double)var4.preparationNanos.get() / (double)1000000.0F);
         int var8 = (int)((double)var4.reloadNanos.get() / (double)1000000.0F);
         int var9 = var7 + var8;
         String var10 = var4.name;
         LOGGER.info(var10 + " took approximately " + var9 + " ms (" + var7 + " ms preparing, " + var8 + " ms applying)");
         var2 += var8;
      }

      LOGGER.info("Total blocking time: " + var2 + " ms");
   }

   public static class State {
      private final String name;
      private final ProfileResults preparationResult;
      private final ProfileResults reloadResult;
      private final AtomicLong preparationNanos;
      private final AtomicLong reloadNanos;

      private State(String var1, ProfileResults var2, ProfileResults var3, AtomicLong var4, AtomicLong var5) {
         this.name = var1;
         this.preparationResult = var2;
         this.reloadResult = var3;
         this.preparationNanos = var4;
         this.reloadNanos = var5;
      }
   }
}
