package net.minecraft.world.item.enchantment;

import java.util.Map;
import java.util.Random;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/ThornsEnchantment.class */
public class ThornsEnchantment extends Enchantment {
    public ThornsEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.ARMOR_CHEST, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return 10 + (20 * (i - 1));
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
        if (itemStack.getItem() instanceof ArmorItem) {
            return true;
        }
        return super.canEnchant(itemStack);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public void doPostHurt(LivingEntity livingEntity, Entity entity, int i) {
        Random random = livingEntity.getRandom();
        Map.Entry<EquipmentSlot, ItemStack> randomItemWith = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, livingEntity);
        if (shouldHit(i, random)) {
            if (entity != null) {
                entity.hurt(DamageSource.thorns(livingEntity), getDamage(i, random));
            }
            if (randomItemWith != null) {
                randomItemWith.getValue().hurtAndBreak(2, livingEntity, livingEntity2 -> {
                    livingEntity2.broadcastBreakEvent((EquipmentSlot) randomItemWith.getKey());
                });
            }
        }
    }

    public static boolean shouldHit(int i, Random random) {
        return i > 0 && random.nextFloat() < 0.15f * ((float) i);
    }

    public static int getDamage(int i, Random random) {
        if (i > 10) {
            return i - 10;
        }
        return 1 + random.nextInt(4);
    }
}
