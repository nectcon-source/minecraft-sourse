package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;

@Immutable
/* loaded from: client_deobf_norm.jar:net/minecraft/world/DifficultyInstance.class */
public class DifficultyInstance {
    private final Difficulty base;
    private final float effectiveDifficulty;

    public DifficultyInstance(Difficulty difficulty, long j, long j2, float f) {
        this.base = difficulty;
        this.effectiveDifficulty = calculateDifficulty(difficulty, j, j2, f);
    }

    public Difficulty getDifficulty() {
        return this.base;
    }

    public float getEffectiveDifficulty() {
        return this.effectiveDifficulty;
    }

    public boolean isHarderThan(float f) {
        return this.effectiveDifficulty > f;
    }

    public float getSpecialMultiplier() {
        if (this.effectiveDifficulty < 2.0f) {
            return 0.0f;
        }
        if (this.effectiveDifficulty > 4.0f) {
            return 1.0f;
        }
        return (this.effectiveDifficulty - 2.0f) / 2.0f;
    }

    private float calculateDifficulty(Difficulty difficulty, long j, long j2, float f) {
        if (difficulty == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        boolean z = difficulty == Difficulty.HARD;
        float clamp = Mth.clamp((j - 72000.0f) / 1440000.0f, 0.0f, 1.0f) * 0.25f;
        float f2 = 0.75f + clamp;
        float clamp2 = 0.0f + (Mth.clamp(j2 / 3600000.0f, 0.0f, 1.0f) * (z ? 1.0f : 0.75f)) + Mth.clamp(f * 0.25f, 0.0f, clamp);
        if (difficulty == Difficulty.EASY) {
            clamp2 *= 0.5f;
        }
        return difficulty.getId() * (f2 + clamp2);
    }
}
