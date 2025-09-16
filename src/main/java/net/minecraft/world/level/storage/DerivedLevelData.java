package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/DerivedLevelData.class */
public class DerivedLevelData implements ServerLevelData {
    private final WorldData worldData;
    private final ServerLevelData wrapped;

    public DerivedLevelData(WorldData worldData, ServerLevelData serverLevelData) {
        this.worldData = worldData;
        this.wrapped = serverLevelData;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public int getXSpawn() {
        return this.wrapped.getXSpawn();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public int getYSpawn() {
        return this.wrapped.getYSpawn();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public int getZSpawn() {
        return this.wrapped.getZSpawn();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public float getSpawnAngle() {
        return this.wrapped.getSpawnAngle();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public long getGameTime() {
        return this.wrapped.getGameTime();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public long getDayTime() {
        return this.wrapped.getDayTime();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public String getLevelName() {
        return this.worldData.getLevelName();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getClearWeatherTime() {
        return this.wrapped.getClearWeatherTime();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setClearWeatherTime(int i) {
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isThundering() {
        return this.wrapped.isThundering();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getThunderTime() {
        return this.wrapped.getThunderTime();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isRaining() {
        return this.wrapped.isRaining();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getRainTime() {
        return this.wrapped.getRainTime();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public GameType getGameType() {
        return this.worldData.getGameType();
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setXSpawn(int i) {
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setYSpawn(int i) {
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setZSpawn(int i) {
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setSpawnAngle(float f) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setGameTime(long j) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setDayTime(long j) {
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setSpawn(BlockPos blockPos, float f) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setThundering(boolean z) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setThunderTime(int i) {
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public void setRaining(boolean z) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setRainTime(int i) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setGameType(GameType gameType) {
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public boolean getAllowCommands() {
        return this.worldData.getAllowCommands();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public boolean isInitialized() {
        return this.wrapped.isInitialized();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setInitialized(boolean z) {
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public GameRules getGameRules() {
        return this.worldData.getGameRules();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public WorldBorder.Settings getWorldBorder() {
        return this.wrapped.getWorldBorder();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWorldBorder(WorldBorder.Settings settings) {
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public Difficulty getDifficulty() {
        return this.worldData.getDifficulty();
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isDifficultyLocked() {
        return this.worldData.isDifficultyLocked();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.wrapped.getScheduledEvents();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getWanderingTraderSpawnDelay() {
        return 0;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWanderingTraderSpawnDelay(int i) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getWanderingTraderSpawnChance() {
        return 0;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWanderingTraderSpawnChance(int i) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWanderingTraderId(UUID uuid) {
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData, net.minecraft.world.level.storage.LevelData
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Derived",  true);
        this.wrapped.fillCrashReportCategory(crashReportCategory);
    }
}
