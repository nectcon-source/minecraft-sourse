package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/PathfindToRaidGoal.class */
public class PathfindToRaidGoal<T extends Raider> extends Goal {
    private final T mob;

    public PathfindToRaidGoal(T t) {
        this.mob = t;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return (this.mob.getTarget() != null || this.mob.isVehicle() || !this.mob.hasActiveRaid() || this.mob.getCurrentRaid().isOver() || ((ServerLevel) this.mob.level).isVillage(this.mob.blockPosition())) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.mob.hasActiveRaid() && !this.mob.getCurrentRaid().isOver() && (this.mob.level instanceof ServerLevel) && !((ServerLevel) this.mob.level).isVillage(this.mob.blockPosition());
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        Vec3 posTowards;
        if (this.mob.hasActiveRaid()) {
            Raid currentRaid = this.mob.getCurrentRaid();
            if (this.mob.tickCount % 20 == 0) {
                recruitNearby(currentRaid);
            }
            if (!this.mob.isPathFinding() && (posTowards = RandomPos.getPosTowards(this.mob, 15, 4, Vec3.atBottomCenterOf(currentRaid.getCenter()))) != null) {
                this.mob.getNavigation().moveTo(posTowards.x, posTowards.y, posTowards.z, 1.0d);
            }
        }
    }

    private void recruitNearby(Raid raid) {
        if (raid.isActive()) {
            Set<Raider> newHashSet = Sets.newHashSet();
            newHashSet.addAll(this.mob.level.getEntitiesOfClass(Raider.class, this.mob.getBoundingBox().inflate(16.0d), raider -> {
                return !raider.hasActiveRaid() && Raids.canJoinRaid(raider, raid);
            }));
            Iterator<Raider> it = newHashSet.iterator();
            while (it.hasNext()) {
                raid.joinRaid(raid.getGroupsSpawned(), it.next(), null, true);
            }
        }
    }
}
