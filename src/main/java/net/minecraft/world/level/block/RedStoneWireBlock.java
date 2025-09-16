package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.math.Vector3f;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RedStoneWireBlock.class */
public class RedStoneWireBlock extends Block {
    private final Map<BlockState, VoxelShape> SHAPES_CACHE;
    private final BlockState crossState;
    private boolean shouldSignal;
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
    private static final VoxelShape SHAPE_DOT = Block.box(3.0d, 0.0d, 3.0d, 13.0d, 1.0d, 13.0d);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0d, 0.0d, 0.0d, 13.0d, 1.0d, 13.0d), Direction.SOUTH, Block.box(3.0d, 0.0d, 3.0d, 13.0d, 1.0d, 16.0d), Direction.EAST, Block.box(3.0d, 0.0d, 3.0d, 16.0d, 1.0d, 13.0d), Direction.WEST, Block.box(0.0d, 0.0d, 3.0d, 13.0d, 1.0d, 13.0d)));
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0d, 0.0d, 0.0d, 13.0d, 16.0d, 1.0d)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0d, 0.0d, 15.0d, 13.0d, 16.0d, 16.0d)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0d, 0.0d, 3.0d, 16.0d, 16.0d, 13.0d)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0d, 0.0d, 3.0d, 1.0d, 16.0d, 13.0d))));
    private static final Vector3f[] COLORS = new Vector3f[16];

    static {
        for (int i = 0; i <= 15; i++) {
            float f = i / 15.0f;
            COLORS[i] = new Vector3f((f * 0.6f) + (f > 0.0f ? 0.4f : 0.3f), Mth.clamp(((f * f) * 0.7f) - 0.5f, 0.0f, 1.0f), Mth.clamp(((f * f) * 0.6f) - 0.7f, 0.0f, 1.0f));
        }
    }

    public RedStoneWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.SHAPES_CACHE = Maps.newHashMap();
        this.shouldSignal = true;
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE)).setValue(EAST, RedstoneSide.NONE)).setValue(SOUTH, RedstoneSide.NONE)).setValue(WEST, RedstoneSide.NONE)).setValue(POWER, 0));
        this.crossState = (BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(NORTH, RedstoneSide.SIDE)).setValue(EAST, RedstoneSide.SIDE)).setValue(SOUTH, RedstoneSide.SIDE)).setValue(WEST, RedstoneSide.SIDE);
        UnmodifiableIterator it = getStateDefinition().getPossibleStates().iterator();
        while (it.hasNext()) {
            BlockState blockState = (BlockState) it.next();
            if (((Integer) blockState.getValue(POWER)).intValue() == 0) {
                this.SHAPES_CACHE.put(blockState, calculateShape(blockState));
            }
        }
    }

    private VoxelShape calculateShape(BlockState blockState) {
        VoxelShape voxelShape = SHAPE_DOT;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            RedstoneSide redstoneSide = (RedstoneSide) blockState.getValue(PROPERTY_BY_DIRECTION.get(next));
            if (redstoneSide == RedstoneSide.SIDE) {
                voxelShape = Shapes.or(voxelShape, SHAPES_FLOOR.get(next));
            } else if (redstoneSide == RedstoneSide.UP) {
                voxelShape = Shapes.or(voxelShape, SHAPES_UP.get(next));
            }
        }
        return voxelShape;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.SHAPES_CACHE.get(blockState.setValue(POWER, 0));
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getConnectionState(blockPlaceContext.getLevel(), this.crossState, blockPlaceContext.getClickedPos());
    }

    private BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean isDot = isDot(blockState);
        BlockState missingConnections = getMissingConnections(blockGetter, (BlockState) defaultBlockState().setValue(POWER, blockState.getValue(POWER)), blockPos);
        if (isDot && isDot(missingConnections)) {
            return missingConnections;
        }
        boolean isConnected = ((RedstoneSide) missingConnections.getValue(NORTH)).isConnected();
        boolean isConnected2 = ((RedstoneSide) missingConnections.getValue(SOUTH)).isConnected();
        boolean isConnected3 = ((RedstoneSide) missingConnections.getValue(EAST)).isConnected();
        boolean isConnected4 = ((RedstoneSide) missingConnections.getValue(WEST)).isConnected();
        boolean z = (isConnected || isConnected2) ? false : true;
        boolean z2 = (isConnected3 || isConnected4) ? false : true;
        if (!isConnected4 && z) {
            missingConnections = (BlockState) missingConnections.setValue(WEST, RedstoneSide.SIDE);
        }
        if (!isConnected3 && z) {
            missingConnections = (BlockState) missingConnections.setValue(EAST, RedstoneSide.SIDE);
        }
        if (!isConnected && z2) {
            missingConnections = (BlockState) missingConnections.setValue(NORTH, RedstoneSide.SIDE);
        }
        if (!isConnected2 && z2) {
            missingConnections = (BlockState) missingConnections.setValue(SOUTH, RedstoneSide.SIDE);
        }
        return missingConnections;
    }

    private BlockState getMissingConnections(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean z = !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos);
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (!((RedstoneSide) blockState.getValue(PROPERTY_BY_DIRECTION.get(next))).isConnected()) {
                blockState = (BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(next), getConnectingSide(blockGetter, blockPos, next, z));
            }
        }
        return blockState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN) {
            return blockState;
        }
        if (direction == Direction.UP) {
            return getConnectionState(levelAccessor, blockState, blockPos);
        }
        RedstoneSide connectingSide = getConnectingSide(levelAccessor, blockPos, direction);
        if (connectingSide.isConnected() == ((RedstoneSide) blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() && !isCross(blockState)) {
            return (BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), connectingSide);
        }
        return getConnectionState(levelAccessor, (BlockState) ((BlockState) this.crossState.setValue(POWER, blockState.getValue(POWER))).setValue(PROPERTY_BY_DIRECTION.get(direction), connectingSide), blockPos);
    }

    private static boolean isCross(BlockState blockState) {
        return ((RedstoneSide) blockState.getValue(NORTH)).isConnected() && ((RedstoneSide) blockState.getValue(SOUTH)).isConnected() && ((RedstoneSide) blockState.getValue(EAST)).isConnected() && ((RedstoneSide) blockState.getValue(WEST)).isConnected();
    }

    private static boolean isDot(BlockState blockState) {
        return (((RedstoneSide) blockState.getValue(NORTH)).isConnected() || ((RedstoneSide) blockState.getValue(SOUTH)).isConnected() || ((RedstoneSide) blockState.getValue(EAST)).isConnected() || ((RedstoneSide) blockState.getValue(WEST)).isConnected()) ? false : true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int i2) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (((RedstoneSide) blockState.getValue(PROPERTY_BY_DIRECTION.get(next))) != RedstoneSide.NONE && !levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, next)).is(this)) {
                mutableBlockPos.move(Direction.DOWN);
                BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
                if (!blockState2.is(Blocks.OBSERVER)) {
                    BlockPos relative = mutableBlockPos.relative(next.getOpposite());
                    updateOrDestroy(blockState2, blockState2.updateShape(next.getOpposite(), levelAccessor.getBlockState(relative), levelAccessor, mutableBlockPos, relative), levelAccessor, mutableBlockPos, i, i2);
                }
                mutableBlockPos.setWithOffset(blockPos, next).move(Direction.UP);
                BlockState blockState3 = levelAccessor.getBlockState(mutableBlockPos);
                if (!blockState3.is(Blocks.OBSERVER)) {
                    BlockPos relative2 = mutableBlockPos.relative(next.getOpposite());
                    updateOrDestroy(blockState3, blockState3.updateShape(next.getOpposite(), levelAccessor.getBlockState(relative2), levelAccessor, mutableBlockPos, relative2), levelAccessor, mutableBlockPos, i, i2);
                }
            }
        }
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return getConnectingSide(blockGetter, blockPos, direction, !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos));
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean z) {
        BlockPos relative = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(relative);
        if (z && canSurviveOn(blockGetter, relative, blockState) && shouldConnectTo(blockGetter.getBlockState(relative.above()))) {
            if (blockState.isFaceSturdy(blockGetter, relative, direction.getOpposite())) {
                return RedstoneSide.UP;
            }
            return RedstoneSide.SIDE;
        }
        if (shouldConnectTo(blockState, direction) || (!blockState.isRedstoneConductor(blockGetter, relative) && shouldConnectTo(blockGetter.getBlockState(relative.below())))) {
            return RedstoneSide.SIDE;
        }
        return RedstoneSide.NONE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        return canSurviveOn(levelReader, below, levelReader.getBlockState(below));
    }

    private boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) || blockState.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState) {
        int calculateTargetStrength = calculateTargetStrength(level, blockPos);
        if (((Integer) blockState.getValue(POWER)).intValue() != calculateTargetStrength) {
            if (level.getBlockState(blockPos) == blockState) {
                level.setBlock(blockPos, (BlockState) blockState.setValue(POWER, Integer.valueOf(calculateTargetStrength)), 2);
            }
            Set<BlockPos> newHashSet = Sets.newHashSet();
            newHashSet.add(blockPos);
            for (Direction direction : Direction.values()) {
                newHashSet.add(blockPos.relative(direction));
            }
            Iterator<BlockPos> it = newHashSet.iterator();
            while (it.hasNext()) {
                level.updateNeighborsAt(it.next(), this);
            }
        }
    }

    private int calculateTargetStrength(Level level, BlockPos blockPos) {
        this.shouldSignal = false;
        int bestNeighborSignal = level.getBestNeighborSignal(blockPos);
        this.shouldSignal = true;
        int i = 0;
        if (bestNeighborSignal < 15) {
            Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
            while (it.hasNext()) {
                BlockPos relative = blockPos.relative(it.next());
                BlockState blockState = level.getBlockState(relative);
                i = Math.max(i, getWireSignal(blockState));
                BlockPos above = blockPos.above();
                if (blockState.isRedstoneConductor(level, relative) && !level.getBlockState(above).isRedstoneConductor(level, above)) {
                    i = Math.max(i, getWireSignal(level.getBlockState(relative.above())));
                } else if (!blockState.isRedstoneConductor(level, relative)) {
                    i = Math.max(i, getWireSignal(level.getBlockState(relative.below())));
                }
            }
        }
        return Math.max(bestNeighborSignal, i - 1);
    }

    private int getWireSignal(BlockState blockState) {
        if (blockState.is(this)) {
            return ((Integer) blockState.getValue(POWER)).intValue();
        }
        return 0;
    }

    private void checkCornerChangeAt(Level level, BlockPos blockPos) {
        if (!level.getBlockState(blockPos).is(this)) {
            return;
        }
        level.updateNeighborsAt(blockPos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock()) || level.isClientSide) {
            return;
        }
        updatePowerStrength(level, blockPos, blockState);
        Iterator<Direction> it = Direction.Plane.VERTICAL.iterator();
        while (it.hasNext()) {
            level.updateNeighborsAt(blockPos.relative(it.next()), this);
        }
        updateNeighborsOfNeighboringWires(level, blockPos);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
        if (level.isClientSide) {
            return;
        }
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
        updatePowerStrength(level, blockPos, blockState);
        updateNeighborsOfNeighboringWires(level, blockPos);
    }

    private void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos) {
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            checkCornerChangeAt(level, blockPos.relative(it.next()));
        }
        Iterator<Direction> it2 = Direction.Plane.HORIZONTAL.iterator();
        while (it2.hasNext()) {
            BlockPos relative = blockPos.relative(it2.next());
            if (level.getBlockState(relative).isRedstoneConductor(level, relative)) {
                checkCornerChangeAt(level, relative.above());
            } else {
                checkCornerChangeAt(level, relative.below());
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (level.isClientSide) {
            return;
        }
        if (blockState.canSurvive(level, blockPos)) {
            updatePowerStrength(level, blockPos, blockState);
        } else {
            dropResources(blockState, level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.shouldSignal) {
            return 0;
        }
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        int intValue;
        if (!this.shouldSignal || direction == Direction.DOWN || (intValue = ((Integer) blockState.getValue(POWER)).intValue()) == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide) getConnectionState(blockGetter, blockState, blockPos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return intValue;
        }
        return 0;
    }

    protected static boolean shouldConnectTo(BlockState blockState) {
        return shouldConnectTo(blockState, null);
    }

    protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
        if (blockState.is(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        if (!blockState.is(Blocks.REPEATER)) {
            return blockState.is(Blocks.OBSERVER) ? direction == blockState.getValue(ObserverBlock.FACING) : blockState.isSignalSource() && direction != null;
        }
        Direction direction2 = (Direction) blockState.getValue(RepeaterBlock.FACING);
        return direction2 == direction || direction2.getOpposite() == direction;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return this.shouldSignal;
    }

    public static int getColorForPower(int i) {
        Vector3f vector3f = COLORS[i];
        return Mth.color(vector3f.x(), vector3f.y(), vector3f.z());
    }

    private void spawnParticlesAlongLine(Level level, Random random, BlockPos blockPos, Vector3f vector3f, Direction direction, Direction direction2, float f, float f2) {
        float f3 = f2 - f;
        if (random.nextFloat() >= 0.2f * f3) {
            return;
        }
        float nextFloat = f + (f3 * random.nextFloat());
        level.addParticle(new DustParticleOptions(vector3f.x(), vector3f.y(), vector3f.z(), 1.0f), blockPos.getX() + 0.5d + (0.4375f * direction.getStepX()) + (nextFloat * direction2.getStepX()), blockPos.getY() + 0.5d + (0.4375f * direction.getStepY()) + (nextFloat * direction2.getStepY()), blockPos.getZ() + 0.5d + (0.4375f * direction.getStepZ()) + (nextFloat * direction2.getStepZ()), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        int intValue = ((Integer) blockState.getValue(POWER)).intValue();
        if (intValue == 0) {
            return;
        }
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            switch ((RedstoneSide) blockState.getValue(PROPERTY_BY_DIRECTION.get(next))) {
                case UP:
                    spawnParticlesAlongLine(level, random, blockPos, COLORS[intValue], next, Direction.UP, -0.5f, 0.5f);
                    break;
                case SIDE:
                    break;
                case NONE:
                default:
                    spawnParticlesAlongLine(level, random, blockPos, COLORS[intValue], Direction.DOWN, next, 0.0f, 0.3f);
                    continue;
            }
            spawnParticlesAlongLine(level, random, blockPos, COLORS[intValue], Direction.DOWN, next, 0.0f, 0.5f);
        }
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

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!player.abilities.mayBuild) {
            return InteractionResult.PASS;
        }
        if (isCross(blockState) || isDot(blockState)) {
            BlockState connectionState = getConnectionState(level, (BlockState) (isCross(blockState) ? defaultBlockState() : this.crossState).setValue(POWER, blockState.getValue(POWER)), blockPos);
            if (connectionState != blockState) {
                level.setBlock(blockPos, connectionState, 3);
                updatesOnShapeChange(level, blockPos, blockState, connectionState);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private void updatesOnShapeChange(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            BlockPos relative = blockPos.relative(next);
            if (((RedstoneSide) blockState.getValue(PROPERTY_BY_DIRECTION.get(next))).isConnected() != ((RedstoneSide) blockState2.getValue(PROPERTY_BY_DIRECTION.get(next))).isConnected() && level.getBlockState(relative).isRedstoneConductor(level, relative)) {
                level.updateNeighborsAtExceptFromFacing(relative, blockState2.getBlock(), next.getOpposite());
            }
        }
    }
}
