package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/FurnaceFuelSlot.class */
public class FurnaceFuelSlot extends Slot {
    private final AbstractFurnaceMenu menu;

    public FurnaceFuelSlot(AbstractFurnaceMenu abstractFurnaceMenu, Container container, int i, int i2, int i3) {
        super(container, i, i2, i3);
        this.menu = abstractFurnaceMenu;
    }

    @Override // net.minecraft.world.inventory.Slot
    public boolean mayPlace(ItemStack itemStack) {
        return this.menu.isFuel(itemStack) || isBucket(itemStack);
    }

    @Override // net.minecraft.world.inventory.Slot
    public int getMaxStackSize(ItemStack itemStack) {
        if (isBucket(itemStack)) {
            return 1;
        }
        return super.getMaxStackSize(itemStack);
    }

    public static boolean isBucket(ItemStack itemStack) {
        return itemStack.getItem() == Items.BUCKET;
    }
}
