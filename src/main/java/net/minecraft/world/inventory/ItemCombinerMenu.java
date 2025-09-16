package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ItemCombinerMenu.class */
public abstract class ItemCombinerMenu extends AbstractContainerMenu {
    protected final ResultContainer resultSlots;
    protected final Container inputSlots;
    protected final ContainerLevelAccess access;
    protected final Player player;

    protected abstract boolean mayPickup(Player player, boolean z);

    protected abstract ItemStack onTake(Player player, ItemStack itemStack);

    protected abstract boolean isValidBlock(BlockState blockState);

    public abstract void createResult();

    public ItemCombinerMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i);
        this.resultSlots = new ResultContainer();
        this.inputSlots = new SimpleContainer(2) { // from class: net.minecraft.world.inventory.ItemCombinerMenu.1
            @Override // net.minecraft.world.SimpleContainer, net.minecraft.world.Container
            public void setChanged() {
                super.setChanged();
                ItemCombinerMenu.this.slotsChanged(this);
            }
        };
        this.access = containerLevelAccess;
        this.player = inventory.player;
        addSlot(new Slot(this.inputSlots, 0, 27, 47));
        addSlot(new Slot(this.inputSlots, 1, 76, 47));
        addSlot(new Slot(this.resultSlots, 2, 134, 47) { // from class: net.minecraft.world.inventory.ItemCombinerMenu.2
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPickup(Player player) {
                return ItemCombinerMenu.this.mayPickup(player, hasItem());
            }

            @Override // net.minecraft.world.inventory.Slot
            public ItemStack onTake(Player player, ItemStack itemStack) {
                return ItemCombinerMenu.this.onTake(player, itemStack);
            }
        });
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new Slot(inventory, i3 + (i2 * 9) + 9, 8 + (i3 * 18), 84 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            addSlot(new Slot(inventory, i4, 8 + (i4 * 18), 142));
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.inputSlots) {
            createResult();
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            clearContainer(player, level, this.inputSlots);
        });
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return ((Boolean) this.access.evaluate((level, blockPos) -> {
            if (isValidBlock(level.getBlockState(blockPos))) {
                return Boolean.valueOf(player.distanceToSqr(((double) blockPos.getX()) + 0.5d, ((double) blockPos.getY()) + 0.5d, ((double) blockPos.getZ()) + 0.5d) <= 64.0d);
            }
            return false;
        }, true)).booleanValue();
    }

    protected boolean shouldQuickMoveToAdditionalSlot(ItemStack itemStack) {
        return false;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i == 2) {
                if (!moveItemStackTo(item, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (i == 0 || i == 1) {
                if (!moveItemStackTo(item, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 3 && i < 39) {
                if (!moveItemStackTo(item, shouldQuickMoveToAdditionalSlot(itemStack) ? 1 : 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (item.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (item.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, item);
        }
        return itemStack;
    }
}
