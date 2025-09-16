package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/BindingCurseEnchantment.class */
public class BindingCurseEnchantment extends Enchantment {
    public BindingCurseEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.WEARABLE, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 25;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return 50;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 1;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isTreasureOnly() {
        return true;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isCurse() {
        return true;
    }
}
