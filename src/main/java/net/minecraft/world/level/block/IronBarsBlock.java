package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/IronBarsBlock.class */
public class IronBarsBlock extends CrossCollisionBlock {
    protected IronBarsBlock(BlockBehaviour.Properties properties) {
        super(1.0f, 1.0f, 16.0f, 16.0f, 16.0f, properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockGetter level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos north = clickedPos.north();
        BlockPos south = clickedPos.south();
        BlockPos west = clickedPos.west();
        BlockPos east = clickedPos.east();
        BlockState blockState = level.getBlockState(north);
        BlockState blockState2 = level.getBlockState(south);
        BlockState blockState3 = level.getBlockState(west);
        BlockState blockState4 = level.getBlockState(east);
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(NORTH, Boolean.valueOf(attachsTo(blockState, blockState.isFaceSturdy(level, north, Direction.SOUTH))))).setValue(SOUTH, Boolean.valueOf(attachsTo(blockState2, blockState2.isFaceSturdy(level, south, Direction.NORTH))))).setValue(WEST, Boolean.valueOf(attachsTo(blockState3, blockState3.isFaceSturdy(level, west, Direction.EAST))))).setValue(EAST, Boolean.valueOf(attachsTo(blockState4, blockState4.isFaceSturdy(level, east, Direction.WEST))))).setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(attachsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction.getOpposite()))));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        if (blockState2.is(this)) {
            if (!direction.getAxis().isHorizontal()) {
                return true;
            }
            if (((Boolean) blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).booleanValue() && ((Boolean) blockState2.getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).booleanValue()) {
                return true;
            }
        }
        return super.skipRendering(blockState, blockState2, direction);
    }

    public final boolean attachsTo(BlockState blockState, boolean z) {
        Block block = blockState.getBlock();
        return (!isExceptionForConnection(block) && z) || (block instanceof IronBarsBlock) || block.is(BlockTags.WALLS);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}
