package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/CustomRecipe.class */
public abstract class CustomRecipe implements CraftingRecipe {

    /* renamed from: id */
    private final ResourceLocation id;

    public CustomRecipe(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ResourceLocation getId() {
        return this.id;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public boolean isSpecial() {
        return true;
    }

    @Override // net.minecraft.world.item.crafting.Recipe
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
