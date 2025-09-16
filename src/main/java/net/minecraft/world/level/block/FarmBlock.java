package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FarmBlock.class */
public class FarmBlock extends Block {
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    protected static final VoxelShape SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 15.0d, 16.0d);

    protected FarmBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(MOISTURE, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.above());
        return !blockState2.getMaterial().isSolid() || (blockState2.getBlock() instanceof FenceGateBlock) || (blockState2.getBlock() instanceof MovingPistonBlock);
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        if (!defaultBlockState().canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
            return Blocks.DIRT.defaultBlockState();
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            turnToDirt(blockState, serverLevel, blockPos);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int intValue = ((Integer) blockState.getValue(MOISTURE)).intValue();
        if (isNearWater(serverLevel, blockPos) || serverLevel.isRainingAt(blockPos.above())) {
            if (intValue < 7) {
                serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(MOISTURE, 7), 2);
            }
        } else if (intValue > 0) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(MOISTURE, Integer.valueOf(intValue - 1)), 2);
        } else if (!isUnderCrops(serverLevel, blockPos)) {
            turnToDirt(blockState, serverLevel, blockPos);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        if (!level.isClientSide && level.random.nextFloat() < f - 0.5f && (entity instanceof LivingEntity) && (((entity instanceof Player) || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512f)) {
            turnToDirt(level.getBlockState(blockPos), level, blockPos);
        }
        super.fallOn(level, blockPos, entity, f);
    }

    public static void turnToDirt(BlockState blockState, Level level, BlockPos blockPos) {
        level.setBlockAndUpdate(blockPos, pushEntitiesUp(blockState, Blocks.DIRT.defaultBlockState(), level, blockPos));
    }

    private static boolean isUnderCrops(BlockGetter blockGetter, BlockPos blockPos) {
        Block block = blockGetter.getBlockState(blockPos.above()).getBlock();
        return (block instanceof CropBlock) || (block instanceof StemBlock) || (block instanceof AttachedStemBlock);
    }

    private static boolean isNearWater(LevelReader levelReader, BlockPos blockPos) {
        Iterator<BlockPos> it = BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 1, 4)).iterator();
        while (it.hasNext()) {
            if (levelReader.getFluidState(it.next()).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
