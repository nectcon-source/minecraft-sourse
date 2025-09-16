package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/Slot.class */
public class Slot {
    private final int slot;
    public final Container container;
    public int index;

    /* renamed from: x */
    public final int x;

    /* renamed from: y */
    public final int y;

    public Slot(Container container, int i, int i2, int i3) {
        this.container = container;
        this.slot = i;
        this.x = i2;
        this.y = i3;
    }

    public void onQuickCraft(ItemStack itemStack, ItemStack itemStack2) {
        int count = itemStack2.getCount() - itemStack.getCount();
        if (count > 0) {
            onQuickCraft(itemStack2, count);
        }
    }

    protected void onQuickCraft(ItemStack itemStack, int i) {
    }

    protected void onSwapCraft(int i) {
    }

    protected void checkTakeAchievements(ItemStack itemStack) {
    }

    public ItemStack onTake(Player player, ItemStack itemStack) {
        setChanged();
        return itemStack;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return true;
    }

    public ItemStack getItem() {
        return this.container.getItem(this.slot);
    }

    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    public void set(ItemStack itemStack) {
        this.container.setItem(this.slot, itemStack);
        setChanged();
    }

    public void setChanged() {
        this.container.setChanged();
    }

    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack itemStack) {
        return getMaxStackSize();
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return null;
    }

    public ItemStack remove(int i) {
        return this.container.removeItem(this.slot, i);
    }

    public boolean mayPickup(Player player) {
        return true;
    }

    public boolean isActive() {
        return true;
    }
}
