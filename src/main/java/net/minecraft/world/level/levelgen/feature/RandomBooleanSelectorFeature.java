package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/RandomBooleanSelectorFeature.class */
public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
    public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration) {
        if (random.nextBoolean()) {
            return randomBooleanFeatureConfiguration.featureTrue.get().place(worldGenLevel, chunkGenerator, random, blockPos);
        }
        return randomBooleanFeatureConfiguration.featureFalse.get().place(worldGenLevel, chunkGenerator, random, blockPos);
    }
}
