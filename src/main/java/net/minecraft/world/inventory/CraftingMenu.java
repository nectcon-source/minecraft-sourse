package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/CraftingMenu.class */
public class CraftingMenu extends RecipeBookMenu<CraftingContainer> {
    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots;
    private final ContainerLevelAccess access;
    private final Player player;

    public CraftingMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public CraftingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.CRAFTING, i);
        this.craftSlots = new CraftingContainer(this, 3, 3);
        this.resultSlots = new ResultContainer();
        this.access = containerLevelAccess;
        this.player = inventory.player;
        addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 3; i3++) {
                addSlot(new Slot(this.craftSlots, i3 + (i2 * 3), 30 + (i3 * 18), 17 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 3; i4++) {
            for (int i5 = 0; i5 < 9; i5++) {
                addSlot(new Slot(inventory, i5 + (i4 * 9) + 9, 8 + (i5 * 18), 84 + (i4 * 18)));
            }
        }
        for (int i6 = 0; i6 < 9; i6++) {
            addSlot(new Slot(inventory, i6, 8 + (i6 * 18), 142));
        }
    }

    protected static void slotChangedCraftingGrid(int i, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer) {
        if (level.isClientSide) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ItemStack itemStack = ItemStack.EMPTY;
        Optional<CraftingRecipe> recipeFor = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, level);
        if (recipeFor.isPresent()) {
            CraftingRecipe craftingRecipe = recipeFor.get();
            if (resultContainer.setRecipeUsed(level, serverPlayer, craftingRecipe)) {
                itemStack = craftingRecipe.assemble(craftingContainer);
            }
        }
        resultContainer.setItem(0, itemStack);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(i, 0, itemStack));
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void slotsChanged(Container container) {
        this.access.execute((level, blockPos) -> {
            slotChangedCraftingGrid(this.containerId, level, this.player, this.craftSlots, this.resultSlots);
        });
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void fillCraftSlotsStackedContents(StackedContents stackedContents) {
        this.craftSlots.fillStackedContents(stackedContents);
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
        return recipe.matches(this.craftSlots, this.player.level);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            clearContainer(player, level, this.craftSlots);
        });
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.CRAFTING_TABLE);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if (i == 0) {
                this.access.execute((level, blockPos) -> {
                    item.getItem().onCraftedBy(item, level, player);
                });
                if (!moveItemStackTo(item, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (i >= 10 && i < 46) {
                if (!moveItemStackTo(item, 1, 10, false)) {
                    if (i < 37) {
                        if (!moveItemStackTo(item, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(item, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(item, 10, 46, false)) {
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
            ItemStack onTake = slot.onTake(player, item);
            if (i == 0) {
                player.drop(onTake, false);
            }
        }
        return itemStack;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getResultSlotIndex() {
        return 0;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public int getSize() {
        return 10;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }
}
