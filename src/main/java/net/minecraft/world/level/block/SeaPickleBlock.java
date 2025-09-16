package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SeaPickleBlock.class */
public class SeaPickleBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape ONE_AABB = Block.box(6.0d, 0.0d, 6.0d, 10.0d, 6.0d, 10.0d);
    protected static final VoxelShape TWO_AABB = Block.box(3.0d, 0.0d, 3.0d, 13.0d, 6.0d, 13.0d);
    protected static final VoxelShape THREE_AABB = Block.box(2.0d, 0.0d, 2.0d, 14.0d, 6.0d, 14.0d);
    protected static final VoxelShape FOUR_AABB = Block.box(2.0d, 0.0d, 2.0d, 14.0d, 7.0d, 14.0d);

    protected SeaPickleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(PICKLES, 1)).setValue(WATERLOGGED, true));
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return (BlockState) blockState.setValue(PICKLES, Integer.valueOf(Math.min(4, ((Integer) blockState.getValue(PICKLES)).intValue() + 1)));
        }
        return (BlockState) super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, Boolean.valueOf(blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos()).getType() == Fluids.WATER));
    }

    public static boolean isDead(BlockState blockState) {
        return !((Boolean) blockState.getValue(WATERLOGGED)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.BushBlock
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !blockState.getCollisionShape(blockGetter, blockPos).getFaceShape(Direction.UP).isEmpty() || blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP);
    }

    @Override // net.minecraft.world.level.block.BushBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        return mayPlaceOn(levelReader.getBlockState(below), levelReader, below);
    }

    @Override // net.minecraft.world.level.block.BushBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        if (blockPlaceContext.getItemInHand().getItem() == asItem() && ((Integer) blockState.getValue(PICKLES)).intValue() < 4) {
            return true;
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (((Integer) blockState.getValue(PICKLES)).intValue()) {
            case 1:
            default:
                return ONE_AABB;
            case 2:
                return TWO_AABB;
            case 3:
                return THREE_AABB;
            case 4:
                return FOUR_AABB;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PICKLES, WATERLOGGED);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        if (!isDead(blockState) && serverLevel.getBlockState(blockPos.below()).is(BlockTags.CORAL_BLOCKS)) {
            int i = 1;
            int i2 = 0;
            int x = blockPos.getX() - 2;
            int i3 = 0;
            for (int i4 = 0; i4 < 5; i4++) {
                for (int i5 = 0; i5 < i; i5++) {
                    int y = (2 + blockPos.getY()) - 1;
                    for (int i6 = y - 2; i6 < y; i6++) {
                        BlockPos blockPos2 = new BlockPos(x + i4, i6, (blockPos.getZ() - i3) + i5);
                        if (blockPos2 != blockPos && random.nextInt(6) == 0 && serverLevel.getBlockState(blockPos2).is(Blocks.WATER) && serverLevel.getBlockState(blockPos2.below()).is(BlockTags.CORAL_BLOCKS)) {
                            serverLevel.setBlock(blockPos2, (BlockState) Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, Integer.valueOf(random.nextInt(4) + 1)), 3);
                        }
                    }
                }
                if (i2 < 2) {
                    i += 2;
                    i3++;
                } else {
                    i -= 2;
                    i3--;
                }
                i2++;
            }
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(PICKLES, 4), 2);
        }
    }

    @Override // net.minecraft.world.level.block.BushBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
