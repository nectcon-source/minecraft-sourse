package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ChestMenu.class */
public class ChestMenu extends AbstractContainerMenu {
    private final Container container;
    private final int containerRows;

    private ChestMenu(MenuType<?> menuType, int i, Inventory inventory, int i2) {
        this(menuType, i, inventory, new SimpleContainer(9 * i2), i2);
    }

    public static ChestMenu oneRow(int i, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, i, inventory, 1);
    }

    public static ChestMenu twoRows(int i, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x2, i, inventory, 2);
    }

    public static ChestMenu threeRows(int i, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, 3);
    }

    public static ChestMenu fourRows(int i, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x4, i, inventory, 4);
    }

    public static ChestMenu fiveRows(int i, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x5, i, inventory, 5);
    }

    public static ChestMenu sixRows(int i, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, 6);
    }

    public static ChestMenu threeRows(int i, Inventory inventory, Container container) {
        return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, container, 3);
    }

    public static ChestMenu sixRows(int i, Inventory inventory, Container container) {
        return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, container, 6);
    }

    public ChestMenu(MenuType<?> menuType, int i, Inventory inventory, Container container, int i2) {
        super(menuType, i);
        checkContainerSize(container, i2 * 9);
        this.container = container;
        this.containerRows = i2;
        container.startOpen(inventory.player);
        int i3 = (this.containerRows - 4) * 18;
        for (int i4 = 0; i4 < this.containerRows; i4++) {
            for (int i5 = 0; i5 < 9; i5++) {
                addSlot(new Slot(container, i5 + (i4 * 9), 8 + (i5 * 18), 18 + (i4 * 18)));
            }
        }
        for (int i6 = 0; i6 < 3; i6++) {
            for (int i7 = 0; i7 < 9; i7++) {
                addSlot(new Slot(inventory, i7 + (i6 * 9) + 9, 8 + (i7 * 18), 103 + (i6 * 18) + i3));
            }
        }
        for (int i8 = 0; i8 < 9; i8++) {
            addSlot(new Slot(inventory, i8, 8 + (i8 * 18), 161 + i3));
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
            if (i < this.containerRows * 9) {
                if (!moveItemStackTo(item, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(item, 0, this.containerRows * 9, false)) {
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

    public Container getContainer() {
        return this.container;
    }

    public int getRowCount() {
        return this.containerRows;
    }
}
