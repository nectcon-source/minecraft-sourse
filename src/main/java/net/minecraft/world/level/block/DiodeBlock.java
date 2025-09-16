package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DiodeBlock.class */
public abstract class DiodeBlock extends HorizontalDirectionalBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected abstract int getDelay(BlockState blockState);

    protected DiodeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return canSupportRigidBlock(levelReader, blockPos.below());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (isLocked(serverLevel, blockPos, blockState)) {
            return;
        }
        boolean booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        boolean shouldTurnOn = shouldTurnOn(serverLevel, blockPos, blockState);
        if (booleanValue && !shouldTurnOn) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, false), 2);
        } else if (!booleanValue) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, true), 2);
            if (!shouldTurnOn) {
                serverLevel.getBlockTicks().scheduleTick(blockPos, this, getDelay(blockState), TickPriority.VERY_HIGH);
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue() && blockState.getValue(FACING) == direction) {
            return getOutputSignal(blockGetter, blockPos, blockState);
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (blockState.canSurvive(level, blockPos)) {
            checkTickOnNeighbor(level, blockPos, blockState);
            return;
        }
        dropResources(blockState, level, blockPos, isEntityBlock() ? level.getBlockEntity(blockPos) : null);
        level.removeBlock(blockPos, false);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        boolean booleanValue;
        if (!isLocked(level, blockPos, blockState) && (booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue()) != shouldTurnOn(level, blockPos, blockState) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (shouldPrioritize(level, blockPos, blockState)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (booleanValue) {
                tickPriority = TickPriority.VERY_HIGH;
            }
            level.getBlockTicks().scheduleTick(blockPos, this, getDelay(blockState), tickPriority);
        }
    }

    public boolean isLocked(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return false;
    }

    protected boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState) {
        return getInputSignal(level, blockPos, blockState) > 0;
    }

    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockPos relative = blockPos.relative(direction);
        int signal = level.getSignal(relative, direction);
        if (signal >= 15) {
            return signal;
        }
        BlockState blockState2 = level.getBlockState(relative);
        return Math.max(signal, blockState2.is(Blocks.REDSTONE_WIRE) ? ((Integer) blockState2.getValue(RedStoneWireBlock.POWER)).intValue() : 0);
    }

    protected int getAlternateSignal(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        Direction clockWise = direction.getClockWise();
        Direction counterClockWise = direction.getCounterClockWise();
        return Math.max(getAlternateSignalAt(levelReader, blockPos.relative(clockWise), clockWise), getAlternateSignalAt(levelReader, blockPos.relative(counterClockWise), counterClockWise));
    }

    protected int getAlternateSignalAt(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockState blockState = levelReader.getBlockState(blockPos);
        if (isAlternateInput(blockState)) {
            if (blockState.is(Blocks.REDSTONE_BLOCK)) {
                return 15;
            }
            if (blockState.is(Blocks.REDSTONE_WIRE)) {
                return ((Integer) blockState.getValue(RedStoneWireBlock.POWER)).intValue();
            }
            return levelReader.getDirectSignal(blockPos, direction);
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (shouldTurnOn(level, blockPos, blockState)) {
            level.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        updateNeighborsInFront(level, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
        updateNeighborsInFront(level, blockPos, blockState);
    }

    protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockPos relative = blockPos.relative(direction.getOpposite());
        level.neighborChanged(relative, this, blockPos);
        level.updateNeighborsAtExceptFromFacing(relative, this, direction);
    }

    protected boolean isAlternateInput(BlockState blockState) {
        return blockState.isSignalSource();
    }

    protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return 15;
    }

    public static boolean isDiode(BlockState blockState) {
        return blockState.getBlock() instanceof DiodeBlock;
    }

    public boolean shouldPrioritize(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Direction opposite = ((Direction) blockState.getValue(FACING)).getOpposite();
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(opposite));
        return isDiode(blockState2) && blockState2.getValue(FACING) != opposite;
    }
}
