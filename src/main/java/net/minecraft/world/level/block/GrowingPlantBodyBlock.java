package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/GrowingPlantBodyBlock.class */
public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
    protected GrowingPlantBodyBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean z) {
        super(properties, direction, voxelShape, z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        Block block;
        if (direction == this.growthDirection.getOpposite() && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        GrowingPlantHeadBlock headBlock = getHeadBlock();
        if (direction == this.growthDirection && (block = blockState2.getBlock()) != this && block != headBlock) {
            return headBlock.getStateForPlacement(levelAccessor);
        }
        if (this.scheduleFluidTicks) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(getHeadBlock());
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        Optional<BlockPos> headPos = getHeadPos(blockGetter, blockPos, blockState);
        return headPos.isPresent() && getHeadBlock().canGrowInto(blockGetter.getBlockState(headPos.get().relative(this.growthDirection)));
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        Optional<BlockPos> headPos = getHeadPos(serverLevel, blockPos, blockState);
        if (headPos.isPresent()) {
            BlockState blockState2 = serverLevel.getBlockState(headPos.get());
            ((GrowingPlantHeadBlock) blockState2.getBlock()).performBonemeal(serverLevel, random, headPos.get(), blockState2);
        }
    }

    private Optional<BlockPos> getHeadPos(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Block block;
        BlockPos blockPos2 = blockPos;
        do {
            blockPos2 = blockPos2.relative(this.growthDirection);
            block = blockGetter.getBlockState(blockPos2).getBlock();
        } while (block == blockState.getBlock());
        if (block == getHeadBlock()) {
            return Optional.of(blockPos2);
        }
        return Optional.empty();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        boolean canBeReplaced = super.canBeReplaced(blockState, blockPlaceContext);
        if (canBeReplaced && blockPlaceContext.getItemInHand().getItem() == getHeadBlock().asItem()) {
            return false;
        }
        return canBeReplaced;
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected Block getBodyBlock() {
        return this;
    }
}
