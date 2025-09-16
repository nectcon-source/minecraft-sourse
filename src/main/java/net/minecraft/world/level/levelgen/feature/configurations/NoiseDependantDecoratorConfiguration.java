package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/NoiseDependantDecoratorConfiguration.class */
public class NoiseDependantDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<NoiseDependantDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.DOUBLE.fieldOf("noise_level").forGetter(noiseDependantDecoratorConfiguration -> {
            return Double.valueOf(noiseDependantDecoratorConfiguration.noiseLevel);
        }), Codec.INT.fieldOf("below_noise").forGetter(noiseDependantDecoratorConfiguration2 -> {
            return Integer.valueOf(noiseDependantDecoratorConfiguration2.belowNoise);
        }), Codec.INT.fieldOf("above_noise").forGetter(noiseDependantDecoratorConfiguration3 -> {
            return Integer.valueOf(noiseDependantDecoratorConfiguration3.aboveNoise);
        })).apply(instance, (v1, v2, v3) -> {
            return new NoiseDependantDecoratorConfiguration(v1, v2, v3);
        });
    });
    public final double noiseLevel;
    public final int belowNoise;
    public final int aboveNoise;

    public NoiseDependantDecoratorConfiguration(double d, int i, int i2) {
        this.noiseLevel = d;
        this.belowNoise = i;
        this.aboveNoise = i2;
    }
}
