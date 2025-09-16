package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/SoulSpeedEnchantment.class */
public class SoulSpeedEnchantment extends Enchantment {
    public SoulSpeedEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.ARMOR_FEET, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return i * 10;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return getMinCost(i) + 15;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isTreasureOnly() {
        return true;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isTradeable() {
        return false;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isDiscoverable() {
        return false;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 3;
    }
}
