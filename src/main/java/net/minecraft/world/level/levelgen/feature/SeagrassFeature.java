package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrass;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SeagrassFeature.class */
public class SeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {
    public SeagrassFeature(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        boolean z = false;
        int nextInt = random.nextInt(8) - random.nextInt(8);
        int nextInt2 = random.nextInt(8) - random.nextInt(8);
        BlockPos blockPos2 = new BlockPos(blockPos.getX() + nextInt, worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + nextInt, blockPos.getZ() + nextInt2), blockPos.getZ() + nextInt2);
        if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
            boolean z2 = random.nextDouble() < ((double) probabilityFeatureConfiguration.probability);
            BlockState defaultBlockState = z2 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
            if (defaultBlockState.canSurvive(worldGenLevel, blockPos2)) {
                if (z2) {
                    BlockState blockState = (BlockState) defaultBlockState.setValue(TallSeagrass.HALF, DoubleBlockHalf.UPPER);
                    BlockPos above = blockPos2.above();
                    if (worldGenLevel.getBlockState(above).is(Blocks.WATER)) {
                        worldGenLevel.setBlock(blockPos2, defaultBlockState, 2);
                        worldGenLevel.setBlock(above, blockState, 2);
                    }
                } else {
                    worldGenLevel.setBlock(blockPos2, defaultBlockState, 2);
                }
                z = true;
            }
        }
        return z;
    }
}
