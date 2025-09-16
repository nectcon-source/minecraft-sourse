package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/piston/PistonBaseBlock.class */
public class PistonBaseBlock extends DirectionalBlock {
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    protected static final VoxelShape EAST_AABB = Block.box(0.0d, 0.0d, 0.0d, 12.0d, 16.0d, 16.0d);
    protected static final VoxelShape WEST_AABB = Block.box(4.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 12.0d);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0d, 0.0d, 4.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape UP_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 12.0d, 16.0d);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0d, 4.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private final boolean isSticky;

    public PistonBaseBlock(boolean z, BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(EXTENDED, false));
        this.isSticky = z;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (((Boolean) blockState.getValue(EXTENDED)).booleanValue()) {
            switch ((Direction) blockState.getValue(FACING)) {
                case DOWN:
                    return DOWN_AABB;
                case UP:
                default:
                    return UP_AABB;
                case NORTH:
                    return NORTH_AABB;
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case EAST:
                    return EAST_AABB;
            }
        }
        return Shapes.block();
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (!level.isClientSide) {
            checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (!level.isClientSide) {
            checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (!blockState2.is(blockState.getBlock()) && !level.isClientSide && level.getBlockEntity(blockPos) == null) {
            checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) ((BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite())).setValue(EXTENDED, false);
    }

    private void checkIfExtend(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        boolean neighborSignal = getNeighborSignal(level, blockPos, direction);
        if (neighborSignal && !((Boolean) blockState.getValue(EXTENDED)).booleanValue()) {
            if (new PistonStructureResolver(level, blockPos, direction, true).resolve()) {
                level.blockEvent(blockPos, this, 0, direction.get3DDataValue());
                return;
            }
            return;
        }
        if (!neighborSignal && ((Boolean) blockState.getValue(EXTENDED)).booleanValue()) {
            BlockPos relative = blockPos.relative(direction, 2);
            BlockState blockState2 = level.getBlockState(relative);
            int i = 1;
            if (blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == direction) {
                BlockEntity blockEntity = level.getBlockEntity(relative);
                if (blockEntity instanceof PistonMovingBlockEntity) {
                    PistonMovingBlockEntity pistonMovingBlockEntity = (PistonMovingBlockEntity) blockEntity;
                    if (pistonMovingBlockEntity.isExtending() && (pistonMovingBlockEntity.getProgress(0.0f) < 0.5f || level.getGameTime() == pistonMovingBlockEntity.getLastTicked() || ((ServerLevel) level).isHandlingTick())) {
                        i = 2;
                    }
                }
            }
            level.blockEvent(blockPos, this, i, direction.get3DDataValue());
        }
    }

    private boolean getNeighborSignal(Level level, BlockPos blockPos, Direction direction) {
        for (Direction direction2 : Direction.values()) {
            if (direction2 != direction && level.hasSignal(blockPos.relative(direction2), direction2)) {
                return true;
            }
        }
        if (level.hasSignal(blockPos, Direction.DOWN)) {
            return true;
        }
        BlockPos above = blockPos.above();
        for (Direction direction3 : Direction.values()) {
            if (direction3 != Direction.DOWN && level.hasSignal(above.relative(direction3), direction3)) {
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int i2) {
        Direction direction = (Direction) blockState.getValue(FACING);
        if (!level.isClientSide) {
            boolean neighborSignal = getNeighborSignal(level, blockPos, direction);
            if (neighborSignal && (i == 1 || i == 2)) {
                level.setBlock(blockPos, (BlockState) blockState.setValue(EXTENDED, true), 2);
                return false;
            }
            if (!neighborSignal && i == 0) {
                return false;
            }
        }
        if (i == 0) {
            if (moveBlocks(level, blockPos, direction, true)) {
                level.setBlock(blockPos, (BlockState) blockState.setValue(EXTENDED, true), 67);
                level.playSound((Player) null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, (level.random.nextFloat() * 0.25f) + 0.6f);
                return true;
            }
            return false;
        }
        if (i == 1 || i == 2) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos.relative(direction));
            if (blockEntity instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity) blockEntity).finalTick();
            }
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            level.setBlock(blockPos, blockState2, 20);
            level.setBlockEntity(blockPos, MovingPistonBlock.newMovingBlockEntity((BlockState) defaultBlockState().setValue(FACING, Direction.from3DDataValue(i2 & 7)), direction, false, true));
            level.blockUpdated(blockPos, blockState2.getBlock());
            blockState2.updateNeighbourShapes(level, blockPos, 2);
            if (this.isSticky) {
                BlockPos offset = blockPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
                BlockState blockState3 = level.getBlockState(offset);
                boolean z = false;
                if (blockState3.is(Blocks.MOVING_PISTON)) {
                    BlockEntity blockEntity2 = level.getBlockEntity(offset);
                    if (blockEntity2 instanceof PistonMovingBlockEntity) {
                        PistonMovingBlockEntity pistonMovingBlockEntity = (PistonMovingBlockEntity) blockEntity2;
                        if (pistonMovingBlockEntity.getDirection() == direction && pistonMovingBlockEntity.isExtending()) {
                            pistonMovingBlockEntity.finalTick();
                            z = true;
                        }
                    }
                }
                if (!z) {
                    if (i == 1 && !blockState3.isAir() && isPushable(blockState3, level, offset, direction.getOpposite(), false, direction) && (blockState3.getPistonPushReaction() == PushReaction.NORMAL || blockState3.is(Blocks.PISTON) || blockState3.is(Blocks.STICKY_PISTON))) {
                        moveBlocks(level, blockPos, direction, false);
                    } else {
                        level.removeBlock(blockPos.relative(direction), false);
                    }
                }
            } else {
                level.removeBlock(blockPos.relative(direction), false);
            }
            level.playSound((Player) null, blockPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5f, (level.random.nextFloat() * 0.15f) + 0.6f);
            return true;
        }
        return true;
    }

    public static boolean isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean z, Direction direction2) {
        if (blockPos.getY() < 0 || blockPos.getY() > level.getMaxBuildHeight() - 1 || !level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        if (blockState.isAir()) {
            return true;
        }
        if (blockState.is(Blocks.OBSIDIAN) || blockState.is(Blocks.CRYING_OBSIDIAN) || blockState.is(Blocks.RESPAWN_ANCHOR)) {
            return false;
        }
        if (direction == Direction.DOWN && blockPos.getY() == 0) {
            return false;
        }
        if (direction == Direction.UP && blockPos.getY() == level.getMaxBuildHeight() - 1) {
            return false;
        }
        if (!blockState.is(Blocks.PISTON) && !blockState.is(Blocks.STICKY_PISTON)) {
            if (blockState.getDestroySpeed(level, blockPos) == -1.0f) {
                return false;
            }
            switch (blockState.getPistonPushReaction()) {
                case BLOCK:
                    return false;
                case DESTROY:
                    return z;
                case PUSH_ONLY:
                    return direction == direction2;
            }
        }
        if (((Boolean) blockState.getValue(EXTENDED)).booleanValue()) {
            return false;
        }
        return !blockState.getBlock().isEntityBlock();
    }

    private boolean moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean z) {
        BlockPos relative = blockPos.relative(direction);
        if (!z && level.getBlockState(relative).is(Blocks.PISTON_HEAD)) {
            level.setBlock(relative, Blocks.AIR.defaultBlockState(), 20);
        }
        PistonStructureResolver pistonStructureResolver = new PistonStructureResolver(level, blockPos, direction, z);
        if (!pistonStructureResolver.resolve()) {
            return false;
        }
        Map<BlockPos, BlockState> newHashMap = Maps.newHashMap();
        List<BlockPos> toPush = pistonStructureResolver.getToPush();
        List<BlockState> newArrayList = Lists.newArrayList();
        for (int i = 0; i < toPush.size(); i++) {
            BlockPos blockPos2 = toPush.get(i);
            BlockState blockState = level.getBlockState(blockPos2);
            newArrayList.add(blockState);
            newHashMap.put(blockPos2, blockState);
        }
        List<BlockPos> toDestroy = pistonStructureResolver.getToDestroy();
        BlockState[] blockStateArr = new BlockState[toPush.size() + toDestroy.size()];
        Direction opposite = z ? direction : direction.getOpposite();
        int i2 = 0;
        for (int size = toDestroy.size() - 1; size >= 0; size--) {
            BlockPos blockPos3 = toDestroy.get(size);
            BlockState blockState2 = level.getBlockState(blockPos3);
            dropResources(blockState2, level, blockPos3, blockState2.getBlock().isEntityBlock() ? level.getBlockEntity(blockPos3) : null);
            level.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 18);
            int i3 = i2;
            i2++;
            blockStateArr[i3] = blockState2;
        }
        for (int size2 = toPush.size() - 1; size2 >= 0; size2--) {
            BlockPos blockPos4 = toPush.get(size2);
            BlockState blockState3 = level.getBlockState(blockPos4);
            BlockPos relative2 = blockPos4.relative(opposite);
            newHashMap.remove(relative2);
            level.setBlock(relative2, (BlockState) Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction), 68);
            level.setBlockEntity(relative2, MovingPistonBlock.newMovingBlockEntity(newArrayList.get(size2), direction, z, false));
            int i4 = i2;
            i2++;
            blockStateArr[i4] = blockState3;
        }
        if (z) {
            BlockState blockState4 = (BlockState) ((BlockState) Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction)).setValue(PistonHeadBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            BlockState blockState5 = (BlockState) ((BlockState) Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            newHashMap.remove(relative);
            level.setBlock(relative, blockState5, 68);
            level.setBlockEntity(relative, MovingPistonBlock.newMovingBlockEntity(blockState4, direction, true, true));
        }
        BlockState defaultBlockState = Blocks.AIR.defaultBlockState();
        Iterator<BlockPos> it = newHashMap.keySet().iterator();
        while (it.hasNext()) {
            level.setBlock(it.next(), defaultBlockState, 82);
        }
        for (Map.Entry<BlockPos, BlockState> entry : newHashMap.entrySet()) {
            BlockPos key = entry.getKey();
            entry.getValue().updateIndirectNeighbourShapes(level, key, 2);
            defaultBlockState.updateNeighbourShapes(level, key, 2);
            defaultBlockState.updateIndirectNeighbourShapes(level, key, 2);
        }
        int i5 = 0;
        for (int size3 = toDestroy.size() - 1; size3 >= 0; size3--) {
            int i6 = i5;
            i5++;
            BlockState blockState6 = blockStateArr[i6];
            BlockPos blockPos5 = toDestroy.get(size3);
            blockState6.updateIndirectNeighbourShapes(level, blockPos5, 2);
            level.updateNeighborsAt(blockPos5, blockState6.getBlock());
        }
        for (int size4 = toPush.size() - 1; size4 >= 0; size4--) {
            int i7 = i5;
            i5++;
            level.updateNeighborsAt(toPush.get(size4), blockStateArr[i7].getBlock());
        }
        if (z) {
            level.updateNeighborsAt(relative, Blocks.PISTON_HEAD);
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return ((Boolean) blockState.getValue(EXTENDED)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
