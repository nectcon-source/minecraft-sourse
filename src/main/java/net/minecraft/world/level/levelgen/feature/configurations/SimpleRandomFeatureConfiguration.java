package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/SimpleRandomFeatureConfiguration.class */
public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ConfiguredFeature.LIST_CODEC.fieldOf("features").xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> {
        return simpleRandomFeatureConfiguration.features;
    }).codec();
    public final List<Supplier<ConfiguredFeature<?, ?>>> features;

    public SimpleRandomFeatureConfiguration(List<Supplier<ConfiguredFeature<?, ?>>> list) {
        this.features = list;
    }

    @Override // net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.features.stream().flatMap(supplier -> {
            return ((ConfiguredFeature) supplier.get()).getFeatures();
        });
    }
}
