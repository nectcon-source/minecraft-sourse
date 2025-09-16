package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FloatGoal.class */
public class FloatGoal extends Goal {
    private final Mob mob;

    public FloatGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Goal.Flag.JUMP));
        mob.getNavigation().setCanFloat(true);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return (this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getFluidJumpThreshold()) || this.mob.isInLava();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (this.mob.getRandom().nextFloat() < 0.8f) {
            this.mob.getJumpControl().jump();
        }
    }
}
