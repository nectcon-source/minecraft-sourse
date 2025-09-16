package net.minecraft.world.item;

import java.util.function.Supplier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.crafting.Ingredient;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/Tiers.class */
public enum Tiers implements Tier {
    WOOD(0, 59, 2.0f, 0.0f, 15, () -> {
        return Ingredient.of(ItemTags.PLANKS);
    }),
    STONE(1, 131, 4.0f, 1.0f, 5, () -> {
        return Ingredient.of(ItemTags.STONE_TOOL_MATERIALS);
    }),
    IRON(2, 250, 6.0f, 2.0f, 14, () -> {
        return Ingredient.of(Items.IRON_INGOT);
    }),
    DIAMOND(3, 1561, 8.0f, 3.0f, 10, () -> {
        return Ingredient.of(Items.DIAMOND);
    }),
    GOLD(0, 32, 12.0f, 0.0f, 22, () -> {
        return Ingredient.of(Items.GOLD_INGOT);
    }),
    NETHERITE(4, 2031, 9.0f, 4.0f, 15, () -> {
        return Ingredient.of(Items.NETHERITE_INGOT);
    });

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    Tiers(int i, int i2, float f, float f2, int i3, Supplier supplier) {
        this.level = i;
        this.uses = i2;
        this.speed = f;
        this.damage = f2;
        this.enchantmentValue = i3;
        this.repairIngredient = new LazyLoadedValue<>(supplier);
    }

    @Override // net.minecraft.world.item.Tier
    public int getUses() {
        return this.uses;
    }

    @Override // net.minecraft.world.item.Tier
    public float getSpeed() {
        return this.speed;
    }

    @Override // net.minecraft.world.item.Tier
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override // net.minecraft.world.item.Tier
    public int getLevel() {
        return this.level;
    }

    @Override // net.minecraft.world.item.Tier
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override // net.minecraft.world.item.Tier
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
