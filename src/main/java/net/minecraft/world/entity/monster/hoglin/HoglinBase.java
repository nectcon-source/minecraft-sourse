package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/hoglin/HoglinBase.class */
public interface HoglinBase {
    int getAttackAnimationRemainingTicks();

    static boolean hurtAndThrowTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        float f;
        float attributeValue = (float) livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (!livingEntity.isBaby() && ((int) attributeValue) > 0) {
            f = (attributeValue / 2.0f) + livingEntity.level.random.nextInt((int) attributeValue);
        } else {
            f = attributeValue;
        }
        boolean hurt = livingEntity2.hurt(DamageSource.mobAttack(livingEntity), f);
        if (hurt) {
            livingEntity.doEnchantDamageEffects(livingEntity, livingEntity2);
            if (!livingEntity.isBaby()) {
                throwTarget(livingEntity, livingEntity2);
            }
        }
        return hurt;
    }

    static void throwTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        double attributeValue = livingEntity.getAttributeValue(Attributes.ATTACK_KNOCKBACK) - livingEntity2.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (attributeValue <= 0.0d) {
            return;
        }
        Vec3 yRot = new Vec3(livingEntity2.getX() - livingEntity.getX(), 0.0d, livingEntity2.getZ() - livingEntity.getZ()).normalize().scale(attributeValue * ((livingEntity.level.random.nextFloat() * 0.5f) + 0.2f)).yRot(livingEntity.level.random.nextInt(21) - 10);
        livingEntity2.push(yRot.x, attributeValue * livingEntity.level.random.nextFloat() * 0.5d, yRot.z);
        livingEntity2.hurtMarked = true;
    }
}
