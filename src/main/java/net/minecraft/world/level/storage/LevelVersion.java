package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/LevelVersion.class */
public class LevelVersion {
    private final int levelDataVersion;
    private final long lastPlayed;
    private final String minecraftVersionName;
    private final int minecraftVersion;
    private final boolean snapshot;

    public LevelVersion(int i, long j, String str, int i2, boolean z) {
        this.levelDataVersion = i;
        this.lastPlayed = j;
        this.minecraftVersionName = str;
        this.minecraftVersion = i2;
        this.snapshot = z;
    }

    public static LevelVersion parse(Dynamic<?> dynamic) {
        int asInt = dynamic.get("version").asInt(0);
        long asLong = dynamic.get("LastPlayed").asLong(0L);
        OptionalDynamic<?> optionalDynamic = dynamic.get("Version");
        if (optionalDynamic.result().isPresent()) {
            return new LevelVersion(asInt, asLong, optionalDynamic.get("Name").asString(SharedConstants.getCurrentVersion().getName()), optionalDynamic.get("Id").asInt(SharedConstants.getCurrentVersion().getWorldVersion()), optionalDynamic.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().isStable()));
        }
        return new LevelVersion(asInt, asLong, "", 0, false);
    }

    public int levelDataVersion() {
        return this.levelDataVersion;
    }

    public long lastPlayed() {
        return this.lastPlayed;
    }

    public String minecraftVersionName() {
        return this.minecraftVersionName;
    }

    public int minecraftVersion() {
        return this.minecraftVersion;
    }

    public boolean snapshot() {
        return this.snapshot;
    }
}
