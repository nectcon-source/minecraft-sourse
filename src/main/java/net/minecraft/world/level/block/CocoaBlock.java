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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CocoaBlock.class */
public class CocoaBlock extends HorizontalDirectionalBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    protected static final VoxelShape[] EAST_AABB = {Block.box(11.0d, 7.0d, 6.0d, 15.0d, 12.0d, 10.0d), Block.box(9.0d, 5.0d, 5.0d, 15.0d, 12.0d, 11.0d), Block.box(7.0d, 3.0d, 4.0d, 15.0d, 12.0d, 12.0d)};
    protected static final VoxelShape[] WEST_AABB = {Block.box(1.0d, 7.0d, 6.0d, 5.0d, 12.0d, 10.0d), Block.box(1.0d, 5.0d, 5.0d, 7.0d, 12.0d, 11.0d), Block.box(1.0d, 3.0d, 4.0d, 9.0d, 12.0d, 12.0d)};
    protected static final VoxelShape[] NORTH_AABB = {Block.box(6.0d, 7.0d, 1.0d, 10.0d, 12.0d, 5.0d), Block.box(5.0d, 5.0d, 1.0d, 11.0d, 12.0d, 7.0d), Block.box(4.0d, 3.0d, 1.0d, 12.0d, 12.0d, 9.0d)};
    protected static final VoxelShape[] SOUTH_AABB = {Block.box(6.0d, 7.0d, 11.0d, 10.0d, 12.0d, 15.0d), Block.box(5.0d, 5.0d, 9.0d, 11.0d, 12.0d, 15.0d), Block.box(4.0d, 3.0d, 7.0d, 12.0d, 12.0d, 15.0d)};

    public CocoaBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 2;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int intValue;
        if (serverLevel.random.nextInt(5) == 0 && (intValue = ((Integer) blockState.getValue(AGE)).intValue()) < 2) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(intValue + 1)), 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.relative((Direction) blockState.getValue(FACING))).getBlock().is(BlockTags.JUNGLE_LOGS);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        int intValue = ((Integer) blockState.getValue(AGE)).intValue();
        switch ((Direction) blockState.getValue(FACING)) {
            case SOUTH:
                return SOUTH_AABB[intValue];
            case NORTH:
            default:
                return NORTH_AABB[intValue];
            case WEST:
                return WEST_AABB[intValue];
            case EAST:
                return EAST_AABB[intValue];
        }
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState defaultBlockState = defaultBlockState();
        LevelReader level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                defaultBlockState = (BlockState) defaultBlockState.setValue(FACING, direction);
                if (defaultBlockState.canSurvive(level, clickedPos)) {
                    return defaultBlockState;
                }
            }
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 2;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(((Integer) blockState.getValue(AGE)).intValue() + 1)), 2);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
