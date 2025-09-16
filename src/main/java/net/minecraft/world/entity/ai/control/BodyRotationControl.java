package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/BodyRotationControl.class */
public class BodyRotationControl {
    private final Mob mob;
    private int headStableTime;
    private float lastStableYHeadRot;

    public BodyRotationControl(Mob mob) {
        this.mob = mob;
    }

    public void clientTick() {
        if (isMoving()) {
            this.mob.yBodyRot = this.mob.yRot;
            rotateHeadIfNecessary();
            this.lastStableYHeadRot = this.mob.yHeadRot;
            this.headStableTime = 0;
            return;
        }
        if (notCarryingMobPassengers()) {
            if (Math.abs(this.mob.yHeadRot - this.lastStableYHeadRot) > 15.0f) {
                this.headStableTime = 0;
                this.lastStableYHeadRot = this.mob.yHeadRot;
                rotateBodyIfNecessary();
            } else {
                this.headStableTime++;
                if (this.headStableTime > 10) {
                    rotateHeadTowardsFront();
                }
            }
        }
    }

    private void rotateBodyIfNecessary() {
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, this.mob.getMaxHeadYRot());
    }

    private void rotateHeadIfNecessary() {
        this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
    }

    private void rotateHeadTowardsFront() {
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, this.mob.getMaxHeadYRot() * (1.0f - Mth.clamp((this.headStableTime - 10) / 10.0f, 0.0f, 1.0f)));
    }

    private boolean notCarryingMobPassengers() {
        return this.mob.getPassengers().isEmpty() || !(this.mob.getPassengers().get(0) instanceof Mob);
    }

    private boolean isMoving() {
        double x = this.mob.getX() - this.mob.xo;
        double z = this.mob.getZ() - this.mob.zo;
        return (x * x) + (z * z) > 2.500000277905201E-7d;
    }
}
