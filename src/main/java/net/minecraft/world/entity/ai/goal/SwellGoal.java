package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/SwellGoal.class */
public class SwellGoal extends Goal {
    private final Creeper creeper;
    private LivingEntity target;

    public SwellGoal(Creeper creeper) {
        this.creeper = creeper;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        LivingEntity target = this.creeper.getTarget();
        return this.creeper.getSwellDir() > 0 || (target != null && this.creeper.distanceToSqr(target) < 9.0d);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.creeper.getNavigation().stop();
        this.target = this.creeper.getTarget();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.target = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (this.target == null) {
            this.creeper.setSwellDir(-1);
            return;
        }
        if (this.creeper.distanceToSqr(this.target) > 49.0d) {
            this.creeper.setSwellDir(-1);
        } else if (!this.creeper.getSensing().canSee(this.target)) {
            this.creeper.setSwellDir(-1);
        } else {
            this.creeper.setSwellDir(1);
        }
    }
}
