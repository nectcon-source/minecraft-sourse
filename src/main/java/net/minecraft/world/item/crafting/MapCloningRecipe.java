package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/MapCloningRecipe.class */
public class MapCloningRecipe extends CustomRecipe {
    public MapCloningRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                if (item.getItem() == Items.FILLED_MAP) {
                    if (!itemStack.isEmpty()) {
                        return false;
                    }
                    itemStack = item;
                } else if (item.getItem() == Items.MAP) {
                    i++;
                } else {
                    return false;
                }
            }
        }
        return !itemStack.isEmpty() && i > 0;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(CraftingContainer craftingContainer) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i2 = 0; i2 < craftingContainer.getContainerSize(); i2++) {
            ItemStack item = craftingContainer.getItem(i2);
            if (!item.isEmpty()) {
                if (item.getItem() == Items.FILLED_MAP) {
                    if (!itemStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    itemStack = item;
                } else if (item.getItem() == Items.MAP) {
                    i++;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (itemStack.isEmpty() || i < 1) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = itemStack.copy();
        copy.setCount(i + 1);
        return copy;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return i >= 3 && i2 >= 3;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}
