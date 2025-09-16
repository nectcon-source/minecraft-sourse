package net.minecraft.world.entity.ai.goal;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FollowBoatGoal.class */
public class FollowBoatGoal extends Goal {
    private int timeToRecalcPath;
    private final PathfinderMob mob;
    private Player following;
    private BoatGoals currentGoal;

    public FollowBoatGoal(PathfinderMob pathfinderMob) {
        this.mob = pathfinderMob;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        boolean z = false;
        Iterator<Boat> it = this.mob.level.getEntitiesOfClass(Boat.class, this.mob.getBoundingBox().inflate(5.0d)).iterator();
        while (it.hasNext()) {
            Entity controllingPassenger = it.next().getControllingPassenger();
            if ((controllingPassenger instanceof Player) && (Mth.abs(((Player) controllingPassenger).xxa) > 0.0f || Mth.abs(((Player) controllingPassenger).zza) > 0.0f)) {
                z = true;
                break;
            }
        }
        return (this.following != null && (Mth.abs(this.following.xxa) > 0.0f || Mth.abs(this.following.zza) > 0.0f)) || z;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean isInterruptable() {
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.following != null && this.following.isPassenger() && (Mth.abs(this.following.xxa) > 0.0f || Mth.abs(this.following.zza) > 0.0f);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        Iterator<Boat> it = this.mob.level.getEntitiesOfClass(Boat.class, this.mob.getBoundingBox().inflate(5.0d)).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Boat next = it.next();
            if (next.getControllingPassenger() != null && (next.getControllingPassenger() instanceof Player)) {
                this.following = (Player) next.getControllingPassenger();
                break;
            }
        }
        this.timeToRecalcPath = 0;
        this.currentGoal = BoatGoals.GO_TO_BOAT;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.following = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.mob.moveRelative(this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION ? (Mth.abs(this.following.xxa) > 0.0f ? 1 : (Mth.abs(this.following.xxa) == 0.0f ? 0 : -1)) > 0 || (Mth.abs(this.following.zza) > 0.0f ? 1 : (Mth.abs(this.following.zza) == 0.0f ? 0 : -1)) > 0 ? 0.01f : 0.0f : 0.015f, new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
        int i = this.timeToRecalcPath - 1;
        this.timeToRecalcPath = i;
        if (i > 0) {
            return;
        }
        this.timeToRecalcPath = 10;
        if (this.currentGoal == BoatGoals.GO_TO_BOAT) {
            BlockPos offset = this.following.blockPosition().relative(this.following.getDirection().getOpposite()).offset(0, -1, 0);
            this.mob.getNavigation().moveTo(offset.getX(), offset.getY(), offset.getZ(), 1.0d);
            if (this.mob.distanceTo(this.following) < 4.0f) {
                this.timeToRecalcPath = 0;
                this.currentGoal = BoatGoals.GO_IN_BOAT_DIRECTION;
                return;
            }
            return;
        }
        if (this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION) {
            BlockPos relative = this.following.blockPosition().relative(this.following.getMotionDirection(), 10);
            this.mob.getNavigation().moveTo(relative.getX(), relative.getY() - 1, relative.getZ(), 1.0d);
            if (this.mob.distanceTo(this.following) > 12.0f) {
                this.timeToRecalcPath = 0;
                this.currentGoal = BoatGoals.GO_TO_BOAT;
            }
        }
    }
}
