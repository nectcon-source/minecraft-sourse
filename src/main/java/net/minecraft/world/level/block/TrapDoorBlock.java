package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TrapDoorBlock.class */
public class TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0d, 0.0d, 0.0d, 3.0d, 16.0d, 16.0d);
    protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 3.0d);
    protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0d, 0.0d, 13.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 3.0d, 16.0d);
    protected static final VoxelShape TOP_AABB = Block.box(0.0d, 13.0d, 0.0d, 16.0d, 16.0d, 16.0d);

    protected TrapDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(OPEN, false)).setValue(HALF, Half.BOTTOM)).setValue(POWERED, false)).setValue(WATERLOGGED, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!((Boolean) blockState.getValue(OPEN)).booleanValue()) {
            return blockState.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
        }
        switch ((Direction) blockState.getValue(FACING)) {
            case NORTH:
            default:
                return NORTH_OPEN_AABB;
            case SOUTH:
                return SOUTH_OPEN_AABB;
            case WEST:
                return WEST_OPEN_AABB;
            case EAST:
                return EAST_OPEN_AABB;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND:
                return ((Boolean) blockState.getValue(OPEN)).booleanValue();
            case WATER:
                return ((Boolean) blockState.getValue(WATERLOGGED)).booleanValue();
            case AIR:
                return ((Boolean) blockState.getValue(OPEN)).booleanValue();
            default:
                return false;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (this.material == Material.METAL) {
            return InteractionResult.PASS;
        }
        BlockState cycle = blockState.cycle(OPEN);
        level.setBlock(blockPos, cycle, 2);
        if (((Boolean) cycle.getValue(WATERLOGGED)).booleanValue()) {
            level.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        playSound(player, level, blockPos, ((Boolean) cycle.getValue(OPEN)).booleanValue());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected void playSound(@Nullable Player player, Level level, BlockPos blockPos, boolean z) {
        if (z) {
            level.levelEvent(player, this.material == Material.METAL ? 1037 : 1007, blockPos, 0);
        } else {
            level.levelEvent(player, this.material == Material.METAL ? 1036 : 1013, blockPos, 0);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        boolean hasNeighborSignal;
        if (!level.isClientSide && (hasNeighborSignal = level.hasNeighborSignal(blockPos)) != ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            if (((Boolean) blockState.getValue(OPEN)).booleanValue() != hasNeighborSignal) {
                blockState = (BlockState) blockState.setValue(OPEN, Boolean.valueOf(hasNeighborSignal));
                playSound(null, level, blockPos, hasNeighborSignal);
            }
            level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, Boolean.valueOf(hasNeighborSignal)), 2);
            if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
                level.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState;
        BlockState defaultBlockState = defaultBlockState();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        Direction clickedFace = blockPlaceContext.getClickedFace();
        if (blockPlaceContext.replacingClickedOnBlock() || !clickedFace.getAxis().isHorizontal()) {
            blockState = (BlockState) ((BlockState) defaultBlockState.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())).setValue(HALF, clickedFace == Direction.UP ? Half.BOTTOM : Half.TOP);
        } else {
            blockState = (BlockState) ((BlockState) defaultBlockState.setValue(FACING, clickedFace)).setValue(HALF, blockPlaceContext.getClickLocation().y - ((double) blockPlaceContext.getClickedPos().getY()) > 0.5d ? Half.TOP : Half.BOTTOM);
        }
        if (blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())) {
            blockState = (BlockState) ((BlockState) blockState.setValue(OPEN, true)).setValue(POWERED, true);
        }
        return (BlockState) blockState.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }
}
