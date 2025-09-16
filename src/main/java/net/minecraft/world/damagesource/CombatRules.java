package net.minecraft.world.damagesource;

import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/damagesource/CombatRules.class */
public class CombatRules {
    public static float getDamageAfterAbsorb(float f, float f2, float f3) {
        return f * (1.0f - (Mth.clamp(f2 - (f / (2.0f + (f3 / 4.0f))), f2 * 0.2f, 20.0f) / 25.0f));
    }

    public static float getDamageAfterMagicAbsorb(float f, float f2) {
        return f * (1.0f - (Mth.clamp(f2, 0.0f, 20.0f) / 25.0f));
    }
}
