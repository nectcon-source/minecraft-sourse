package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StructureBlock.class */
public class StructureBlock extends BaseEntityBlock {
    public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

    protected StructureBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new StructureBlockEntity();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof StructureBlockEntity) {
            return ((StructureBlockEntity) blockEntity).usedBy(player) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (!level.isClientSide && livingEntity != null) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof StructureBlockEntity) {
                ((StructureBlockEntity) blockEntity).createdBy(livingEntity);
            }
        }
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(MODE, StructureMode.DATA);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof StructureBlockEntity)) {
            return;
        }
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity) blockEntity;
        boolean hasNeighborSignal = level.hasNeighborSignal(blockPos);
        boolean isPowered = structureBlockEntity.isPowered();
        if (hasNeighborSignal && !isPowered) {
            structureBlockEntity.setPowered(true);
            trigger((ServerLevel) level, structureBlockEntity);
        } else if (!hasNeighborSignal && isPowered) {
            structureBlockEntity.setPowered(false);
        }
    }

    private void trigger(ServerLevel serverLevel, StructureBlockEntity structureBlockEntity) {
        switch (structureBlockEntity.getMode()) {
            case SAVE:
                structureBlockEntity.saveStructure(false);
                break;
            case LOAD:
                structureBlockEntity.loadStructure(serverLevel, false);
                break;
            case CORNER:
                structureBlockEntity.unloadStructure();
                break;
        }
    }
}
