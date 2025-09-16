package net.minecraft.world.item.enchantment;

import java.util.Random;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/DigDurabilityEnchantment.class */
public class DigDurabilityEnchantment extends Enchantment {
    protected DigDurabilityEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.BREAKABLE, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 5 + ((i - 1) * 8);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return super.getMinCost(i) + 50;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 3;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean canEnchant(ItemStack itemStack) {
        if (itemStack.isDamageableItem()) {
            return true;
        }
        return super.canEnchant(itemStack);
    }

    public static boolean shouldIgnoreDurabilityDrop(ItemStack itemStack, int i, Random random) {
        return (!(itemStack.getItem() instanceof ArmorItem) || random.nextFloat() >= 0.6f) && random.nextInt(i + 1) > 0;
    }
}
