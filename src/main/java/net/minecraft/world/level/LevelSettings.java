package net.minecraft.world.level;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import net.minecraft.world.Difficulty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/LevelSettings.class */
public final class LevelSettings {
    private final String levelName;
    private final GameType gameType;
    private final boolean hardcore;
    private final Difficulty difficulty;
    private final boolean allowCommands;
    private final GameRules gameRules;
    private final DataPackConfig dataPackConfig;

    public LevelSettings(String str, GameType gameType, boolean z, Difficulty difficulty, boolean z2, GameRules gameRules, DataPackConfig dataPackConfig) {
        this.levelName = str;
        this.gameType = gameType;
        this.hardcore = z;
        this.difficulty = difficulty;
        this.allowCommands = z2;
        this.gameRules = gameRules;
        this.dataPackConfig = dataPackConfig;
    }

    public static LevelSettings parse(Dynamic<?> dynamic, DataPackConfig dataPackConfig) {
        GameType byId = GameType.byId(dynamic.get("GameType").asInt(0));
        return new LevelSettings(dynamic.get("LevelName").asString(""), byId, dynamic.get("hardcore").asBoolean(false),  dynamic.get("Difficulty").asNumber().map(number -> {
            return Difficulty.byId(number.byteValue());
        }).result().orElse(Difficulty.NORMAL), dynamic.get("allowCommands").asBoolean(byId == GameType.CREATIVE), new GameRules( dynamic.get("GameRules")), dataPackConfig);
    }

    public String levelName() {
        return this.levelName;
    }

    public GameType gameType() {
        return this.gameType;
    }

    public boolean hardcore() {
        return this.hardcore;
    }

    public Difficulty difficulty() {
        return this.difficulty;
    }

    public boolean allowCommands() {
        return this.allowCommands;
    }

    public GameRules gameRules() {
        return this.gameRules;
    }

    public DataPackConfig getDataPackConfig() {
        return this.dataPackConfig;
    }

    public LevelSettings withGameType(GameType gameType) {
        return new LevelSettings(this.levelName, gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
    }

    public LevelSettings withDifficulty(Difficulty difficulty) {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
    }

    public LevelSettings withDataPackConfig(DataPackConfig dataPackConfig) {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, dataPackConfig);
    }

    public LevelSettings copy() {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.dataPackConfig);
    }
}
