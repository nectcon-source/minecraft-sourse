package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ScaffoldingBlock.class */
public class ScaffoldingBlock extends Block implements SimpleWaterloggedBlock {
    private static final VoxelShape UNSTABLE_SHAPE;
    private static final VoxelShape UNSTABLE_SHAPE_BOTTOM = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d);
    private static final VoxelShape BELOW_BLOCK = Shapes.block().move(0.0d, -1.0d, 0.0d);
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    private static final VoxelShape STABLE_SHAPE = Shapes.or(Block.box(0.0d, 14.0d, 0.0d, 16.0d, 16.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 2.0d, 16.0d, 2.0d), Block.box(14.0d, 0.0d, 0.0d, 16.0d, 16.0d, 2.0d), Block.box(0.0d, 0.0d, 14.0d, 2.0d, 16.0d, 16.0d), Block.box(14.0d, 0.0d, 14.0d, 16.0d, 16.0d, 16.0d));

    static {
        VoxelShape box = Block.box(0.0d, 0.0d, 0.0d, 2.0d, 2.0d, 16.0d);
        UNSTABLE_SHAPE = Shapes.or(UNSTABLE_SHAPE_BOTTOM, STABLE_SHAPE, Block.box(14.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d), box, Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 2.0d), Block.box(0.0d, 0.0d, 14.0d, 16.0d, 2.0d, 16.0d));
    }

    protected ScaffoldingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(DISTANCE, 7)).setValue(WATERLOGGED, false)).setValue(BOTTOM, false));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, WATERLOGGED, BOTTOM);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (collisionContext.isHoldingItem(blockState.getBlock().asItem())) {
            return Shapes.block();
        }
        return ((Boolean) blockState.getValue(BOTTOM)).booleanValue() ? UNSTABLE_SHAPE : STABLE_SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.block();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockPlaceContext.getItemInHand().getItem() == asItem();
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        int distance = getDistance(level, clickedPos);
        return (BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(level.getFluidState(clickedPos).getType() == Fluids.WATER))).setValue(DISTANCE, Integer.valueOf(distance))).setValue(BOTTOM, Boolean.valueOf(isBottom(level, clickedPos, distance)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (!level.isClientSide) {
            level.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (!levelAccessor.isClientSide()) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        return blockState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int distance = getDistance(serverLevel, blockPos);
        BlockState blockState2 = (BlockState) ((BlockState) blockState.setValue(DISTANCE, Integer.valueOf(distance))).setValue(BOTTOM, Boolean.valueOf(isBottom(serverLevel, blockPos, distance)));
        if (((Integer) blockState2.getValue(DISTANCE)).intValue() == 7) {
            if (((Integer) blockState.getValue(DISTANCE)).intValue() == 7) {
                serverLevel.addFreshEntity(new FallingBlockEntity(serverLevel, blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d, (BlockState) blockState2.setValue(WATERLOGGED, false)));
                return;
            } else {
                serverLevel.destroyBlock(blockPos, true);
                return;
            }
        }
        if (blockState != blockState2) {
            serverLevel.setBlock(blockPos, blockState2, 3);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return getDistance(levelReader, blockPos) < 7;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!collisionContext.isAbove(Shapes.block(), blockPos, true) || collisionContext.isDescending()) {
            if (((Integer) blockState.getValue(DISTANCE)).intValue() != 0 && ((Boolean) blockState.getValue(BOTTOM)).booleanValue() && collisionContext.isAbove(BELOW_BLOCK, blockPos, true)) {
                return UNSTABLE_SHAPE_BOTTOM;
            }
            return Shapes.empty();
        }
        return STABLE_SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    private boolean isBottom(BlockGetter blockGetter, BlockPos blockPos, int i) {
        return i > 0 && !blockGetter.getBlockState(blockPos.below()).is(this);
    }

    public static int getDistance(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos.MutableBlockPos move = blockPos.mutable().move(Direction.DOWN);
        BlockState blockState = blockGetter.getBlockState(move);
        int i = 7;
        if (blockState.is(Blocks.SCAFFOLDING)) {
            i = ((Integer) blockState.getValue(DISTANCE)).intValue();
        } else if (blockState.isFaceSturdy(blockGetter, move, Direction.UP)) {
            return 0;
        }
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            BlockState blockState2 = blockGetter.getBlockState(move.setWithOffset(blockPos, it.next()));
            if (blockState2.is(Blocks.SCAFFOLDING)) {
                i = Math.min(i, ((Integer) blockState2.getValue(DISTANCE)).intValue() + 1);
                if (i == 1) {
                    break;
                }
            }
        }
        return i;
    }
}
