package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/PatrolSpawner.class */
public class PatrolSpawner implements CustomSpawner {
    private int nextTick;

    @Override // net.minecraft.world.level.CustomSpawner
    public int tick(ServerLevel serverLevel, boolean z, boolean z2) {
        int size;
        if (!z || !serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }
        Random random = serverLevel.random;
        this.nextTick--;
        if (this.nextTick > 0) {
            return 0;
        }
        this.nextTick += 12000 + random.nextInt(1200);
        if (serverLevel.getDayTime() / 24000 < 5 || !serverLevel.isDay() || random.nextInt(5) != 0 || (size = serverLevel.players().size()) < 1) {
            return 0;
        }
        Player player = serverLevel.players().get(random.nextInt(size));
        if (player.isSpectator() || serverLevel.isCloseToVillage(player.blockPosition(), 2)) {
            return 0;
        }
        BlockPos.MutableBlockPos move = player.blockPosition().mutable().move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1), 0, (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));
        if (!serverLevel.hasChunksAt(move.getX() - 10, move.getY() - 10, move.getZ() - 10, move.getX() + 10, move.getY() + 10, move.getZ() + 10) || serverLevel.getBiome(move).getBiomeCategory() == Biome.BiomeCategory.MUSHROOM) {
            return 0;
        }
        int i = 0;
        int ceil = ((int) Math.ceil(serverLevel.getCurrentDifficultyAt(move).getEffectiveDifficulty())) + 1;
        for (int i2 = 0; i2 < ceil; i2++) {
            i++;
            move.setY(serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, move).getY());
            if (i2 == 0) {
                if (!spawnPatrolMember(serverLevel, move, random, true)) {
                    break;
                }
            } else {
                spawnPatrolMember(serverLevel, move, random, false);
            }
            move.setX((move.getX() + random.nextInt(5)) - random.nextInt(5));
            move.setZ((move.getZ() + random.nextInt(5)) - random.nextInt(5));
        }
        return i;
    }

    private boolean spawnPatrolMember(ServerLevel serverLevel, BlockPos blockPos, Random random, boolean z) {
        PatrollingMonster create;
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos, blockState, blockState.getFluidState(), EntityType.PILLAGER) && PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, serverLevel, MobSpawnType.PATROL, blockPos, random) && (create = EntityType.PILLAGER.create(serverLevel)) != null) {
            if (z) {
                create.setPatrolLeader(true);
                create.findPatrolTarget();
            }
            create.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            create.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), MobSpawnType.PATROL, null, null);
            serverLevel.addFreshEntityWithPassengers(create);
            return true;
        }
        return false;
    }
}
