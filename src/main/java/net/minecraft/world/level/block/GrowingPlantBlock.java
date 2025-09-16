package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/GrowingPlantBlock.class */
public abstract class GrowingPlantBlock extends Block {
    protected final Direction growthDirection;
    protected final boolean scheduleFluidTicks;
    protected final VoxelShape shape;

    protected abstract GrowingPlantHeadBlock getHeadBlock();

    protected abstract Block getBodyBlock();

    protected GrowingPlantBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean z) {
        super(properties);
        this.growthDirection = direction;
        this.shape = voxelShape;
        this.scheduleFluidTicks = z;
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(this.growthDirection));
        if (blockState.is(getHeadBlock()) || blockState.is(getBodyBlock())) {
            return getBodyBlock().defaultBlockState();
        }
        return getStateForPlacement(blockPlaceContext.getLevel());
    }

    public BlockState getStateForPlacement(LevelAccessor levelAccessor) {
        return defaultBlockState();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos relative = blockPos.relative(this.growthDirection.getOpposite());
        BlockState blockState2 = levelReader.getBlockState(relative);
        Block block = blockState2.getBlock();
        if (canAttachToBlock(block)) {
            return block == getHeadBlock() || block == getBodyBlock() || blockState2.isFaceSturdy(levelReader, relative, this.growthDirection);
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    protected boolean canAttachToBlock(Block block) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shape;
    }
}
