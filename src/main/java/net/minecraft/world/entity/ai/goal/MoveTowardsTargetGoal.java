package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/MoveTowardsTargetGoal.class */
public class MoveTowardsTargetGoal extends Goal {
    private final PathfinderMob mob;
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;

    public MoveTowardsTargetGoal(PathfinderMob pathfinderMob, double d, float f) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.within = f;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        Vec3 posTowards;
        this.target = this.mob.getTarget();
        if (this.target == null || this.target.distanceToSqr(this.mob) > this.within * this.within || (posTowards = RandomPos.getPosTowards(this.mob, 16, 7, this.target.position())) == null) {
            return false;
        }
        this.wantedX = posTowards.x;
        this.wantedY = posTowards.y;
        this.wantedZ = posTowards.z;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceToSqr(this.mob) < ((double) (this.within * this.within));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.target = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}
