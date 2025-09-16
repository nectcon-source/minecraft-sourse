package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DropperBlock.class */
public class DropperBlock extends DispenserBlock {
    private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

    public DropperBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.DispenserBlock
    protected DispenseItemBehavior getDispenseMethod(ItemStack itemStack) {
        return DISPENSE_BEHAVIOUR;
    }

    @Override // net.minecraft.world.level.block.DispenserBlock, net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new DropperBlockEntity();
    }

    @Override // net.minecraft.world.level.block.DispenserBlock
    protected void dispenseFrom(ServerLevel serverLevel, BlockPos blockPos) {
        ItemStack copy;
        BlockSourceImpl blockSourceImpl = new BlockSourceImpl(serverLevel, blockPos);
        DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity) blockSourceImpl.getEntity();
        int randomSlot = dispenserBlockEntity.getRandomSlot();
        if (randomSlot < 0) {
            serverLevel.levelEvent(1001, blockPos, 0);
            return;
        }
        ItemStack item = dispenserBlockEntity.getItem(randomSlot);
        if (item.isEmpty()) {
            return;
        }
        Direction direction = (Direction) serverLevel.getBlockState(blockPos).getValue(FACING);
        Container containerAt = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(direction));
        if (containerAt == null) {
            copy = DISPENSE_BEHAVIOUR.dispense(blockSourceImpl, item);
        } else if (HopperBlockEntity.addItem(dispenserBlockEntity, containerAt, item.copy().split(1), direction.getOpposite()).isEmpty()) {
            copy = item.copy();
            copy.shrink(1);
        } else {
            copy = item.copy();
        }
        dispenserBlockEntity.setItem(randomSlot, copy);
    }
}
