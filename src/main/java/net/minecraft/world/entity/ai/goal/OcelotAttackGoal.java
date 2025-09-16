package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/OcelotAttackGoal.class */
public class OcelotAttackGoal extends Goal {
    private final BlockGetter level;
    private final Mob mob;
    private LivingEntity target;
    private int attackTime;

    public OcelotAttackGoal(Mob mob) {
        this.mob = mob;
        this.level = mob.level;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return false;
        }
        this.target = target;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        if (this.target.isAlive() && this.mob.distanceToSqr(this.target) <= 225.0d) {
            return !this.mob.getNavigation().isDone() || canUse();
        }
        return false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        double bbWidth = this.mob.getBbWidth() * 2.0f * this.mob.getBbWidth() * 2.0f;
        double distanceToSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double d = 0.8d;
        if (distanceToSqr > bbWidth && distanceToSqr < 16.0d) {
            d = 1.33d;
        } else if (distanceToSqr < 225.0d) {
            d = 0.6d;
        }
        this.mob.getNavigation().moveTo(this.target, d);
        this.attackTime = Math.max(this.attackTime - 1, 0);
        if (distanceToSqr > bbWidth || this.attackTime > 0) {
            return;
        }
        this.attackTime = 20;
        this.mob.doHurtTarget(this.target);
    }
}
