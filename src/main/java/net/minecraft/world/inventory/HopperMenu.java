package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/HopperMenu.class */
public class HopperMenu extends AbstractContainerMenu {
    private final Container hopper;

    public HopperMenu(int i, Inventory inventory) {
        this(i, inventory, new SimpleContainer(5));
    }

    public HopperMenu(int i, Inventory inventory, Container container) {
        super(MenuType.HOPPER, i);
        this.hopper = container;
        checkContainerSize(container, 5);
        container.startOpen(inventory.player);
        for (int i2 = 0; i2 < 5; i2++) {
            addSlot(new Slot(container, i2, 44 + (i2 * 18), 20));
        }
        for (int i3 = 0; i3 < 3; i3++) {
            for (int i4 = 0; i4 < 9; i4++) {
                addSlot(new Slot(inventory, i4 + (i3 * 9) + 9, 8 + (i4 * 18), (i3 * 18) + 51));
            }
        }
        for (int i5 = 0; i5 < 9; i5++) {
            addSlot(new Slot(inventory, i5, 8 + (i5 * 18), 109));
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.hopper.stillValid(player);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i < this.hopper.getContainerSize()) {
                if (!moveItemStackTo(item, this.hopper.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(item, 0, this.hopper.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (item.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.hopper.stopOpen(player);
    }
}
