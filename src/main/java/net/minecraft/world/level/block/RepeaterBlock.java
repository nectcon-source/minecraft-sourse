package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RepeaterBlock.class */
public class RepeaterBlock extends DiodeBlock {
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    protected RepeaterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(DELAY, 1)).setValue(LOCKED, false)).setValue(POWERED, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!player.abilities.mayBuild) {
            return InteractionResult.PASS;
        }
        level.setBlock(blockPos, blockState.cycle(DELAY), 3);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected int getDelay(BlockState blockState) {
        return ((Integer) blockState.getValue(DELAY)).intValue() * 2;
    }

    @Override // net.minecraft.world.level.block.DiodeBlock, net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState stateForPlacement = super.getStateForPlacement(blockPlaceContext);
        return (BlockState) stateForPlacement.setValue(LOCKED, Boolean.valueOf(isLocked(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), stateForPlacement)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!levelAccessor.isClientSide() && direction.getAxis() != ((Direction) blockState.getValue(FACING)).getAxis()) {
            return (BlockState) blockState.setValue(LOCKED, Boolean.valueOf(isLocked(levelAccessor, blockPos, blockState)));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    public boolean isLocked(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return getAlternateSignal(levelReader, blockPos, blockState) > 0;
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected boolean isAlternateInput(BlockState blockState) {
        return isDiode(blockState);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return;
        }
        Direction direction = (Direction) blockState.getValue(FACING);
        double x = blockPos.getX() + 0.5d + ((random.nextDouble() - 0.5d) * 0.2d);
        double y = blockPos.getY() + 0.4d + ((random.nextDouble() - 0.5d) * 0.2d);
        double z = blockPos.getZ() + 0.5d + ((random.nextDouble() - 0.5d) * 0.2d);
        float f = -5.0f;
        if (random.nextBoolean()) {
            f = (((Integer) blockState.getValue(DELAY)).intValue() * 2) - 1;
        }
        float f2 = f / 16.0f;
        level.addParticle(DustParticleOptions.REDSTONE, x + (f2 * direction.getStepX()), y, z + (f2 * direction.getStepZ()), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DELAY, LOCKED, POWERED);
    }
}
