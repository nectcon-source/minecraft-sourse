package net.minecraft.util.profiling;

import java.util.function.Supplier;

public class InactiveProfiler implements ProfileCollector {
   public static final InactiveProfiler INSTANCE = new InactiveProfiler();

   private InactiveProfiler() {
   }

   @Override
   public void startTick() {
   }

   @Override
   public void endTick() {
   }

   @Override
   public void push(String var1) {
   }

   @Override
   public void push(Supplier<String> var1) {
   }

   @Override
   public void pop() {
   }

   @Override
   public void popPush(String var1) {
   }

   @Override
   public void popPush(Supplier<String> var1) {
   }

   @Override
   public void incrementCounter(String var1) {
   }

   @Override
   public void incrementCounter(Supplier<String> var1) {
   }

   @Override
   public ProfileResults getResults() {
      return EmptyProfileResults.EMPTY;
   }
}
