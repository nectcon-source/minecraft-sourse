package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/SitWhenOrderedToGoal.class */
public class SitWhenOrderedToGoal extends Goal {
    private final TamableAnimal mob;

    public SitWhenOrderedToGoal(TamableAnimal tamableAnimal) {
        this.mob = tamableAnimal;
        setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (!this.mob.isTame() || this.mob.isInWaterOrBubble() || !this.mob.isOnGround()) {
            return false;
        }
        LivingEntity owner = this.mob.getOwner();
        if (owner == null) {
            return true;
        }
        if (this.mob.distanceToSqr(owner) < 144.0d && owner.getLastHurtByMob() != null) {
            return false;
        }
        return this.mob.isOrderedToSit();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setInSittingPose(true);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.mob.setInSittingPose(false);
    }
}
