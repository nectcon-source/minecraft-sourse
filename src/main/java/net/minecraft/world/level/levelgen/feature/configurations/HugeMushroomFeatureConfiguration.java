package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/HugeMushroomFeatureConfiguration.class */
public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(hugeMushroomFeatureConfiguration -> {
            return hugeMushroomFeatureConfiguration.capProvider;
        }), BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(hugeMushroomFeatureConfiguration2 -> {
            return hugeMushroomFeatureConfiguration2.stemProvider;
        }), Codec.INT.fieldOf("foliage_radius").orElse(2).forGetter(hugeMushroomFeatureConfiguration3 -> {
            return Integer.valueOf(hugeMushroomFeatureConfiguration3.foliageRadius);
        })).apply(instance, (v1, v2, v3) -> {
            return new HugeMushroomFeatureConfiguration(v1, v2, v3);
        });
    });
    public final BlockStateProvider capProvider;
    public final BlockStateProvider stemProvider;
    public final int foliageRadius;

    public HugeMushroomFeatureConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, int i) {
        this.capProvider = blockStateProvider;
        this.stemProvider = blockStateProvider2;
        this.foliageRadius = i;
    }
}
