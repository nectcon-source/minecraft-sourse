package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/RandomFeatureConfiguration.class */
public class RandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.apply2(RandomFeatureConfiguration::new, WeightedConfiguredFeature.CODEC.listOf().fieldOf("features").forGetter(randomFeatureConfiguration -> {
            return randomFeatureConfiguration.features;
        }), ConfiguredFeature.CODEC.fieldOf("default").forGetter(randomFeatureConfiguration2 -> {
            return randomFeatureConfiguration2.defaultFeature;
        }));
    });
    public final List<WeightedConfiguredFeature> features;
    public final Supplier<ConfiguredFeature<?, ?>> defaultFeature;

    public RandomFeatureConfiguration(List<WeightedConfiguredFeature> list, ConfiguredFeature<?, ?> configuredFeature) {
        this(list, (Supplier<ConfiguredFeature<?, ?>>) () -> {
            return configuredFeature;
        });
    }

    private RandomFeatureConfiguration(List<WeightedConfiguredFeature> list, Supplier<ConfiguredFeature<?, ?>> supplier) {
        this.features = list;
        this.defaultFeature = supplier;
    }

    @Override // net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.features.stream().flatMap(weightedConfiguredFeature -> {
            return weightedConfiguredFeature.feature.get().getFeatures();
        }), this.defaultFeature.get().getFeatures());
    }
}
