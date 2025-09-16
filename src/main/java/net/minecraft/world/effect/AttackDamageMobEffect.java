package net.minecraft.world.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/AttackDamageMobEffect.class */
public class AttackDamageMobEffect extends MobEffect {
    protected final double multiplier;

    protected AttackDamageMobEffect(MobEffectCategory mobEffectCategory, int i, double d) {
        super(mobEffectCategory, i);
        this.multiplier = d;
    }

    @Override // net.minecraft.world.effect.MobEffect
    public double getAttributeModifierValue(int i, AttributeModifier attributeModifier) {
        return this.multiplier * (i + 1);
    }
}
