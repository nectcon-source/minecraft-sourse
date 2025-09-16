package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/SmokingRecipe.class */
public class SmokingRecipe extends AbstractCookingRecipe {
    public SmokingRecipe(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(RecipeType.SMOKING, resourceLocation, str, ingredient, itemStack, f, i);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMOKER);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMOKING_RECIPE;
    }
}
