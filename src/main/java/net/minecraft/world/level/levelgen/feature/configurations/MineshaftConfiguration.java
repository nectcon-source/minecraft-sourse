package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/MineshaftConfiguration.class */
public class MineshaftConfiguration implements FeatureConfiguration {
    public static final Codec<MineshaftConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.floatRange(0.0f, 1.0f).fieldOf("probability").forGetter(mineshaftConfiguration -> {
            return Float.valueOf(mineshaftConfiguration.probability);
        }), MineshaftFeature.Type.CODEC.fieldOf("type").forGetter(mineshaftConfiguration2 -> {
            return mineshaftConfiguration2.type;
        })).apply(instance, (v1, v2) -> {
            return new MineshaftConfiguration(v1, v2);
        });
    });
    public final float probability;
    public final MineshaftFeature.Type type;

    public MineshaftConfiguration(float f, MineshaftFeature.Type type) {
        this.probability = f;
        this.type = type;
    }
}
