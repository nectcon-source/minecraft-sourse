package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ShulkerBoxSlot.class */
public class ShulkerBoxSlot extends Slot {
    public ShulkerBoxSlot(Container container, int i, int i2, int i3) {
        super(container, i, i2, i3);
    }

    @Override // net.minecraft.world.inventory.Slot
    public boolean mayPlace(ItemStack itemStack) {
        return !(Block.byItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
    }
}
