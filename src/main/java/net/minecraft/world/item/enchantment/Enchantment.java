package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/Enchantment.class */
public abstract class Enchantment {
    private final EquipmentSlot[] slots;
    private final Rarity rarity;
    public final EnchantmentCategory category;

    @Nullable
    protected String descriptionId;

    @Nullable
    public static Enchantment byId(int i) {
        return Registry.ENCHANTMENT.byId(i);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/Enchantment$Rarity.class */
    public enum Rarity {
        COMMON(10),
        UNCOMMON(5),
        RARE(2),
        VERY_RARE(1);

        private final int weight;

        Rarity(int i) {
            this.weight = i;
        }

        public int getWeight() {
            return this.weight;
        }
    }

    protected Enchantment(Rarity rarity, EnchantmentCategory enchantmentCategory, EquipmentSlot[] equipmentSlotArr) {
        this.rarity = rarity;
        this.category = enchantmentCategory;
        this.slots = equipmentSlotArr;
    }

    public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity livingEntity) {
        Map<EquipmentSlot, ItemStack> newEnumMap = Maps.newEnumMap(EquipmentSlot.class);
        for (EquipmentSlot equipmentSlot : this.slots) {
            ItemStack itemBySlot = livingEntity.getItemBySlot(equipmentSlot);
            if (!itemBySlot.isEmpty()) {
                newEnumMap.put(equipmentSlot, itemBySlot);
            }
        }
        return newEnumMap;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinCost(int i) {
        return 1 + (i * 10);
    }

    public int getMaxCost(int i) {
        return getMinCost(i) + 5;
    }

    public int getDamageProtection(int i, DamageSource damageSource) {
        return 0;
    }

    public float getDamageBonus(int i, MobType mobType) {
        return 0.0f;
    }

    public final boolean isCompatibleWith(Enchantment enchantment) {
        return checkCompatibility(enchantment) && enchantment.checkCompatibility(this);
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("enchantment", Registry.ENCHANTMENT.getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    public Component getFullname(int i) {
        MutableComponent translatableComponent = new TranslatableComponent(getDescriptionId());
        if (isCurse()) {
            translatableComponent.withStyle(ChatFormatting.RED);
        } else {
            translatableComponent.withStyle(ChatFormatting.GRAY);
        }
        if (i != 1 || getMaxLevel() != 1) {
            translatableComponent.append(" ").append(new TranslatableComponent("enchantment.level." + i));
        }
        return translatableComponent;
    }

    public boolean canEnchant(ItemStack itemStack) {
        return this.category.canEnchant(itemStack.getItem());
    }

    public void doPostAttack(LivingEntity livingEntity, Entity entity, int i) {
    }

    public void doPostHurt(LivingEntity livingEntity, Entity entity, int i) {
    }

    public boolean isTreasureOnly() {
        return false;
    }

    public boolean isCurse() {
        return false;
    }

    public boolean isTradeable() {
        return true;
    }

    public boolean isDiscoverable() {
        return true;
    }
}
