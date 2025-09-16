package net.minecraft.world.item.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/DamageEnchantment.class */
public class DamageEnchantment extends Enchantment {
    private static final String[] NAMES = {"all", "undead", "arthropods"};
    private static final int[] MIN_COST = {1, 5, 5};
    private static final int[] LEVEL_COST = {11, 8, 8};
    private static final int[] LEVEL_COST_SPAN = {20, 20, 20};
    public final int type;

    public DamageEnchantment(Enchantment.Rarity rarity, int i, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.WEAPON, equipmentSlotArr);
        this.type = i;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return MIN_COST[this.type] + ((i - 1) * LEVEL_COST[this.type]);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return getMinCost(i) + LEVEL_COST_SPAN[this.type];
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 5;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public float getDamageBonus(int i, MobType mobType) {
        if (this.type == 0) {
            return 1.0f + (Math.max(0, i - 1) * 0.5f);
        }
        if (this.type == 1 && mobType == MobType.UNDEAD) {
            return i * 2.5f;
        }
        if (this.type == 2 && mobType == MobType.ARTHROPOD) {
            return i * 2.5f;
        }
        return 0.0f;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean checkCompatibility(Enchantment enchantment) {
        return !(enchantment instanceof DamageEnchantment);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean canEnchant(ItemStack itemStack) {
        if (itemStack.getItem() instanceof AxeItem) {
            return true;
        }
        return super.canEnchant(itemStack);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public void doPostAttack(LivingEntity livingEntity, Entity entity, int i) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) entity;
            if (this.type == 2 && livingEntity2.getMobType() == MobType.ARTHROPOD) {
                livingEntity2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 + livingEntity.getRandom().nextInt(10 * i), 3));
            }
        }
    }
}
