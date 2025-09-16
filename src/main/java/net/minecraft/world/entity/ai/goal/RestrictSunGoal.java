package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RestrictSunGoal.class */
public class RestrictSunGoal extends Goal {
    private final PathfinderMob mob;

    public RestrictSunGoal(PathfinderMob pathfinderMob) {
        this.mob = pathfinderMob;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.mob.level.isDay() && this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && GoalUtils.hasGroundPathNavigation(this.mob);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        ((GroundPathNavigation) this.mob.getNavigation()).setAvoidSun(true);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        if (GoalUtils.hasGroundPathNavigation(this.mob)) {
            ((GroundPathNavigation) this.mob.getNavigation()).setAvoidSun(false);
        }
    }
}
