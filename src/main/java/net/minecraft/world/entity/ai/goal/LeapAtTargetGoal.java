package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/LeapAtTargetGoal.class */
public class LeapAtTargetGoal extends Goal {
    private final Mob mob;
    private LivingEntity target;

    /* renamed from: yd */
    private final float f436yd;

    public LeapAtTargetGoal(Mob mob, float f) {
        this.mob = mob;
        this.f436yd = f;
        setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        this.target = this.mob.getTarget();
        if (this.target == null) {
            return false;
        }
        double distanceToSqr = this.mob.distanceToSqr(this.target);
        if (distanceToSqr < 4.0d || distanceToSqr > 16.0d || !this.mob.isOnGround() || this.mob.getRandom().nextInt(5) != 0) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return !this.mob.isOnGround();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        Vec3 deltaMovement = this.mob.getDeltaMovement();
        Vec3 vec3 = new Vec3(this.target.getX() - this.mob.getX(), 0.0d, this.target.getZ() - this.mob.getZ());
        if (vec3.lengthSqr() > 1.0E-7d) {
            vec3 = vec3.normalize().scale(0.4d).add(deltaMovement.scale(0.2d));
        }
        this.mob.setDeltaMovement(vec3.x, this.f436yd, vec3.z);
    }
}
