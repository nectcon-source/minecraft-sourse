package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BellBlock.class */
public class BellBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0d, 0.0d, 4.0d, 16.0d, 16.0d, 12.0d);
    private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0d, 0.0d, 0.0d, 12.0d, 16.0d, 16.0d);
    private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0d, 6.0d, 5.0d, 11.0d, 13.0d, 11.0d);
    private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0d, 4.0d, 4.0d, 12.0d, 6.0d, 12.0d);
    private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
    private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0d, 13.0d, 0.0d, 9.0d, 15.0d, 16.0d));
    private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0d, 13.0d, 7.0d, 16.0d, 15.0d, 9.0d));
    private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0d, 13.0d, 7.0d, 13.0d, 15.0d, 9.0d));
    private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0d, 13.0d, 7.0d, 16.0d, 15.0d, 9.0d));
    private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0d, 13.0d, 0.0d, 9.0d, 15.0d, 13.0d));
    private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0d, 13.0d, 3.0d, 9.0d, 15.0d, 16.0d));
    private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0d, 13.0d, 7.0d, 9.0d, 16.0d, 9.0d));

    public BellBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        boolean hasNeighborSignal = level.hasNeighborSignal(blockPos);
        if (hasNeighborSignal != ( blockState.getValue(POWERED)).booleanValue()) {
            if (hasNeighborSignal) {
                attemptToRing(level, blockPos, null);
            }
            level.setBlock(blockPos,  blockState.setValue(POWERED, Boolean.valueOf(hasNeighborSignal)), 3);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        Entity owner = projectile.getOwner();
        onHit(level, blockState, blockHitResult, owner instanceof Player ? (Player) owner : null, true);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return onHit(level, blockState, blockHitResult, player, true) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    public boolean onHit(Level level, BlockState blockState, BlockHitResult blockHitResult, @Nullable Player player, boolean z) {
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!z || isProperHit(blockState, direction, blockHitResult.getLocation().y - ((double) blockPos.getY()))) {
            if (attemptToRing(level, blockPos, direction) && player != null) {
                player.awardStat(Stats.BELL_RING);
                return true;
            }
            return true;
        }
        return false;
    }

    private boolean isProperHit(BlockState blockState, Direction direction, double d) {
        if (direction.getAxis() == Direction.Axis.Y || d > 0.8123999834060669d) {
            return false;
        }
        Direction direction2 = (Direction) blockState.getValue(FACING);
        switch ((BellAttachType) blockState.getValue(ATTACHMENT)) {
            case FLOOR:
                return direction2.getAxis() == direction.getAxis();
            case SINGLE_WALL:
            case DOUBLE_WALL:
                return direction2.getAxis() != direction.getAxis();
            case CEILING:
                return true;
            default:
                return false;
        }
    }

    public boolean attemptToRing(Level level, BlockPos blockPos, @Nullable Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!level.isClientSide && (blockEntity instanceof BellBlockEntity)) {
            if (direction == null) {
                direction = (Direction) level.getBlockState(blockPos).getValue(FACING);
            }
            ((BellBlockEntity) blockEntity).onHit(direction);
            level.playSound((Player) null, blockPos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 1.0f);
            return true;
        }
        return false;
    }

    private VoxelShape getVoxelShape(BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BellAttachType bellAttachType = (BellAttachType) blockState.getValue(ATTACHMENT);
        if (bellAttachType == BellAttachType.FLOOR) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                return NORTH_SOUTH_FLOOR_SHAPE;
            }
            return EAST_WEST_FLOOR_SHAPE;
        }
        if (bellAttachType == BellAttachType.CEILING) {
            return CEILING_SHAPE;
        }
        if (bellAttachType == BellAttachType.DOUBLE_WALL) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                return NORTH_SOUTH_BETWEEN;
            }
            return EAST_WEST_BETWEEN;
        }
        if (direction == Direction.NORTH) {
            return TO_NORTH;
        }
        if (direction == Direction.SOUTH) {
            return TO_SOUTH;
        }
        if (direction == Direction.EAST) {
            return TO_EAST;
        }
        return TO_WEST;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getVoxelShape(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getVoxelShape(blockState);
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction clickedFace = blockPlaceContext.getClickedFace();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        Direction.Axis axis = clickedFace.getAxis();
        if (axis == Direction.Axis.Y) {
            BlockState blockState = (BlockState) ((BlockState) defaultBlockState().setValue(ATTACHMENT, clickedFace == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)).setValue(FACING, blockPlaceContext.getHorizontalDirection());
            if (blockState.canSurvive(blockPlaceContext.getLevel(), clickedPos)) {
                return blockState;
            }
            return null;
        }
        BlockState blockState2 = (BlockState) ((BlockState) defaultBlockState().setValue(FACING, clickedFace.getOpposite())).setValue(ATTACHMENT, (axis == Direction.Axis.X && level.getBlockState(clickedPos.west()).isFaceSturdy(level, clickedPos.west(), Direction.EAST) && level.getBlockState(clickedPos.east()).isFaceSturdy(level, clickedPos.east(), Direction.WEST)) || (axis == Direction.Axis.Z && level.getBlockState(clickedPos.north()).isFaceSturdy(level, clickedPos.north(), Direction.SOUTH) && level.getBlockState(clickedPos.south()).isFaceSturdy(level, clickedPos.south(), Direction.NORTH)) ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
        if (blockState2.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
            return blockState2;
        }
        BlockState blockState3 = (BlockState) blockState2.setValue(ATTACHMENT, level.getBlockState(clickedPos.below()).isFaceSturdy(level, clickedPos.below(), Direction.UP) ? BellAttachType.FLOOR : BellAttachType.CEILING);
        if (blockState3.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
            return blockState3;
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        BellAttachType bellAttachType = (BellAttachType) blockState.getValue(ATTACHMENT);
        Direction opposite = getConnectedDirection(blockState).getOpposite();
        if (opposite == direction && !blockState.canSurvive(levelAccessor, blockPos) && bellAttachType != BellAttachType.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction.getAxis() == ((Direction) blockState.getValue(FACING)).getAxis()) {
            if (bellAttachType == BellAttachType.DOUBLE_WALL && !blockState2.isFaceSturdy(levelAccessor, blockPos2, direction)) {
                return (BlockState) ((BlockState) blockState.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL)).setValue(FACING, direction.getOpposite());
            }
            if (bellAttachType == BellAttachType.SINGLE_WALL && opposite.getOpposite() == direction && blockState2.isFaceSturdy(levelAccessor, blockPos2, (Direction) blockState.getValue(FACING))) {
                return (BlockState) blockState.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
            }
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction opposite = getConnectedDirection(blockState).getOpposite();
        if (opposite == Direction.UP) {
            return Block.canSupportCenter(levelReader, blockPos.above(), Direction.DOWN);
        }
        return FaceAttachedHorizontalDirectionalBlock.canAttach(levelReader, blockPos, opposite);
    }

    private static Direction getConnectedDirection(BlockState blockState) {
        switch ((BellAttachType) blockState.getValue(ATTACHMENT)) {
            case FLOOR:
                return Direction.UP;
            case CEILING:
                return Direction.DOWN;
            default:
                return ((Direction) blockState.getValue(FACING)).getOpposite();
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    @Nullable
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new BellBlockEntity();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
