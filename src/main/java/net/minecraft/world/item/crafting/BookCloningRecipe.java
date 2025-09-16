package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/BookCloningRecipe.class */
public class BookCloningRecipe extends CustomRecipe {
    public BookCloningRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                if (item.getItem() == Items.WRITTEN_BOOK) {
                    if (!itemStack.isEmpty()) {
                        return false;
                    }
                    itemStack = item;
                } else if (item.getItem() == Items.WRITABLE_BOOK) {
                    i++;
                } else {
                    return false;
                }
            }
        }
        return !itemStack.isEmpty() && itemStack.hasTag() && i > 0;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                if (item.getItem() == Items.WRITTEN_BOOK) {
                    if (!itemStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    itemStack = item;
                } else if (item.getItem() == Items.WRITABLE_BOOK) {
                    i++;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (itemStack.isEmpty() || !itemStack.hasTag() || i < 1 || WrittenBookItem.getGeneration(itemStack) >= 2) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack2 = new ItemStack(Items.WRITTEN_BOOK, i);
        CompoundTag copy = itemStack.getTag().copy();
        copy.putInt("generation", WrittenBookItem.getGeneration(itemStack) + 1);
        itemStack2.setTag(copy);
        return itemStack2;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
        NonNullList<ItemStack> withSize = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);
        int i = 0;
        while (true) {
            if (i >= withSize.size()) {
                break;
            }
            ItemStack item = craftingContainer.getItem(i);
            if (item.getItem().hasCraftingRemainingItem()) {
                withSize.set(i, new ItemStack(item.getItem().getCraftingRemainingItem()));
            } else if (item.getItem() instanceof WrittenBookItem) {
                ItemStack copy = item.copy();
                copy.setCount(1);
                withSize.set(i, copy);
                break;
            }
            i++;
        }
        return withSize;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i >= 3 && i2 >= 3;
    }
}
