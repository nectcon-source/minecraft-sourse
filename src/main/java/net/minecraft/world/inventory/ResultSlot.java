package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ResultSlot.class */
public class ResultSlot extends Slot {
    private final CraftingContainer craftSlots;
    private final Player player;
    private int removeCount;

    public ResultSlot(Player player, CraftingContainer craftingContainer, Container container, int i, int i2, int i3) {
        super(container, i, i2, i3);
        this.player = player;
        this.craftSlots = craftingContainer;
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
    protected void onQuickCraft(ItemStack itemStack, int i) {
        this.removeCount += i;
        checkTakeAchievements(itemStack);
    }

    @Override // net.minecraft.world.inventory.Slot
    protected void onSwapCraft(int i) {
        this.removeCount += i;
    }

    @Override // net.minecraft.world.inventory.Slot
    protected void checkTakeAchievements(ItemStack itemStack) {
        if (this.removeCount > 0) {
            itemStack.onCraftedBy(this.player.level, this.player, this.removeCount);
        }
        if (this.container instanceof RecipeHolder) {
            ((RecipeHolder) this.container).awardUsedRecipes(this.player);
        }
        this.removeCount = 0;
    }

    @Override // net.minecraft.world.inventory.Slot
    public ItemStack onTake(Player player, ItemStack itemStack) {
        checkTakeAchievements(itemStack);
        NonNullList<ItemStack> remainingItemsFor = player.level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, player.level);
        for (int i = 0; i < remainingItemsFor.size(); i++) {
            ItemStack item = this.craftSlots.getItem(i);
            ItemStack itemStack2 = remainingItemsFor.get(i);
            if (!item.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                item = this.craftSlots.getItem(i);
            }
            if (!itemStack2.isEmpty()) {
                if (item.isEmpty()) {
                    this.craftSlots.setItem(i, itemStack2);
                } else if (ItemStack.isSame(item, itemStack2) && ItemStack.tagMatches(item, itemStack2)) {
                    itemStack2.grow(item.getCount());
                    this.craftSlots.setItem(i, itemStack2);
                } else if (!this.player.inventory.add(itemStack2)) {
                    this.player.drop(itemStack2, false);
                }
            }
        }
        return itemStack;
    }
}
