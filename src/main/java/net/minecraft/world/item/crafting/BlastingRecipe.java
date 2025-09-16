package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/BlastingRecipe.class */
public class BlastingRecipe extends AbstractCookingRecipe {
    public BlastingRecipe(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(RecipeType.BLASTING, resourceLocation, str, ingredient, itemStack, f, i);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.BLAST_FURNACE);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BLASTING_RECIPE;
    }
}
