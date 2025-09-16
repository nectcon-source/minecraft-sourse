package net.minecraft.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/VillagerData.class */
public class VillagerData {
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = {0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Registry.VILLAGER_TYPE.fieldOf("type").orElseGet(() -> {
            return VillagerType.PLAINS;
        }).forGetter(villagerData -> {
            return villagerData.type;
        }), Registry.VILLAGER_PROFESSION.fieldOf("profession").orElseGet(() -> {
            return VillagerProfession.NONE;
        }).forGetter(villagerData2 -> {
            return villagerData2.profession;
        }), Codec.INT.fieldOf("level").orElse(1).forGetter(villagerData3 -> {
            return Integer.valueOf(villagerData3.level);
        })).apply(instance, (v1, v2, v3) -> {
            return new VillagerData(v1, v2, v3);
        });
    });
    private final VillagerType type;
    private final VillagerProfession profession;
    private final int level;

    public VillagerData(VillagerType villagerType, VillagerProfession villagerProfession, int i) {
        this.type = villagerType;
        this.profession = villagerProfession;
        this.level = Math.max(1, i);
    }

    public VillagerType getType() {
        return this.type;
    }

    public VillagerProfession getProfession() {
        return this.profession;
    }

    public int getLevel() {
        return this.level;
    }

    public VillagerData setType(VillagerType villagerType) {
        return new VillagerData(villagerType, this.profession, this.level);
    }

    public VillagerData setProfession(VillagerProfession villagerProfession) {
        return new VillagerData(this.type, villagerProfession, this.level);
    }

    public VillagerData setLevel(int i) {
        return new VillagerData(this.type, this.profession, i);
    }

    public static int getMinXpPerLevel(int i) {
        if (canLevelUp(i)) {
            return NEXT_LEVEL_XP_THRESHOLDS[i - 1];
        }
        return 0;
    }

    public static int getMaxXpPerLevel(int i) {
        if (canLevelUp(i)) {
            return NEXT_LEVEL_XP_THRESHOLDS[i];
        }
        return 0;
    }

    public static boolean canLevelUp(int i) {
        return i >= 1 && i < 5;
    }
}
