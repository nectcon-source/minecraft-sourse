package net.minecraft.world.inventory;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/BrewingStandMenu.class */
public class BrewingStandMenu extends AbstractContainerMenu {
    private final Container brewingStand;
    private final ContainerData brewingStandData;
    private final Slot ingredientSlot;

    public BrewingStandMenu(int i, Inventory inventory) {
        this(i, inventory, new SimpleContainer(5), new SimpleContainerData(2));
    }

    public BrewingStandMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
        super(MenuType.BREWING_STAND, i);
        checkContainerSize(container, 5);
        checkContainerDataCount(containerData, 2);
        this.brewingStand = container;
        this.brewingStandData = containerData;
        addSlot(new PotionSlot(container, 0, 56, 51));
        addSlot(new PotionSlot(container, 1, 79, 58));
        addSlot(new PotionSlot(container, 2, 102, 51));
        this.ingredientSlot = addSlot(new IngredientsSlot(container, 3, 79, 17));
        addSlot(new FuelSlot(container, 4, 17, 17));
        addDataSlots(containerData);
        for (int i2 = 0; i2 < 3; i2++) {
            for (int i3 = 0; i3 < 9; i3++) {
                addSlot(new Slot(inventory, i3 + (i2 * 9) + 9, 8 + (i3 * 18), 84 + (i2 * 18)));
            }
        }
        for (int i4 = 0; i4 < 9; i4++) {
            addSlot(new Slot(inventory, i4, 8 + (i4 * 18), 142));
        }
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return this.brewingStand.stillValid(player);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            if ((i >= 0 && i <= 2) || i == 3 || i == 4) {
                if (!moveItemStackTo(item, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (FuelSlot.mayPlaceItem(itemStack)) {
                if (moveItemStackTo(item, 4, 5, false) || (this.ingredientSlot.mayPlace(item) && !moveItemStackTo(item, 3, 4, false))) {
                    return ItemStack.EMPTY;
                }
            } else if (this.ingredientSlot.mayPlace(item)) {
                if (!moveItemStackTo(item, 3, 4, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (PotionSlot.mayPlaceItem(itemStack) && itemStack.getCount() == 1) {
                if (!moveItemStackTo(item, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 5 && i < 32) {
                if (!moveItemStackTo(item, 32, 41, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= 32 && i < 41) {
                if (!moveItemStackTo(item, 5, 32, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(item, 5, 41, false)) {
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

    public int getFuel() {
        return this.brewingStandData.get(1);
    }

    public int getBrewingTicks() {
        return this.brewingStandData.get(0);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/BrewingStandMenu$PotionSlot.class */
    static class PotionSlot extends Slot {
        public PotionSlot(Container container, int i, int i2, int i3) {
            super(container, i, i2, i3);
        }

        @Override // net.minecraft.world.inventory.Slot
        public boolean mayPlace(ItemStack itemStack) {
            return mayPlaceItem(itemStack);
        }

        @Override // net.minecraft.world.inventory.Slot
        public int getMaxStackSize() {
            return 1;
        }

        @Override // net.minecraft.world.inventory.Slot
        public ItemStack onTake(Player player, ItemStack itemStack) {
            Potion potion = PotionUtils.getPotion(itemStack);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.BREWED_POTION.trigger((ServerPlayer) player, potion);
            }
            super.onTake(player, itemStack);
            return itemStack;
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            Item item = itemStack.getItem();
            return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/BrewingStandMenu$IngredientsSlot.class */
    static class IngredientsSlot extends Slot {
        public IngredientsSlot(Container container, int i, int i2, int i3) {
            super(container, i, i2, i3);
        }

        @Override // net.minecraft.world.inventory.Slot
        public boolean mayPlace(ItemStack itemStack) {
            return PotionBrewing.isIngredient(itemStack);
        }

        @Override // net.minecraft.world.inventory.Slot
        public int getMaxStackSize() {
            return 64;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/BrewingStandMenu$FuelSlot.class */
    static class FuelSlot extends Slot {
        public FuelSlot(Container container, int i, int i2, int i3) {
            super(container, i, i2, i3);
        }

        @Override // net.minecraft.world.inventory.Slot
        public boolean mayPlace(ItemStack itemStack) {
            return mayPlaceItem(itemStack);
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.getItem() == Items.BLAZE_POWDER;
        }

        @Override // net.minecraft.world.inventory.Slot
        public int getMaxStackSize() {
            return 64;
        }
    }
}
