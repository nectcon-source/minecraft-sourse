package net.minecraft.world.item.enchantment;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/ProtectionEnchantment.class */
public class ProtectionEnchantment extends Enchantment {
    public final Type type;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/ProtectionEnchantment$Type.class */
    public enum Type {
        ALL("all", 1, 11),
        FIRE("fire", 10, 8),
        FALL("fall", 5, 6),
        EXPLOSION("explosion", 5, 8),
        PROJECTILE("projectile", 3, 6);

        private final String name;
        private final int minCost;
        private final int levelCost;

        Type(String str, int i, int i2) {
            this.name = str;
            this.minCost = i;
            this.levelCost = i2;
        }

        public int getMinCost() {
            return this.minCost;
        }

        public int getLevelCost() {
            return this.levelCost;
        }
    }

    public ProtectionEnchantment(Enchantment.Rarity rarity, Type type, EquipmentSlot... equipmentSlotArr) {
        super(rarity, type == Type.FALL ? EnchantmentCategory.ARMOR_FEET : EnchantmentCategory.ARMOR, equipmentSlotArr);
        this.type = type;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return this.type.getMinCost() + ((i - 1) * this.type.getLevelCost());
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return getMinCost(i) + this.type.getLevelCost();
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 4;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getDamageProtection(int i, DamageSource damageSource) {
        if (damageSource.isBypassInvul()) {
            return 0;
        }
        if (this.type == Type.ALL) {
            return i;
        }
        if (this.type == Type.FIRE && damageSource.isFire()) {
            return i * 2;
        }
        if (this.type == Type.FALL && damageSource == DamageSource.FALL) {
            return i * 3;
        }
        if (this.type == Type.EXPLOSION && damageSource.isExplosion()) {
            return i * 2;
        }
        if (this.type == Type.PROJECTILE && damageSource.isProjectile()) {
            return i * 2;
        }
        return 0;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean checkCompatibility(Enchantment enchantment) {
        if (enchantment instanceof ProtectionEnchantment) {
            ProtectionEnchantment protectionEnchantment = (ProtectionEnchantment) enchantment;
            if (this.type == protectionEnchantment.type) {
                return false;
            }
            return this.type == Type.FALL || protectionEnchantment.type == Type.FALL;
        }
        return super.checkCompatibility(enchantment);
    }

    public static int getFireAfterDampener(LivingEntity livingEntity, int i) {
        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, livingEntity);
        if (enchantmentLevel > 0) {
            i -= Mth.floor(i * (enchantmentLevel * 0.15f));
        }
        return i;
    }

    public static double getExplosionKnockbackAfterDampener(LivingEntity livingEntity, double var2) {
        int var3 = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, livingEntity);
        if (var3 > 0) {
            var2 -= (double)Mth.floor(var2 * (double)((float)var3 * 0.15F));
        }

        return var2;
    }
}
