package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BlockPileFeature.class */
public class BlockPileFeature extends Feature<BlockPileConfiguration> {
    public BlockPileFeature(Codec<BlockPileConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration) {
        if (blockPos.getY() < 5) {
            return false;
        }
        int nextInt = 2 + random.nextInt(2);
        int nextInt2 = 2 + random.nextInt(2);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-nextInt, 0, -nextInt2), blockPos.offset(nextInt, 1, nextInt2))) {
            int x = blockPos.getX() - blockPos2.getX();
            int z = blockPos.getZ() - blockPos2.getZ();
            if ((x * x) + (z * z) <= (random.nextFloat() * 10.0f) - (random.nextFloat() * 6.0f)) {
                tryPlaceBlock(worldGenLevel, blockPos2, random, blockPileConfiguration);
            } else if (random.nextFloat() < 0.031d) {
                tryPlaceBlock(worldGenLevel, blockPos2, random, blockPileConfiguration);
            }
        }
        return true;
    }

    private boolean mayPlaceOn(LevelAccessor levelAccessor, BlockPos blockPos, Random random) {
        BlockPos below = blockPos.below();
        BlockState blockState = levelAccessor.getBlockState(below);
        if (blockState.is(Blocks.GRASS_PATH)) {
            return random.nextBoolean();
        }
        return blockState.isFaceSturdy(levelAccessor, below, Direction.UP);
    }

    private void tryPlaceBlock(LevelAccessor levelAccessor, BlockPos blockPos, Random random, BlockPileConfiguration blockPileConfiguration) {
        if (levelAccessor.isEmptyBlock(blockPos) && mayPlaceOn(levelAccessor, blockPos, random)) {
            levelAccessor.setBlock(blockPos, blockPileConfiguration.stateProvider.getState(random, blockPos), 4);
        }
    }
}
