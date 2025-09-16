package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/StrollThroughVillageGoal.class */
public class StrollThroughVillageGoal extends Goal {
    private final PathfinderMob mob;
    private final int interval;

    @Nullable
    private BlockPos wantedPos;

    public StrollThroughVillageGoal(PathfinderMob pathfinderMob, int i) {
        this.mob = pathfinderMob;
        this.interval = i;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.isVehicle() || this.mob.level.isDay() || this.mob.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel) this.mob.level;
        if (!serverLevel.isCloseToVillage(this.mob.blockPosition(), 6)) {
            return false;
        }
        Vec3 landPos = RandomPos.getLandPos(this.mob, 15, 7, blockPos -> {
            return -serverLevel.sectionsToVillage(SectionPos.of(blockPos));
        });
        this.wantedPos = landPos == null ? null : new BlockPos(landPos);
        return this.wantedPos != null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return (this.wantedPos == null || this.mob.getNavigation().isDone() || !this.mob.getNavigation().getTargetPos().equals(this.wantedPos)) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (this.wantedPos == null) {
            return;
        }
        PathNavigation navigation = this.mob.getNavigation();
        if (navigation.isDone() && !this.wantedPos.closerThan(this.mob.position(), 10.0d)) {
            Vec3 atBottomCenterOf = Vec3.atBottomCenterOf(this.wantedPos);
            Vec3 position = this.mob.position();
            BlockPos heightmapPos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(position.subtract(atBottomCenterOf).scale(0.4d).add(atBottomCenterOf).subtract(position).normalize().scale(10.0d).add(position)));
            if (!navigation.moveTo(heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ(), 1.0d)) {
                moveRandomly();
            }
        }
    }

    private void moveRandomly() {
        Random random = this.mob.getRandom();
        BlockPos heightmapPos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset((-8) + random.nextInt(16), 0, (-8) + random.nextInt(16)));
        this.mob.getNavigation().moveTo(heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ(), 1.0d);
    }
}
