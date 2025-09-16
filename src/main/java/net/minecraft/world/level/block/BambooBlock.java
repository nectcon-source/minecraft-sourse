package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BambooBlock.class */
public class BambooBlock extends Block implements BonemealableBlock {
    protected static final VoxelShape SMALL_SHAPE = Block.box(5.0d, 0.0d, 5.0d, 11.0d, 16.0d, 11.0d);
    protected static final VoxelShape LARGE_SHAPE = Block.box(3.0d, 0.0d, 3.0d, 13.0d, 16.0d, 13.0d);
    protected static final VoxelShape COLLISION_SHAPE = Block.box(6.5d, 0.0d, 6.5d, 9.5d, 16.0d, 9.5d);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;

    public BambooBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(LEAVES, BambooLeaves.NONE).setValue(STAGE, 0));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, LEAVES, STAGE);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
        Vec3 offset = blockState.getOffset(blockGetter, blockPos);
        return voxelShape.move(offset.x, offset.y, offset.z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Vec3 offset = blockState.getOffset(blockGetter, blockPos);
        return COLLISION_SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos()).isEmpty()) {
            return null;
        }
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().below());
        if (blockState.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockState.is(Blocks.BAMBOO_SAPLING)) {
                return  defaultBlockState().setValue(AGE, 0);
            }
            if (blockState.is(Blocks.BAMBOO)) {
                return  defaultBlockState().setValue(AGE, Integer.valueOf(((Integer) blockState.getValue(AGE)).intValue() > 0 ? 1 : 0));
            }
            BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
            if (blockState2.is(Blocks.BAMBOO) || blockState2.is(Blocks.BAMBOO_SAPLING)) {
                return (BlockState) defaultBlockState().setValue(AGE, blockState2.getValue(AGE));
            }
            return Blocks.BAMBOO_SAPLING.defaultBlockState();
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ( blockState.getValue(STAGE)).intValue() == 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int heightBelowUpToMax;
        if (( blockState.getValue(STAGE)).intValue() == 0 && random.nextInt(3) == 0 && serverLevel.isEmptyBlock(blockPos.above()) && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9 && (heightBelowUpToMax = getHeightBelowUpToMax(serverLevel, blockPos) + 1) < 16) {
            growBamboo(blockState, serverLevel, blockPos, random, heightBelowUpToMax);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        if (direction == Direction.UP && blockState2.is(Blocks.BAMBOO) && ((Integer) blockState2.getValue(AGE)).intValue() > ((Integer) blockState.getValue(AGE)).intValue()) {
            levelAccessor.setBlock(blockPos, blockState.cycle(AGE), 2);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        int heightAboveUpToMax = getHeightAboveUpToMax(blockGetter, blockPos);
        return (heightAboveUpToMax + getHeightBelowUpToMax(blockGetter, blockPos)) + 1 < 16 && ((Integer) blockGetter.getBlockState(blockPos.above(heightAboveUpToMax)).getValue(STAGE)).intValue() != 1;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        int heightAboveUpToMax = getHeightAboveUpToMax(serverLevel, blockPos);
        int heightBelowUpToMax = heightAboveUpToMax + getHeightBelowUpToMax(serverLevel, blockPos) + 1;
        int nextInt = 1 + random.nextInt(2);
        for (int i = 0; i < nextInt; i++) {
            BlockPos above = blockPos.above(heightAboveUpToMax);
            BlockState blockState2 = serverLevel.getBlockState(above);
            if (heightBelowUpToMax >= 16 || ((Integer) blockState2.getValue(STAGE)).intValue() == 1 || !serverLevel.isEmptyBlock(above.above())) {
                return;
            }
            growBamboo(blockState2, serverLevel, above, random, heightBelowUpToMax);
            heightAboveUpToMax++;
            heightBelowUpToMax++;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        if (player.getMainHandItem().getItem() instanceof SwordItem) {
            return 1.0f;
        }
        return super.getDestroyProgress(blockState, player, blockGetter, blockPos);
    }

    protected void growBamboo(BlockState blockState, Level level, BlockPos blockPos, Random random, int i) {
        BlockState blockState2 = level.getBlockState(blockPos.below());
        BlockPos below = blockPos.below(2);
        BlockState blockState3 = level.getBlockState(below);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        if (i >= 1) {
            if (!blockState2.is(Blocks.BAMBOO) || blockState2.getValue(LEAVES) == BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.SMALL;
            } else if (blockState2.is(Blocks.BAMBOO) && blockState2.getValue(LEAVES) != BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.LARGE;
                if (blockState3.is(Blocks.BAMBOO)) {
                    level.setBlock(blockPos.below(), (BlockState) blockState2.setValue(LEAVES, BambooLeaves.SMALL), 3);
                    level.setBlock(below, (BlockState) blockState3.setValue(LEAVES, BambooLeaves.NONE), 3);
                }
            }
        }
        level.setBlock(blockPos.above(), (BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(AGE, Integer.valueOf((((Integer) blockState.getValue(AGE)).intValue() == 1 || blockState3.is(Blocks.BAMBOO)) ? 1 : 0))).setValue(LEAVES, bambooLeaves)).setValue(STAGE, Integer.valueOf(((i < 11 || random.nextFloat() >= 0.25f) && i != 15) ? 0 : 1)), 3);
    }

    protected int getHeightAboveUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
        int i = 0;
        while (i < 16 && blockGetter.getBlockState(blockPos.above(i + 1)).is(Blocks.BAMBOO)) {
            i++;
        }
        return i;
    }

    protected int getHeightBelowUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
        int i = 0;
        while (i < 16 && blockGetter.getBlockState(blockPos.below(i + 1)).is(Blocks.BAMBOO)) {
            i++;
        }
        return i;
    }
}
