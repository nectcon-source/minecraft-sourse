package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/MobCategory.class */
public enum MobCategory implements StringRepresentable {
    MONSTER("monster", 70, false, false, 128),
    CREATURE("creature", 10, true, true, 128),
    AMBIENT("ambient", 15, true, false, 128),
    WATER_CREATURE("water_creature", 5, true, false, 128),
    WATER_AMBIENT("water_ambient", 20, true, false, 64),
    MISC("misc", -1, true, true, 128);

    public static final Codec<MobCategory> CODEC = StringRepresentable.fromEnum(MobCategory::values, MobCategory::byName);
    private static final Map<String, MobCategory> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
        return v0.getName();
    }, mobCategory -> {
        return mobCategory;
    }));
    private final int max;
    private final boolean isFriendly;
    private final boolean isPersistent;
    private final String name;
    private final int noDespawnDistance = 32;
    private final int despawnDistance;

    MobCategory(String str, int i, boolean z, boolean z2, int i2) {
        this.name = str;
        this.max = i;
        this.isFriendly = z;
        this.isPersistent = z2;
        this.despawnDistance = i2;
    }

    public String getName() {
        return this.name;
    }

    @Override // net.minecraft.util.StringRepresentable
    public String getSerializedName() {
        return this.name;
    }

    public static MobCategory byName(String str) {
        return BY_NAME.get(str);
    }

    public int getMaxInstancesPerChunk() {
        return this.max;
    }

    public boolean isFriendly() {
        return this.isFriendly;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    public int getDespawnDistance() {
        return this.despawnDistance;
    }

    public int getNoDespawnDistance() {
        return 32;
    }
}
