package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeGenerationSettings.class */
public class BiomeGenerationSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(() -> {
        return SurfaceBuilders.NOPE;
    }, ImmutableMap.of(), ImmutableList.of(), ImmutableList.of());
//    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> {
//        RecordCodecBuilder forGetter = ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").forGetter(biomeGenerationSettings -> {
//            return biomeGenerationSettings.surfaceBuilder;
//        });
//
//
//
//        RecordCodecBuilder forGetter2 = Codec.simpleMap(GenerationStep.Carving.CODEC, ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)), StringRepresentable.keys(GenerationStep.Carving.values())).fieldOf("carvers").forGetter(biomeGenerationSettings2 -> {
//            return biomeGenerationSettings2.carvers;
//        });
//        Codec<List<Supplier<ConfiguredFeature<?, ?>>>> codec3 = ConfiguredFeature.LIST_CODEC;
//        Logger logger2 = LOGGER;
//        logger2.getClass();
//        RecordCodecBuilder forGetter3 = codec3.promotePartial(Util.prefix("Feature: ", logger2::error)).listOf().fieldOf("features").forGetter(biomeGenerationSettings3 -> {
//            return biomeGenerationSettings3.features;
//        });
//        Codec<List<Supplier<ConfiguredStructureFeature<?, ?>>>> codec4 = ConfiguredStructureFeature.LIST_CODEC;
//        Logger logger3 = LOGGER;
//        logger3.getClass();
//        return instance.group(forGetter, forGetter2, forGetter3, codec4.promotePartial(Util.prefix("Structure start: ", logger3::error)).fieldOf("starts").forGetter(biomeGenerationSettings4 -> {
//            return biomeGenerationSettings4.structureStarts;
//        })).apply(instance, BiomeGenerationSettings::new);
//    });
public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> {
    return instance.group(
            ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder")
                    .forGetter(settings -> settings.surfaceBuilder),

            Codec.simpleMap(
                            GenerationStep.Carving.CODEC,
                            ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)),
                            StringRepresentable.keys(GenerationStep.Carving.values())
                    ).fieldOf("carvers")
                    .forGetter(settings -> settings.carvers),

            ConfiguredFeature.LIST_CODEC
                    .promotePartial(Util.prefix("Feature: ", LOGGER::error))
                    .listOf()
                    .fieldOf("features")
                    .forGetter(settings -> settings.features),

            ConfiguredStructureFeature.LIST_CODEC
                    .promotePartial(Util.prefix("Structure start: ", LOGGER::error))
                    .fieldOf("starts")
                    .forGetter(settings -> settings.structureStarts)
    ).apply(instance, BiomeGenerationSettings::new);
});
//
     //


    private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
    private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
    private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
    private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts;
    private final List<ConfiguredFeature<?, ?>> flowerFeatures;

    private BiomeGenerationSettings(Supplier<ConfiguredSurfaceBuilder<?>> supplier, Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> map, List<List<Supplier<ConfiguredFeature<?, ?>>>> list, List<Supplier<ConfiguredStructureFeature<?, ?>>> list2) {
        this.surfaceBuilder = supplier;
        this.carvers = map;
        this.features = list;
        this.structureStarts = list2;
        this.flowerFeatures =  list.stream().flatMap((v0) -> {
            return v0.stream();
        }).map((v0) -> {
            return v0.get();
        }).flatMap((v0) -> {
            return v0.getFeatures();
        }).filter(configuredFeature -> {
            return configuredFeature.feature == Feature.FLOWER;
        }).collect(ImmutableList.toImmutableList());
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
        return this.carvers.getOrDefault(carving, ImmutableList.of());
    }

    public boolean isValidStart(StructureFeature<?> structureFeature) {
        return this.structureStarts.stream().anyMatch(supplier -> {
            return ((ConfiguredStructureFeature) supplier.get()).feature == structureFeature;
        });
    }

    public Collection<Supplier<ConfiguredStructureFeature<?, ?>>> structures() {
        return this.structureStarts;
    }

    public ConfiguredStructureFeature<?, ?> withBiomeConfig(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        return (ConfiguredStructureFeature) DataFixUtils.orElse(this.structureStarts.stream().map((v0) -> {
            return v0.get();
        }).filter(configuredStructureFeature2 -> {
            return configuredStructureFeature2.feature == configuredStructureFeature.feature;
        }).findAny(), configuredStructureFeature);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
        return this.features;
    }

    public Supplier<ConfiguredSurfaceBuilder<?>> getSurfaceBuilder() {
        return this.surfaceBuilder;
    }

    /* JADX WARN: Type inference failed for: r0v4, types: [net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration] */
    public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
        return this.surfaceBuilder.get().config();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeGenerationSettings$Builder.class */
    public static class Builder {
        private Optional<Supplier<ConfiguredSurfaceBuilder<?>>> surfaceBuilder = Optional.empty();
        private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
        private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = Lists.newArrayList();
        private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts = Lists.newArrayList();

        public Builder surfaceBuilder(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder) {
            return surfaceBuilder(() -> {
                return configuredSurfaceBuilder;
            });
        }

        public Builder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> supplier) {
            this.surfaceBuilder = Optional.of(supplier);
            return this;
        }

        public Builder addFeature(GenerationStep.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
            return addFeature(decoration.ordinal(), () -> {
                return configuredFeature;
            });
        }

        public Builder addFeature(int i, Supplier<ConfiguredFeature<?, ?>> supplier) {
            addFeatureStepsUpTo(i);
            this.features.get(i).add(supplier);
            return this;
        }

        public <C extends CarverConfiguration> Builder addCarver(GenerationStep.Carving carving, ConfiguredWorldCarver<C> configuredWorldCarver) {
            this.carvers.computeIfAbsent(carving, carving2 -> {
                return Lists.newArrayList();
            }).add(() -> {
                return configuredWorldCarver;
            });
            return this;
        }

        public Builder addStructureStart(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
            this.structureStarts.add(() -> {
                return configuredStructureFeature;
            });
            return this;
        }

        private void addFeatureStepsUpTo(int i) {
            while (this.features.size() <= i) {
                this.features.add(Lists.newArrayList());
            }
        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings(this.surfaceBuilder.orElseThrow(() -> {
                return new IllegalStateException("Missing surface builder");
            }), (Map) this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap((v0) -> {
                return v0.getKey();
            }, entry -> {
                return ImmutableList.copyOf((Collection) entry.getValue());
            })), (List) this.features.stream().map((v0) -> {
                return ImmutableList.copyOf(v0);
            }).collect(ImmutableList.toImmutableList()), ImmutableList.copyOf(this.structureStarts));
        }
    }
}
