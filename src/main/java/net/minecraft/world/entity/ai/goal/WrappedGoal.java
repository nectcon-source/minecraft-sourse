package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/WrappedGoal.class */
public class WrappedGoal extends Goal {
    private final Goal goal;
    private final int priority;
    private boolean isRunning;

    public WrappedGoal(int i, Goal goal) {
        this.priority = i;
        this.goal = goal;
    }

    public boolean canBeReplacedBy(WrappedGoal wrappedGoal) {
        return isInterruptable() && wrappedGoal.getPriority() < getPriority();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.goal.canUse();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.goal.canContinueToUse();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean isInterruptable() {
        return this.goal.isInterruptable();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        if (this.isRunning) {
            return;
        }
        this.isRunning = true;
        this.goal.start();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        if (!this.isRunning) {
            return;
        }
        this.isRunning = false;
        this.goal.stop();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.goal.tick();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void setFlags(EnumSet<Goal.Flag> enumSet) {
        this.goal.setFlags(enumSet);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public EnumSet<Goal.Flag> getFlags() {
        return this.goal.getFlags();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public int getPriority() {
        return this.priority;
    }

    public Goal getGoal() {
        return this.goal;
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.goal.equals(((WrappedGoal) obj).goal);
    }

    public int hashCode() {
        return this.goal.hashCode();
    }
}
