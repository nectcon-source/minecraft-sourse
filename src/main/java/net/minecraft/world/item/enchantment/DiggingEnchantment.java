package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/DiggingEnchantment.class */
public class DiggingEnchantment extends Enchantment {
    protected DiggingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.DIGGER, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 1 + (10 * (i - 1));
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return super.getMinCost(i) + 50;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 5;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean canEnchant(ItemStack itemStack) {
        if (itemStack.getItem() == Items.SHEARS) {
            return true;
        }
        return super.canEnchant(itemStack);
    }
}
