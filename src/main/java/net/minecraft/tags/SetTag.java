package net.minecraft.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

public class SetTag<T> implements Tag<T> {
   private final ImmutableList<T> valuesList;
   private final Set<T> values;
   @VisibleForTesting
   protected final Class<?> closestCommonSuperType;

   protected SetTag(Set<T> var1, Class<?> var2) {
      this.closestCommonSuperType = var2;
      this.values = var1;
      this.valuesList = ImmutableList.copyOf(var1);
   }

   public static <T> SetTag<T> empty() {
      return new SetTag<>(ImmutableSet.of(), Void.class);
   }

   public static <T> SetTag<T> create(Set<T> var0) {
      return new SetTag<>(var0, findCommonSuperClass(var0));
   }

   @Override
   public boolean contains(T var1) {
      return this.closestCommonSuperType.isInstance(var1) && this.values.contains(var1);
   }

   @Override
   public List<T> getValues() {
      return this.valuesList;
   }

   private static <T> Class<?> findCommonSuperClass(Set<T> var0) {
      if (var0.isEmpty()) {
         return Void.class;
      } else {
         Class<?> var1 = null;

         for(T var3 : var0) {
            if (var1 == null) {
               var1 = var3.getClass();
            } else {
               var1 = findClosestAncestor(var1, var3.getClass());
            }
         }

         return var1;
      }
   }

   private static Class<?> findClosestAncestor(Class<?> var0, Class<?> var1) {
      while(!var0.isAssignableFrom(var1)) {
         var0 = var0.getSuperclass();
      }

      return var0;
   }
}
