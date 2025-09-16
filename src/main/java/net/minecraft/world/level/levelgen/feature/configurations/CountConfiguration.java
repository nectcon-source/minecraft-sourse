package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.util.UniformInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/CountConfiguration.class */
public class CountConfiguration implements DecoratorConfiguration, FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = UniformInt.codec(-10, 128, 128).fieldOf("count").xmap(CountConfiguration::new, (v0) -> {
        return v0.count();
    }).codec();
    private final UniformInt count;

    public CountConfiguration(int i) {
        this.count = UniformInt.fixed(i);
    }

    public CountConfiguration(UniformInt uniformInt) {
        this.count = uniformInt;
    }

    public UniformInt count() {
        return this.count;
    }
}
