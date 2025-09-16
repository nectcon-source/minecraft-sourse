package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ObserverBlock.class */
public class ObserverBlock extends DirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ObserverBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.SOUTH)).setValue(POWERED, false));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, false), 2);
        } else {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, true), 2);
            serverLevel.getBlockTicks().scheduleTick(blockPos, this, 2);
        }
        updateNeighborsInFront(serverLevel, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(FACING) == direction && !((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            startSignal(levelAccessor, blockPos);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    private void startSignal(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!levelAccessor.isClientSide() && !levelAccessor.getBlockTicks().hasScheduledTick(blockPos, this)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 2);
        }
    }

    protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockPos relative = blockPos.relative(direction.getOpposite());
        level.neighborChanged(relative, this, blockPos);
        level.updateNeighborsAtExceptFromFacing(relative, this, direction);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue() && blockState.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (!blockState.is(blockState2.getBlock()) && !level.isClientSide() && ((Boolean) blockState.getValue(POWERED)).booleanValue() && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            BlockState blockState3 = (BlockState) blockState.setValue(POWERED, false);
            level.setBlock(blockPos, blockState3, 18);
            updateNeighborsInFront(level, blockPos, blockState3);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (!blockState.is(blockState2.getBlock()) && !level.isClientSide && ((Boolean) blockState.getValue(POWERED)).booleanValue() && level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            updateNeighborsInFront(level, blockPos, (BlockState) blockState.setValue(POWERED, false));
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite().getOpposite());
    }
}
