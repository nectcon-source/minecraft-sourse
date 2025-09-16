package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FollowMobGoal.class */
public class FollowMobGoal extends Goal {
    private final Mob mob;
    private final Predicate<Mob> followPredicate;
    private Mob followingMob;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final float areaSize;

    public FollowMobGoal(Mob mob, double d, float f, float f2) {
        this.mob = mob;
        this.followPredicate = mob2 -> {
            return (mob2 == null || mob.getClass() == mob2.getClass()) ? false : true;
        };
        this.speedModifier = d;
        this.navigation = mob.getNavigation();
        this.stopDistance = f;
        this.areaSize = f2;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        List<Mob> entitiesOfClass = this.mob.level.getEntitiesOfClass(Mob.class, this.mob.getBoundingBox().inflate(this.areaSize), this.followPredicate);
        if (!entitiesOfClass.isEmpty()) {
            for (Mob mob : entitiesOfClass) {
                if (!mob.isInvisible()) {
                    this.followingMob = mob;
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return (this.followingMob == null || this.navigation.isDone() || this.mob.distanceToSqr(this.followingMob) <= ((double) (this.stopDistance * this.stopDistance))) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.followingMob = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (this.followingMob == null || this.mob.isLeashed()) {
            return;
        }
        this.mob.getLookControl().setLookAt(this.followingMob, 10.0f, this.mob.getMaxHeadXRot());
        int i = this.timeToRecalcPath - 1;
        this.timeToRecalcPath = i;
        if (i > 0) {
            return;
        }
        this.timeToRecalcPath = 10;
        double x = this.mob.getX() - this.followingMob.getX();
        double y = this.mob.getY() - this.followingMob.getY();
        double z = this.mob.getZ() - this.followingMob.getZ();
        double d = (x * x) + (y * y) + (z * z);
        if (d <= this.stopDistance * this.stopDistance) {
            this.navigation.stop();
            LookControl lookControl = this.followingMob.getLookControl();
            if (d <= this.stopDistance || (lookControl.getWantedX() == this.mob.getX() && lookControl.getWantedY() == this.mob.getY() && lookControl.getWantedZ() == this.mob.getZ())) {
                this.navigation.moveTo(this.mob.getX() - (this.followingMob.getX() - this.mob.getX()), this.mob.getY(), this.mob.getZ() - (this.followingMob.getZ() - this.mob.getZ()), this.speedModifier);
                return;
            }
            return;
        }
        this.navigation.moveTo(this.followingMob, this.speedModifier);
    }
}
