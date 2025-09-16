package net.minecraft.world.item;

import net.minecraft.world.item.crafting.Ingredient;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/Tier.class */
public interface Tier {
    int getUses();

    float getSpeed();

    float getAttackDamageBonus();

    int getLevel();

    int getEnchantmentValue();

    Ingredient getRepairIngredient();
}
