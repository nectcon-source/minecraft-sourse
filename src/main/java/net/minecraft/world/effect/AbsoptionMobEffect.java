package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/AbsoptionMobEffect.class */
public class AbsoptionMobEffect extends MobEffect {
    protected AbsoptionMobEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override // net.minecraft.world.effect.MobEffect
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
        livingEntity.setAbsorptionAmount(livingEntity.getAbsorptionAmount() - (4 * (i + 1)));
        super.removeAttributeModifiers(livingEntity, attributeMap, i);
    }

    @Override // net.minecraft.world.effect.MobEffect
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
        livingEntity.setAbsorptionAmount(livingEntity.getAbsorptionAmount() + (4 * (i + 1)));
        super.addAttributeModifiers(livingEntity, attributeMap, i);
    }
}
