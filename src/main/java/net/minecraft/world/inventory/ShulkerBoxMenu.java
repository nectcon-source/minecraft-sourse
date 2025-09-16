package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ShulkerBoxMenu.class */
public class ShulkerBoxMenu extends AbstractContainerMenu {
    private final Container container;

    public ShulkerBoxMenu(int i, Inventory inventory) {
        this(i, inventory, new SimpleContainer(27));
    }

    public ShulkerBoxMenu(int i, Inventory inventory, Container container) {
        super(MenuType.SHULKER_BOX, i);
        checkContainerSize(container, 27);
        this.container = container;
        container.startOpen(inventory.player);
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new ShulkerBoxSlot(container, i3 + (i2 * 9), 8 + (i3 * 18), 18 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 3; i4++) {
            for (int i5 = 0; i5 < 9; i5++) {
                addSlot(new Slot(inventory, i5 + (i4 * 9) + 9, 8 + (i5 * 18), 84 + (i4 * 18)));
            }
        }
        for (int i6 = 0; i6 < 9; i6++) {
            addSlot(new Slot(inventory, i6, 8 + (i6 * 18), 142));
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i < this.container.getContainerSize()) {
                if (!moveItemStackTo(item, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(item, 0, this.container.getContainerSize(), false)) {
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
        this.container.stopOpen(player);
    }
}
