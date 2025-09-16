package net.minecraft.world.inventory;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ResultContainer.class */
public class ResultContainer implements Container, RecipeHolder {
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);

    @Nullable
    private Recipe<?> recipeUsed;

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return 1;
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.itemStacks.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        return this.itemStacks.get(0);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        return ContainerHelper.takeItem(this.itemStacks, 0);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.itemStacks, 0);
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        this.itemStacks.set(0, itemStack);
    }

    @Override // net.minecraft.world.Container
    public void setChanged() {
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        return true;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.itemStacks.clear();
    }

    @Override // net.minecraft.world.inventory.RecipeHolder
    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        this.recipeUsed = recipe;
    }

    @Override // net.minecraft.world.inventory.RecipeHolder
    @Nullable
    public Recipe<?> getRecipeUsed() {
        return this.recipeUsed;
    }
}
