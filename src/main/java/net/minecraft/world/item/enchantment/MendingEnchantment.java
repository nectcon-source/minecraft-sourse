package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/MendingEnchantment.class */
public class MendingEnchantment extends Enchantment {
    public MendingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.BREAKABLE, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return i * 25;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return getMinCost(i) + 50;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isTreasureOnly() {
        return true;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 1;
    }
}
