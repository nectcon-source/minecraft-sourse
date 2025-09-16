package net.minecraft.world.level.block.grower;

import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/grower/AbstractTreeGrower.class */
public abstract class AbstractTreeGrower {
    @Nullable
    protected abstract ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean z);

    public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
        ConfiguredFeature<TreeConfiguration, ?> configuredFeature = getConfiguredFeature(random, hasFlowers(serverLevel, blockPos));
        if (configuredFeature == null) {
            return false;
        }
        serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
        configuredFeature.config.setFromSapling();
        if (configuredFeature.place(serverLevel, chunkGenerator, random, blockPos)) {
            return true;
        }
        serverLevel.setBlock(blockPos, blockState, 4);
        return false;
    }

    private boolean hasFlowers(LevelAccessor levelAccessor, BlockPos blockPos) {
        Iterator<BlockPos> it = BlockPos.MutableBlockPos.betweenClosed(blockPos.below().north(2).west(2), blockPos.above().south(2).east(2)).iterator();
        while (it.hasNext()) {
            if (levelAccessor.getBlockState(it.next()).is(BlockTags.FLOWERS)) {
                return true;
            }
        }
        return false;
    }
}
