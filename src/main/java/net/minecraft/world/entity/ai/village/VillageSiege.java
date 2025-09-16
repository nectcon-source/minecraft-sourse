package net.minecraft.world.entity.ai.village;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/VillageSiege.class */
public class VillageSiege implements CustomSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean hasSetupSiege;
    private State siegeState = State.SIEGE_DONE;
    private int zombiesToSpawn;
    private int nextSpawnTime;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/VillageSiege$State.class */
    enum State {
        SIEGE_CAN_ACTIVATE,
        SIEGE_TONIGHT,
        SIEGE_DONE
    }

    @Override // net.minecraft.world.level.CustomSpawner
    public int tick(ServerLevel serverLevel, boolean z, boolean z2) {
        if (serverLevel.isDay() || !z) {
            this.siegeState = State.SIEGE_DONE;
            this.hasSetupSiege = false;
            return 0;
        }
        if (serverLevel.getTimeOfDay(0.0f) == 0.5d) {
            this.siegeState = serverLevel.random.nextInt(10) == 0 ? State.SIEGE_TONIGHT : State.SIEGE_DONE;
        }
        if (this.siegeState == State.SIEGE_DONE) {
            return 0;
        }
        if (!this.hasSetupSiege) {
            if (tryToSetupSiege(serverLevel)) {
                this.hasSetupSiege = true;
            } else {
                return 0;
            }
        }
        if (this.nextSpawnTime > 0) {
            this.nextSpawnTime--;
            return 0;
        }
        this.nextSpawnTime = 2;
        if (this.zombiesToSpawn > 0) {
            trySpawn(serverLevel);
            this.zombiesToSpawn--;
            return 1;
        }
        this.siegeState = State.SIEGE_DONE;
        return 1;
    }

    private boolean tryToSetupSiege(ServerLevel serverLevel) {
        for (Player player : serverLevel.players()) {
            if (!player.isSpectator()) {
                BlockPos blockPosition = player.blockPosition();
                if (serverLevel.isVillage(blockPosition) && serverLevel.getBiome(blockPosition).getBiomeCategory() != Biome.BiomeCategory.MUSHROOM) {
                    for (int i = 0; i < 10; i++) {
                        float nextFloat = serverLevel.random.nextFloat() * 6.2831855f;
                        this.spawnX = blockPosition.getX() + Mth.floor(Mth.cos(nextFloat) * 32.0f);
                        this.spawnY = blockPosition.getY();
                        this.spawnZ = blockPosition.getZ() + Mth.floor(Mth.sin(nextFloat) * 32.0f);
                        if (findRandomSpawnPos(serverLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ)) != null) {
                            this.nextSpawnTime = 0;
                            this.zombiesToSpawn = 20;
                            return true;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void trySpawn(ServerLevel serverLevel) {
        Vec3 findRandomSpawnPos = findRandomSpawnPos(serverLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
        if (findRandomSpawnPos == null) {
            return;
        }
        try {
            Zombie zombie = new Zombie(serverLevel);
            zombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.EVENT, null, null);
            zombie.moveTo(findRandomSpawnPos.x, findRandomSpawnPos.y, findRandomSpawnPos.z, serverLevel.random.nextFloat() * 360.0f, 0.0f);
            serverLevel.addFreshEntityWithPassengers(zombie);
        } catch (Exception e) {
            LOGGER.warn("Failed to create zombie for village siege at {}", findRandomSpawnPos, e);
        }
    }

    @Nullable
    private Vec3 findRandomSpawnPos(ServerLevel serverLevel, BlockPos blockPos) {
        for (int i = 0; i < 10; i++) {
            int x = (blockPos.getX() + serverLevel.random.nextInt(16)) - 8;
            int z = (blockPos.getZ() + serverLevel.random.nextInt(16)) - 8;
            BlockPos blockPos2 = new BlockPos(x, serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
            if (serverLevel.isVillage(blockPos2) && Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, serverLevel, MobSpawnType.EVENT, blockPos2, serverLevel.random)) {
                return Vec3.atBottomCenterOf(blockPos2);
            }
        }
        return null;
    }
}
