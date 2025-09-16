package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/WeightedConfiguredFeature.class */
public class WeightedConfiguredFeature {
    public static final Codec<WeightedConfiguredFeature> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter(weightedConfiguredFeature -> {
            return weightedConfiguredFeature.feature;
        }), Codec.floatRange(0.0f, 1.0f).fieldOf("chance").forGetter(weightedConfiguredFeature2 -> {
            return Float.valueOf(weightedConfiguredFeature2.chance);
        })).apply(instance, (v1, v2) -> {
            return new WeightedConfiguredFeature(v1, v2);
        });
    });
    public final Supplier<ConfiguredFeature<?, ?>> feature;
    public final float chance;

    public WeightedConfiguredFeature(ConfiguredFeature<?, ?> configuredFeature, float f) {
        this((Supplier<ConfiguredFeature<?, ?>>) () -> {
            return configuredFeature;
        }, f);
    }

    private WeightedConfiguredFeature(Supplier<ConfiguredFeature<?, ?>> supplier, float f) {
        this.feature = supplier;
        this.chance = f;
    }

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return this.feature.get().place(worldGenLevel, chunkGenerator, random, blockPos);
    }
}
