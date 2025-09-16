package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/grower/AbstractMegaTreeGrower.class */
public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
    @Nullable
    protected abstract ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random random);

    @Override // net.minecraft.world.level.block.grower.AbstractTreeGrower
    public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
        for (int i = 0; i >= -1; i--) {
            for (int i2 = 0; i2 >= -1; i2--) {
                if (isTwoByTwoSapling(blockState, serverLevel, blockPos, i, i2)) {
                    return placeMega(serverLevel, chunkGenerator, blockPos, blockState, random, i, i2);
                }
            }
        }
        return super.growTree(serverLevel, chunkGenerator, blockPos, blockState, random);
    }

    public boolean placeMega(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, int i, int i2) {
        ConfiguredFeature<TreeConfiguration, ?> configuredMegaFeature = getConfiguredMegaFeature(random);
        if (configuredMegaFeature == null) {
            return false;
        }
        configuredMegaFeature.config.setFromSapling();
        BlockState defaultBlockState = Blocks.AIR.defaultBlockState();
        serverLevel.setBlock(blockPos.offset(i, 0, i2), defaultBlockState, 4);
        serverLevel.setBlock(blockPos.offset(i + 1, 0, i2), defaultBlockState, 4);
        serverLevel.setBlock(blockPos.offset(i, 0, i2 + 1), defaultBlockState, 4);
        serverLevel.setBlock(blockPos.offset(i + 1, 0, i2 + 1), defaultBlockState, 4);
        if (configuredMegaFeature.place(serverLevel, chunkGenerator, random, blockPos.offset(i, 0, i2))) {
            return true;
        }
        serverLevel.setBlock(blockPos.offset(i, 0, i2), blockState, 4);
        serverLevel.setBlock(blockPos.offset(i + 1, 0, i2), blockState, 4);
        serverLevel.setBlock(blockPos.offset(i, 0, i2 + 1), blockState, 4);
        serverLevel.setBlock(blockPos.offset(i + 1, 0, i2 + 1), blockState, 4);
        return false;
    }

    public static boolean isTwoByTwoSapling(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, int i, int i2) {
        Block block = blockState.getBlock();
        return block == blockGetter.getBlockState(blockPos.offset(i, 0, i2)).getBlock() && block == blockGetter.getBlockState(blockPos.offset(i + 1, 0, i2)).getBlock() && block == blockGetter.getBlockState(blockPos.offset(i, 0, i2 + 1)).getBlock() && block == blockGetter.getBlockState(blockPos.offset(i + 1, 0, i2 + 1)).getBlock();
    }
}
