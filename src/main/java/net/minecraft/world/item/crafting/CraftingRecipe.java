package net.minecraft.world.item.crafting;

import net.minecraft.world.inventory.CraftingContainer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/CraftingRecipe.class */
public interface CraftingRecipe extends Recipe<CraftingContainer> {
    @Override // net.minecraft.world.item.crafting.Recipe
    default RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}
