package net.minecraft.world.entity.npc;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/WanderingTraderSpawner.class */
public class WanderingTraderSpawner implements CustomSpawner {
    private final ServerLevelData serverLevelData;
    private int spawnDelay;
    private int spawnChance;
    private final Random random = new Random();
    private int tickDelay = 1200;

    public WanderingTraderSpawner(ServerLevelData serverLevelData) {
        this.serverLevelData = serverLevelData;
        this.spawnDelay = serverLevelData.getWanderingTraderSpawnDelay();
        this.spawnChance = serverLevelData.getWanderingTraderSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
        }
    }

    @Override // net.minecraft.world.level.CustomSpawner
    public int tick(ServerLevel serverLevel, boolean z, boolean z2) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
            return 0;
        }
        int i = this.tickDelay - 1;
        this.tickDelay = i;
        if (i > 0) {
            return 0;
        }
        this.tickDelay = 1200;
        this.spawnDelay -= 1200;
        this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
        if (this.spawnDelay > 0) {
            return 0;
        }
        this.spawnDelay = 24000;
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return 0;
        }
        int i2 = this.spawnChance;
        this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
        this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
        if (this.random.nextInt(100) <= i2 && spawn(serverLevel)) {
            this.spawnChance = 25;
            return 1;
        }
        return 0;
    }

    private boolean spawn(ServerLevel serverLevel) {
        WanderingTrader spawn;
        Player randomPlayer = serverLevel.getRandomPlayer();
        if (randomPlayer == null) {
            return true;
        }
        if (this.random.nextInt(10) != 0) {
            return false;
        }
        BlockPos blockPosition = randomPlayer.blockPosition();
        BlockPos orElse = serverLevel.getPoiManager().find(PoiType.MEETING.getPredicate(), blockPos -> {
            return true;
        }, blockPosition, 48, PoiManager.Occupancy.ANY).orElse(blockPosition);
        BlockPos findSpawnPositionNear = findSpawnPositionNear(serverLevel, orElse, 48);
        if (findSpawnPositionNear != null && hasEnoughSpace(serverLevel, findSpawnPositionNear) && !serverLevel.getBiomeName(findSpawnPositionNear).equals(Optional.of(Biomes.THE_VOID)) && (spawn = EntityType.WANDERING_TRADER.spawn(serverLevel, null, null, null, findSpawnPositionNear, MobSpawnType.EVENT, false, false)) != null) {
            for (int i = 0; i < 2; i++) {
                tryToSpawnLlamaFor(serverLevel, spawn, 4);
            }
            this.serverLevelData.setWanderingTraderId(spawn.getUUID());
            spawn.setDespawnDelay(48000);
            spawn.setWanderTarget(orElse);
            spawn.restrictTo(orElse, 16);
            return true;
        }
        return false;
    }

    private void tryToSpawnLlamaFor(ServerLevel serverLevel, WanderingTrader wanderingTrader, int i) {
        TraderLlama spawn;
        BlockPos findSpawnPositionNear = findSpawnPositionNear(serverLevel, wanderingTrader.blockPosition(), i);
        if (findSpawnPositionNear == null || (spawn = EntityType.TRADER_LLAMA.spawn(serverLevel, null, null, null, findSpawnPositionNear, MobSpawnType.EVENT, false, false)) == null) {
            return;
        }
        spawn.setLeashedTo(wanderingTrader, true);
    }

    @Nullable
    private BlockPos findSpawnPositionNear(LevelReader levelReader, BlockPos blockPos, int i) {
        BlockPos blockPos2 = null;
        int i2 = 0;
        while (true) {
            if (i2 >= 10) {
                break;
            }
            int x = (blockPos.getX() + this.random.nextInt(i * 2)) - i;
            int z = (blockPos.getZ() + this.random.nextInt(i * 2)) - i;
            BlockPos blockPos3 = new BlockPos(x, levelReader.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
            if (!NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, levelReader, blockPos3, EntityType.WANDERING_TRADER)) {
                i2++;
            } else {
                blockPos2 = blockPos3;
                break;
            }
        }
        return blockPos2;
    }

    private boolean hasEnoughSpace(BlockGetter blockGetter, BlockPos blockPos) {
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(1, 2, 1))) {
            if (!blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
