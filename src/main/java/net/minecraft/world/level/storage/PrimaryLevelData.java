package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/PrimaryLevelData.class */
public class PrimaryLevelData implements ServerLevelData, WorldData {
    private static final Logger LOGGER = LogManager.getLogger();
    private LevelSettings settings;
    private final WorldGenSettings worldGenSettings;
    private final Lifecycle worldGenSettingsLifecycle;
    private int xSpawn;
    private int ySpawn;
    private int zSpawn;
    private float spawnAngle;
    private long gameTime;
    private long dayTime;

    @Nullable
    private final DataFixer fixerUpper;
    private final int playerDataVersion;
    private boolean upgradedPlayerTag;

    @Nullable
    private CompoundTag loadedPlayerTag;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private boolean difficultyLocked;
    private WorldBorder.Settings worldBorder;
    private CompoundTag endDragonFightData;

    @Nullable
    private CompoundTag customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;

    @Nullable
    private UUID wanderingTraderId;
    private final Set<String> knownServerBrands;
    private boolean wasModded;
    private final TimerQueue<MinecraftServer> scheduledEvents;

    private PrimaryLevelData(@Nullable DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag, boolean z, int i2, int i3, int i4, float f, long j, long j2, int i5, int i6, int i7, boolean z2, int i8, boolean z3, boolean z4, boolean z5, WorldBorder.Settings settings, int i9, int i10, @Nullable UUID uuid, LinkedHashSet<String> linkedHashSet, TimerQueue<MinecraftServer> timerQueue, @Nullable CompoundTag compoundTag2, CompoundTag compoundTag3, LevelSettings levelSettings, WorldGenSettings worldGenSettings, Lifecycle lifecycle) {
        this.fixerUpper = dataFixer;
        this.wasModded = z;
        this.xSpawn = i2;
        this.ySpawn = i3;
        this.zSpawn = i4;
        this.spawnAngle = f;
        this.gameTime = j;
        this.dayTime = j2;
        this.version = i5;
        this.clearWeatherTime = i6;
        this.rainTime = i7;
        this.raining = z2;
        this.thunderTime = i8;
        this.thundering = z3;
        this.initialized = z4;
        this.difficultyLocked = z5;
        this.worldBorder = settings;
        this.wanderingTraderSpawnDelay = i9;
        this.wanderingTraderSpawnChance = i10;
        this.wanderingTraderId = uuid;
        this.knownServerBrands = linkedHashSet;
        this.loadedPlayerTag = compoundTag;
        this.playerDataVersion = i;
        this.scheduledEvents = timerQueue;
        this.customBossEvents = compoundTag2;
        this.endDragonFightData = compoundTag3;
        this.settings = levelSettings;
        this.worldGenSettings = worldGenSettings;
        this.worldGenSettingsLifecycle = lifecycle;
    }

    public PrimaryLevelData(LevelSettings levelSettings, WorldGenSettings worldGenSettings, Lifecycle lifecycle) {
        this(null, SharedConstants.getCurrentVersion().getWorldVersion(), null, false, 0, 0, 0, 0.0f, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, null, Sets.newLinkedHashSet(), new TimerQueue(TimerCallbacks.SERVER_CALLBACKS), null, new CompoundTag(), levelSettings.copy(), worldGenSettings, lifecycle);
    }

    public static PrimaryLevelData parse(Dynamic<Tag> dynamic, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag, LevelSettings levelSettings, LevelVersion levelVersion, WorldGenSettings worldGenSettings, Lifecycle lifecycle) {
        long asLong = dynamic.get("Time").asLong(0L);
        return new PrimaryLevelData(dataFixer, i, compoundTag, dynamic.get("WasModded").asBoolean(false), dynamic.get("SpawnX").asInt(0), dynamic.get("SpawnY").asInt(0), dynamic.get("SpawnZ").asInt(0), dynamic.get("SpawnAngle").asFloat(0.0f), asLong, dynamic.get("DayTime").asLong(asLong), levelVersion.levelDataVersion(), dynamic.get("clearWeatherTime").asInt(0), dynamic.get("rainTime").asInt(0), dynamic.get("raining").asBoolean(false), dynamic.get("thunderTime").asInt(0), dynamic.get("thundering").asBoolean(false), dynamic.get("initialized").asBoolean(true), dynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Settings.read(dynamic, WorldBorder.DEFAULT_SETTINGS), dynamic.get("WanderingTraderSpawnDelay").asInt(0), dynamic.get("WanderingTraderSpawnChance").asInt(0), (UUID) dynamic.get("WanderingTraderId").read(SerializableUUID.CODEC).result().orElse(null), (LinkedHashSet) dynamic.get("ServerBrands").asStream().flatMap(dynamic2 -> {
            return Util.toStream(dynamic2.asString().result());
        }).collect(Collectors.toCollection(Sets::newLinkedHashSet)), new TimerQueue(TimerCallbacks.SERVER_CALLBACKS, dynamic.get("ScheduledEvents").asStream()), (CompoundTag) dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), (CompoundTag) dynamic.get("DragonFight").result().map((v0) -> {
            return v0.getValue();
        }).orElseGet(() -> {
            return (Tag) dynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap().getValue();
        }), levelSettings, worldGenSettings, lifecycle);
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public CompoundTag createTag(RegistryAccess registryAccess, @Nullable CompoundTag compoundTag) {
        updatePlayerTag();
        if (compoundTag == null) {
            compoundTag = this.loadedPlayerTag;
        }
        CompoundTag compoundTag2 = new CompoundTag();
        setTagData(registryAccess, compoundTag2, compoundTag);
        return compoundTag2;
    }

