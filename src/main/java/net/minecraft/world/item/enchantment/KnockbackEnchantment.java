package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/KnockbackEnchantment.class */
public class KnockbackEnchantment extends Enchantment {
    protected KnockbackEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.WEAPON, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 5 + (20 * (i - 1));
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return super.getMinCost(i) + 50;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 2;
    }
}
