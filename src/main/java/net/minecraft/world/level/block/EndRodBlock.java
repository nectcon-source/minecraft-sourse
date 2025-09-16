package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/EndRodBlock.class */
public class EndRodBlock extends DirectionalBlock {
    protected static final VoxelShape Y_AXIS_AABB = Block.box(6.0d, 0.0d, 6.0d, 10.0d, 16.0d, 10.0d);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0d, 6.0d, 0.0d, 10.0d, 10.0d, 16.0d);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0d, 6.0d, 6.0d, 16.0d, 10.0d, 10.0d);

    protected EndRodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState) blockState.setValue(FACING, mirror.mirror((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (((Direction) blockState.getValue(FACING)).getAxis()) {
            case X:
            default:
                return X_AXIS_AABB;
            case Z:
                return Z_AXIS_AABB;
            case Y:
                return Y_AXIS_AABB;
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction clickedFace = blockPlaceContext.getClickedFace();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(clickedFace.getOpposite()));
        if (blockState.is(this) && blockState.getValue(FACING) == clickedFace) {
            return (BlockState) defaultBlockState().setValue(FACING, clickedFace.getOpposite());
        }
        return (BlockState) defaultBlockState().setValue(FACING, clickedFace);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        Direction direction = (Direction) blockState.getValue(FACING);
        double x = (blockPos.getX() + 0.55d) - (random.nextFloat() * 0.1f);
        double y = (blockPos.getY() + 0.55d) - (random.nextFloat() * 0.1f);
        double z = (blockPos.getZ() + 0.55d) - (random.nextFloat() * 0.1f);
        double nextFloat = 0.4f - ((random.nextFloat() + random.nextFloat()) * 0.4f);
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD, x + (direction.getStepX() * nextFloat), y + (direction.getStepY() * nextFloat), z + (direction.getStepZ() * nextFloat), random.nextGaussian() * 0.005d, random.nextGaussian() * 0.005d, random.nextGaussian() * 0.005d);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.NORMAL;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
