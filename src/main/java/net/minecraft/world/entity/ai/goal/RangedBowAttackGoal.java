package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RangedBowAttackGoal.class */
public class RangedBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int attackTime = -1;
    private int strafingTime = -1;

    public RangedBowAttackGoal(T t, double d, int i, float f) {
        this.mob = t;
        this.speedModifier = d;
        this.attackIntervalMin = i;
        this.attackRadiusSqr = f * f;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int i) {
        this.attackIntervalMin = i;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.getTarget() == null) {
            return false;
        }
        return isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(Items.BOW);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return (canUse() || !this.mob.getNavigation().isDone()) && isHoldingBow();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        int ticksUsingItem;
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        double distanceToSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = this.mob.getSensing().canSee(target);
        if (canSee != (this.seeTime > 0)) {
            this.seeTime = 0;
        }
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }
        if (distanceToSqr > this.attackRadiusSqr || this.seeTime < 20) {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        } else {
            this.mob.getNavigation().stop();
            this.strafingTime++;
        }
        if (this.strafingTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3d) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if (this.mob.getRandom().nextFloat() < 0.3d) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }
        if (this.strafingTime > -1) {
            if (distanceToSqr > this.attackRadiusSqr * 0.75f) {
                this.strafingBackwards = false;
            } else if (distanceToSqr < this.attackRadiusSqr * 0.25f) {
                this.strafingBackwards = true;
            }
            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5f : 0.5f, this.strafingClockwise ? 0.5f : -0.5f);
            this.mob.lookAt(target, 30.0f, 30.0f);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        if (this.mob.isUsingItem()) {
            if (!canSee && this.seeTime < -60) {
                this.mob.stopUsingItem();
                return;
            } else {
                if (canSee && (ticksUsingItem = this.mob.getTicksUsingItem()) >= 20) {
                    this.mob.stopUsingItem();
                    this.mob.performRangedAttack(target, BowItem.getPowerForTime(ticksUsingItem));
                    this.attackTime = this.attackIntervalMin;
                    return;
                }
                return;
            }
        }
        int i = this.attackTime - 1;
        this.attackTime = i;
        if (i <= 0 && this.seeTime >= -60) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.BOW));
        }
    }
}
