package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BaseRailBlock.class */
public abstract class BaseRailBlock extends Block {
    protected static final VoxelShape FLAT_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d);
    protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d);
    private final boolean isStraight;

    public abstract Property<RailShape> getShapeProperty();

    public static boolean isRail(Level level, BlockPos blockPos) {
        return isRail(level.getBlockState(blockPos));
    }

    public static boolean isRail(BlockState blockState) {
        return blockState.is(BlockTags.RAILS) && (blockState.getBlock() instanceof BaseRailBlock);
    }

    protected BaseRailBlock(boolean z, BlockBehaviour.Properties properties) {
        super(properties);
        this.isStraight = z;
    }

    public boolean isStraight() {
        return this.isStraight;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        RailShape railShape = blockState.is(this) ? (RailShape) blockState.getValue(getShapeProperty()) : null;
        if (railShape != null && railShape.isAscending()) {
            return HALF_BLOCK_AABB;
        }
        return FLAT_AABB;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return canSupportRigidBlock(levelReader, blockPos.below());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        updateState(blockState, level, blockPos, z);
    }

    protected BlockState updateState(BlockState blockState, Level level, BlockPos blockPos, boolean z) {
        BlockState updateDir = updateDir(level, blockPos, blockState, true);
        if (this.isStraight) {
            updateDir.neighborChanged(level, blockPos, this, blockPos, z);
        }
        return updateDir;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (level.isClientSide || !level.getBlockState(blockPos).is(this)) {
            return;
        }
        if (shouldBeRemoved(blockPos, level, (RailShape) blockState.getValue(getShapeProperty()))) {
            dropResources(blockState, level, blockPos);
            level.removeBlock(blockPos, z);
        } else {
            updateState(blockState, level, blockPos, block);
        }
    }

    private static boolean shouldBeRemoved(BlockPos blockPos, Level level, RailShape railShape) {
        if (!canSupportRigidBlock(level, blockPos.below())) {
            return true;
        }
        switch (railShape) {
            case ASCENDING_EAST:
                return !canSupportRigidBlock(level, blockPos.east());
            case ASCENDING_WEST:
                return !canSupportRigidBlock(level, blockPos.west());
            case ASCENDING_NORTH:
                return !canSupportRigidBlock(level, blockPos.north());
            case ASCENDING_SOUTH:
                return !canSupportRigidBlock(level, blockPos.south());
            default:
                return false;
        }
    }

    protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
    }

    protected BlockState updateDir(Level level, BlockPos blockPos, BlockState blockState, boolean z) {
        if (level.isClientSide) {
            return blockState;
        }
        return new RailState(level, blockPos, blockState).place(level.hasNeighborSignal(blockPos), z, (RailShape) blockState.getValue(getShapeProperty())).getState();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.NORMAL;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z) {
            return;
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
        if (((RailShape) blockState.getValue(getShapeProperty())).isAscending()) {
            level.updateNeighborsAt(blockPos.above(), this);
        }
        if (this.isStraight) {
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState defaultBlockState = super.defaultBlockState();
        Direction horizontalDirection = blockPlaceContext.getHorizontalDirection();
        return (BlockState) defaultBlockState.setValue(getShapeProperty(), horizontalDirection == Direction.EAST || horizontalDirection == Direction.WEST ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
    }
}
