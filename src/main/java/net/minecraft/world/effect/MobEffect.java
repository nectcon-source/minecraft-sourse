package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/MobEffect.class */
public class MobEffect {
    private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
    private final MobEffectCategory category;
    private final int color;

    @Nullable
    private String descriptionId;

    @Nullable
    public static MobEffect byId(int i) {
        return Registry.MOB_EFFECT.byId(i);
    }

    public static int getId(MobEffect mobEffect) {
        return Registry.MOB_EFFECT.getId(mobEffect);
    }

    protected MobEffect(MobEffectCategory mobEffectCategory, int i) {
        this.category = mobEffectCategory;
        this.color = i;
    }

    public void applyEffectTick(LivingEntity livingEntity, int i) {
        if (this == MobEffects.REGENERATION) {
            if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
                livingEntity.heal(1.0f);
                return;
            }
            return;
        }
        if (this == MobEffects.POISON) {
            if (livingEntity.getHealth() > 1.0f) {
                livingEntity.hurt(DamageSource.MAGIC, 1.0f);
                return;
            }
            return;
        }
        if (this == MobEffects.WITHER) {
            livingEntity.hurt(DamageSource.WITHER, 1.0f);
            return;
        }
        if (this == MobEffects.HUNGER && (livingEntity instanceof Player)) {
            ((Player) livingEntity).causeFoodExhaustion(0.005f * (i + 1));
            return;
        }
        if (this == MobEffects.SATURATION && (livingEntity instanceof Player)) {
            if (!livingEntity.level.isClientSide) {
                ((Player) livingEntity).getFoodData().eat(i + 1, 1.0f);
            }
        } else if ((this == MobEffects.HEAL && !livingEntity.isInvertedHealAndHarm()) || (this == MobEffects.HARM && livingEntity.isInvertedHealAndHarm())) {
            livingEntity.heal(Math.max(4 << i, 0));
        } else if ((this == MobEffects.HARM && !livingEntity.isInvertedHealAndHarm()) || (this == MobEffects.HEAL && livingEntity.isInvertedHealAndHarm())) {
            livingEntity.hurt(DamageSource.MAGIC, 6 << i);
        }
    }

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int i, double d) {
        if ((this == MobEffects.HEAL && !livingEntity.isInvertedHealAndHarm()) || (this == MobEffects.HARM && livingEntity.isInvertedHealAndHarm())) {
            livingEntity.heal((int) ((d * (4 << i)) + 0.5d));
            return;
        }
        if ((this == MobEffects.HARM && !livingEntity.isInvertedHealAndHarm()) || (this == MobEffects.HEAL && livingEntity.isInvertedHealAndHarm())) {
            int i2 = (int) ((d * (6 << i)) + 0.5d);
            if (entity == null) {
                livingEntity.hurt(DamageSource.MAGIC, i2);
                return;
            } else {
                livingEntity.hurt(DamageSource.indirectMagic(entity, entity2), i2);
                return;
            }
        }
        applyEffectTick(livingEntity, i);
    }

    public boolean isDurationEffectTick(int i, int i2) {
        if (this == MobEffects.REGENERATION) {
            int i3 = 50 >> i2;
            return i3 <= 0 || i % i3 == 0;
        }
        if (this == MobEffects.POISON) {
            int i4 = 25 >> i2;
            return i4 <= 0 || i % i4 == 0;
        }
        if (this == MobEffects.WITHER) {
            int i5 = 40 >> i2;
            return i5 <= 0 || i % i5 == 0;
        }
        if (this == MobEffects.HUNGER) {
            return true;
        }
        return false;
    }

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("effect", Registry.MOB_EFFECT.getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return new TranslatableComponent(getDescriptionId());
    }

    public MobEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffect addAttributeModifier(Attribute attribute, String str, double d, AttributeModifier.Operation operation) {
        this.attributeModifiers.put(attribute, new AttributeModifier(UUID.fromString(str), (Supplier<String>) this::getDescriptionId, d, operation));
        return this;
    }

    public Map<Attribute, AttributeModifier> getAttributeModifiers() {
        return this.attributeModifiers;
    }

    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
        for (Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeMap2 = attributeMap.getInstance(entry.getKey());
            if (attributeMap2 != null) {
                attributeMap2.removeModifier(entry.getValue());
            }
        }
    }

    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
        for (Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeMap2 = attributeMap.getInstance(entry.getKey());
            if (attributeMap2 != null) {
                AttributeModifier value = entry.getValue();
                attributeMap2.removeModifier(value);
                attributeMap2.addPermanentModifier(new AttributeModifier(value.getId(), getDescriptionId() + " " + i, getAttributeModifierValue(i, value), value.getOperation()));
            }
        }
    }

    public double getAttributeModifierValue(int i, AttributeModifier attributeModifier) {
        return attributeModifier.getAmount() * (i + 1);
    }

    public boolean isBeneficial() {
        return this.category == MobEffectCategory.BENEFICIAL;
    }
}
