package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/ServerLevelData.class */
public interface ServerLevelData extends WritableLevelData {
    String getLevelName();

    void setThundering(boolean z);

    int getRainTime();

    void setRainTime(int i);

    void setThunderTime(int i);

    int getThunderTime();

    int getClearWeatherTime();

    void setClearWeatherTime(int i);

    int getWanderingTraderSpawnDelay();

    void setWanderingTraderSpawnDelay(int i);

    int getWanderingTraderSpawnChance();

    void setWanderingTraderSpawnChance(int i);

    void setWanderingTraderId(UUID uuid);

    GameType getGameType();

    void setWorldBorder(WorldBorder.Settings settings);

    WorldBorder.Settings getWorldBorder();

    boolean isInitialized();

    void setInitialized(boolean z);

    boolean getAllowCommands();

    void setGameType(GameType gameType);

    TimerQueue<MinecraftServer> getScheduledEvents();

    void setGameTime(long j);

    void setDayTime(long j);

    @Override // net.minecraft.world.level.storage.LevelData
    default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        WritableLevelData.super.fillCrashReportCategory(crashReportCategory);
        crashReportCategory.setDetail("Level name", this::getLevelName);
        crashReportCategory.setDetail("Level game mode", () -> {
            return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", getGameType().getName(), Integer.valueOf(getGameType().getId()), Boolean.valueOf(isHardcore()), Boolean.valueOf(getAllowCommands()));
        });
        crashReportCategory.setDetail("Level weather", () -> {
            return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", Integer.valueOf(getRainTime()), Boolean.valueOf(isRaining()), Integer.valueOf(getThunderTime()), Boolean.valueOf(isThundering()));
        });
    }
}
