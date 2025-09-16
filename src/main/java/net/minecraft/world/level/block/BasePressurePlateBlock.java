package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BasePressurePlateBlock.class */
public abstract class BasePressurePlateBlock extends Block {
    protected static final VoxelShape PRESSED_AABB = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 0.5d, 15.0d);
    protected static final VoxelShape AABB = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 1.0d, 15.0d);
    protected static final AABB TOUCH_AABB = new AABB(0.125d, 0.0d, 0.125d, 0.875d, 0.25d, 0.875d);

    protected abstract void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos);

    protected abstract void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos);

    protected abstract int getSignalStrength(Level level, BlockPos blockPos);

    protected abstract int getSignalForState(BlockState blockState);

    protected abstract BlockState setSignalForState(BlockState blockState, int i);

    protected BasePressurePlateBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getSignalForState(blockState) > 0 ? PRESSED_AABB : AABB;
    }

    protected int getPressedTime() {
        return 20;
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        return canSupportRigidBlock(levelReader, below) || canSupportCenter(levelReader, below, Direction.UP);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int signalForState = getSignalForState(blockState);
        if (signalForState > 0) {
            checkPressed(serverLevel, blockPos, blockState, signalForState);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        int signalForState;
        if (!level.isClientSide && (signalForState = getSignalForState(blockState)) == 0) {
            checkPressed(level, blockPos, blockState, signalForState);
        }
    }

    protected void checkPressed(Level level, BlockPos blockPos, BlockState blockState, int i) {
        int signalStrength = getSignalStrength(level, blockPos);
        boolean z = i > 0;
        boolean z2 = signalStrength > 0;
        if (i != signalStrength) {
            BlockState signalForState = setSignalForState(blockState, signalStrength);
            level.setBlock(blockPos, signalForState, 2);
            updateNeighbours(level, blockPos);
            level.setBlocksDirty(blockPos, blockState, signalForState);
        }
        if (!z2 && z) {
            playOffSound(level, blockPos);
        } else if (z2 && !z) {
            playOnSound(level, blockPos);
        }
        if (z2) {
            level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, getPressedTime());
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        if (getSignalForState(blockState) > 0) {
            updateNeighbours(level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    protected void updateNeighbours(Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, this);
        level.updateNeighborsAt(blockPos.below(), this);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return getSignalForState(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.UP) {
            return getSignalForState(blockState);
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }
}
