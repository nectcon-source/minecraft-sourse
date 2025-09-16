package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/dimension/end/DragonRespawnAnimation.class */
public enum DragonRespawnAnimation {
    START { // from class: net.minecraft.world.level.dimension.end.DragonRespawnAnimation.1
        @Override // net.minecraft.world.level.dimension.end.DragonRespawnAnimation
        public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
            BlockPos blockPos2 = new BlockPos(0, 128, 0);
            Iterator<EndCrystal> it = list.iterator();
            while (it.hasNext()) {
                it.next().setBeamTarget(blockPos2);
            }
            endDragonFight.setRespawnStage(PREPARING_TO_SUMMON_PILLARS);
        }
    },
    PREPARING_TO_SUMMON_PILLARS { // from class: net.minecraft.world.level.dimension.end.DragonRespawnAnimation.2
        @Override // net.minecraft.world.level.dimension.end.DragonRespawnAnimation
        public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
            if (i < 100) {
                if (i == 0 || i == 50 || i == 51 || i == 52 || i >= 95) {
                    serverLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
                    return;
                }
                return;
            }
            endDragonFight.setRespawnStage(SUMMONING_PILLARS);
        }
    },
    SUMMONING_PILLARS { // from class: net.minecraft.world.level.dimension.end.DragonRespawnAnimation.3
        @Override // net.minecraft.world.level.dimension.end.DragonRespawnAnimation
        public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
            boolean z = i % 40 == 0;
            boolean z2 = i % 40 == 39;
            if (z || z2) {
                List<SpikeFeature.EndSpike> spikesForLevel = SpikeFeature.getSpikesForLevel(serverLevel);
                int i2 = i / 40;
                if (i2 >= spikesForLevel.size()) {
                    if (z) {
                        endDragonFight.setRespawnStage(SUMMONING_DRAGON);
                        return;
                    }
                    return;
                }
                SpikeFeature.EndSpike endSpike = spikesForLevel.get(i2);
                if (z) {
                    Iterator<EndCrystal> it = list.iterator();
                    while (it.hasNext()) {
                        it.next().setBeamTarget(new BlockPos(endSpike.getCenterX(), endSpike.getHeight() + 1, endSpike.getCenterZ()));
                    }
                } else {
                    Iterator<BlockPos> it2 = BlockPos.betweenClosed(new BlockPos(endSpike.getCenterX() - 10, endSpike.getHeight() - 10, endSpike.getCenterZ() - 10), new BlockPos(endSpike.getCenterX() + 10, endSpike.getHeight() + 10, endSpike.getCenterZ() + 10)).iterator();
                    while (it2.hasNext()) {
                        serverLevel.removeBlock(it2.next(), false);
                    }
                    serverLevel.explode(null, endSpike.getCenterX() + 0.5f, endSpike.getHeight(), endSpike.getCenterZ() + 0.5f, 5.0f, Explosion.BlockInteraction.DESTROY);
                    Feature.END_SPIKE.configured(new SpikeConfiguration(true, (List<SpikeFeature.EndSpike>) ImmutableList.of(endSpike), new BlockPos(0, 128, 0))).place(serverLevel, serverLevel.getChunkSource().getGenerator(), new Random(), new BlockPos(endSpike.getCenterX(), 45, endSpike.getCenterZ()));
                }
            }
        }
    },
    SUMMONING_DRAGON { // from class: net.minecraft.world.level.dimension.end.DragonRespawnAnimation.4
        @Override // net.minecraft.world.level.dimension.end.DragonRespawnAnimation
        public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
            if (i >= 100) {
                endDragonFight.setRespawnStage(END);
                endDragonFight.resetSpikeCrystals();
                for (EndCrystal endCrystal : list) {
                    endCrystal.setBeamTarget(null);
                    serverLevel.explode(endCrystal, endCrystal.getX(), endCrystal.getY(), endCrystal.getZ(), 6.0f, Explosion.BlockInteraction.NONE);
                    endCrystal.remove();
                }
                return;
            }
            if (i >= 80) {
                serverLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
                return;
            }
            if (i == 0) {
                Iterator<EndCrystal> it = list.iterator();
                while (it.hasNext()) {
                    it.next().setBeamTarget(new BlockPos(0, 128, 0));
                }
            } else if (i < 5) {
                serverLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            }
        }
    },
    END { // from class: net.minecraft.world.level.dimension.end.DragonRespawnAnimation.5
        @Override // net.minecraft.world.level.dimension.end.DragonRespawnAnimation
        public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
        }
    };

    public abstract void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos);
}
