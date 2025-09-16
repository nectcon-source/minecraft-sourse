package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ComparatorBlock.class */
public class ComparatorBlock extends DiodeBlock implements EntityBlock {
    public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

    public ComparatorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(MODE, ComparatorMode.COMPARE));
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected int getDelay(BlockState blockState) {
        return 2;
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof ComparatorBlockEntity) {
            return ((ComparatorBlockEntity) blockEntity).getOutputSignal();
        }
        return 0;
    }

    private int calculateOutputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(MODE) == ComparatorMode.SUBTRACT) {
            return Math.max(getInputSignal(level, blockPos, blockState) - getAlternateSignal(level, blockPos, blockState), 0);
        }
        return getInputSignal(level, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState) {
        int inputSignal = getInputSignal(level, blockPos, blockState);
        if (inputSignal == 0) {
            return false;
        }
        int alternateSignal = getAlternateSignal(level, blockPos, blockState);
        if (inputSignal > alternateSignal) {
            return true;
        }
        return inputSignal == alternateSignal && blockState.getValue(MODE) == ComparatorMode.COMPARE;
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        int inputSignal = super.getInputSignal(level, blockPos, blockState);
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockPos relative = blockPos.relative(direction);
        BlockState blockState2 = level.getBlockState(relative);
        if (blockState2.hasAnalogOutputSignal()) {
            inputSignal = blockState2.getAnalogOutputSignal(level, relative);
        } else if (inputSignal < 15 && blockState2.isRedstoneConductor(level, relative)) {
            BlockPos relative2 = relative.relative(direction);
            BlockState blockState3 = level.getBlockState(relative2);
            ItemFrame itemFrame = getItemFrame(level, direction, relative2);
            int max = Math.max(itemFrame == null ? Integer.MIN_VALUE : itemFrame.getAnalogOutput(), blockState3.hasAnalogOutputSignal() ? blockState3.getAnalogOutputSignal(level, relative2) : Integer.MIN_VALUE);
            if (max != Integer.MIN_VALUE) {
                inputSignal = max;
            }
        }
        return inputSignal;
    }

    @Nullable
    private ItemFrame getItemFrame(Level level, Direction direction, BlockPos blockPos) {
        List<ItemFrame> entitiesOfClass = level.getEntitiesOfClass(ItemFrame.class, new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1), itemFrame -> {
            return itemFrame != null && itemFrame.getDirection() == direction;
        });
        if (entitiesOfClass.size() == 1) {
            return entitiesOfClass.get(0);
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!player.abilities.mayBuild) {
            return InteractionResult.PASS;
        }
        BlockState cycle = blockState.cycle(MODE);
        level.playSound(player, blockPos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3f, cycle.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55f : 0.5f);
        level.setBlock(blockPos, cycle, 2);
        refreshOutputState(level, blockPos, cycle);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override // net.minecraft.world.level.block.DiodeBlock
    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.getBlockTicks().willTickThisTick(blockPos, this)) {
            return;
        }
        int calculateOutputSignal = calculateOutputSignal(level, blockPos, blockState);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (calculateOutputSignal != (blockEntity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity) blockEntity).getOutputSignal() : 0) || ((Boolean) blockState.getValue(POWERED)).booleanValue() != shouldTurnOn(level, blockPos, blockState)) {
            level.getBlockTicks().scheduleTick(blockPos, this, 2, shouldPrioritize(level, blockPos, blockState) ? TickPriority.HIGH : TickPriority.NORMAL);
        }
    }

    private void refreshOutputState(Level level, BlockPos blockPos, BlockState blockState) {
        int calculateOutputSignal = calculateOutputSignal(level, blockPos, blockState);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        int i = 0;
        if (blockEntity instanceof ComparatorBlockEntity) {
            ComparatorBlockEntity comparatorBlockEntity = (ComparatorBlockEntity) blockEntity;
            i = comparatorBlockEntity.getOutputSignal();
            comparatorBlockEntity.setOutputSignal(calculateOutputSignal);
        }
        if (i != calculateOutputSignal || blockState.getValue(MODE) == ComparatorMode.COMPARE) {
            boolean shouldTurnOn = shouldTurnOn(level, blockPos, blockState);
            boolean booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue();
            if (booleanValue && !shouldTurnOn) {
                level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, false), 2);
            } else if (!booleanValue && shouldTurnOn) {
                level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, true), 2);
            }
            updateNeighborsInFront(level, blockPos, blockState);
        }
    }

    @Override // net.minecraft.world.level.block.DiodeBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        refreshOutputState(serverLevel, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int i2) {
        super.triggerEvent(blockState, level, blockPos, i, i2);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return blockEntity != null && blockEntity.triggerEvent(i, i2);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new ComparatorBlockEntity();
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE, POWERED);
    }
}
