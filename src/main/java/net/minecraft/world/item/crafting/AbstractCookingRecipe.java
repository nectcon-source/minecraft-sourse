package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/AbstractCookingRecipe.class */
public abstract class AbstractCookingRecipe implements Recipe<Container> {
    protected final RecipeType<?> type;

    /* renamed from: id */
    protected final ResourceLocation id;
    protected final String group;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final float experience;
    protected final int cookingTime;

    public AbstractCookingRecipe(RecipeType<?> recipeType, ResourceLocation resourceLocation, String str, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        this.type = recipeType;
        this.id = resourceLocation;
        this.group = str;
        this.ingredient = ingredient;
        this.result = itemStack;
        this.experience = f;
        this.cookingTime = i;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean matches(Container container, Level level) {
        return this.ingredient.test(container.getItem(0));
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack assemble(Container container) {
        return this.result.copy();
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean canCraftInDimensions(int i, int i2) {
        return true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> create = NonNullList.create();
        create.add(this.ingredient);
        return create;
    }

    public float getExperience() {
        return this.experience;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public String getGroup() {
        return this.group;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ResourceLocation getId() {
        return this.id;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public RecipeType<?> getType() {
        return this.type;
    }
}
