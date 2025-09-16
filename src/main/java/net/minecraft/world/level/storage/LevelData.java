package net.minecraft.world.level.storage;

import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/LevelData.class */
public interface LevelData {
    int getXSpawn();

    int getYSpawn();

    int getZSpawn();

    float getSpawnAngle();

    long getGameTime();

    long getDayTime();

    boolean isThundering();

    boolean isRaining();

    void setRaining(boolean z);

    boolean isHardcore();

    GameRules getGameRules();

    Difficulty getDifficulty();

    boolean isDifficultyLocked();

    default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Level spawn location", () -> {
            return CrashReportCategory.formatLocation(getXSpawn(), getYSpawn(), getZSpawn());
        });
        crashReportCategory.setDetail("Level time", () -> {
            return String.format("%d game time, %d day time", Long.valueOf(getGameTime()), Long.valueOf(getDayTime()));
        });
    }
}
