package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.block.Block;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/EnchantmentCategory.class */
public enum EnchantmentCategory {
    ARMOR { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.1
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof ArmorItem;
        }
    },
    ARMOR_FEET { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.2
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return (item instanceof ArmorItem) && ((ArmorItem) item).getSlot() == EquipmentSlot.FEET;
        }
    },
    ARMOR_LEGS { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.3
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return (item instanceof ArmorItem) && ((ArmorItem) item).getSlot() == EquipmentSlot.LEGS;
        }
    },
    ARMOR_CHEST { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.4
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return (item instanceof ArmorItem) && ((ArmorItem) item).getSlot() == EquipmentSlot.CHEST;
        }
    },
    ARMOR_HEAD { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.5
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return (item instanceof ArmorItem) && ((ArmorItem) item).getSlot() == EquipmentSlot.HEAD;
        }
    },
    WEAPON { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.6
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof SwordItem;
        }
    },
    DIGGER { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.7
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof DiggerItem;
        }
    },
    FISHING_ROD { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.8
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof FishingRodItem;
        }
    },
    TRIDENT { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.9
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof TridentItem;
        }
    },
    BREAKABLE { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.10
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item.canBeDepleted();
        }
    },
    BOW { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.11
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof BowItem;
        }
    },
    WEARABLE { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.12
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return (item instanceof Wearable) || (Block.byItem(item) instanceof Wearable);
        }
    },
    CROSSBOW { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.13
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return item instanceof CrossbowItem;
        }
    },
    VANISHABLE { // from class: net.minecraft.world.item.enchantment.EnchantmentCategory.14
        @Override // net.minecraft.world.item.enchantment.EnchantmentCategory
        public boolean canEnchant(Item item) {
            return (item instanceof Vanishable) || (Block.byItem(item) instanceof Vanishable) || BREAKABLE.canEnchant(item);
        }
    };

    public abstract boolean canEnchant(Item item);
}
