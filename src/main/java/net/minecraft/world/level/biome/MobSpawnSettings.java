package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/MobSpawnSettings.class */
public class MobSpawnSettings {
    public static final Logger LOGGER = LogManager.getLogger();
//    public static final MobSpawnSettings EMPTY = new MobSpawnSettings(0.1f,  Stream.of((Object[]) MobCategory.values()).collect(ImmutableMap.toImmutableMap(mobCategory -> {
//        return mobCategory;
//    }, mobCategory2 -> {
//        return ImmutableList.of();
//    })), ImmutableMap.of(), false);
//    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> {
//        RecordCodecBuilder forGetter = Codec.FLOAT.optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1f)).forGetter(mobSpawnSettings -> {
//            return Float.valueOf(mobSpawnSettings.creatureGenerationProbability);
//        });
//        Codec<MobCategory> codec = MobCategory.CODEC;
//        Codec listOf = SpawnerData.CODEC.listOf();
//        Logger logger = LOGGER;
//        logger.getClass();
//        return instance.group(forGetter, Codec.simpleMap(codec, listOf.promotePartial(Util.prefix("Spawn data: ", logger::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(mobSpawnSettings2 -> {
//            return mobSpawnSettings2.spawners;
//        }), Codec.simpleMap(Registry.ENTITY_TYPE, MobSpawnCost.CODEC, Registry.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(mobSpawnSettings3 -> {
//            return mobSpawnSettings3.mobSpawnCosts;
//        }), Codec.BOOL.fieldOf("player_spawn_friendly").orElse(false).forGetter((v0) -> {
//            return v0.playerSpawnFriendly();
//        })).apply(instance, (v1, v2, v3, v4) -> {
//            return new MobSpawnSettings(v1, v2, v3, v4);
//        });
//    });
public static final MobSpawnSettings EMPTY = new MobSpawnSettings(
        0.1f,
        Stream.of(MobCategory.values())
                .collect(ImmutableMap.toImmutableMap(
                        Function.identity(),
                        mobCategory -> ImmutableList.of()
                )),
        ImmutableMap.of(),
        false
);

    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(
            var0 -> var0.group(
                            Codec.FLOAT.optionalFieldOf("creature_spawn_probability", 0.1F).forGetter(var0x -> var0x.creatureGenerationProbability),
                            Codec.simpleMap(
                                            MobCategory.CODEC,
                                            MobSpawnSettings.SpawnerData.CODEC.listOf().promotePartial(Util.prefix("Spawn data: ", LOGGER::error)),
                                            StringRepresentable.keys(MobCategory.values())
                                    )
                                    .fieldOf("spawners")
                                    .forGetter(var0x -> var0x.spawners),
                            Codec.simpleMap(Registry.ENTITY_TYPE, MobSpawnSettings.MobSpawnCost.CODEC, Registry.ENTITY_TYPE)
                                    .fieldOf("spawn_costs")
                                    .forGetter(var0x -> var0x.mobSpawnCosts),
                            Codec.BOOL.fieldOf("player_spawn_friendly").orElse(false).forGetter(MobSpawnSettings::playerSpawnFriendly)
                    )
                    .apply(var0, MobSpawnSettings::new)
    );

    //
    //
    private final float creatureGenerationProbability;
    private final Map<MobCategory, List<SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;
    private final boolean playerSpawnFriendly;

    private MobSpawnSettings(float f, Map<MobCategory, List<SpawnerData>> map, Map<EntityType<?>, MobSpawnCost> map2, boolean z) {
        this.creatureGenerationProbability = f;
        this.spawners = map;
        this.mobSpawnCosts = map2;
        this.playerSpawnFriendly = z;
    }

    public List<SpawnerData> getMobs(MobCategory mobCategory) {
        return this.spawners.getOrDefault(mobCategory, ImmutableList.of());
    }

    @Nullable
    public MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
        return this.mobSpawnCosts.get(entityType);
    }

    public float getCreatureProbability() {
        return this.creatureGenerationProbability;
    }

    public boolean playerSpawnFriendly() {
        return this.playerSpawnFriendly;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/MobSpawnSettings$SpawnerData.class */
    public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
        public static final Codec<SpawnerData> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(Registry.ENTITY_TYPE.fieldOf("type").forGetter(spawnerData -> {
                return spawnerData.type;
            }), Codec.INT.fieldOf("weight").forGetter(spawnerData2 -> {
                return Integer.valueOf(spawnerData2.weight);
            }), Codec.INT.fieldOf("minCount").forGetter(spawnerData3 -> {
                return Integer.valueOf(spawnerData3.minCount);
            }), Codec.INT.fieldOf("maxCount").forGetter(spawnerData4 -> {
                return Integer.valueOf(spawnerData4.maxCount);
            })).apply(instance, (v1, v2, v3, v4) -> {
                return new SpawnerData(v1, v2, v3, v4);
            });
        });
        public final EntityType<?> type;
        public final int minCount;
        public final int maxCount;

        public SpawnerData(EntityType<?> entityType, int i, int i2, int i3) {
            super(i);
            this.type = entityType.getCategory() == MobCategory.MISC ? EntityType.PIG : entityType;
            this.minCount = i2;
            this.maxCount = i3;
        }

        public String toString() {
            return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/MobSpawnSettings$MobSpawnCost.class */
    public static class MobSpawnCost {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(Codec.DOUBLE.fieldOf("energy_budget").forGetter(mobSpawnCost -> {
                return Double.valueOf(mobSpawnCost.energyBudget);
            }), Codec.DOUBLE.fieldOf("charge").forGetter(mobSpawnCost2 -> {
                return Double.valueOf(mobSpawnCost2.charge);
            })).apply(instance, (v1, v2) -> {
                return new MobSpawnCost(v1, v2);
            });
        });
        private final double energyBudget;
        private final double charge;

        private MobSpawnCost(double d, double d2) {
            this.energyBudget = d;
            this.charge = d2;
        }

        public double getEnergyBudget() {
            return this.energyBudget;
        }

        public double getCharge() {
            return this.charge;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/MobSpawnSettings$Builder.class */
    public static class Builder {
        private final Map<MobCategory, List<SpawnerData>> spawners = (Map) Stream.of((Object[]) MobCategory.values()).collect(ImmutableMap.toImmutableMap(mobCategory -> {
            return mobCategory;
        }, mobCategory2 -> {
            return Lists.newArrayList();
        }));
        private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
        private float creatureGenerationProbability = 0.1f;
        private boolean playerCanSpawn;

        public Builder addSpawn(MobCategory mobCategory, SpawnerData spawnerData) {
            this.spawners.get(mobCategory).add(spawnerData);
            return this;
        }

        public Builder addMobCharge(EntityType<?> entityType, double d, double d2) {
            this.mobSpawnCosts.put(entityType, new MobSpawnCost(d2, d));
            return this;
        }

        public Builder creatureGenerationProbability(float f) {
            this.creatureGenerationProbability = f;
            return this;
        }

        public Builder setPlayerCanSpawn() {
            this.playerCanSpawn = true;
            return this;
        }

        public MobSpawnSettings build() {
            return new MobSpawnSettings(this.creatureGenerationProbability, (Map) this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap((v0) -> {
                return v0.getKey();
            }, entry -> {
                return ImmutableList.copyOf((Collection) entry.getValue());
            })), ImmutableMap.copyOf(this.mobSpawnCosts), this.playerCanSpawn);
        }
    }
}
