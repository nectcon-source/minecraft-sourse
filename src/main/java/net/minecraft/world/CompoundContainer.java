package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/CompoundContainer.class */
public class CompoundContainer implements Container {
    private final Container container1;
    private final Container container2;

    public CompoundContainer(Container container, Container container2) {
        container = container == null ? container2 : container;
        container2 = container2 == null ? container : container2;
        this.container1 = container;
        this.container2 = container2;
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.container1.getContainerSize() + this.container2.getContainerSize();
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        return this.container1.isEmpty() && this.container2.isEmpty();
    }

    public boolean contains(Container container) {
        return this.container1 == container || this.container2 == container;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        if (i >= this.container1.getContainerSize()) {
            return this.container2.getItem(i - this.container1.getContainerSize());
        }
        return this.container1.getItem(i);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        if (i >= this.container1.getContainerSize()) {
            return this.container2.removeItem(i - this.container1.getContainerSize(), i2);
        }
        return this.container1.removeItem(i, i2);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        if (i >= this.container1.getContainerSize()) {
            return this.container2.removeItemNoUpdate(i - this.container1.getContainerSize());
        }
        return this.container1.removeItemNoUpdate(i);
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        if (i >= this.container1.getContainerSize()) {
            this.container2.setItem(i - this.container1.getContainerSize(), itemStack);
        } else {
            this.container1.setItem(i, itemStack);
        }
    }

    @Override // net.minecraft.world.Container
    public int getMaxStackSize() {
        return this.container1.getMaxStackSize();
    }

    @Override // net.minecraft.world.Container
    public void setChanged() {
        this.container1.setChanged();
        this.container2.setChanged();
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        return this.container1.stillValid(player) && this.container2.stillValid(player);
    }

    @Override // net.minecraft.world.Container
    public void startOpen(Player player) {
        this.container1.startOpen(player);
        this.container2.startOpen(player);
    }

    @Override // net.minecraft.world.Container
    public void stopOpen(Player player) {
        this.container1.stopOpen(player);
        this.container2.stopOpen(player);
    }

    @Override // net.minecraft.world.Container
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        if (i >= this.container1.getContainerSize()) {
            return this.container2.canPlaceItem(i - this.container1.getContainerSize(), itemStack);
        }
        return this.container1.canPlaceItem(i, itemStack);
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.container1.clearContent();
        this.container2.clearContent();
    }
}
