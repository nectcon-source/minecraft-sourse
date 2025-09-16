package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceSmeltingRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/AbstractFurnaceMenu.class */
public abstract class AbstractFurnaceMenu extends RecipeBookMenu<Container> {
    private final Container container;
    private final ContainerData data;
    protected final Level level;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;
    private final RecipeBookType recipeBookType;

    protected AbstractFurnaceMenu(MenuType<?> menuType, RecipeType<? extends AbstractCookingRecipe> recipeType, RecipeBookType recipeBookType, int i, Inventory inventory) {
        this(menuType, recipeType, recipeBookType, i, inventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    protected AbstractFurnaceMenu(MenuType<?> menuType, RecipeType<? extends AbstractCookingRecipe> recipeType, RecipeBookType recipeBookType, int i, Inventory inventory, Container container, ContainerData containerData) {
        super(menuType, i);
        this.recipeType = recipeType;
        this.recipeBookType = recipeBookType;
        checkContainerSize(container, 3);
        checkContainerDataCount(containerData, 4);
        this.container = container;
        this.data = containerData;
        this.level = inventory.player.level;
        addSlot(new Slot(container, 0, 56, 17));
        addSlot(new FurnaceFuelSlot(this, container, 1, 56, 53));
        addSlot(new FurnaceResultSlot(inventory.player, container, 2, 116, 35));
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new Slot(inventory, i3 + (i2 * 9) + 9, 8 + (i3 * 18), 84 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            addSlot(new Slot(inventory, i4, 8 + (i4 * 18), 142));
        }
        addDataSlots(containerData);
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void fillCraftSlotsStackedContents(StackedContents stackedContents) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible) this.container).fillStackedContents(stackedContents);
        }
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void clearCraftingContent() {
        this.container.clearContent();
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void handlePlacement(boolean z, Recipe<?> recipe, ServerPlayer serverPlayer) {
        new ServerPlaceSmeltingRecipe(this).recipeClicked(serverPlayer, recipe, z);
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public boolean recipeMatches(Recipe<? super Container> recipe) {
        return recipe.matches(this.container, this.level);
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getResultSlotIndex() {
        return 2;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getGridWidth() {
        return 1;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getGridHeight() {
        return 1;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getSize() {
        return 3;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i == 2) {
                if (!moveItemStackTo(item, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (i == 1 || i == 0) {
                if (!moveItemStackTo(item, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (canSmelt(item)) {
                if (!moveItemStackTo(item, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isFuel(item)) {
                if (!moveItemStackTo(item, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 3 && i < 30) {
                if (!moveItemStackTo(item, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 30 && i < 39 && !moveItemStackTo(item, 3, 30, false)) {
                return ItemStack.EMPTY;
            }
            if (item.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (item.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, item);
        }
        return itemStack;
    }

    protected boolean canSmelt(ItemStack itemStack) {
        return this.level.getRecipeManager().getRecipeFor(this.recipeType, new SimpleContainer(itemStack), this.level).isPresent();
    }

    protected boolean isFuel(ItemStack itemStack) {
        return AbstractFurnaceBlockEntity.isFuel(itemStack);
    }

    public int getBurnProgress() {
        int i = this.data.get(2);
        int i2 = this.data.get(3);
        if (i2 == 0 || i == 0) {
            return 0;
        }
        return (i * 24) / i2;
    }

    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) {
            i = 200;
        }
        return (this.data.get(0) * 13) / i;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }
}
