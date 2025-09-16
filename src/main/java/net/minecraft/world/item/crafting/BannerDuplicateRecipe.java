package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/BannerDuplicateRecipe.class */
public class BannerDuplicateRecipe extends CustomRecipe {
    public BannerDuplicateRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        DyeColor dyeColor = null;
        ItemStack itemStack = null;
        ItemStack itemStack2 = null;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            Item item2 = item.getItem();
            if (item2 instanceof BannerItem) {
                BannerItem bannerItem = (BannerItem) item2;
                if (dyeColor == null) {
                    dyeColor = bannerItem.getColor();
                } else if (dyeColor != bannerItem.getColor()) {
                    return false;
                }
                int patternCount = BannerBlockEntity.getPatternCount(item);
                if (patternCount > 6) {
                    return false;
                }
                if (patternCount > 0) {
                    if (itemStack == null) {
                        itemStack = item;
                    } else {
                        return false;
                    }
                } else if (itemStack2 == null) {
                    itemStack2 = item;
                } else {
                    return false;
                }
            }
        }
        return (itemStack == null || itemStack2 == null) ? false : true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        int patternCount;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty() && (patternCount = BannerBlockEntity.getPatternCount(item)) > 0 && patternCount <= 6) {
                ItemStack copy = item.copy();
                copy.setCount(1);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
        NonNullList<ItemStack> withSize = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < withSize.size(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem().hasCraftingRemainingItem()) {
                    withSize.set(i, new ItemStack(item.getItem().getCraftingRemainingItem()));
                } else if (item.hasTag() && BannerBlockEntity.getPatternCount(item) > 0) {
                    ItemStack copy = item.copy();
                    copy.setCount(1);
                    withSize.set(i, copy);
                }
            }
        }
        return withSize;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i * i2 >= 2;
    }
}
