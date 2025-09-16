package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/SimpleContainer.class */
public class SimpleContainer implements Container, StackedContentsCompatible {
    private final int size;
    private final NonNullList<ItemStack> items;
    private List<ContainerListener> listeners;

    public SimpleContainer(int i) {
        this.size = i;
        this.items = NonNullList.withSize(i, ItemStack.EMPTY);
    }

    public SimpleContainer(ItemStack... itemStackArr) {
        this.size = itemStackArr.length;
        this.items = NonNullList.of(ItemStack.EMPTY, itemStackArr);
    }

    public void addListener(ContainerListener containerListener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }
        this.listeners.add(containerListener);
    }

    public void removeListener(ContainerListener containerListener) {
        this.listeners.remove(containerListener);
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        if (i < 0 || i >= this.items.size()) {
            return ItemStack.EMPTY;
        }
        return this.items.get(i);
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list =  this.items.stream().filter(itemStack -> {
            return !itemStack.isEmpty();
        }).collect(Collectors.toList());
        clearContent();
        return list;
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        ItemStack removeItem = ContainerHelper.removeItem(this.items, i, i2);
        if (!removeItem.isEmpty()) {
            setChanged();
        }
        return removeItem;
    }

    public ItemStack removeItemType(Item item, int i) {
        ItemStack itemStack = new ItemStack(item, 0);
        for (int i2 = this.size - 1; i2 >= 0; i2--) {
            ItemStack item2 = getItem(i2);
            if (item2.getItem().equals(item)) {
                itemStack.grow(item2.split(i - itemStack.getCount()).getCount());
                if (itemStack.getCount() == i) {
                    break;
                }
            }
        }
        if (!itemStack.isEmpty()) {
            setChanged();
        }
        return itemStack;
    }

    public ItemStack addItem(ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        moveItemToOccupiedSlotsWithSameType(copy);
        if (copy.isEmpty()) {
            return ItemStack.EMPTY;
        }
        moveItemToEmptySlots(copy);
        if (copy.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return copy;
    }

    public boolean canAddItem(ItemStack itemStack) {
        boolean z = false;
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            ItemStack next = it.next();
            if (next.isEmpty() || (isSameItem(next, itemStack) && next.getCount() < next.getMaxStackSize())) {
                z = true;
                break;
            }
        }
        return z;
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack itemStack = this.items.get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.items.set(i, ItemStack.EMPTY);
        return itemStack;
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.size;
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
    public void setChanged() {
        if (this.listeners != null) {
            Iterator<ContainerListener> it = this.listeners.iterator();
            while (it.hasNext()) {
                it.next().containerChanged(this);
            }
        }
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        return true;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.items.clear();
        setChanged();
    }

    @Override // net.minecraft.world.inventory.StackedContentsCompatible
    public void fillStackedContents(StackedContents stackedContents) {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            stackedContents.accountStack(it.next());
        }
    }

    public String toString() {
        return ( this.items.stream().filter(itemStack -> {
            return !itemStack.isEmpty();
        }).collect(Collectors.toList())).toString();
    }

    private void moveItemToEmptySlots(ItemStack itemStack) {
        for (int i = 0; i < this.size; i++) {
            if (getItem(i).isEmpty()) {
                setItem(i, itemStack.copy());
                itemStack.setCount(0);
                return;
            }
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack itemStack) {
        for (int i = 0; i < this.size; i++) {
            ItemStack item = getItem(i);
            if (isSameItem(item, itemStack)) {
                moveItemsBetweenStacks(itemStack, item);
                if (itemStack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
    }

    private void moveItemsBetweenStacks(ItemStack itemStack, ItemStack itemStack2) {
        int min = Math.min(itemStack.getCount(), Math.min(getMaxStackSize(), itemStack2.getMaxStackSize()) - itemStack2.getCount());
        if (min > 0) {
            itemStack2.grow(min);
            itemStack.shrink(min);
            setChanged();
        }
    }

    public void fromTag(ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            ItemStack m66of = ItemStack.of(listTag.getCompound(i));
            if (!m66of.isEmpty()) {
                addItem(m66of);
            }
        }
    }

    public ListTag createTag() {
        ListTag listTag = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack item = getItem(i);
            if (!item.isEmpty()) {
                listTag.add(item.save(new CompoundTag()));
            }
        }
        return listTag;
    }
}
