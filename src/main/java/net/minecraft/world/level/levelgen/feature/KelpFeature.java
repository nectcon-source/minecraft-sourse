package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/KelpFeature.class */
public class KelpFeature extends Feature<NoneFeatureConfiguration> {
    public KelpFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        int i = 0;
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ()), blockPos.getZ());
        if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
            BlockState defaultBlockState = Blocks.KELP.defaultBlockState();
            BlockState defaultBlockState2 = Blocks.KELP_PLANT.defaultBlockState();
            int nextInt = 1 + random.nextInt(10);
            int i2 = 0;
            while (true) {
                if (i2 > nextInt) {
                    break;
                }
                if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER) && worldGenLevel.getBlockState(blockPos2.above()).is(Blocks.WATER) && defaultBlockState2.canSurvive(worldGenLevel, blockPos2)) {
                    if (i2 == nextInt) {
                        worldGenLevel.setBlock(blockPos2, (BlockState) defaultBlockState.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(4) + 20)), 2);
                        i++;
                    } else {
                        worldGenLevel.setBlock(blockPos2, defaultBlockState2, 2);
                    }
                } else if (i2 > 0) {
                    BlockPos below = blockPos2.below();
                    if (defaultBlockState.canSurvive(worldGenLevel, below) && !worldGenLevel.getBlockState(below.below()).is(Blocks.KELP)) {
                        worldGenLevel.setBlock(below, (BlockState) defaultBlockState.setValue(KelpBlock.AGE, Integer.valueOf(random.nextInt(4) + 20)), 2);
                        i++;
                    }
                }
                blockPos2 = blockPos2.above();
                i2++;
            }
        }
        return i > 0;
    }
}
