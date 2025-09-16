package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/GrowingPlantHeadBlock.class */
public abstract class GrowingPlantHeadBlock extends GrowingPlantBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    private final double growPerTickProbability;

    protected abstract int getBlocksToGrowWhenBonemealed(Random random);

    protected abstract boolean canGrowInto(BlockState blockState);

    protected GrowingPlantHeadBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean z, double d) {
        super(properties, direction, voxelShape, z);
        this.growPerTickProbability = d;
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    public BlockState getStateForPlacement(LevelAccessor levelAccessor) {
        return (BlockState) defaultBlockState().setValue(AGE, Integer.valueOf(levelAccessor.getRandom().nextInt(25)));
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 25;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (((Integer) blockState.getValue(AGE)).intValue() < 25 && random.nextDouble() < this.growPerTickProbability) {
            BlockPos relative = blockPos.relative(this.growthDirection);
            if (canGrowInto(serverLevel.getBlockState(relative))) {
                serverLevel.setBlockAndUpdate(relative, blockState.cycle(AGE));
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == this.growthDirection.getOpposite() && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        if (direction == this.growthDirection && (blockState2.is(this) || blockState2.is(getBodyBlock()))) {
            return getBodyBlock().defaultBlockState();
        }
        if (this.scheduleFluidTicks) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return canGrowInto(blockGetter.getBlockState(blockPos.relative(this.growthDirection)));
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        BlockPos relative = blockPos.relative(this.growthDirection);
        int min = Math.min(((Integer) blockState.getValue(AGE)).intValue() + 1, 25);
        int blocksToGrowWhenBonemealed = getBlocksToGrowWhenBonemealed(random);
        for (int i = 0; i < blocksToGrowWhenBonemealed && canGrowInto(serverLevel.getBlockState(relative)); i++) {
            serverLevel.setBlockAndUpdate(relative, (BlockState) blockState.setValue(AGE, Integer.valueOf(min)));
            relative = relative.relative(this.growthDirection);
            min = Math.min(min + 1, 25);
        }
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}
