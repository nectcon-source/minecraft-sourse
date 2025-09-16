package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ConcretePowderBlock.class */
public class ConcretePowderBlock extends FallingBlock {
    private final BlockState concrete;

    public ConcretePowderBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.concrete = block.defaultBlockState();
    }

    @Override // net.minecraft.world.level.block.FallingBlock
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        if (shouldSolidify(level, blockPos, blockState2)) {
            level.setBlock(blockPos, this.concrete, 3);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockGetter level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        if (shouldSolidify(level, clickedPos, level.getBlockState(clickedPos))) {
            return this.concrete;
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    private static boolean shouldSolidify(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return canSolidify(blockState) || touchesLiquid(blockGetter, blockPos);
    }

    private static boolean touchesLiquid(BlockGetter blockGetter, BlockPos blockPos) {
        boolean z = false;
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        Direction[] values = Direction.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Direction direction = values[i];
            BlockState blockState = blockGetter.getBlockState(mutable);
            if (direction != Direction.DOWN || canSolidify(blockState)) {
                mutable.setWithOffset(blockPos, direction);
                BlockState blockState2 = blockGetter.getBlockState(mutable);
                if (canSolidify(blockState2) && !blockState2.isFaceSturdy(blockGetter, blockPos, direction.getOpposite())) {
                    z = true;
                    break;
                }
            }
            i++;
        }
        return z;
    }

    private static boolean canSolidify(BlockState blockState) {
        return blockState.getFluidState().is(FluidTags.WATER);
    }

    @Override // net.minecraft.world.level.block.FallingBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (touchesLiquid(levelAccessor, blockPos)) {
            return this.concrete;
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.FallingBlock
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.getMapColor(blockGetter, blockPos).col;
    }
}
