package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RandomLookAroundGoal.class */
public class RandomLookAroundGoal extends Goal {
    private final Mob mob;
    private double relX;
    private double relZ;
    private int lookTime;

    public RandomLookAroundGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.mob.getRandom().nextFloat() < 0.02f;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.lookTime >= 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        double nextDouble = 6.283185307179586d * this.mob.getRandom().nextDouble();
        this.relX = Math.cos(nextDouble);
        this.relZ = Math.sin(nextDouble);
        this.lookTime = 20 + this.mob.getRandom().nextInt(20);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.lookTime--;
        this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
    }
}
