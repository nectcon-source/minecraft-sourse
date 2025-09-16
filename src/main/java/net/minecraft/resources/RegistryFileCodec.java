package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Supplier<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<E> elementCodec;
   private final boolean allowInline;

   public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> var0, Codec<E> var1) {
      return create(var0, var1, true);
   }

   public static <E> Codec<List<Supplier<E>>> homogeneousList(ResourceKey<? extends Registry<E>> var0, Codec<E> var1) {
//      return Codec.either(create(var0, var1, false).listOf(), var1.xmap(var0x -> () -> var0x, Supplier::get).listOf())
//         .xmap(var0x -> var0x.map(var0xx -> var0xx, var0xx -> var0xx), Either::left);
      Codec<List<Supplier<E>>> firstOption = create(var0, var1, false).listOf();
      Codec<List<Supplier<E>>> secondOption = var1.xmap(
              element -> (Supplier<E>) () -> element,
              Supplier::get
      ).listOf();

      return Codec.either(firstOption, secondOption)
              .xmap(
                      either -> either.map(Function.identity(), Function.identity()),
                      Either::left
              );

   }

   private static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> var0, Codec<E> var1, boolean var2) {
      return new RegistryFileCodec<E>(var0, var1, var2);
   }

   private RegistryFileCodec(ResourceKey<? extends Registry<E>> var1, Codec<E> var2, boolean var3) {
      this.registryKey = var1;
      this.elementCodec = var2;
      this.allowInline = var3;
   }

   public <T> DataResult<T> encode(Supplier<E> var1, DynamicOps<T> var2, T var3) {
      return var2 instanceof RegistryWriteOps ? ((RegistryWriteOps)var2).encode(var1.get(), var3, this.registryKey, this.elementCodec) : this.elementCodec.encode(var1.get(), var2, var3);
   }

   public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> var1, T var2) {
      return var1 instanceof RegistryReadOps ? ((RegistryReadOps)var1).decodeElement(var2, this.registryKey, this.elementCodec, this.allowInline) : this.elementCodec.decode(var1, var2).map((var0) -> var0.mapFirst((var0x) -> () -> var0x));
   }

   @Override
   public String toString() {
      return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
   }
}
