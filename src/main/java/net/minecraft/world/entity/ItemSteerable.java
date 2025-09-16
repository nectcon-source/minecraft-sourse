package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ItemSteerable.class */
public interface ItemSteerable {
    boolean boost();

    void travelWithInput(Vec3 vec3);

    float getSteeringSpeed();

    default boolean travel(Mob mob, ItemBasedSteering itemBasedSteering, Vec3 vec3) {
        if (!mob.isAlive()) {
            return false;
        }
        Entity entity = mob.getPassengers().isEmpty() ? null : mob.getPassengers().get(0);
        if (!mob.isVehicle() || !mob.canBeControlledByRider() || !(entity instanceof Player)) {
            mob.maxUpStep = 0.5f;
            mob.flyingSpeed = 0.02f;
            travelWithInput(vec3);
            return false;
        }
        mob.yRot = entity.yRot;
        mob.yRotO = mob.yRot;
        mob.xRot = entity.xRot * 0.5f;
        mob.setRot(mob.yRot, mob.xRot);
        mob.yBodyRot = mob.yRot;
        mob.yHeadRot = mob.yRot;
        mob.maxUpStep = 1.0f;
        mob.flyingSpeed = mob.getSpeed() * 0.1f;
        if (itemBasedSteering.boosting) {
            int i = itemBasedSteering.boostTime;
            itemBasedSteering.boostTime = i + 1;
            if (i > itemBasedSteering.boostTimeTotal) {
                itemBasedSteering.boosting = false;
            }
        }
        if (mob.isControlledByLocalInstance()) {
            float steeringSpeed = getSteeringSpeed();
            if (itemBasedSteering.boosting) {
                steeringSpeed += steeringSpeed * 1.15f * Mth.sin((itemBasedSteering.boostTime / itemBasedSteering.boostTimeTotal) * 3.1415927f);
            }
            mob.setSpeed(steeringSpeed);
            travelWithInput(new Vec3(0.0d, 0.0d, 1.0d));
            mob.lerpSteps = 0;
            return true;
        }
        mob.calculateEntityAnimation(mob, false);
        mob.setDeltaMovement(Vec3.ZERO);
        return true;
    }
}
