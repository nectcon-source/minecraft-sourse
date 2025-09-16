package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/RandomBooleanFeatureConfiguration.class */
public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ConfiguredFeature.CODEC.fieldOf("feature_true").forGetter(randomBooleanFeatureConfiguration -> {
            return randomBooleanFeatureConfiguration.featureTrue;
        }), ConfiguredFeature.CODEC.fieldOf("feature_false").forGetter(randomBooleanFeatureConfiguration2 -> {
            return randomBooleanFeatureConfiguration2.featureFalse;
        })).apply(instance, RandomBooleanFeatureConfiguration::new);
    });
    public final Supplier<ConfiguredFeature<?, ?>> featureTrue;
    public final Supplier<ConfiguredFeature<?, ?>> featureFalse;

    public RandomBooleanFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> supplier, Supplier<ConfiguredFeature<?, ?>> supplier2) {
        this.featureTrue = supplier;
        this.featureFalse = supplier2;
    }

    @Override // net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.concat(this.featureTrue.get().getFeatures(), this.featureFalse.get().getFeatures());
    }
}
