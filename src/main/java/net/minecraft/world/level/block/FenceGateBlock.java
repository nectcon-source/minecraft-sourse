package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FenceGateBlock.class */
public class FenceGateBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
    protected static final VoxelShape Z_SHAPE = Block.box(0.0d, 0.0d, 6.0d, 16.0d, 16.0d, 10.0d);
    protected static final VoxelShape X_SHAPE = Block.box(6.0d, 0.0d, 0.0d, 10.0d, 16.0d, 16.0d);
    protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0d, 0.0d, 6.0d, 16.0d, 13.0d, 10.0d);
    protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0d, 0.0d, 0.0d, 10.0d, 13.0d, 16.0d);
    protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0d, 0.0d, 6.0d, 16.0d, 24.0d, 10.0d);
    protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0d, 0.0d, 0.0d, 10.0d, 24.0d, 16.0d);
    protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0d, 5.0d, 7.0d, 2.0d, 16.0d, 9.0d), Block.box(14.0d, 5.0d, 7.0d, 16.0d, 16.0d, 9.0d));
    protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0d, 5.0d, 0.0d, 9.0d, 16.0d, 2.0d), Block.box(7.0d, 5.0d, 14.0d, 9.0d, 16.0d, 16.0d));
    protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0d, 2.0d, 7.0d, 2.0d, 13.0d, 9.0d), Block.box(14.0d, 2.0d, 7.0d, 16.0d, 13.0d, 9.0d));
    protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0d, 2.0d, 0.0d, 9.0d, 13.0d, 2.0d), Block.box(7.0d, 2.0d, 14.0d, 9.0d, 13.0d, 16.0d));

    public FenceGateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(OPEN, false)).setValue(POWERED, false)).setValue(IN_WALL, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return ((Boolean) blockState.getValue(IN_WALL)).booleanValue() ? ((Direction) blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW : ((Direction) blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Direction) blockState.getValue(FACING)).getClockWise().getAxis() == direction.getAxis()) {
            return (BlockState) blockState.setValue(IN_WALL, Boolean.valueOf(isWall(blockState2) || isWall(levelAccessor.getBlockState(blockPos.relative(direction.getOpposite())))));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (((Boolean) blockState.getValue(OPEN)).booleanValue()) {
            return Shapes.empty();
        }
        return ((Direction) blockState.getValue(FACING)).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return ((Boolean) blockState.getValue(IN_WALL)).booleanValue() ? ((Direction) blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW : ((Direction) blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
        }
        return ((Boolean) blockState.getValue(OPEN)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        boolean hasNeighborSignal = level.hasNeighborSignal(clickedPos);
        Direction horizontalDirection = blockPlaceContext.getHorizontalDirection();
        Direction.Axis axis = horizontalDirection.getAxis();
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(FACING, horizontalDirection)).setValue(OPEN, Boolean.valueOf(hasNeighborSignal))).setValue(POWERED, Boolean.valueOf(hasNeighborSignal))).setValue(IN_WALL, Boolean.valueOf((axis == Direction.Axis.Z && (isWall(level.getBlockState(clickedPos.west())) || isWall(level.getBlockState(clickedPos.east())))) || (axis == Direction.Axis.X && (isWall(level.getBlockState(clickedPos.north())) || isWall(level.getBlockState(clickedPos.south()))))));
    }

    private boolean isWall(BlockState blockState) {
        return blockState.getBlock().is(BlockTags.WALLS);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockState blockState2;
        if (((Boolean) blockState.getValue(OPEN)).booleanValue()) {
            blockState2 = (BlockState) blockState.setValue(OPEN, false);
            level.setBlock(blockPos, blockState2, 10);
        } else {
            Direction direction = player.getDirection();
            if (blockState.getValue(FACING) == direction.getOpposite()) {
                blockState = (BlockState) blockState.setValue(FACING, direction);
            }
            blockState2 = (BlockState) blockState.setValue(OPEN, true);
            level.setBlock(blockPos, blockState2, 10);
        }
        level.levelEvent(player, ((Boolean) blockState2.getValue(OPEN)).booleanValue() ? 1008 : 1014, blockPos, 0);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        boolean hasNeighborSignal;
        if (!level.isClientSide && ((Boolean) blockState.getValue(POWERED)).booleanValue() != (hasNeighborSignal = level.hasNeighborSignal(blockPos))) {
            level.setBlock(blockPos, (BlockState) ((BlockState) blockState.setValue(POWERED, Boolean.valueOf(hasNeighborSignal))).setValue(OPEN, Boolean.valueOf(hasNeighborSignal)), 2);
            if (((Boolean) blockState.getValue(OPEN)).booleanValue() != hasNeighborSignal) {
                level.levelEvent(null, hasNeighborSignal ? 1008 : 1014, blockPos, 0);
            }
        }
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, IN_WALL);
    }

    public static boolean connectsToDirection(BlockState blockState, Direction direction) {
        return ((Direction) blockState.getValue(FACING)).getAxis() == direction.getClockWise().getAxis();
    }
}
