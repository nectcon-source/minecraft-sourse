package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/ContainerHelper.class */
public class ContainerHelper {
    public static ItemStack removeItem(List<ItemStack> list, int i, int i2) {
        if (i < 0 || i >= list.size() || list.get(i).isEmpty() || i2 <= 0) {
            return ItemStack.EMPTY;
        }
        return list.get(i).split(i2);
    }

    public static ItemStack takeItem(List<ItemStack> list, int i) {
        if (i < 0 || i >= list.size()) {
            return ItemStack.EMPTY;
        }
        return list.set(i, ItemStack.EMPTY);
    }

    public static CompoundTag saveAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList) {
        return saveAllItems(compoundTag, nonNullList, true);
    }

    public static CompoundTag saveAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList, boolean z) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < nonNullList.size(); i++) {
            ItemStack itemStack = nonNullList.get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putByte("Slot", (byte) i);
                itemStack.save(compoundTag2);
                listTag.add(compoundTag2);
            }
        }
        if (!listTag.isEmpty() || z) {
            compoundTag.put("Items", listTag);
        }
        return compoundTag;
    }

    public static void loadAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList) {
        ListTag list = compoundTag.getList("Items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            int i2 = compound.getByte("Slot") & 255;
            if (i2 >= 0 && i2 < nonNullList.size()) {
                nonNullList.set(i2, ItemStack.of(compound));
            }
        }
    }

    public static int clearOrCountMatchingItems(Container container, Predicate<ItemStack> predicate, int i, boolean z) {
        int i2 = 0;
        for (int i3 = 0; i3 < container.getContainerSize(); i3++) {
            ItemStack item = container.getItem(i3);
            int clearOrCountMatchingItems = clearOrCountMatchingItems(item, predicate, i - i2, z);
            if (clearOrCountMatchingItems > 0 && !z && item.isEmpty()) {
                container.setItem(i3, ItemStack.EMPTY);
            }
            i2 += clearOrCountMatchingItems;
        }
        return i2;
    }

    public static int clearOrCountMatchingItems(ItemStack itemStack, Predicate<ItemStack> predicate, int i, boolean z) {
        if (itemStack.isEmpty() || !predicate.test(itemStack)) {
            return 0;
        }
        if (z) {
            return itemStack.getCount();
        }
        int count = i < 0 ? itemStack.getCount() : Math.min(i, itemStack.getCount());
        itemStack.shrink(count);
        return count;
    }
}
