package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/ProbabilityFeatureConfiguration.class */
public class ProbabilityFeatureConfiguration implements CarverConfiguration, FeatureConfiguration {
    public static final Codec<ProbabilityFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.floatRange(0.0f, 1.0f).fieldOf("probability").forGetter(probabilityFeatureConfiguration -> {
            return Float.valueOf(probabilityFeatureConfiguration.probability);
        })).apply(instance, (v1) -> {
            return new ProbabilityFeatureConfiguration(v1);
        });
    });
    public final float probability;

    public ProbabilityFeatureConfiguration(float f) {
        this.probability = f;
    }
}
