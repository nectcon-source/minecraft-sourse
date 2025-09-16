package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/InventoryMenu.class */
public class InventoryMenu extends RecipeBookMenu<CraftingContainer> {
    public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = {EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlot[] SLOT_IDS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots;
    public final boolean active;
    private final Player owner;

    public InventoryMenu(Inventory inventory, boolean z, Player player) {
        super(null, 0);
        this.craftSlots = new CraftingContainer(this, 2, 2);
        this.resultSlots = new ResultContainer();
        this.active = z;
        this.owner = player;
        addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));
        for (int i = 0; i < 2; i++) {
            for (int i2 = 0; i2 < 2; i2++) {
                addSlot(new Slot(this.craftSlots, i2 + (i * 2), 98 + (i2 * 18), 18 + (i * 18)));
            }
        }
        for (int i3 = 0; i3 < 4; i3++) {
            final EquipmentSlot equipmentSlot = SLOT_IDS[i3];
            addSlot(new Slot(inventory, 39 - i3, 8, 8 + (i3 * 18)) { // from class: net.minecraft.world.inventory.InventoryMenu.1
                @Override // net.minecraft.world.inventory.Slot
                public int getMaxStackSize() {
                    return 1;
                }

                @Override // net.minecraft.world.inventory.Slot
                public boolean mayPlace(ItemStack itemStack) {
                    return equipmentSlot == Mob.getEquipmentSlotForItem(itemStack);
                }

                @Override // net.minecraft.world.inventory.Slot
                public boolean mayPickup(Player player2) {
                    ItemStack item = getItem();
                    if (!item.isEmpty() && !player2.isCreative() && EnchantmentHelper.hasBindingCurse(item)) {
                        return false;
                    }
                    return super.mayPickup(player2);
                }

                @Override // net.minecraft.world.inventory.Slot
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.TEXTURE_EMPTY_SLOTS[equipmentSlot.getIndex()]);
                }
            });
        }
        for (int i4 = 0; i4 < 3; i4++) {
            for (int i5 = 0; i5 < 9; i5++) {
                addSlot(new Slot(inventory, i5 + ((i4 + 1) * 9), 8 + (i5 * 18), 84 + (i4 * 18)));
            }
        }
        for (int i6 = 0; i6 < 9; i6++) {
            addSlot(new Slot(inventory, i6, 8 + (i6 * 18), 142));
        }
        addSlot(new Slot(inventory, 40, 77, 62) { // from class: net.minecraft.world.inventory.InventoryMenu.2
            @Override // net.minecraft.world.inventory.Slot
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void fillCraftSlotsStackedContents(StackedContents stackedContents) {
        this.craftSlots.fillStackedContents(stackedContents);
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public void clearCraftingContent() {
        this.resultSlots.clearContent();
        this.craftSlots.clearContent();
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
        return recipe.matches(this.craftSlots, this.owner.level);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void slotsChanged(Container container) {
        CraftingMenu.slotChangedCraftingGrid(this.containerId, this.owner.level, this.owner, this.craftSlots, this.resultSlots);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.resultSlots.clearContent();
        if (player.level.isClientSide) {
            return;
        }
        clearContainer(player, player.level, this.craftSlots);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return true;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            EquipmentSlot equipmentSlotForItem = Mob.getEquipmentSlotForItem(itemStack);
            if (i == 0) {
                if (!moveItemStackTo(item, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (i >= 1 && i < 5) {
                if (!moveItemStackTo(item, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 5 && i < 9) {
                if (!moveItemStackTo(item, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentSlotForItem.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentSlotForItem.getIndex()).hasItem()) {
                int index = 8 - equipmentSlotForItem.getIndex();
                if (!moveItemStackTo(item, index, index + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentSlotForItem == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
                if (!moveItemStackTo(item, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 9 && i < 36) {
                if (!moveItemStackTo(item, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 36 && i < 45) {
                if (!moveItemStackTo(item, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(item, 9, 45, false)) {
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
        return 5;
    }

    public CraftingContainer getCraftSlots() {
        return this.craftSlots;
    }

    @Override // net.minecraft.world.inventory.RecipeBookMenu
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }
}
