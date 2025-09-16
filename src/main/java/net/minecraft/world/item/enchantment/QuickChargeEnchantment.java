package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/QuickChargeEnchantment.class */
public class QuickChargeEnchantment extends Enchantment {
    public QuickChargeEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.CROSSBOW, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 12 + ((i - 1) * 20);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return 50;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 3;
    }
}
