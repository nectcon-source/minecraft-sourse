package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/StonecutterRecipe.class */
public class StonecutterRecipe extends SingleItemRecipe {
    public StonecutterRecipe(ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack) {
        super(RecipeType.STONECUTTING, RecipeSerializer.STONECUTTER, resourceLocation, str, ingredient, itemStack);
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(Container container, Level level) {
        return this.ingredient.test(container.getItem(0));
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.STONECUTTER);
    }
}
