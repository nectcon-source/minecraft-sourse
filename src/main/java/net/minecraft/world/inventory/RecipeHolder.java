package net.minecraft.world.inventory;

import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/RecipeHolder.class */
public interface RecipeHolder {
    void setRecipeUsed(@Nullable Recipe<?> recipe);

    @Nullable
    Recipe<?> getRecipeUsed();

    default void awardUsedRecipes(Player player) {
        Recipe<?> recipeUsed = getRecipeUsed();
        if (recipeUsed != null && !recipeUsed.isSpecial()) {
            player.awardRecipes(Collections.singleton(recipeUsed));
            setRecipeUsed(null);
        }
    }

    default boolean setRecipeUsed(Level level, ServerPlayer serverPlayer, Recipe<?> recipe) {
        if (recipe.isSpecial() || !level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || serverPlayer.getRecipeBook().contains(recipe)) {
            setRecipeUsed(recipe);
            return true;
        }
        return false;
    }
}
