package net.minecraft.world;

import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/Container.class */
public interface Container extends Clearable {
    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int i);

    ItemStack removeItem(int i, int i2);

    ItemStack removeItemNoUpdate(int i);

    void setItem(int i, ItemStack itemStack);

    void setChanged();

    boolean stillValid(Player player);

    default int getMaxStackSize() {
        return 64;
    }

    default void startOpen(Player player) {
    }

    default void stopOpen(Player player) {
    }

    default boolean canPlaceItem(int i, ItemStack itemStack) {
        return true;
    }

    default int countItem(Item item) {
        int i = 0;
        for (int i2 = 0; i2 < getContainerSize(); i2++) {
            ItemStack item2 = getItem(i2);
            if (item2.getItem().equals(item)) {
                i += item2.getCount();
            }
        }
        return i;
    }

    default boolean hasAnyOf(Set<Item> set) {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack item = getItem(i);
            if (set.contains(item.getItem()) && item.getCount() > 0) {
                return true;
            }
        }
        return false;
    }
}
