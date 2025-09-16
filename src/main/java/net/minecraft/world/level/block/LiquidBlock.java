package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/LiquidBlock.class */
public class LiquidBlock extends Block implements BucketPickup {
    protected final FlowingFluid fluid;
    private final List<FluidState> stateCache;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final VoxelShape STABLE_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d);

    protected LiquidBlock(FlowingFluid flowingFluid, BlockBehaviour.Properties properties) {
        super(properties);
        this.fluid = flowingFluid;
        this.stateCache = Lists.newArrayList();
        this.stateCache.add(flowingFluid.getSource(false));
        for (int i = 1; i < 8; i++) {
            this.stateCache.add(flowingFluid.getFlowing(8 - i, false));
        }
        this.stateCache.add(flowingFluid.getFlowing(8, true));
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (collisionContext.isAbove(STABLE_SHAPE, blockPos, true) && ((Integer) blockState.getValue(LEVEL)).intValue() == 0 && collisionContext.canStandOnFluid(blockGetter.getFluidState(blockPos.above()), this.fluid)) {
            return STABLE_SHAPE;
        }
        return Shapes.empty();
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getFluidState().isRandomlyTicking();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        blockState.getFluidState().randomTick(serverLevel, blockPos, random);
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return !this.fluid.is(FluidTags.LAVA);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        return this.stateCache.get(Math.min(((Integer) blockState.getValue(LEVEL)).intValue(), 8));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return blockState2.getFluidState().getType().isSame(this.fluid);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.INVISIBLE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (shouldSpreadLiquid(level, blockPos, blockState)) {
            level.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(level));
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getFluidState().isSource() || blockState2.getFluidState().isSource()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (shouldSpreadLiquid(level, blockPos, blockState)) {
            level.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(level));
        }
    }

    private boolean shouldSpreadLiquid(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.fluid.is(FluidTags.LAVA)) {
            boolean is = level.getBlockState(blockPos.below()).is(Blocks.SOUL_SOIL);
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN) {
                    BlockPos relative = blockPos.relative(direction);
                    if (level.getFluidState(relative).is(FluidTags.WATER)) {
                        level.setBlockAndUpdate(blockPos, (level.getFluidState(blockPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE).defaultBlockState());
                        fizz(level, blockPos);
                        return false;
                    }
                    if (is && level.getBlockState(relative).is(Blocks.BLUE_ICE)) {
                        level.setBlockAndUpdate(blockPos, Blocks.BASALT.defaultBlockState());
                        fizz(level, blockPos);
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    private void fizz(LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.levelEvent(1501, blockPos, 0);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override // net.minecraft.world.level.block.BucketPickup
    public Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (((Integer) blockState.getValue(LEVEL)).intValue() == 0) {
            levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            return this.fluid;
        }
        return Fluids.EMPTY;
    }
}
