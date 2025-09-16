package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SmeltingRecipe.class */
public class SmeltingRecipe extends AbstractCookingRecipe {
    public SmeltingRecipe(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(RecipeType.SMELTING, resourceLocation, str, ingredient, itemStack, f, i);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.FURNACE);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMELTING_RECIPE;
    }
}
