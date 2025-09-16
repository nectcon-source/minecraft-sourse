package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/PhantomSpawner.class */
public class PhantomSpawner implements CustomSpawner {
    private int nextTick;

    @Override // net.minecraft.world.level.CustomSpawner
    public int tick(ServerLevel serverLevel, boolean z, boolean z2) {
        if (!z || !serverLevel.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
            return 0;
        }
        Random random = serverLevel.random;
        this.nextTick--;
        if (this.nextTick > 0) {
            return 0;
        }
        this.nextTick += (60 + random.nextInt(60)) * 20;
        if (serverLevel.getSkyDarken() < 5 && serverLevel.dimensionType().hasSkyLight()) {
            return 0;
        }
        int i = 0;
        for (Player player : serverLevel.players()) {
            if (!player.isSpectator()) {
                BlockPos blockPosition = player.blockPosition();
                if (!serverLevel.dimensionType().hasSkyLight() || (blockPosition.getY() >= serverLevel.getSeaLevel() && serverLevel.canSeeSky(blockPosition))) {
                    DifficultyInstance currentDifficultyAt = serverLevel.getCurrentDifficultyAt(blockPosition);
                    if (currentDifficultyAt.isHarderThan(random.nextFloat() * 3.0f) && random.nextInt(Mth.clamp(((ServerPlayer) player).getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE)) >= 72000) {
                        BlockPos south = blockPosition.above(20 + random.nextInt(15)).east((-10) + random.nextInt(21)).south((-10) + random.nextInt(21));
                        if (NaturalSpawner.isValidEmptySpawnBlock(serverLevel, south, serverLevel.getBlockState(south), serverLevel.getFluidState(south), EntityType.PHANTOM)) {
                            SpawnGroupData spawnGroupData = null;
                            int nextInt = 1 + random.nextInt(currentDifficultyAt.getDifficulty().getId() + 1);
                            for (int i2 = 0; i2 < nextInt; i2++) {
                                Phantom create = EntityType.PHANTOM.create(serverLevel);
                                create.moveTo(south, 0.0f, 0.0f);
                                spawnGroupData = create.finalizeSpawn(serverLevel, currentDifficultyAt, MobSpawnType.NATURAL, spawnGroupData, null);
                                serverLevel.addFreshEntityWithPassengers(create);
                            }
                            i += nextInt;
                        }
                    }
                }
            }
        }
        return i;
    }
}
