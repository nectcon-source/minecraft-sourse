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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SnowLayerBlock.class */
public class SnowLayerBlock extends Block {
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = {Shapes.empty(), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 4.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 6.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 10.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 12.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 14.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d)};

    protected SnowLayerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND:
                if (((Integer) blockState.getValue(LAYERS)).intValue() < 5) {
                }
                break;
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_LAYER[((Integer) blockState.getValue(LAYERS)).intValue()];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_LAYER[((Integer) blockState.getValue(LAYERS)).intValue() - 1];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPE_BY_LAYER[((Integer) blockState.getValue(LAYERS)).intValue()];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_LAYER[((Integer) blockState.getValue(LAYERS)).intValue()];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        if (blockState2.is(Blocks.ICE) || blockState2.is(Blocks.PACKED_ICE) || blockState2.is(Blocks.BARRIER)) {
            return false;
        }
        return blockState2.is(Blocks.HONEY_BLOCK) || blockState2.is(Blocks.SOUL_SAND) || Block.isFaceFull(blockState2.getCollisionShape(levelReader, blockPos.below()), Direction.UP) || (blockState2.getBlock() == this && ((Integer) blockState2.getValue(LAYERS)).intValue() == 8);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (serverLevel.getBrightness(LightLayer.BLOCK, blockPos) > 11) {
            dropResources(blockState, serverLevel, blockPos);
            serverLevel.removeBlock(blockPos, false);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        int intValue = ((Integer) blockState.getValue(LAYERS)).intValue();
        return (blockPlaceContext.getItemInHand().getItem() != asItem() || intValue >= 8) ? intValue == 1 : !blockPlaceContext.replacingClickedOnBlock() || blockPlaceContext.getClickedFace() == Direction.UP;
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return (BlockState) blockState.setValue(LAYERS, Integer.valueOf(Math.min(8, ((Integer) blockState.getValue(LAYERS)).intValue() + 1)));
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }
}
