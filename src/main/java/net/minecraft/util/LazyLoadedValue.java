package net.minecraft.util;

import java.util.function.Supplier;

public class LazyLoadedValue<T> {
   private Supplier<T> factory;
   private T value;

   public LazyLoadedValue(Supplier<T> var1) {
      this.factory = var1;
   }

   public T get() {
      Supplier<T> var1 = this.factory;
      if (var1 != null) {
         this.value = var1.get();
         this.factory = null;
      }

      return this.value;
   }
}
