package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/VineBlock.class */
public class VineBlock extends Block {

    /* renamed from: UP */
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map) PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter(entry -> {
        return entry.getKey() != Direction.DOWN;
    }).collect(Util.toMap());
    private static final VoxelShape UP_AABB = Block.box(0.0d, 15.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape WEST_AABB = Block.box(0.0d, 0.0d, 0.0d, 1.0d, 16.0d, 16.0d);
    private static final VoxelShape EAST_AABB = Block.box(15.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape NORTH_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 1.0d);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0d, 0.0d, 15.0d, 16.0d, 16.0d, 16.0d);
    private final Map<BlockState, VoxelShape> shapesCache;

    public VineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(UP, false)).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false));
        this.shapesCache = ImmutableMap.copyOf((Map) this.stateDefinition.getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::calculateShape)));
    }

    private static VoxelShape calculateShape(BlockState blockState) {
        VoxelShape empty = Shapes.empty();
        if (((Boolean) blockState.getValue(UP)).booleanValue()) {
            empty = UP_AABB;
        }
        if (((Boolean) blockState.getValue(NORTH)).booleanValue()) {
            empty = Shapes.or(empty, NORTH_AABB);
        }
        if (((Boolean) blockState.getValue(SOUTH)).booleanValue()) {
            empty = Shapes.or(empty, SOUTH_AABB);
        }
        if (((Boolean) blockState.getValue(EAST)).booleanValue()) {
            empty = Shapes.or(empty, EAST_AABB);
        }
        if (((Boolean) blockState.getValue(WEST)).booleanValue()) {
            empty = Shapes.or(empty, WEST_AABB);
        }
        return empty;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapesCache.get(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return hasFaces(getUpdatedState(blockState, levelReader, blockPos));
    }

    private boolean hasFaces(BlockState blockState) {
        return countFaces(blockState) > 0;
    }

    private int countFaces(BlockState blockState) {
        int i = 0;
        Iterator<BooleanProperty> it = PROPERTY_BY_DIRECTION.values().iterator();
        while (it.hasNext()) {
            if (((Boolean) blockState.getValue(it.next())).booleanValue()) {
                i++;
            }
        }
        return i;
    }

    private boolean canSupportAtFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.DOWN) {
            return false;
        }
        if (isAcceptableNeighbour(blockGetter, blockPos.relative(direction), direction)) {
            return true;
        }
        if (direction.getAxis() != Direction.Axis.Y) {
            BooleanProperty booleanProperty = PROPERTY_BY_DIRECTION.get(direction);
            BlockState blockState = blockGetter.getBlockState(blockPos.above());
            return blockState.is(this) && ((Boolean) blockState.getValue(booleanProperty)).booleanValue();
        }
        return false;
    }

    public static boolean isAcceptableNeighbour(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return Block.isFaceFull(blockGetter.getBlockState(blockPos).getCollisionShape(blockGetter, blockPos), direction.getOpposite());
    }

    private BlockState getUpdatedState(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos above = blockPos.above();
        if (((Boolean) blockState.getValue(UP)).booleanValue()) {
            blockState = (BlockState) blockState.setValue(UP, Boolean.valueOf(isAcceptableNeighbour(blockGetter, above, Direction.DOWN)));
        }
        BlockState blockState2 = null;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            BooleanProperty propertyForFace = getPropertyForFace(next);
            if (((Boolean) blockState.getValue(propertyForFace)).booleanValue()) {
                boolean canSupportAtFace = canSupportAtFace(blockGetter, blockPos, next);
                if (!canSupportAtFace) {
                    if (blockState2 == null) {
                        blockState2 = blockGetter.getBlockState(above);
                    }
                    canSupportAtFace = blockState2.is(this) && ((Boolean) blockState2.getValue(propertyForFace)).booleanValue();
                }
                blockState = (BlockState) blockState.setValue(propertyForFace, Boolean.valueOf(canSupportAtFace));
            }
        }
        return blockState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        BlockState updatedState = getUpdatedState(blockState, levelAccessor, blockPos);
        if (!hasFaces(updatedState)) {
            return Blocks.AIR.defaultBlockState();
        }
        return updatedState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (serverLevel.random.nextInt(4) != 0) {
            return;
        }
        Direction random2 = Direction.getRandom(random);
        BlockPos above = blockPos.above();
        if (random2.getAxis().isHorizontal() && !((Boolean) blockState.getValue(getPropertyForFace(random2))).booleanValue()) {
            if (!canSpread(serverLevel, blockPos)) {
                return;
            }
            BlockPos relative = blockPos.relative(random2);
            if (!serverLevel.getBlockState(relative).isAir()) {
                if (isAcceptableNeighbour(serverLevel, relative, random2)) {
                    serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(getPropertyForFace(random2), true), 2);
                    return;
                }
                return;
            }
            Direction clockWise = random2.getClockWise();
            Direction counterClockWise = random2.getCounterClockWise();
            boolean booleanValue = ((Boolean) blockState.getValue(getPropertyForFace(clockWise))).booleanValue();
            boolean booleanValue2 = ((Boolean) blockState.getValue(getPropertyForFace(counterClockWise))).booleanValue();
            BlockPos relative2 = relative.relative(clockWise);
            BlockPos relative3 = relative.relative(counterClockWise);
            if (booleanValue && isAcceptableNeighbour(serverLevel, relative2, clockWise)) {
                serverLevel.setBlock(relative, (BlockState) defaultBlockState().setValue(getPropertyForFace(clockWise), true), 2);
                return;
            }
            if (booleanValue2 && isAcceptableNeighbour(serverLevel, relative3, counterClockWise)) {
                serverLevel.setBlock(relative, (BlockState) defaultBlockState().setValue(getPropertyForFace(counterClockWise), true), 2);
                return;
            }
            Direction opposite = random2.getOpposite();
            if (booleanValue && serverLevel.isEmptyBlock(relative2) && isAcceptableNeighbour(serverLevel, blockPos.relative(clockWise), opposite)) {
                serverLevel.setBlock(relative2, (BlockState) defaultBlockState().setValue(getPropertyForFace(opposite), true), 2);
                return;
            }
            if (booleanValue2 && serverLevel.isEmptyBlock(relative3) && isAcceptableNeighbour(serverLevel, blockPos.relative(counterClockWise), opposite)) {
                serverLevel.setBlock(relative3, (BlockState) defaultBlockState().setValue(getPropertyForFace(opposite), true), 2);
                return;
            } else {
                if (serverLevel.random.nextFloat() < 0.05d && isAcceptableNeighbour(serverLevel, relative.above(), Direction.UP)) {
                    serverLevel.setBlock(relative, (BlockState) defaultBlockState().setValue(UP, true), 2);
                    return;
                }
                return;
            }
        }
        if (random2 == Direction.UP && blockPos.getY() < 255) {
            if (canSupportAtFace(serverLevel, blockPos, random2)) {
                serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(UP, true), 2);
                return;
            }
            if (serverLevel.isEmptyBlock(above)) {
                if (!canSpread(serverLevel, blockPos)) {
                    return;
                }
                BlockState blockState2 = blockState;
                Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
                while (it.hasNext()) {
                    Direction next = it.next();
                    if (random.nextBoolean() || !isAcceptableNeighbour(serverLevel, above.relative(next), Direction.UP)) {
                        blockState2 = (BlockState) blockState2.setValue(getPropertyForFace(next), false);
                    }
                }
                if (hasHorizontalConnection(blockState2)) {
                    serverLevel.setBlock(above, blockState2, 2);
                    return;
                }
                return;
            }
        }
        if (blockPos.getY() > 0) {
            BlockPos below = blockPos.below();
            BlockState blockState3 = serverLevel.getBlockState(below);
            if (blockState3.isAir() || blockState3.is(this)) {
                BlockState defaultBlockState = blockState3.isAir() ? defaultBlockState() : blockState3;
                BlockState copyRandomFaces = copyRandomFaces(blockState, defaultBlockState, random);
                if (defaultBlockState != copyRandomFaces && hasHorizontalConnection(copyRandomFaces)) {
                    serverLevel.setBlock(below, copyRandomFaces, 2);
                }
            }
        }
    }

    private BlockState copyRandomFaces(BlockState blockState, BlockState blockState2, Random random) {
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (random.nextBoolean()) {
                BooleanProperty propertyForFace = getPropertyForFace(next);
                if (((Boolean) blockState.getValue(propertyForFace)).booleanValue()) {
                    blockState2 = (BlockState) blockState2.setValue(propertyForFace, true);
                }
            }
        }
        return blockState2;
    }

    private boolean hasHorizontalConnection(BlockState blockState) {
        return ((Boolean) blockState.getValue(NORTH)).booleanValue() || ((Boolean) blockState.getValue(EAST)).booleanValue() || ((Boolean) blockState.getValue(SOUTH)).booleanValue() || ((Boolean) blockState.getValue(WEST)).booleanValue();
    }

    private boolean canSpread(BlockGetter blockGetter, BlockPos blockPos) {
        int i = 5;
        Iterator<BlockPos> it = BlockPos.betweenClosed(blockPos.getX() - 4, blockPos.getY() - 1, blockPos.getZ() - 4, blockPos.getX() + 4, blockPos.getY() + 1, blockPos.getZ() + 4).iterator();
        while (it.hasNext()) {
            if (blockGetter.getBlockState(it.next()).is(this)) {
                i--;
                if (i <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState2.is(this)) {
            return countFaces(blockState2) < PROPERTY_BY_DIRECTION.size();
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        boolean is = blockState.is(this);
        BlockState defaultBlockState = is ? blockState : defaultBlockState();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            if (direction != Direction.DOWN) {
                BooleanProperty propertyForFace = getPropertyForFace(direction);
                if (!(is && ((Boolean) blockState.getValue(propertyForFace)).booleanValue()) && canSupportAtFace(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), direction)) {
                    return (BlockState) defaultBlockState.setValue(propertyForFace, true);
                }
            }
        }
        if (is) {
            return defaultBlockState;
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH, EAST, SOUTH, WEST);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            case CLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            default:
                return blockState;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return (BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            case FRONT_BACK:
                return (BlockState) ((BlockState) blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            default:
                return super.mirror(blockState, mirror);
        }
    }

    public static BooleanProperty getPropertyForFace(Direction direction) {
        return PROPERTY_BY_DIRECTION.get(direction);
    }
}
