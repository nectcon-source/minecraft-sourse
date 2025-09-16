package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/CampfireCookingRecipe.class */
public class CampfireCookingRecipe extends AbstractCookingRecipe {
    public CampfireCookingRecipe(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(RecipeType.CAMPFIRE_COOKING, resourceLocation, str, ingredient, itemStack, f, i);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CAMPFIRE);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
    }
}
