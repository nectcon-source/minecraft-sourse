package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/KelpBlock.class */
public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
    protected static final VoxelShape SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 9.0d, 16.0d);

    protected KelpBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SHAPE, true, 0.14d);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantHeadBlock
    protected boolean canGrowInto(BlockState blockState) {
        return blockState.is(Blocks.WATER);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected Block getBodyBlock() {
        return Blocks.KELP_PLANT;
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected boolean canAttachToBlock(Block block) {
        return block != Blocks.MAGMA_BLOCK;
    }

    @Override // net.minecraft.world.level.block.LiquidBlockContainer
    public boolean canPlaceLiquid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return false;
    }

    @Override // net.minecraft.world.level.block.LiquidBlockContainer
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        return false;
    }

    @Override // net.minecraft.world.level.block.GrowingPlantHeadBlock
    protected int getBlocksToGrowWhenBonemealed(Random random) {
        return 1;
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock, net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        if (fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8) {
            return super.getStateForPlacement(blockPlaceContext);
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        return Fluids.WATER.getSource(false);
    }
}
