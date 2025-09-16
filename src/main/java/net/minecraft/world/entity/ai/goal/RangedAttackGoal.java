package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RangedAttackGoal.class */
public class RangedAttackGoal extends Goal {
    private final Mob mob;
    private final RangedAttackMob rangedAttackMob;
    private LivingEntity target;
    private int attackTime;
    private final double speedModifier;
    private int seeTime;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    public RangedAttackGoal(RangedAttackMob rangedAttackMob, double d, int i, float f) {
        this(rangedAttackMob, d, i, i, f);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public RangedAttackGoal(RangedAttackMob rangedAttackMob, double d, int i, int i2, float f) {
        this.attackTime = -1;
        if (!(rangedAttackMob instanceof LivingEntity)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        this.rangedAttackMob = rangedAttackMob;
        this.mob = (Mob) rangedAttackMob;
        this.speedModifier = d;
        this.attackIntervalMin = i;
        this.attackIntervalMax = i2;
        this.attackRadius = f;
        this.attackRadiusSqr = f * f;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        this.target = target;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return canUse() || !this.mob.getNavigation().isDone();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        double distanceToSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean canSee = this.mob.getSensing().canSee(this.target);
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }
        if (distanceToSqr > this.attackRadiusSqr || this.seeTime < 5) {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
        }
        this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        int i = this.attackTime - 1;
        this.attackTime = i;
        if (i != 0) {
            if (this.attackTime < 0) {
                this.attackTime = Mth.floor(((Mth.sqrt(distanceToSqr) / this.attackRadius) * (this.attackIntervalMax - this.attackIntervalMin)) + this.attackIntervalMin);
            }
        } else {
            if (!canSee) {
                return;
            }
            float sqrt = Mth.sqrt(distanceToSqr) / this.attackRadius;
            this.rangedAttackMob.performRangedAttack(this.target, Mth.clamp(sqrt, 0.1f, 1.0f));
            this.attackTime = Mth.floor((sqrt * (this.attackIntervalMax - this.attackIntervalMin)) + this.attackIntervalMin);
        }
    }
}
