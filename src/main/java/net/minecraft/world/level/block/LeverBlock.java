package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/LeverBlock.class */
public class LeverBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0d, 4.0d, 10.0d, 11.0d, 12.0d, 16.0d);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0d, 4.0d, 0.0d, 11.0d, 12.0d, 6.0d);
    protected static final VoxelShape WEST_AABB = Block.box(10.0d, 4.0d, 5.0d, 16.0d, 12.0d, 11.0d);
    protected static final VoxelShape EAST_AABB = Block.box(0.0d, 4.0d, 5.0d, 6.0d, 12.0d, 11.0d);
    protected static final VoxelShape UP_AABB_Z = Block.box(5.0d, 0.0d, 4.0d, 11.0d, 6.0d, 12.0d);
    protected static final VoxelShape UP_AABB_X = Block.box(4.0d, 0.0d, 5.0d, 12.0d, 6.0d, 11.0d);
    protected static final VoxelShape DOWN_AABB_Z = Block.box(5.0d, 10.0d, 4.0d, 11.0d, 16.0d, 12.0d);
    protected static final VoxelShape DOWN_AABB_X = Block.box(4.0d, 10.0d, 5.0d, 12.0d, 16.0d, 11.0d);

    protected LeverBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(FACE, AttachFace.WALL));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch ((AttachFace) blockState.getValue(FACE)) {
            case FLOOR:
                switch (((Direction) blockState.getValue(FACING)).getAxis()) {
                    case X:
                        return UP_AABB_X;
                    case Z:
                    default:
                        return UP_AABB_Z;
                }
            case WALL:
                switch ((Direction) blockState.getValue(FACING)) {
                    case EAST:
                        return EAST_AABB;
                    case WEST:
                        return WEST_AABB;
                    case SOUTH:
                        return SOUTH_AABB;
                    case NORTH:
                    default:
                        return NORTH_AABB;
                }
            case CEILING:
            default:
                switch (((Direction) blockState.getValue(FACING)).getAxis()) {
                    case X:
                        return DOWN_AABB_X;
                    case Z:
                    default:
                        return DOWN_AABB_Z;
                }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            BlockState cycle = blockState.cycle(POWERED);
            if (((Boolean) cycle.getValue(POWERED)).booleanValue()) {
                makeParticle(cycle, level, blockPos, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        level.playSound((Player) null, blockPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, ((Boolean) pull(blockState, level, blockPos).getValue(POWERED)).booleanValue() ? 0.6f : 0.5f);
        return InteractionResult.CONSUME;
    }

    public BlockState pull(BlockState blockState, Level level, BlockPos blockPos) {
        BlockState cycle = blockState.cycle(POWERED);
        level.setBlock(blockPos, cycle, 3);
        updateNeighbours(cycle, level, blockPos);
        return cycle;
    }

    private static void makeParticle(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, float f) {
        Direction opposite = ((Direction) blockState.getValue(FACING)).getOpposite();
        Direction opposite2 = getConnectedDirection(blockState).getOpposite();
        levelAccessor.addParticle(new DustParticleOptions(1.0f, 0.0f, 0.0f, f), blockPos.getX() + 0.5d + (0.1d * opposite.getStepX()) + (0.2d * opposite2.getStepX()), blockPos.getY() + 0.5d + (0.1d * opposite.getStepY()) + (0.2d * opposite2.getStepY()), blockPos.getZ() + 0.5d + (0.1d * opposite.getStepZ()) + (0.2d * opposite2.getStepZ()), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue() && random.nextFloat() < 0.25f) {
            makeParticle(blockState, level, blockPos, 0.5f);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        if (((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            updateNeighbours(blockState, level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue() && getConnectedDirection(blockState) == direction) {
            return 15;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, this);
        level.updateNeighborsAt(blockPos.relative(getConnectedDirection(blockState).getOpposite()), this);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, POWERED);
    }
}
