package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/FurnaceResultSlot.class */
public class FurnaceResultSlot extends Slot {
    private final Player player;
    private int removeCount;

    public FurnaceResultSlot(Player player, Container container, int i, int i2, int i3) {
        super(container, i, i2, i3);
        this.player = player;
    }

    @Override // net.minecraft.world.inventory.Slot
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override // net.minecraft.world.inventory.Slot
    public ItemStack remove(int i) {
        if (hasItem()) {
            this.removeCount += Math.min(i, getItem().getCount());
        }
        return super.remove(i);
    }

    @Override // net.minecraft.world.inventory.Slot
    public ItemStack onTake(Player player, ItemStack itemStack) {
        checkTakeAchievements(itemStack);
        super.onTake(player, itemStack);
        return itemStack;
    }

    @Override // net.minecraft.world.inventory.Slot
    protected void onQuickCraft(ItemStack itemStack, int i) {
        this.removeCount += i;
        checkTakeAchievements(itemStack);
    }

    @Override // net.minecraft.world.inventory.Slot
    protected void checkTakeAchievements(ItemStack itemStack) {
        itemStack.onCraftedBy(this.player.level, this.player, this.removeCount);
        if (!this.player.level.isClientSide && (this.container instanceof AbstractFurnaceBlockEntity)) {
            ((AbstractFurnaceBlockEntity) this.container).awardUsedRecipesAndPopExperience(this.player);
        }
        this.removeCount = 0;
    }
}
