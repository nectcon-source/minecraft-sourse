package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/OceanRuinConfiguration.class */
public class OceanRuinConfiguration implements FeatureConfiguration {
    public static final Codec<OceanRuinConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(OceanRuinFeature.Type.CODEC.fieldOf("biome_temp").forGetter(oceanRuinConfiguration -> {
            return oceanRuinConfiguration.biomeTemp;
        }), Codec.floatRange(0.0f, 1.0f).fieldOf("large_probability").forGetter(oceanRuinConfiguration2 -> {
            return Float.valueOf(oceanRuinConfiguration2.largeProbability);
        }), Codec.floatRange(0.0f, 1.0f).fieldOf("cluster_probability").forGetter(oceanRuinConfiguration3 -> {
            return Float.valueOf(oceanRuinConfiguration3.clusterProbability);
        })).apply(instance, (v1, v2, v3) -> {
            return new OceanRuinConfiguration(v1, v2, v3);
        });
    });
    public final OceanRuinFeature.Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinConfiguration(OceanRuinFeature.Type type, float f, float f2) {
        this.biomeTemp = type;
        this.largeProbability = f;
        this.clusterProbability = f2;
    }
}
