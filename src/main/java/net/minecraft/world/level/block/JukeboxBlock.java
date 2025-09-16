package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/JukeboxBlock.class */
public class JukeboxBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    protected JukeboxBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(HAS_RECORD, false));
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        CompoundTag orCreateTag = itemStack.getOrCreateTag();
        if (orCreateTag.contains("BlockEntityTag") && orCreateTag.getCompound("BlockEntityTag").contains("RecordItem")) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(HAS_RECORD, true), 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (((Boolean) blockState.getValue(HAS_RECORD)).booleanValue()) {
            dropRecording(level, blockPos);
            level.setBlock(blockPos, (BlockState) blockState.setValue(HAS_RECORD, false), 2);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public void setRecord(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity)) {
            return;
        }
        ((JukeboxBlockEntity) blockEntity).setRecord(itemStack.copy());
        levelAccessor.setBlock(blockPos, (BlockState) blockState.setValue(HAS_RECORD, true), 2);
    }

    private void dropRecording(Level level, BlockPos blockPos) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity)) {
            return;
        }
        JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity) blockEntity;
        ItemStack record = jukeboxBlockEntity.getRecord();
        if (record.isEmpty()) {
            return;
        }
        level.levelEvent(1010, blockPos, 0);
        jukeboxBlockEntity.clearContent();
        ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + (level.random.nextFloat() * 0.7f) + 0.15000000596046448d, blockPos.getY() + (level.random.nextFloat() * 0.7f) + 0.06000000238418579d + 0.6d, blockPos.getZ() + (level.random.nextFloat() * 0.7f) + 0.15000000596046448d, record.copy());
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        dropRecording(level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new JukeboxBlockEntity();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof JukeboxBlockEntity) {
            Item item = ((JukeboxBlockEntity) blockEntity).getRecord().getItem();
            if (item instanceof RecordItem) {
                return ((RecordItem) item).getAnalogOutput();
            }
            return 0;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD);
    }
}
