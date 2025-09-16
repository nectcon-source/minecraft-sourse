package net.minecraft.world.item;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ArmorMaterials.class */
public enum ArmorMaterials implements ArmorMaterial {
    LEATHER("leather", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, () -> {
        return Ingredient.of(Items.LEATHER);
    }),
    CHAIN("chainmail", 15, new int[]{1, 4, 5, 2}, 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0f, 0.0f, () -> {
        return Ingredient.of(Items.IRON_INGOT);
    }),
    IRON("iron", 15, new int[]{2, 5, 6, 2}, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.0f, () -> {
        return Ingredient.of(Items.IRON_INGOT);
    }),
    GOLD("gold", 7, new int[]{1, 3, 5, 2}, 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0f, 0.0f, () -> {
        return Ingredient.of(Items.GOLD_INGOT);
    }),
    DIAMOND("diamond", 33, new int[]{3, 6, 8, 3}, 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0f, 0.0f, () -> {
        return Ingredient.of(Items.DIAMOND);
    }),
    TURTLE("turtle", 25, new int[]{2, 5, 6, 2}, 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0f, 0.0f, () -> {
        return Ingredient.of(Items.SCUTE);
    }),
    NETHERITE("netherite", 37, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f, () -> {
        return Ingredient.of(Items.NETHERITE_INGOT);
    });

    private static final int[] HEALTH_PER_SLOT = {13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ArmorMaterials(String str, int i, int[] iArr, int i2, SoundEvent soundEvent, float f, float f2, Supplier supplier) {
        this.name = str;
        this.durabilityMultiplier = i;
        this.slotProtections = iArr;
        this.enchantmentValue = i2;
        this.sound = soundEvent;
        this.toughness = f;
        this.knockbackResistance = f2;
        this.repairIngredient = new LazyLoadedValue<>(supplier);
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public int getDurabilityForSlot(EquipmentSlot equipmentSlot) {
        return HEALTH_PER_SLOT[equipmentSlot.getIndex()] * this.durabilityMultiplier;
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public int getDefenseForSlot(EquipmentSlot equipmentSlot) {
        return this.slotProtections[equipmentSlot.getIndex()];
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public SoundEvent getEquipSound() {
        return this.sound;
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public String getName() {
        return this.name;
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public float getToughness() {
        return this.toughness;
    }

    @Override // net.minecraft.world.item.ArmorMaterial
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
