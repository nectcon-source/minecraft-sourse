package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/DecoratedFeatureConfiguration.class */
public class DecoratedFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<DecoratedFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter(decoratedFeatureConfiguration -> {
            return decoratedFeatureConfiguration.feature;
        }), ConfiguredDecorator.CODEC.fieldOf("decorator").forGetter(decoratedFeatureConfiguration2 -> {
            return decoratedFeatureConfiguration2.decorator;
        })).apply(instance, DecoratedFeatureConfiguration::new);
    });
    public final Supplier<ConfiguredFeature<?, ?>> feature;
    public final ConfiguredDecorator<?> decorator;

    public DecoratedFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> supplier, ConfiguredDecorator<?> configuredDecorator) {
        this.feature = supplier;
        this.decorator = configuredDecorator;
    }

    public String toString() {
        return String.format("< %s [%s | %s] >", getClass().getSimpleName(), Registry.FEATURE.getKey(this.feature.get().feature()), this.decorator);
    }

    @Override // net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.get().getFeatures();
    }
}
