package net.minecraft.world.effect;

import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/MobEffectUtil.class */
public final class MobEffectUtil {
    public static String formatDuration(MobEffectInstance mobEffectInstance, float f) {
        if (mobEffectInstance.isNoCounter()) {
            return "**:**";
        }
        return StringUtil.formatTickDuration(Mth.floor(mobEffectInstance.getDuration() * f));
    }

    public static boolean hasDigSpeed(LivingEntity livingEntity) {
        return livingEntity.hasEffect(MobEffects.DIG_SPEED) || livingEntity.hasEffect(MobEffects.CONDUIT_POWER);
    }

    public static int getDigSpeedAmplification(LivingEntity livingEntity) {
        int i = 0;
        int i2 = 0;
        if (livingEntity.hasEffect(MobEffects.DIG_SPEED)) {
            i = livingEntity.getEffect(MobEffects.DIG_SPEED).getAmplifier();
        }
        if (livingEntity.hasEffect(MobEffects.CONDUIT_POWER)) {
            i2 = livingEntity.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
        }
        return Math.max(i, i2);
    }

    public static boolean hasWaterBreathing(LivingEntity livingEntity) {
        return livingEntity.hasEffect(MobEffects.WATER_BREATHING) || livingEntity.hasEffect(MobEffects.CONDUIT_POWER);
    }
}
