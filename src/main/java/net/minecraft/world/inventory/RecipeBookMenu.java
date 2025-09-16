package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/RecipeBookMenu.class */
public abstract class RecipeBookMenu<C extends Container> extends AbstractContainerMenu {
    public abstract void fillCraftSlotsStackedContents(StackedContents stackedContents);

    public abstract void clearCraftingContent();

    public abstract boolean recipeMatches(Recipe<? super C> recipe);

    public abstract int getResultSlotIndex();

    public abstract int getGridWidth();

    public abstract int getGridHeight();

    public abstract int getSize();

    public abstract RecipeBookType getRecipeBookType();

    public RecipeBookMenu(MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    public void handlePlacement(boolean z, Recipe<?> recipe, ServerPlayer serverPlayer) {
        new ServerPlaceRecipe(this).recipeClicked(serverPlayer, recipe, z);
    }
}
