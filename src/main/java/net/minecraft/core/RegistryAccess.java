package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RegistryAccess {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
      Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0 = ImmutableMap.builder();
      put(var0, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
      put(var0, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
      put(var0, Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, ConfiguredSurfaceBuilder.DIRECT_CODEC);
      put(var0, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
      put(var0, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
      put(var0, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
      put(var0, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
      put(var0, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
      put(var0, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
      return var0.build();
   });
   private static final RegistryAccess.RegistryHolder BUILTIN = Util.make(() -> {
      RegistryAccess.RegistryHolder var0 = new RegistryAccess.RegistryHolder();
      DimensionType.registerBuiltin(var0);
      REGISTRIES.keySet().stream().filter(var0x -> !var0x.equals(Registry.DIMENSION_TYPE_REGISTRY)).forEach(var1 -> copyBuiltin(var0, var1));
      return var0;
   });

   public abstract <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> var1);

   public <E> WritableRegistry<E> registryOrThrow(ResourceKey<? extends Registry<E>> var1) {
      return this.registry(var1).orElseThrow(() -> new IllegalStateException("Missing registry: " + var1));
   }

   public Registry<DimensionType> dimensionTypes() {
      return this.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
   }

   private static <E> void put(
      Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0, ResourceKey<? extends Registry<E>> var1, Codec<E> var2
   ) {
      var0.put(var1, new RegistryData(var1, var2, null));
   }

   private static <E> void put(
      Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0, ResourceKey<? extends Registry<E>> var1, Codec<E> var2, Codec<E> var3
   ) {
      var0.put(var1, new RegistryData(var1, var2, var3));
   }

   public static RegistryAccess.RegistryHolder builtin() {
      RegistryHolder var0 = new RegistryHolder();
      RegistryReadOps.ResourceAccess.MemoryMap var1 = new RegistryReadOps.ResourceAccess.MemoryMap();

      for(RegistryData<?> var3 : REGISTRIES.values()) {
         addBuiltinElements(var0, var1, var3);
      }

      RegistryReadOps.create(JsonOps.INSTANCE, var1, var0);
      return var0;
   }

   private static <E> void addBuiltinElements(
      RegistryAccess.RegistryHolder var0, RegistryReadOps.ResourceAccess.MemoryMap var1, RegistryAccess.RegistryData<E> var2
   ) {
      ResourceKey<? extends Registry<E>> var3 = var2.key();
      boolean var4 = !var3.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !var3.equals(Registry.DIMENSION_TYPE_REGISTRY);
      Registry<E> var5 = BUILTIN.registryOrThrow(var3);
      WritableRegistry<E> var6 = var0.registryOrThrow(var3);

      for(Map.Entry<ResourceKey<E>, E> var8 : var5.entrySet()) {
         E var9 = var8.getValue();
         if (var4) {
            var1.add(BUILTIN, var8.getKey(), var2.codec(), var5.getId(var9), var9, var5.lifecycle(var9));
         } else {
            var6.registerMapping(var5.getId(var9), var8.getKey(), var9, var5.lifecycle(var9));
         }
      }
   }

   private static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder var0, ResourceKey<R> var1) {
      Registry<R> var2 = (Registry<R>) BuiltinRegistries.REGISTRY;
      Registry<?> var3x = var2.get(var1);
      if (var3x == null) {
         throw new IllegalStateException("Missing builtin registry: " + var1);
      } else {
         copy(var0, var3x);
      }
   }

   private static <E> void copy(RegistryAccess.RegistryHolder var0, Registry<E> var1) {
      WritableRegistry<E> var2 = var0.registry(var1.key()).orElseThrow(() -> new IllegalStateException("Missing registry: " + var1.key()));

      for(Map.Entry<ResourceKey<E>, E> var4 : var1.entrySet()) {
         E var5 = (E)var4.getValue();
         var2.registerMapping(var1.getId(var5), var4.getKey(), var5, var1.lifecycle(var5));
      }
   }

   public static void load(RegistryAccess.RegistryHolder var0, RegistryReadOps<?> var1) {
      for(RegistryData<?> var3 : REGISTRIES.values()) {
         readRegistry(var1, var0, var3);
      }
   }

   private static <E> void readRegistry(RegistryReadOps<?> var0, RegistryAccess.RegistryHolder var1, RegistryAccess.RegistryData<E> var2) {
      ResourceKey<? extends Registry<E>> var3 = var2.key();
      MappedRegistry<E> var4x = (MappedRegistry)Optional.ofNullable(var1.registries.get(var3))
         .map((Function<? super MappedRegistry<?>, ? extends MappedRegistry<?>>)(var0x -> var0x))
         .orElseThrow(() -> new IllegalStateException("Missing registry: " + var3));
      DataResult<MappedRegistry<E>> var5xx = var0.decodeElements(var4x, var2.key(), var2.codec());
      var5xx.error().ifPresent(var0x -> LOGGER.error("Error loading registry data: {}", var0x.message()));
   }

   static final class RegistryData<E> {
      private final ResourceKey<? extends Registry<E>> key;
      private final Codec<E> codec;
      @Nullable
      private final Codec<E> networkCodec;

      public RegistryData(ResourceKey<? extends Registry<E>> var1, Codec<E> var2, @Nullable Codec<E> var3) {
         this.key = var1;
         this.codec = var2;
         this.networkCodec = var3;
      }

      public ResourceKey<? extends Registry<E>> key() {
         return this.key;
      }

      public Codec<E> codec() {
         return this.codec;
      }

      @Nullable
      public Codec<E> networkCodec() {
         return this.networkCodec;
      }

      public boolean sendToClient() {
         return this.networkCodec != null;
      }
   }

   public static final class RegistryHolder extends RegistryAccess {
      public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeNetworkCodec();
      private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

      private static <E> Codec<RegistryAccess.RegistryHolder> makeNetworkCodec() {
         Codec<ResourceKey<? extends Registry<E>>> var0 = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
         Codec<MappedRegistry<E>> var1x = var0.partialDispatch(
            "type",
            var0x -> DataResult.success(var0x.key()),
            var0x -> getNetworkCodec(var0x).map(var21x -> MappedRegistry.networkCodec(var0x, Lifecycle.experimental(), var21x))
         );
         UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> var2xx = Codec.unboundedMap(var0, var1x);
         return captureMap(var2xx);
      }

      private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryAccess.RegistryHolder> captureMap(
         UnboundedMapCodec<K, V> var0
      ) {
         return var0.xmap(
            RegistryAccess.RegistryHolder::new,
            var0x -> (ImmutableMap)var0x.registries
                  .entrySet()
                  .stream()
                  .filter(var0xx -> RegistryAccess.REGISTRIES.get(var0xx.getKey()).sendToClient())
                  .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue))
         );
      }

//      private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> var0) {
//         return (DataResult<? extends Codec<E>>) Optional.ofNullable(RegistryAccess.REGISTRIES.get(var0))
//            .map(var0x -> var0x.networkCodec())
//            .map(DataResult::success)
//            .orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + var0));
//      }
private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> registryKey) {
   RegistryData<?> registry = RegistryAccess.REGISTRIES.get(registryKey);
   if (registry == null) {
      return DataResult.error("Unknown or not serializable registry: " + registryKey);
   }

   Codec<E> codec = (Codec<E>) ((RegistryData<?>) registry).networkCodec();
   if (codec == null) {
      return DataResult.error("Registry has no network codec: " + registryKey);
   }

   return DataResult.success(codec);
}
      public RegistryHolder() {
         this(RegistryAccess.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry)));
      }

      private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> var1) {
         this.registries = var1;
      }

      private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> var0) {
         return new MappedRegistry(var0, Lifecycle.stable());
      }

//      @Override
//      public <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> var1) {
//         return (Optional<WritableRegistry<E>>) Optional.ofNullable(this.registries.get(var1)).map(var0 -> var0);
//      }
@Override
public <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> registryKey) {
   Registry<?> registry = this.registries.get(registryKey);
   if (registry instanceof WritableRegistry) {
      @SuppressWarnings("unchecked")
      WritableRegistry<E> writableRegistry = (WritableRegistry<E>) registry;
      return Optional.of(writableRegistry);
   }
   return Optional.empty();
}
   }
}
