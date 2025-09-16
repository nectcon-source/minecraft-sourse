package net.minecraft.world.inventory;

import java.util.Iterator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/CraftingContainer.class */
public class CraftingContainer implements Container, StackedContentsCompatible {
    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private final AbstractContainerMenu menu;

    public CraftingContainer(AbstractContainerMenu abstractContainerMenu, int i, int i2) {
        this.items = NonNullList.withSize(i * i2, ItemStack.EMPTY);
        this.menu = abstractContainerMenu;
        this.width = i;
        this.height = i2;
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.items.size();
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        if (i >= getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.items.get(i);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.items, i);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        ItemStack removeItem = ContainerHelper.removeItem(this.items, i, i2);
        if (!removeItem.isEmpty()) {
            this.menu.slotsChanged(this);
        }
        return removeItem;
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        this.menu.slotsChanged(this);
    }

    @Override // net.minecraft.world.Container
    public void setChanged() {
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        return true;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.items.clear();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    @Override // net.minecraft.world.inventory.StackedContentsCompatible
    public void fillStackedContents(StackedContents stackedContents) {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            stackedContents.accountSimpleStack(it.next());
        }
    }
}
