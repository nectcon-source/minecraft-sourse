package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/MushroomBlock.class */
public class MushroomBlock extends BushBlock implements BonemealableBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0d, 0.0d, 5.0d, 11.0d, 6.0d, 11.0d);

    public MushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (random.nextInt(25) == 0) {
            int i = 5;
            Iterator<BlockPos> it = BlockPos.betweenClosed(blockPos.offset(-4, -1, -4), blockPos.offset(4, 1, 4)).iterator();
            while (it.hasNext()) {
                if (serverLevel.getBlockState(it.next()).is(this)) {
                    i--;
                    if (i <= 0) {
                        return;
                    }
                }
            }
            BlockPos offset = blockPos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            for (int i2 = 0; i2 < 4; i2++) {
                if (serverLevel.isEmptyBlock(offset) && blockState.canSurvive(serverLevel, offset)) {
                    blockPos = offset;
                }
                offset = blockPos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            }
            if (serverLevel.isEmptyBlock(offset) && blockState.canSurvive(serverLevel, offset)) {
                serverLevel.setBlock(offset, blockState, 2);
            }
        }
    }

    @Override // net.minecraft.world.level.block.BushBlock
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.isSolidRender(blockGetter, blockPos);
    }

    @Override // net.minecraft.world.level.block.BushBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(below);
        if (blockState2.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return true;
        }
        return levelReader.getRawBrightness(blockPos, 0) < 13 && mayPlaceOn(blockState2, levelReader, below);
    }

    public boolean growMushroom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Random random) {
        ConfiguredFeature<?, ?> configuredFeature;
        serverLevel.removeBlock(blockPos, false);
        if (this == Blocks.BROWN_MUSHROOM) {
            configuredFeature = Features.HUGE_BROWN_MUSHROOM;
        } else if (this == Blocks.RED_MUSHROOM) {
            configuredFeature = Features.HUGE_RED_MUSHROOM;
        } else {
            serverLevel.setBlock(blockPos, blockState, 3);
            return false;
        }
        if (configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), random, blockPos)) {
            return true;
        }
        serverLevel.setBlock(blockPos, blockState, 3);
        return false;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return ((double) random.nextFloat()) < 0.4d;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        growMushroom(serverLevel, blockPos, blockState, random);
    }
}
