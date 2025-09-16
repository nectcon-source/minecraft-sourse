package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/WorldData.class */
public interface WorldData {
    DataPackConfig getDataPackConfig();

    void setDataPackConfig(DataPackConfig dataPackConfig);

    boolean wasModded();

    Set<String> getKnownServerBrands();

    void setModdedInfo(String str, boolean z);

    @Nullable
    CompoundTag getCustomBossEvents();

    void setCustomBossEvents(@Nullable CompoundTag compoundTag);

    ServerLevelData overworldData();

    LevelSettings getLevelSettings();

    CompoundTag createTag(RegistryAccess registryAccess, @Nullable CompoundTag compoundTag);

    boolean isHardcore();

    int getVersion();

    String getLevelName();

    GameType getGameType();

    void setGameType(GameType gameType);

    boolean getAllowCommands();

    Difficulty getDifficulty();

    void setDifficulty(Difficulty difficulty);

    boolean isDifficultyLocked();

    void setDifficultyLocked(boolean z);

    GameRules getGameRules();

    CompoundTag getLoadedPlayerTag();

    CompoundTag endDragonFightData();

    void setEndDragonFightData(CompoundTag compoundTag);

    WorldGenSettings worldGenSettings();

    Lifecycle worldGenSettingsLifecycle();

    default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Known server brands", () -> {
            return String.join(", ", getKnownServerBrands());
        });
        crashReportCategory.setDetail("Level was modded", () -> {
            return Boolean.toString(wasModded());
        });
        crashReportCategory.setDetail("Level storage version", () -> {
            int version = getVersion();
            return String.format("0x%05X - %s", Integer.valueOf(version), getStorageVersionName(version));
        });
    }

    default String getStorageVersionName(int i) {
        switch (i) {
            case 19132:
                return "McRegion";
            case 19133:
                return "Anvil";
            default:
                return "Unknown?";
        }
    }
}
