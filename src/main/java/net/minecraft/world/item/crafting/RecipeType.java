package net.minecraft.world.item.crafting;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/RecipeType.class */
public interface RecipeType<T extends Recipe<?>> {
    public static final RecipeType<CraftingRecipe> CRAFTING = register("crafting");
    public static final RecipeType<SmeltingRecipe> SMELTING = register("smelting");
    public static final RecipeType<BlastingRecipe> BLASTING = register("blasting");
    public static final RecipeType<SmokingRecipe> SMOKING = register("smoking");
    public static final RecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING = register("campfire_cooking");
    public static final RecipeType<StonecutterRecipe> STONECUTTING = register("stonecutting");
    public static final RecipeType<UpgradeRecipe> SMITHING = register("smithing");

    static <T extends Recipe<?>> RecipeType<T> register(final String str) {
        return  Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(str), new RecipeType<T>() { // from class: net.minecraft.world.item.crafting.RecipeType.1
            public String toString() {
                return str;
            }
        });
    }

    default <C extends Container> Optional<T> tryMatch(Recipe<C> recipe, Level level, C c) {
        return recipe.matches(c, level) ? (Optional<T>) Optional.of(recipe) : Optional.empty();
    }
}
