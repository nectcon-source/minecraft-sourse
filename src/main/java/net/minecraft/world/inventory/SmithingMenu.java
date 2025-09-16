package net.minecraft.world.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/SmithingMenu.class */
public class SmithingMenu extends ItemCombinerMenu {
    private final Level level;

    @Nullable
    private UpgradeRecipe selectedRecipe;
    private final List<UpgradeRecipe> recipes;

    public SmithingMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.SMITHING, i, inventory, containerLevelAccess);
        this.level = inventory.player.level;
        this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(Blocks.SMITHING_TABLE);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected boolean mayPickup(Player player, boolean z) {
        return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected ItemStack onTake(Player player, ItemStack itemStack) {
        itemStack.onCraftedBy(player.level, player, itemStack.getCount());
        this.resultSlots.awardUsedRecipes(player);
        shrinkStackInSlot(0);
        shrinkStackInSlot(1);
        this.access.execute((level, blockPos) -> {
            level.levelEvent(1044, blockPos, 0);
        });
        return itemStack;
    }

    private void shrinkStackInSlot(int i) {
        ItemStack item = this.inputSlots.getItem(i);
        item.shrink(1);
        this.inputSlots.setItem(i, item);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    public void createResult() {
        List<UpgradeRecipe> recipesFor = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
        if (recipesFor.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }
        this.selectedRecipe = recipesFor.get(0);
        ItemStack assemble = this.selectedRecipe.assemble(this.inputSlots);
        this.resultSlots.setRecipeUsed(this.selectedRecipe);
        this.resultSlots.setItem(0, assemble);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected boolean shouldQuickMoveToAdditionalSlot(ItemStack itemStack) {
        return this.recipes.stream().anyMatch(upgradeRecipe -> {
            return upgradeRecipe.isAdditionIngredient(itemStack);
        });
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }
}
