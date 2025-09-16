package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/SpawnPlacements.class */
public class SpawnPlacements {
    private static final Map<EntityType<?>, Data> DATA_BY_TYPE = Maps.newHashMap();

    @FunctionalInterface
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/SpawnPlacements$SpawnPredicate.class */
    public interface SpawnPredicate<T extends Entity> {
        boolean test(EntityType<T> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/SpawnPlacements$Type.class */
    public enum Type {
        ON_GROUND,
        IN_WATER,
        NO_RESTRICTIONS,
        IN_LAVA
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/SpawnPlacements$Data.class */
    static class Data {
        private final Heightmap.Types heightMap;
        private final Type placement;
        private final SpawnPredicate<?> predicate;

        public Data(Heightmap.Types types, Type type, SpawnPredicate<?> spawnPredicate) {
            this.heightMap = types;
            this.placement = type;
            this.predicate = spawnPredicate;
        }
    }

    static {
        register(EntityType.COD, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return AbstractFish.checkFishSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.DOLPHIN, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Dolphin.checkDolphinSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.DROWNED, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Drowned::checkDrownedSpawnRules);
        register(EntityType.GUARDIAN, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Guardian.checkGuardianSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.PUFFERFISH, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return AbstractFish.checkFishSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SALMON, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return AbstractFish.checkFishSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SQUID, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Squid.checkSquidSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.TROPICAL_FISH, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return AbstractFish.checkFishSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.BAT, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Bat.checkBatSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.BLAZE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Monster.checkAnyLightMonsterSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.CAVE_SPIDER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.CHICKEN, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.COW, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.CREEPER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.DONKEY, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ENDERMAN, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.ENDERMITE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Endermite.checkEndermiteSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ENDER_DRAGON, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.GHAST, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Ghast.checkGhastSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.GIANT, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.HORSE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.HUSK, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Husk::checkHuskSpawnRules);
        register(EntityType.IRON_GOLEM, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.LLAMA, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.MAGMA_CUBE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return MagmaCube.checkMagmaCubeSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.MOOSHROOM, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return MushroomCow.checkMushroomSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.MULE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.OCELOT, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, (v0, v1, v2, v3, v4) -> {
            return Ocelot.checkOcelotSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.PARROT, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, (v0, v1, v2, v3, v4) -> {
            return Parrot.checkParrotSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.PIG, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.HOGLIN, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Hoglin.checkHoglinSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.PIGLIN, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Piglin.checkPiglinSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.PILLAGER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return PatrollingMonster.checkPatrollingMonsterSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.POLAR_BEAR, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return PolarBear.checkPolarBearSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.RABBIT, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Rabbit.checkRabbitSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SHEEP, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SILVERFISH, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Silverfish.checkSliverfishSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SKELETON, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.SKELETON_HORSE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SLIME, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Slime.checkSlimeSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SNOW_GOLEM, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.SPIDER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.STRAY, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Stray::checkStraySpawnRules);
        register(EntityType.STRIDER, Type.IN_LAVA, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Strider.checkStriderSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.TURTLE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Turtle.checkTurtleSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.VILLAGER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.WITCH, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.WITHER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.WITHER_SKELETON, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.WOLF, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ZOMBIE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.ZOMBIE_HORSE, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ZOMBIFIED_PIGLIN, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return ZombifiedPiglin.checkZombifiedPiglinSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ZOMBIE_VILLAGER, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.CAT, Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ELDER_GUARDIAN, Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Guardian.checkGuardianSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.EVOKER, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.FOX, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.ILLUSIONER, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.PANDA, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.PHANTOM, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.RAVAGER, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.SHULKER, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.TRADER_LLAMA, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Animal.checkAnimalSpawnRules(v0, v1, v2, v3, v4);
        });
        register(EntityType.VEX, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.VINDICATOR, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        register(EntityType.WANDERING_TRADER, Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (v0, v1, v2, v3, v4) -> {
            return Mob.checkMobSpawnRules(v0, v1, v2, v3, v4);
        });
    }

    private static <T extends Mob> void register(EntityType<T> entityType, Type type, Heightmap.Types types, SpawnPredicate<T> spawnPredicate) {
        if (DATA_BY_TYPE.put(entityType, new Data(types, type, spawnPredicate)) != null) {
            throw new IllegalStateException("Duplicate registration for type " + Registry.ENTITY_TYPE.getKey(entityType));
        }
    }

    public static Type getPlacementType(EntityType<?> entityType) {
        Data data = DATA_BY_TYPE.get(entityType);
        return data == null ? Type.NO_RESTRICTIONS : data.placement;
    }

    public static Heightmap.Types getHeightmapType(@Nullable EntityType<?> entityType) {
        Data data = DATA_BY_TYPE.get(entityType);
        return data == null ? Heightmap.Types.MOTION_BLOCKING_NO_LEAVES : data.heightMap;
    }

//    public static <T extends Entity> boolean checkSpawnRules(EntityType<T> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
//        Data data = DATA_BY_TYPE.get(entityType);
//        return data == null || data.predicate.test(entityType, serverLevelAccessor, mobSpawnType, blockPos, random);
//    }
public static <T extends Entity> boolean checkSpawnRules(EntityType<T> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
    Data data = DATA_BY_TYPE.get(entityType);
    if (data == null) {
        return true;
    }

    // Безопасное приведение типа с проверкой
    @SuppressWarnings("unchecked")
    SpawnPredicate<T> predicate = (SpawnPredicate<T>) data.predicate;
    return predicate.test(entityType, serverLevelAccessor, mobSpawnType, blockPos, random);
}
}
