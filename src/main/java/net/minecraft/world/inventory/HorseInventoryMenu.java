package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/HorseInventoryMenu.class */
public class HorseInventoryMenu extends AbstractContainerMenu {
    private final Container horseContainer;
    private final AbstractHorse horse;

    public HorseInventoryMenu(int i, Inventory inventory, Container container, final AbstractHorse abstractHorse) {
        super(null, i);
        this.horseContainer = container;
        this.horse = abstractHorse;
        container.startOpen(inventory.player);
        addSlot(new Slot(container, 0, 8, 18) { // from class: net.minecraft.world.inventory.HorseInventoryMenu.1
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.getItem() == Items.SADDLE && !hasItem() && abstractHorse.isSaddleable();
            }

            @Override // net.minecraft.world.inventory.Slot
            public boolean isActive() {
                return abstractHorse.isSaddleable();
            }
        });
        addSlot(new Slot(container, 1, 8, 36) { // from class: net.minecraft.world.inventory.HorseInventoryMenu.2
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return abstractHorse.isArmor(itemStack);
            }

            @Override // net.minecraft.world.inventory.Slot
            public boolean isActive() {
                return abstractHorse.canWearArmor();
            }

            @Override // net.minecraft.world.inventory.Slot
            public int getMaxStackSize() {
                return 1;
            }
        });
        if ((abstractHorse instanceof AbstractChestedHorse) && ((AbstractChestedHorse) abstractHorse).hasChest()) {
            for (int i2 = 0; i2 < 3; i2++) {
                for (int i3 = 0; i3 < ((AbstractChestedHorse) abstractHorse).getInventoryColumns(); i3++) {
                    addSlot(new Slot(container, 2 + i3 + (i2 * ((AbstractChestedHorse) abstractHorse).getInventoryColumns()), 80 + (i3 * 18), 18 + (i2 * 18)));
                }
            }
        }
        for (int i4 = 0; i4 < 3; i4++) {
            for (int i5 = 0; i5 < 9; i5++) {
                addSlot(new Slot(inventory, i5 + (i4 * 9) + 9, 8 + (i5 * 18), (102 + (i4 * 18)) - 18));
            }
        }
        for (int i6 = 0; i6 < 9; i6++) {
            addSlot(new Slot(inventory, i6, 8 + (i6 * 18), 142));
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.horseContainer.stillValid(player) && this.horse.isAlive() && this.horse.distanceTo(player) < 8.0f;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            int containerSize = this.horseContainer.getContainerSize();
            if (i < containerSize) {
                if (!moveItemStackTo(item, containerSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (getSlot(1).mayPlace(item) && !getSlot(1).hasItem()) {
                if (!moveItemStackTo(item, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (getSlot(0).mayPlace(item)) {
                if (!moveItemStackTo(item, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (containerSize <= 2 || !moveItemStackTo(item, 2, containerSize, false)) {
                int i2 = containerSize + 27;
                int i3 = i2 + 9;
                if (i >= i2 && i < i3) {
                    if (!moveItemStackTo(item, containerSize, i2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= containerSize && i < i2) {
                    if (!moveItemStackTo(item, i2, i3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(item, i2, i2, false)) {
                    return ItemStack.EMPTY;
                }
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
        this.horseContainer.stopOpen(player);
    }
}
