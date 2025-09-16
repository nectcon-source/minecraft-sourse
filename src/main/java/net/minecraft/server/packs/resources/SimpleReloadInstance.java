package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S> implements ReloadInstance {
   protected final ResourceManager resourceManager;
   protected final CompletableFuture<Unit> allPreparations = new CompletableFuture<>();
   protected final CompletableFuture<List<S>> allDone;
   private final Set<PreparableReloadListener> preparingListeners;
   private final int listenerCount;
   private int startedReloads;
   private int finishedReloads;
   private final AtomicInteger startedTaskCounter = new AtomicInteger();
   private final AtomicInteger doneTaskCounter = new AtomicInteger();

   public static SimpleReloadInstance<Void> of(
      ResourceManager var0, List<PreparableReloadListener> var1, Executor var2, Executor var3, CompletableFuture<Unit> var4
   ) {
      return new SimpleReloadInstance<>(
              var2, var3, var0, var1, (var1x, var2x, var3x, var4x, var5) -> var3x.reload(var1x, var2x, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, var2, var5), var4
      );
   }

   protected SimpleReloadInstance(
      Executor var1,
      final Executor var2,
      ResourceManager var3,
      List<PreparableReloadListener> var4,
      SimpleReloadInstance.StateFactory<S> var5,
      CompletableFuture<Unit> var6
   ) {
      this.resourceManager = var3;
      this.listenerCount = var4.size();
      this.startedTaskCounter.incrementAndGet();
      var6.thenRun(this.doneTaskCounter::incrementAndGet);
      List<CompletableFuture<S>> var7 = Lists.newArrayList();
      CompletableFuture<?> var11x = var6;
      this.preparingListeners = Sets.newHashSet(var4);

      for(final PreparableReloadListener var10 : var4) {
         final CompletableFuture<?> var12xx = var11x;
         CompletableFuture<S> var12xxx = var5.create(new PreparableReloadListener.PreparationBarrier() {
            @Override
            public <T> CompletableFuture<T> wait(T var1) {
               var2.execute(() -> {
                  SimpleReloadInstance.this.preparingListeners.remove(var10);
                  if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
                     SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                  }
               });
               return SimpleReloadInstance.this.allPreparations.thenCombine(var12xx, (var1x, var2xx) -> var1);
            }
         }, var3, var10, var2x -> {
            this.startedTaskCounter.incrementAndGet();
            var1.execute(() -> {
               var2x.run();
               this.doneTaskCounter.incrementAndGet();
            });
         }, var2x -> {
            ++this.startedReloads;
            var2.execute(() -> {
               var2x.run();
               ++this.finishedReloads;
            });
         });
         var7.add(var12xxx);
         var11x = var12xxx;
      }

      this.allDone = Util.sequence(var7);
   }

   @Override
   public CompletableFuture<Unit> done() {
      return this.allDone.thenApply(var0 -> Unit.INSTANCE);
   }

   @Override
   public float getActualProgress() {
      int var1 = this.listenerCount - this.preparingListeners.size();
      float var2 = (float)(this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + var1 * 1);
      float var3 = (float)(this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1);
      return var2 / var3;
   }

   @Override
   public boolean isApplying() {
      return this.allPreparations.isDone();
   }

   @Override
   public boolean isDone() {
      return this.allDone.isDone();
   }

   @Override
   public void checkExceptions() {
      if (this.allDone.isCompletedExceptionally()) {
         this.allDone.join();
      }
   }

   public interface StateFactory<S> {
      CompletableFuture<S> create(
         PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, PreparableReloadListener var3, Executor var4, Executor var5
      );
   }
}
