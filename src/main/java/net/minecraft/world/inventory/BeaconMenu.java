package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/BeaconMenu.class */
public class BeaconMenu extends AbstractContainerMenu {
    private final Container beacon;
    private final PaymentSlot paymentSlot;
    private final ContainerLevelAccess access;
    private final ContainerData beaconData;

    public BeaconMenu(int i, Container container) {
        this(i, container, new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    public BeaconMenu(int i, Container container, ContainerData containerData, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.BEACON, i);
        this.beacon = new SimpleContainer(1) { // from class: net.minecraft.world.inventory.BeaconMenu.1
            @Override // net.minecraft.world.Container
            public boolean canPlaceItem(int i2, ItemStack itemStack) {
                return itemStack.getItem().is(ItemTags.BEACON_PAYMENT_ITEMS);
            }

            @Override // net.minecraft.world.Container
            public int getMaxStackSize() {
                return 1;
            }
        };
        checkContainerDataCount(containerData, 3);
        this.beaconData = containerData;
        this.access = containerLevelAccess;
        this.paymentSlot = new PaymentSlot(this.beacon, 0, 136, 110);
        addSlot(this.paymentSlot);
        addDataSlots(containerData);
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new Slot(container, i3 + (i2 * 9) + 9, 36 + (i3 * 18), 137 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            addSlot(new Slot(container, i4, 36 + (i4 * 18), 195));
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        if (player.level.isClientSide) {
            return;
        }
        ItemStack remove = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
        if (!remove.isEmpty()) {
            player.drop(remove, false);
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.BEACON);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void setData(int i, int i2) {
        super.setData(i, i2);
        broadcastChanges();
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i == 0) {
                if (!moveItemStackTo(item, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace(item) && item.getCount() == 1) {
                if (!moveItemStackTo(item, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 1 && i < 28) {
                if (!moveItemStackTo(item, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 28 && i < 37) {
                if (!moveItemStackTo(item, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(item, 1, 37, false)) {
                return ItemStack.EMPTY;
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

    public int getLevels() {
        return this.beaconData.get(0);
    }

    @Nullable
    public MobEffect getPrimaryEffect() {
        return MobEffect.byId(this.beaconData.get(1));
    }

    @Nullable
    public MobEffect getSecondaryEffect() {
        return MobEffect.byId(this.beaconData.get(2));
    }

    public void updateEffects(int i, int i2) {
        if (this.paymentSlot.hasItem()) {
            this.beaconData.set(1, i);
            this.beaconData.set(2, i2);
            this.paymentSlot.remove(1);
        }
    }

    public boolean hasPayment() {
        return !this.beacon.getItem(0).isEmpty();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/BeaconMenu$PaymentSlot.class */
    class PaymentSlot extends Slot {
        public PaymentSlot(Container container, int i, int i2, int i3) {
            super(container, i, i2, i3);
        }

        @Override // net.minecraft.world.inventory.Slot
        public boolean mayPlace(ItemStack itemStack) {
            return itemStack.getItem().is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override // net.minecraft.world.inventory.Slot
        public int getMaxStackSize() {
            return 1;
        }
    }
}
