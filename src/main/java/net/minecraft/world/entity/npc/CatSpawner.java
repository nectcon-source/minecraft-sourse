package net.minecraft.world.entity.npc;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/CatSpawner.class */
public class CatSpawner implements CustomSpawner {
    private int nextTick;

    @Override // net.minecraft.world.level.CustomSpawner
    public int tick(ServerLevel serverLevel, boolean z, boolean z2) {
        if (!z2 || !serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return 0;
        }
        this.nextTick--;
        if (this.nextTick > 0) {
            return 0;
        }
        this.nextTick = 1200;
        Player randomPlayer = serverLevel.getRandomPlayer();
        if (randomPlayer == null) {
            return 0;
        }
        Random random = serverLevel.random;
        BlockPos offset = randomPlayer.blockPosition().offset((8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1), 0, (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));
        if (serverLevel.hasChunksAt(offset.getX() - 10, offset.getY() - 10, offset.getZ() - 10, offset.getX() + 10, offset.getY() + 10, offset.getZ() + 10) && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, serverLevel, offset, EntityType.CAT)) {
            if (serverLevel.isCloseToVillage(offset, 2)) {
                return spawnInVillage(serverLevel, offset);
            }
            if (serverLevel.structureFeatureManager().getStructureAt(offset, true, StructureFeature.SWAMP_HUT).isValid()) {
                return spawnInHut(serverLevel, offset);
            }
            return 0;
        }
        return 0;
    }

    private int spawnInVillage(ServerLevel serverLevel, BlockPos blockPos) {
        if (serverLevel.getPoiManager().getCountInRange(PoiType.HOME.getPredicate(), blockPos, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4 && serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(48.0d, 8.0d, 48.0d)).size() < 5) {
            return spawnCat(blockPos, serverLevel);
        }
        return 0;
    }

    private int spawnInHut(ServerLevel serverLevel, BlockPos blockPos) {
        if (serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(16.0d, 8.0d, 16.0d)).size() < 1) {
            return spawnCat(blockPos, serverLevel);
        }
        return 0;
    }

    private int spawnCat(BlockPos blockPos, ServerLevel serverLevel) {
        Cat create = EntityType.CAT.create(serverLevel);
        if (create == null) {
            return 0;
        }
        create.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), MobSpawnType.NATURAL, null, null);
        create.moveTo(blockPos, 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(create);
        return 1;
    }
}
