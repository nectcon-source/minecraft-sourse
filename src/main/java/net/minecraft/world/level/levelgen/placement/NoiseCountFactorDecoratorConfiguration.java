package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/NoiseCountFactorDecoratorConfiguration.class */
public class NoiseCountFactorDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<NoiseCountFactorDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("noise_to_count_ratio").forGetter(noiseCountFactorDecoratorConfiguration -> {
            return Integer.valueOf(noiseCountFactorDecoratorConfiguration.noiseToCountRatio);
        }), Codec.DOUBLE.fieldOf("noise_factor").forGetter(noiseCountFactorDecoratorConfiguration2 -> {
            return Double.valueOf(noiseCountFactorDecoratorConfiguration2.noiseFactor);
        }), Codec.DOUBLE.fieldOf("noise_offset").orElse(Double.valueOf(0.0d)).forGetter(noiseCountFactorDecoratorConfiguration3 -> {
            return Double.valueOf(noiseCountFactorDecoratorConfiguration3.noiseOffset);
        })).apply(instance, (v1, v2, v3) -> {
            return new NoiseCountFactorDecoratorConfiguration(v1, v2, v3);
        });
    });
    public final int noiseToCountRatio;
    public final double noiseFactor;
    public final double noiseOffset;

    public NoiseCountFactorDecoratorConfiguration(int i, double d, double d2) {
        this.noiseToCountRatio = i;
        this.noiseFactor = d;
        this.noiseOffset = d2;
    }
}