    private void setTagData(RegistryAccess registryAccess, CompoundTag compoundTag, @Nullable CompoundTag compoundTag2) {
        ListTag listTag = new ListTag();
        this.knownServerBrands.stream().map(StringTag::valueOf).forEach(listTag::add);
        compoundTag.put("ServerBrands", listTag);
        compoundTag.putBoolean("WasModded", this.wasModded);
        CompoundTag compoundTag3 = new CompoundTag();
        compoundTag3.putString("Name", SharedConstants.getCurrentVersion().getName());
        compoundTag3.putInt("Id", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundTag3.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
        compoundTag.put("Version", compoundTag3);
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        DataResult encodeStart = WorldGenSettings.CODEC.encodeStart(RegistryWriteOps.create(NbtOps.INSTANCE, registryAccess), this.worldGenSettings);
        Logger logger = LOGGER;
        logger.getClass();
        encodeStart.resultOrPartial(Util.prefix("WorldGenSettings: ", logger::error)).ifPresent(tag -> {
            compoundTag.put("WorldGenSettings", (Tag) tag);
        });
        compoundTag.putInt("GameType", this.settings.gameType().getId());
        compoundTag.putInt("SpawnX", this.xSpawn);
        compoundTag.putInt("SpawnY", this.ySpawn);
        compoundTag.putInt("SpawnZ", this.zSpawn);
        compoundTag.putFloat("SpawnAngle", this.spawnAngle);
        compoundTag.putLong("Time", this.gameTime);
        compoundTag.putLong("DayTime", this.dayTime);
        compoundTag.putLong("LastPlayed", Util.getEpochMillis());
        compoundTag.putString("LevelName", this.settings.levelName());
        compoundTag.putInt("version", 19133);
        compoundTag.putInt("clearWeatherTime", this.clearWeatherTime);
        compoundTag.putInt("rainTime", this.rainTime);
        compoundTag.putBoolean("raining", this.raining);
        compoundTag.putInt("thunderTime", this.thunderTime);
        compoundTag.putBoolean("thundering", this.thundering);
        compoundTag.putBoolean("hardcore", this.settings.hardcore());
        compoundTag.putBoolean("allowCommands", this.settings.allowCommands());
        compoundTag.putBoolean("initialized", this.initialized);
        this.worldBorder.write(compoundTag);
        compoundTag.putByte("Difficulty", (byte) this.settings.difficulty().getId());
        compoundTag.putBoolean("DifficultyLocked", this.difficultyLocked);
        compoundTag.put("GameRules", this.settings.gameRules().createTag());
        compoundTag.put("DragonFight", this.endDragonFightData);
        if (compoundTag2 != null) {
            compoundTag.put("Player", compoundTag2);
        }
        DataPackConfig.CODEC.encodeStart(NbtOps.INSTANCE, this.settings.getDataPackConfig()).result().ifPresent(tag2 -> {
            compoundTag.put("DataPacks", tag2);
        });
        if (this.customBossEvents != null) {
            compoundTag.put("CustomBossEvents", this.customBossEvents);
        }
        compoundTag.put("ScheduledEvents", this.scheduledEvents.store());
        compoundTag.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        compoundTag.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        if (this.wanderingTraderId != null) {
            compoundTag.putUUID("WanderingTraderId", this.wanderingTraderId);
        }
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public int getXSpawn() {
        return this.xSpawn;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public int getYSpawn() {
        return this.ySpawn;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public int getZSpawn() {
        return this.zSpawn;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public float getSpawnAngle() {
        return this.spawnAngle;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public long getGameTime() {
        return this.gameTime;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public long getDayTime() {
        return this.dayTime;
    }

    private void updatePlayerTag() {
        if (this.upgradedPlayerTag || this.loadedPlayerTag == null) {
            return;
        }
        if (this.playerDataVersion < SharedConstants.getCurrentVersion().getWorldVersion()) {
            if (this.fixerUpper == null) {
                throw ((NullPointerException) Util.pauseInIde(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded.")));
            }
            this.loadedPlayerTag = NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, this.loadedPlayerTag, this.playerDataVersion);
        }
        this.upgradedPlayerTag = true;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public CompoundTag getLoadedPlayerTag() {
        updatePlayerTag();
        return this.loadedPlayerTag;
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setXSpawn(int i) {
        this.xSpawn = i;
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setYSpawn(int i) {
        this.ySpawn = i;
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setZSpawn(int i) {
        this.zSpawn = i;
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setSpawnAngle(float f) {
        this.spawnAngle = f;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setGameTime(long j) {
        this.gameTime = j;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setDayTime(long j) {
        this.dayTime = j;
    }

    @Override // net.minecraft.world.level.storage.WritableLevelData
    public void setSpawn(BlockPos blockPos, float f) {
        this.xSpawn = blockPos.getX();
        this.ySpawn = blockPos.getY();
        this.zSpawn = blockPos.getZ();
        this.spawnAngle = f;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public String getLevelName() {
        return this.settings.levelName();
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public int getVersion() {
        return this.version;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setClearWeatherTime(int i) {
        this.clearWeatherTime = i;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isThundering() {
        return this.thundering;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setThundering(boolean z) {
        this.thundering = z;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setThunderTime(int i) {
        this.thunderTime = i;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isRaining() {
        return this.raining;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public void setRaining(boolean z) {
        this.raining = z;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getRainTime() {
        return this.rainTime;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setRainTime(int i) {
        this.rainTime = i;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public GameType getGameType() {
        return this.settings.gameType();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setGameType(GameType gameType) {
        this.settings = this.settings.withGameType(gameType);
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public boolean getAllowCommands() {
        return this.settings.allowCommands();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setInitialized(boolean z) {
        this.initialized = z;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public GameRules getGameRules() {
        return this.settings.gameRules();
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public WorldBorder.Settings getWorldBorder() {
        return this.worldBorder;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWorldBorder(WorldBorder.Settings settings) {
        this.worldBorder = settings;
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public Difficulty getDifficulty() {
        return this.settings.difficulty();
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public void setDifficulty(Difficulty difficulty) {
        this.settings = this.settings.withDifficulty(difficulty);
    }

    @Override // net.minecraft.world.level.storage.LevelData
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public void setDifficultyLocked(boolean z) {
        this.difficultyLocked = z;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData, net.minecraft.world.level.storage.LevelData
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        ServerLevelData.super.fillCrashReportCategory(crashReportCategory);
        ServerLevelData.super.fillCrashReportCategory(crashReportCategory);
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public WorldGenSettings worldGenSettings() {
        return this.worldGenSettings;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public Lifecycle worldGenSettingsLifecycle() {
        return this.worldGenSettingsLifecycle;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public CompoundTag endDragonFightData() {
        return this.endDragonFightData;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public void setEndDragonFightData(CompoundTag compoundTag) {
        this.endDragonFightData = compoundTag;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public DataPackConfig getDataPackConfig() {
        return this.settings.getDataPackConfig();
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public void setDataPackConfig(DataPackConfig dataPackConfig) {
        this.settings = this.settings.withDataPackConfig(dataPackConfig);
    }

    @Override // net.minecraft.world.level.storage.WorldData
    @Nullable
    public CompoundTag getCustomBossEvents() {
        return this.customBossEvents;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public void setCustomBossEvents(@Nullable CompoundTag compoundTag) {
        this.customBossEvents = compoundTag;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getWanderingTraderSpawnDelay() {
        return this.wanderingTraderSpawnDelay;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWanderingTraderSpawnDelay(int i) {
        this.wanderingTraderSpawnDelay = i;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public int getWanderingTraderSpawnChance() {
        return this.wanderingTraderSpawnChance;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWanderingTraderSpawnChance(int i) {
        this.wanderingTraderSpawnChance = i;
    }

    @Override // net.minecraft.world.level.storage.ServerLevelData
    public void setWanderingTraderId(UUID uuid) {
        this.wanderingTraderId = uuid;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public void setModdedInfo(String str, boolean z) {
        this.knownServerBrands.add(str);
        this.wasModded |= z;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public boolean wasModded() {
        return this.wasModded;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public Set<String> getKnownServerBrands() {
        return ImmutableSet.copyOf(this.knownServerBrands);
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public ServerLevelData overworldData() {
        return this;
    }

    @Override // net.minecraft.world.level.storage.WorldData
    public LevelSettings getLevelSettings() {
        return this.settings.copy();
    }
}
