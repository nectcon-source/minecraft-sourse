package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/InteractGoal.class */
public class InteractGoal extends LookAtPlayerGoal {
    public InteractGoal(Mob mob, Class<? extends LivingEntity> cls, float f, float f2) {
        super(mob, cls, f, f2);
        setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}
