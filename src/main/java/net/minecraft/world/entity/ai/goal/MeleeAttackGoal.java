package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/MeleeAttackGoal.class */
public class MeleeAttackGoal extends Goal {
    protected final PathfinderMob mob;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    private final int attackInterval = 20;
    private long lastCanUseCheck;

    public MeleeAttackGoal(PathfinderMob pathfinderMob, double d, boolean z) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.followingTargetEvenIfNotSeen = z;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        long gameTime = this.mob.level.getGameTime();
        if (gameTime - this.lastCanUseCheck < 20) {
            return false;
        }
        this.lastCanUseCheck = gameTime;
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(target, 0);
        if (this.path != null || getAttackReachSqr(target) >= this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ())) {
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        }
        if (!this.mob.isWithinRestriction(target.blockPosition())) {
            return false;
        }
        if (!(target instanceof Player)) {
            return true;
        }
        if (target.isSpectator() || ((Player) target).isCreative()) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(this.mob.getTarget())) {
            this.mob.setTarget(null);
        }
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        double distanceToSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().canSee(target)) && this.ticksUntilNextPathRecalculation <= 0 && ((this.pathedTargetX == 0.0d && this.pathedTargetY == 0.0d && this.pathedTargetZ == 0.0d) || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0d || this.mob.getRandom().nextFloat() < 0.05f)) {
            this.pathedTargetX = target.getX();
            this.pathedTargetY = target.getY();
            this.pathedTargetZ = target.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            if (distanceToSqr > 1024.0d) {
                this.ticksUntilNextPathRecalculation += 10;
            } else if (distanceToSqr > 256.0d) {
                this.ticksUntilNextPathRecalculation += 5;
            }
            if (!this.mob.getNavigation().moveTo(target, this.speedModifier)) {
                this.ticksUntilNextPathRecalculation += 15;
            }
        }
        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        checkAndPerformAttack(target, distanceToSqr);
    }

    protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
        if (d <= getAttackReachSqr(livingEntity) && this.ticksUntilNextAttack <= 0) {
            resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(livingEntity);
        }
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = 20;
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected int getAttackInterval() {
        return 20;
    }

    protected double getAttackReachSqr(LivingEntity livingEntity) {
        return (this.mob.getBbWidth() * 2.0f * this.mob.getBbWidth() * 2.0f) + livingEntity.getBbWidth();
    }
}
