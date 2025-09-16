package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/TridentImpalerEnchantment.class */
public class TridentImpalerEnchantment extends Enchantment {
    public TridentImpalerEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.TRIDENT, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 1 + ((i - 1) * 8);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return getMinCost(i) + 20;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 5;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public float getDamageBonus(int i, MobType mobType) {
        if (mobType == MobType.WATER) {
            return i * 2.5f;
        }
        return 0.0f;
    }
}
