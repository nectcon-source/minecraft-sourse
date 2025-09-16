package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/Recipe.class */
public interface Recipe<C extends Container> {
    boolean matches(C c, Level level);

    ItemStack assemble(C c);

    boolean canCraftInDimensions(int i, int i2);

    ItemStack getResultItem();

    ResourceLocation getId();

    RecipeSerializer<?> getSerializer();

    RecipeType<?> getType();

    default NonNullList<ItemStack> getRemainingItems(C c) {
        NonNullList<ItemStack> withSize = NonNullList.withSize(c.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < withSize.size(); i++) {
            Item item = c.getItem(i).getItem();
            if (item.hasCraftingRemainingItem()) {
                withSize.set(i, new ItemStack(item.getCraftingRemainingItem()));
            }
        }
        return withSize;
    }

    default NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    default boolean isSpecial() {
        return false;
    }

    default String getGroup() {
        return "";
    }

    default ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }
}
