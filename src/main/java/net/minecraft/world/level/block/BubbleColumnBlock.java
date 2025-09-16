package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BubbleColumnBlock.class */
public class BubbleColumnBlock extends Block implements BucketPickup {
    public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;

    public BubbleColumnBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState( this.stateDefinition.any().setValue(DRAG_DOWN, true));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.getBlockState(blockPos.above()).isAir()) {
            entity.onAboveBubbleCol(((Boolean) blockState.getValue(DRAG_DOWN)).booleanValue());
            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;
                for (int i = 0; i < 2; i++) {
                    serverLevel.sendParticles(ParticleTypes.SPLASH, blockPos.getX() + level.random.nextDouble(), blockPos.getY() + 1, blockPos.getZ() + level.random.nextDouble(), 1, 0.0d, 0.0d, 0.0d, 1.0d);
                    serverLevel.sendParticles(ParticleTypes.BUBBLE, blockPos.getX() + level.random.nextDouble(), blockPos.getY() + 1, blockPos.getZ() + level.random.nextDouble(), 1, 0.0d, 0.01d, 0.0d, 0.2d);
                }
                return;
            }
            return;
        }
        entity.onInsideBubbleColumn(((Boolean) blockState.getValue(DRAG_DOWN)).booleanValue());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        growColumn(level, blockPos.above(), getDrag(level, blockPos.below()));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        growColumn(serverLevel, blockPos.above(), getDrag(serverLevel, blockPos));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        return Fluids.WATER.getSource(false);
    }

    public static void growColumn(LevelAccessor levelAccessor, BlockPos blockPos, boolean z) {
        if (canExistIn(levelAccessor, blockPos)) {
            levelAccessor.setBlock(blockPos, (BlockState) Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(z)), 2);
        }
    }

    public static boolean canExistIn(LevelAccessor levelAccessor, BlockPos blockPos) {
        FluidState fluidState = levelAccessor.getFluidState(blockPos);
        return levelAccessor.getBlockState(blockPos).is(Blocks.WATER) && fluidState.getAmount() >= 8 && fluidState.isSource();
    }

    private static boolean getDrag(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockState.is(Blocks.BUBBLE_COLUMN)) {
            return ((Boolean) blockState.getValue(DRAG_DOWN)).booleanValue();
        }
        return !blockState.is(Blocks.SOUL_SAND);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        double x = blockPos.getX();
        double y = blockPos.getY();
        double z = blockPos.getZ();
        if (((Boolean) blockState.getValue(DRAG_DOWN)).booleanValue()) {
            level.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, x + 0.5d, y + 0.8d, z, 0.0d, 0.0d, 0.0d);
            if (random.nextInt(200) == 0) {
                level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2f + (random.nextFloat() * 0.2f), 0.9f + (random.nextFloat() * 0.15f), false);
                return;
            }
            return;
        }
        level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + 0.5d, y, z + 0.5d, 0.0d, 0.04d, 0.0d);
        level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0d, 0.04d, 0.0d);
        if (random.nextInt(200) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2f + (random.nextFloat() * 0.2f), 0.9f + (random.nextFloat() * 0.15f), false);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.WATER.defaultBlockState();
        }
        if (direction == Direction.DOWN) {
            levelAccessor.setBlock(blockPos, (BlockState) Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(getDrag(levelAccessor, blockPos2))), 2);
        } else if (direction == Direction.UP && !blockState2.is(Blocks.BUBBLE_COLUMN) && canExistIn(levelAccessor, blockPos2)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 5);
        }
        levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        return blockState2.is(Blocks.BUBBLE_COLUMN) || blockState2.is(Blocks.MAGMA_BLOCK) || blockState2.is(Blocks.SOUL_SAND);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.INVISIBLE;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DRAG_DOWN);
    }

    @Override // net.minecraft.world.level.block.BucketPickup
    public Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
        return Fluids.WATER;
    }
}
