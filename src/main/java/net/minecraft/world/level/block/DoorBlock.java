package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoorBlock.class */
public class DoorBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 3.0d);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0d, 0.0d, 13.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape WEST_AABB = Block.box(13.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape EAST_AABB = Block.box(0.0d, 0.0d, 0.0d, 3.0d, 16.0d, 16.0d);

    protected DoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(OPEN, false)).setValue(HINGE, DoorHingeSide.LEFT)).setValue(POWERED, false)).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction direction = (Direction) blockState.getValue(FACING);
        boolean z = !((Boolean) blockState.getValue(OPEN)).booleanValue();
        boolean z2 = blockState.getValue(HINGE) == DoorHingeSide.RIGHT;
        switch (direction) {
            case EAST:
            default:
                return z ? EAST_AABB : z2 ? NORTH_AABB : SOUTH_AABB;
            case SOUTH:
                return z ? SOUTH_AABB : z2 ? EAST_AABB : WEST_AABB;
            case WEST:
                return z ? WEST_AABB : z2 ? SOUTH_AABB : NORTH_AABB;
            case NORTH:
                return z ? NORTH_AABB : z2 ? WEST_AABB : EAST_AABB;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        DoubleBlockHalf doubleBlockHalf = (DoubleBlockHalf) blockState.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y) {
            if ((doubleBlockHalf == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
                if (blockState2.is(this) && blockState2.getValue(HALF) != doubleBlockHalf) {
                    return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(FACING, blockState2.getValue(FACING))).setValue(OPEN, blockState2.getValue(OPEN))).setValue(HINGE, blockState2.getValue(HINGE))).setValue(POWERED, blockState2.getValue(POWERED));
                }
                return Blocks.AIR.defaultBlockState();
            }
        }
        if (doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            DoublePlantBlock.preventCreativeDropFromBottomPart(level, blockPos, blockState, player);
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
        }
        return ((Boolean) blockState.getValue(OPEN)).booleanValue();
    }

    private int getCloseSound() {
        return this.material == Material.METAL ? 1011 : 1012;
    }

    private int getOpenSound() {
        return this.material == Material.METAL ? 1005 : 1006;
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        if (clickedPos.getY() < 255 && blockPlaceContext.getLevel().getBlockState(clickedPos.above()).canBeReplaced(blockPlaceContext)) {
            Level level = blockPlaceContext.getLevel();
            boolean z = level.hasNeighborSignal(clickedPos) || level.hasNeighborSignal(clickedPos.above());
            return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection())).setValue(HINGE, getHinge(blockPlaceContext))).setValue(POWERED, Boolean.valueOf(z))).setValue(OPEN, Boolean.valueOf(z))).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        level.setBlock(blockPos.above(), (BlockState) blockState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    private DoorHingeSide getHinge(BlockPlaceContext blockPlaceContext) {
        BlockGetter level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Direction horizontalDirection = blockPlaceContext.getHorizontalDirection();
        BlockPos above = clickedPos.above();
        Direction counterClockWise = horizontalDirection.getCounterClockWise();
        BlockPos relative = clickedPos.relative(counterClockWise);
        BlockState blockState = level.getBlockState(relative);
        BlockPos relative2 = above.relative(counterClockWise);
        BlockState blockState2 = level.getBlockState(relative2);
        Direction clockWise = horizontalDirection.getClockWise();
        BlockPos relative3 = clickedPos.relative(clockWise);
        BlockState blockState3 = level.getBlockState(relative3);
        BlockPos relative4 = above.relative(clockWise);
        int i = (blockState.isCollisionShapeFullBlock(level, relative) ? -1 : 0) + (blockState2.isCollisionShapeFullBlock(level, relative2) ? -1 : 0) + (blockState3.isCollisionShapeFullBlock(level, relative3) ? 1 : 0) + (level.getBlockState(relative4).isCollisionShapeFullBlock(level, relative4) ? 1 : 0);
        boolean z = blockState.is(this) && blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean z2 = blockState3.is(this) && blockState3.getValue(HALF) == DoubleBlockHalf.LOWER;
        if ((z && !z2) || i > 0) {
            return DoorHingeSide.RIGHT;
        }
        if ((z2 && !z) || i < 0) {
            return DoorHingeSide.LEFT;
        }
        int stepX = horizontalDirection.getStepX();
        int stepZ = horizontalDirection.getStepZ();
        Vec3 clickLocation = blockPlaceContext.getClickLocation();
        double x = clickLocation.x - clickedPos.getX();
        double z3 = clickLocation.z - clickedPos.getZ();
        return ((stepX >= 0 || z3 >= 0.5d) && (stepX <= 0 || z3 <= 0.5d) && ((stepZ >= 0 || x <= 0.5d) && (stepZ <= 0 || x >= 0.5d))) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (this.material == Material.METAL) {
            return InteractionResult.PASS;
        }
        BlockState cycle = blockState.cycle(OPEN);
        level.setBlock(blockPos, cycle, 10);
        level.levelEvent(player, ((Boolean) cycle.getValue(OPEN)).booleanValue() ? getOpenSound() : getCloseSound(), blockPos, 0);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public boolean isOpen(BlockState blockState) {
        return ((Boolean) blockState.getValue(OPEN)).booleanValue();
    }

    public void setOpen(Level level, BlockState blockState, BlockPos blockPos, boolean z) {
        if (!blockState.is(this) || ((Boolean) blockState.getValue(OPEN)).booleanValue() == z) {
            return;
        }
        level.setBlock(blockPos, (BlockState) blockState.setValue(OPEN, Boolean.valueOf(z)), 10);
        playSound(level, blockPos, z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        boolean z2;
        if (!level.hasNeighborSignal(blockPos)) {
            if (!level.hasNeighborSignal(blockPos.relative(blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN))) {
                z2 = false;
                boolean z3 = z2;
                if (block == this && z3 != ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
                    if (z3 != ((Boolean) blockState.getValue(OPEN)).booleanValue()) {
                        playSound(level, blockPos, z3);
                    }
                    level.setBlock(blockPos, (BlockState) ((BlockState) blockState.setValue(POWERED, Boolean.valueOf(z3))).setValue(OPEN, Boolean.valueOf(z3)), 2);
                    return;
                }
            }
        }
        z2 = true;
        boolean z32 = z2;
        if (block == this) {
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(below);
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return blockState2.isFaceSturdy(levelReader, below, Direction.UP);
        }
        return blockState2.is(this);
    }

    private void playSound(Level level, BlockPos blockPos, boolean z) {
        level.levelEvent(null, z ? getOpenSound() : getCloseSound(), blockPos, 0);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return blockState;
        }
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING))).cycle(HINGE);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        return Mth.getSeed(blockPos.getX(), blockPos.below(blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), blockPos.getZ());
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, OPEN, HINGE, POWERED);
    }

    public static boolean isWoodenDoor(Level level, BlockPos blockPos) {
        return isWoodenDoor(level.getBlockState(blockPos));
    }

    public static boolean isWoodenDoor(BlockState blockState) {
        return (blockState.getBlock() instanceof DoorBlock) && (blockState.getMaterial() == Material.WOOD || blockState.getMaterial() == Material.NETHER_WOOD);
    }
}
