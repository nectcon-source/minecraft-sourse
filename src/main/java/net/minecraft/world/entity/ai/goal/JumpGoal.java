package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/JumpGoal.class */
public abstract class JumpGoal extends Goal {
    public JumpGoal() {
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }
}
