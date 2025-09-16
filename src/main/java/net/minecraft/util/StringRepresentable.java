package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface StringRepresentable {
   String getSerializedName();

   static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> var0, Function<? super String, ? extends E> var1) {
      E[] var2 = var0.get();
      return fromStringResolver(Enum::ordinal, var1x -> var2[var1x], var1);
   }

   static <E extends StringRepresentable> Codec<E> fromStringResolver(
      final ToIntFunction<E> var0, final IntFunction<E> var1, final Function<? super String, ? extends E> var2
   ) {
      return new Codec<E>() {
         public <T> DataResult<T> encode(E var1x, DynamicOps<T> var2x, T var3) {
            return var2x.compressMaps() ? var2x.mergeToPrimitive(var3, var2x.createInt(var0.applyAsInt(var1x))) : var2x.mergeToPrimitive(var3, var2x.createString(var1x.getSerializedName()));
         }

         public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> var1x, T var2x) {
            return var1x.compressMaps()
               ? var1x.getNumberValue(var2x)
                  .flatMap(
                     var1xxx -> (DataResult)Optional.ofNullable(var1.apply(var1xxx.intValue()))
                           .map(DataResult::success)
                           .orElseGet(() -> DataResult.error("Unknown element id: " + var1xxx))
                  )
                  .map(var1xxx -> Pair.of(var1xxx, var1x.empty()))
               : var1x.getStringValue(var2x)
                  .flatMap(
                     var1xxx -> (DataResult)Optional.ofNullable(var2.apply(var1xxx))
                           .map(DataResult::success)
                           .orElseGet(() -> DataResult.error("Unknown element name: " + var1xxx))
                  )
                  .map(var1xxx -> Pair.of(var1xxx, var1x.empty()));
         }

         @Override
         public String toString() {
            return "StringRepresentable[" + var0 + "]";
         }
      };
   }

   static Keyable keys(final StringRepresentable[] var0) {
      return new Keyable() {
         public <T> Stream<T> keys(DynamicOps<T> var1) {
            return var1.compressMaps()
               ? IntStream.range(0, var0.length).mapToObj(var1::createInt)
               : Arrays.stream(var0).map(StringRepresentable::getSerializedName).map(var1::createString);
         }
      };
   }
}
