package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class WeightedList<U> {
   protected final List<WeightedList.WeightedEntry<U>> entries;
   private final Random random = new Random();

   public WeightedList() {
      this(Lists.newArrayList());
   }

   private WeightedList(List<WeightedList.WeightedEntry<U>> var1) {
      this.entries = Lists.newArrayList(var1);
   }

   public static <U> Codec<WeightedList<U>> codec(Codec<U> var0) {
      return WeightedList.WeightedEntry.codec(var0).listOf().xmap(WeightedList::new, (var0x) -> var0x.entries);
   }

   public WeightedList<U> add(U var1, int var2) {
      this.entries.add(new WeightedEntry(var1, var2));
      return this;
   }

   public WeightedList<U> shuffle() {
      return this.shuffle(this.random);
   }

   public WeightedList<U> shuffle(Random var1) {
      this.entries.forEach(var1x -> var1x.setRandom(var1.nextFloat()));
      this.entries.sort(Comparator.comparingDouble(var0 -> var0.getRandWeight()));
      return this;
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   public Stream<U> stream() {
      return this.entries.stream().map(WeightedList.WeightedEntry::getData);
   }

   public U getOne(Random var1) {
      return this.shuffle(var1).stream().findFirst().orElseThrow(RuntimeException::new);
   }

   @Override
   public String toString() {
      return "WeightedList[" + this.entries + "]";
   }

   public static class WeightedEntry<T> {
      private final T data;
      private final int weight;
      private double randWeight;

      private WeightedEntry(T var1, int var2) {
         this.weight = var2;
         this.data = var1;
      }

      private double getRandWeight() {
         return this.randWeight;
      }

      private void setRandom(float var1) {
         this.randWeight = -Math.pow((double)var1, (double)(1.0F / (float)this.weight));
      }

      public T getData() {
         return this.data;
      }

      @Override
      public String toString() {
         return "" + this.weight + ":" + this.data;
      }

      public static <E> Codec<WeightedList.WeightedEntry<E>> codec(final Codec<E> var0) {
         return new Codec<WeightedList.WeightedEntry<E>>() {
            public <T> DataResult<Pair<WeightedList.WeightedEntry<E>, T>> decode(DynamicOps<T> var1, T var2) {
               Dynamic<T> var3 = new Dynamic(var1, var2);
               return var3.get("data")
                  .flatMap(var0::parse)
                  .map(var1x -> new WeightedList.WeightedEntry(var1x, var3.get("weight").asInt(1)))
                  .map(var1x -> Pair.of(var1x, var1.empty()));
            }

            public <T> DataResult<T> encode(WeightedList.WeightedEntry<E> var1, DynamicOps<T> var2, T var3) {
               return var2.mapBuilder().add("weight", var2.createInt(var1.weight)).add("data", var0.encodeStart(var2, var1.data)).build(var3);
            }
         };
      }
   }
}
