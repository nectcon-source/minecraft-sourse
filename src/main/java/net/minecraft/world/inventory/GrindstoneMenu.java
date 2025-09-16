package net.minecraft.world.inventory;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/GrindstoneMenu.class */
public class GrindstoneMenu extends AbstractContainerMenu {
    private final Container resultSlots;
    private final Container repairSlots;
    private final ContainerLevelAccess access;

    public GrindstoneMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public GrindstoneMenu(int i, Inventory inventory, final ContainerLevelAccess containerLevelAccess) {
        super(MenuType.GRINDSTONE, i);
        this.resultSlots = new ResultContainer();
        this.repairSlots = new SimpleContainer(2) { // from class: net.minecraft.world.inventory.GrindstoneMenu.1
            @Override // net.minecraft.world.SimpleContainer, net.minecraft.world.Container
            public void setChanged() {
                super.setChanged();
                GrindstoneMenu.this.slotsChanged(this);
            }
        };
        this.access = containerLevelAccess;
        addSlot(new Slot(this.repairSlots, 0, 49, 19) { // from class: net.minecraft.world.inventory.GrindstoneMenu.2
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.isDamageableItem() || itemStack.getItem() == Items.ENCHANTED_BOOK || itemStack.isEnchanted();
            }
        });
        addSlot(new Slot(this.repairSlots, 1, 49, 40) { // from class: net.minecraft.world.inventory.GrindstoneMenu.3
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.isDamageableItem() || itemStack.getItem() == Items.ENCHANTED_BOOK || itemStack.isEnchanted();
            }
        });
        addSlot(new Slot(this.resultSlots, 2, 129, 34) { // from class: net.minecraft.world.inventory.GrindstoneMenu.4
            @Override // net.minecraft.world.inventory.Slot
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override // net.minecraft.world.inventory.Slot
            public ItemStack onTake(Player player, ItemStack itemStack) {
                containerLevelAccess.execute((level, blockPos) -> {
                    int experienceAmount = getExperienceAmount(level);
                    while (experienceAmount > 0) {
                        int experienceValue = ExperienceOrb.getExperienceValue(experienceAmount);
                        experienceAmount -= experienceValue;
                        level.addFreshEntity(new ExperienceOrb(level, blockPos.getX(), blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, experienceValue));
                    }
                    level.levelEvent(1042, blockPos, 0);
                });
                GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
                GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
                return itemStack;
            }

            private int getExperienceAmount(Level level) {
                int experienceFromItem = 0 + getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0)) + getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1));
                if (experienceFromItem > 0) {
                    int ceil = (int) Math.ceil(experienceFromItem / 2.0d);
                    return ceil + level.random.nextInt(ceil);
                }
                return 0;
            }

            private int getExperienceFromItem(ItemStack itemStack) {
                int i2 = 0;
                for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(itemStack).entrySet()) {
                    Enchantment key = entry.getKey();
                    Integer value = entry.getValue();
                    if (!key.isCurse()) {
                        i2 += key.getMinCost(value.intValue());
                    }
                }
                return i2;
            }
        });
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
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.repairSlots) {
            createResult();
        }
    }

    private void createResult() {
        int damageValue;
        ItemStack itemStack;
        ItemStack item = this.repairSlots.getItem(0);
        ItemStack item2 = this.repairSlots.getItem(1);
        boolean z = (item.isEmpty() && item2.isEmpty()) ? false : true;
        boolean z2 = (item.isEmpty() || item2.isEmpty()) ? false : true;
        if (z) {
            boolean z3 = ((item.isEmpty() || item.getItem() == Items.ENCHANTED_BOOK || item.isEnchanted()) && (item2.isEmpty() || item2.getItem() == Items.ENCHANTED_BOOK || item2.isEnchanted())) ? false : true;
            if (item.getCount() > 1 || item2.getCount() > 1 || (!z2 && z3)) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                broadcastChanges();
                return;
            }
            int i = 1;
            if (z2) {
                if (item.getItem() == item2.getItem()) {
                    Item item3 = item.getItem();
                    damageValue = Math.max(item3.getMaxDamage() - (((item3.getMaxDamage() - item.getDamageValue()) + (item3.getMaxDamage() - item2.getDamageValue())) + ((item3.getMaxDamage() * 5) / 100)), 0);
                    itemStack = mergeEnchants(item, item2);
                    if (!itemStack.isDamageableItem()) {
                        if (!ItemStack.matches(item, item2)) {
                            this.resultSlots.setItem(0, ItemStack.EMPTY);
                            broadcastChanges();
                            return;
                        }
                        i = 2;
                    }
                } else {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
            } else {
                boolean z4 = !item.isEmpty();
                damageValue = z4 ? item.getDamageValue() : item2.getDamageValue();
                itemStack = z4 ? item : item2;
            }
            this.resultSlots.setItem(0, removeNonCurses(itemStack, damageValue, i));
        } else {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
        broadcastChanges();
    }

    private ItemStack mergeEnchants(ItemStack itemStack, ItemStack itemStack2) {
        ItemStack copy = itemStack.copy();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(itemStack2).entrySet()) {
            Enchantment key = entry.getKey();
            if (!key.isCurse() || EnchantmentHelper.getItemEnchantmentLevel(key, copy) == 0) {
                copy.enchant(key, entry.getValue().intValue());
            }
        }
        return copy;
    }

    private ItemStack removeNonCurses(ItemStack itemStack, int i, int i2) {
        ItemStack copy = itemStack.copy();
        copy.removeTagKey("Enchantments");
        copy.removeTagKey("StoredEnchantments");
        if (i > 0) {
            copy.setDamageValue(i);
        } else {
            copy.removeTagKey("Damage");
        }
        copy.setCount(i2);
        Map<Enchantment, Integer> map = (Map) EnchantmentHelper.getEnchantments(itemStack).entrySet().stream().filter(entry -> {
            return ((Enchantment) entry.getKey()).isCurse();
        }).collect(Collectors.toMap((v0) -> {
            return v0.getKey();
        }, (v0) -> {
            return v0.getValue();
        }));
        EnchantmentHelper.setEnchantments(map, copy);
        copy.setRepairCost(0);
        if (copy.getItem() == Items.ENCHANTED_BOOK && map.size() == 0) {
            copy = new ItemStack(Items.BOOK);
            if (itemStack.hasCustomHoverName()) {
                copy.setHoverName(itemStack.getHoverName());
            }
        }
        for (int i3 = 0; i3 < map.size(); i3++) {
            copy.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(copy.getBaseRepairCost()));
        }
        return copy;
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            clearContainer(player, level, this.repairSlots);
        });
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.GRINDSTONE);
    }

    @Override // net.minecraft.world.inventory.AbstractContainerMenu
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemStack = item.copy();
            ItemStack item2 = this.repairSlots.getItem(0);
            ItemStack item3 = this.repairSlots.getItem(1);
            if (i == 2) {
                if (!moveItemStackTo(item, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(item, itemStack);
            } else if (i == 0 || i == 1) {
                if (!moveItemStackTo(item, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (item2.isEmpty() || item3.isEmpty()) {
                if (!moveItemStackTo(item, 0, 2, false)) {
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
}
