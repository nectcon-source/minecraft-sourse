package net.minecraft.world.inventory;

import java.util.Map;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/AnvilMenu.class */
public class AnvilMenu extends ItemCombinerMenu {
    private static final Logger LOGGER = LogManager.getLogger();
    private int repairItemCountCost;
    private String itemName;
    private final DataSlot cost;

    public AnvilMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public AnvilMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.ANVIL, i, inventory, containerLevelAccess);
        this.cost = DataSlot.standalone();
        addDataSlot(this.cost);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(BlockTags.ANVIL);
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected boolean mayPickup(Player player, boolean z) {
        return (player.abilities.instabuild || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    protected ItemStack onTake(Player player, ItemStack itemStack) {
        if (!player.abilities.instabuild) {
            player.giveExperienceLevels(-this.cost.get());
        }
        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.repairItemCountCost > 0) {
            ItemStack item = this.inputSlots.getItem(1);
            if (!item.isEmpty() && item.getCount() > this.repairItemCountCost) {
                item.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, item);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }
        this.cost.set(0);
        this.access.execute((level, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            if (!player.abilities.instabuild && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12f) {
                BlockState damage = AnvilBlock.damage(blockState);
                if (damage == null) {
                    level.removeBlock(blockPos, false);
                    level.levelEvent(1029, blockPos, 0);
                    return;
                } else {
                    level.setBlock(blockPos, damage, 2);
                    level.levelEvent(1030, blockPos, 0);
                    return;
                }
            }
            level.levelEvent(1030, blockPos, 0);
        });
        return itemStack;
    }

    @Override // net.minecraft.world.inventory.ItemCombinerMenu
    public void createResult() {
        ItemStack item = this.inputSlots.getItem(0);
        this.cost.set(1);
        int i = 0;
        int i2 = 0;
        if (item.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }
        ItemStack copy = item.copy();
        ItemStack item2 = this.inputSlots.getItem(1);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(copy);
        int baseRepairCost = 0 + item.getBaseRepairCost() + (item2.isEmpty() ? 0 : item2.getBaseRepairCost());
        this.repairItemCountCost = 0;
        if (!item2.isEmpty()) {
            boolean z = item2.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(item2).isEmpty();
            if (copy.isDamageableItem() && copy.getItem().isValidRepairItem(item, item2)) {
                int min = Math.min(copy.getDamageValue(), copy.getMaxDamage() / 4);
                if (min <= 0) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }
                int i3 = 0;
                while (min > 0 && i3 < item2.getCount()) {
                    copy.setDamageValue(copy.getDamageValue() - min);
                    i++;
                    min = Math.min(copy.getDamageValue(), copy.getMaxDamage() / 4);
                    i3++;
                }
                this.repairItemCountCost = i3;
            } else {
                if (!z && (copy.getItem() != item2.getItem() || !copy.isDamageableItem())) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }
                if (copy.isDamageableItem() && !z) {
                    int maxDamage = copy.getMaxDamage() - ((item.getMaxDamage() - item.getDamageValue()) + ((item2.getMaxDamage() - item2.getDamageValue()) + ((copy.getMaxDamage() * 12) / 100)));
                    if (maxDamage < 0) {
                        maxDamage = 0;
                    }
                    if (maxDamage < copy.getDamageValue()) {
                        copy.setDamageValue(maxDamage);
                        i = 0 + 2;
                    }
                }
                Map<Enchantment, Integer> enchantments2 = EnchantmentHelper.getEnchantments(item2);
                boolean z2 = false;
                boolean z3 = false;
                for (Enchantment enchantment : enchantments2.keySet()) {
                    if (enchantment != null) {
                        int intValue = enchantments.getOrDefault(enchantment, 0).intValue();
                        int intValue2 = enchantments2.get(enchantment).intValue();
                        int max = intValue == intValue2 ? intValue2 + 1 : Math.max(intValue2, intValue);
                        boolean canEnchant = enchantment.canEnchant(item);
                        if (this.player.abilities.instabuild || item.getItem() == Items.ENCHANTED_BOOK) {
                            canEnchant = true;
                        }
                        for (Enchantment enchantment2 : enchantments.keySet()) {
                            if (enchantment2 != enchantment && !enchantment.isCompatibleWith(enchantment2)) {
                                canEnchant = false;
                                i++;
                            }
                        }
                        if (!canEnchant) {
                            z3 = true;
                        } else {
                            z2 = true;
                            if (max > enchantment.getMaxLevel()) {
                                max = enchantment.getMaxLevel();
                            }
                            enchantments.put(enchantment, Integer.valueOf(max));
                            int i4 = 0;
                            switch (enchantment.getRarity()) {
                                case COMMON:
                                    i4 = 1;
                                    break;
                                case UNCOMMON:
                                    i4 = 2;
                                    break;
                                case RARE:
                                    i4 = 4;
                                    break;
                                case VERY_RARE:
                                    i4 = 8;
                                    break;
                            }
                            if (z) {
                                i4 = Math.max(1, i4 / 2);
                            }
                            i += i4 * max;
                            if (item.getCount() > 1) {
                                i = 40;
                            }
                        }
                    }
                }
                if (z3 && !z2) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }
            }
        }
        if (StringUtils.isBlank(this.itemName)) {
            if (item.hasCustomHoverName()) {
                i2 = 1;
                i++;
                copy.resetHoverName();
            }
        } else if (!this.itemName.equals(item.getHoverName().getString())) {
            i2 = 1;
            i++;
            copy.setHoverName(new TextComponent(this.itemName));
        }
        this.cost.set(baseRepairCost + i);
        if (i <= 0) {
            copy = ItemStack.EMPTY;
        }
        if (i2 == i && i2 > 0 && this.cost.get() >= 40) {
            this.cost.set(39);
        }
        if (this.cost.get() >= 40 && !this.player.abilities.instabuild) {
            copy = ItemStack.EMPTY;
        }
        if (!copy.isEmpty()) {
            int baseRepairCost2 = copy.getBaseRepairCost();
            if (!item2.isEmpty() && baseRepairCost2 < item2.getBaseRepairCost()) {
                baseRepairCost2 = item2.getBaseRepairCost();
            }
            if (i2 != i || i2 == 0) {
                baseRepairCost2 = calculateIncreasedRepairCost(baseRepairCost2);
            }
            copy.setRepairCost(baseRepairCost2);
            EnchantmentHelper.setEnchantments(enchantments, copy);
        }
        this.resultSlots.setItem(0, copy);
        broadcastChanges();
    }

    public static int calculateIncreasedRepairCost(int i) {
        return (i * 2) + 1;
    }

    public void setItemName(String str) {
        this.itemName = str;
        if (getSlot(2).hasItem()) {
            ItemStack item = getSlot(2).getItem();
            if (StringUtils.isBlank(str)) {
                item.resetHoverName();
            } else {
                item.setHoverName(new TextComponent(this.itemName));
            }
        }
        createResult();
    }

    public int getCost() {
        return this.cost.get();
    }
}
