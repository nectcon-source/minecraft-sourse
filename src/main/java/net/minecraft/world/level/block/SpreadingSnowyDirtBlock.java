package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SpreadingSnowyDirtBlock.class */
public abstract class SpreadingSnowyDirtBlock extends SnowyDirtBlock {
    protected SpreadingSnowyDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static boolean canBeGrass(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos above = blockPos.above();
        BlockState blockState2 = levelReader.getBlockState(above);
        if (blockState2.is(Blocks.SNOW) && ((Integer) blockState2.getValue(SnowLayerBlock.LAYERS)).intValue() == 1) {
            return true;
        }
        return blockState2.getFluidState().getAmount() != 8 && LayerLightEngine.getLightBlockInto(levelReader, blockState, blockPos, blockState2, above, Direction.UP, blockState2.getLightBlock(levelReader, above)) < levelReader.getMaxLightLevel();
    }

    private static boolean canPropagate(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return canBeGrass(blockState, levelReader, blockPos) && !levelReader.getFluidState(blockPos.above()).is(FluidTags.WATER);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!canBeGrass(blockState, serverLevel, blockPos)) {
            serverLevel.setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
            return;
        }
        if (serverLevel.getMaxLocalRawBrightness(blockPos.above()) >= 9) {
            BlockState defaultBlockState = defaultBlockState();
            for (int i = 0; i < 4; i++) {
                BlockPos offset = blockPos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (serverLevel.getBlockState(offset).is(Blocks.DIRT) && canPropagate(defaultBlockState, serverLevel, offset)) {
                    serverLevel.setBlockAndUpdate(offset, (BlockState) defaultBlockState.setValue(SNOWY, Boolean.valueOf(serverLevel.getBlockState(offset.above()).is(Blocks.SNOW))));
                }
            }
        }
    }
}
