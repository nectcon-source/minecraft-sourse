package net.minecraft.world.level;

import net.minecraft.world.level.dimension.DimensionType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/LevelTimeAccess.class */
public interface LevelTimeAccess extends LevelReader {
    long dayTime();

    default float getMoonBrightness() {
        return DimensionType.MOON_BRIGHTNESS_PER_PHASE[dimensionType().moonPhase(dayTime())];
    }

    default float getTimeOfDay(float f) {
        return dimensionType().timeOfDay(dayTime());
    }

    default int getMoonPhase() {
        return dimensionType().moonPhase(dayTime());
    }
}
