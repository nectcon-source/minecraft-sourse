package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/Goal.class */
public abstract class Goal {
    private final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/Goal$Flag.class */
    public enum Flag {
        MOVE,
        LOOK,
        JUMP,
        TARGET
    }

    public abstract boolean canUse();

    public boolean canContinueToUse() {
        return canUse();
    }

    public boolean isInterruptable() {
        return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public void tick() {
    }

    public void setFlags(EnumSet<Flag> enumSet) {
        this.flags.clear();
        this.flags.addAll(enumSet);
    }

    public String toString() {
        return getClass().getSimpleName();
    }

    public EnumSet<Flag> getFlags() {
        return this.flags;
    }
}
