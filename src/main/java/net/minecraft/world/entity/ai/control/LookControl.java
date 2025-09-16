package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/LookControl.class */
public class LookControl {
    protected final Mob mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected boolean hasWanted;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(Mob mob) {
        this.mob = mob;
    }

    public void setLookAt(Vec3 vec3) {
        setLookAt(vec3.x, vec3.y, vec3.z);
    }

    public void setLookAt(Entity entity, float f, float f2) {
        setLookAt(entity.getX(), getWantedY(entity), entity.getZ(), f, f2);
    }

    public void setLookAt(double d, double d2, double d3) {
        setLookAt(d, d2, d3, this.mob.getHeadRotSpeed(), this.mob.getMaxHeadXRot());
    }

    public void setLookAt(double d, double d2, double d3, float f, float f2) {
        this.wantedX = d;
        this.wantedY = d2;
        this.wantedZ = d3;
        this.yMaxRotSpeed = f;
        this.xMaxRotAngle = f2;
        this.hasWanted = true;
    }

    public void tick() {
        if (resetXRotOnTick()) {
            this.mob.xRot = 0.0f;
        }
        if (this.hasWanted) {
            this.hasWanted = false;
            this.mob.yHeadRot = rotateTowards(this.mob.yHeadRot, getYRotD(), this.yMaxRotSpeed);
            this.mob.xRot = rotateTowards(this.mob.xRot, getXRotD(), this.xMaxRotAngle);
        } else {
            this.mob.yHeadRot = rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0f);
        }
        if (!this.mob.getNavigation().isDone()) {
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
        }
    }

    protected boolean resetXRotOnTick() {
        return true;
    }

    public boolean isHasWanted() {
        return this.hasWanted;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected float getXRotD() {
        double x = this.wantedX - this.mob.getX();
        double eyeY = this.wantedY - this.mob.getEyeY();
        double z = this.wantedZ - this.mob.getZ();
        return (float) (-(Mth.atan2(eyeY, Mth.sqrt((x * x) + (z * z))) * 57.2957763671875d));
    }

    protected float getYRotD() {
        return ((float) (Mth.atan2(this.wantedZ - this.mob.getZ(), this.wantedX - this.mob.getX()) * 57.2957763671875d)) - 90.0f;
    }

    protected float rotateTowards(float f, float f2, float f3) {
        return f + Mth.clamp(Mth.degreesDifference(f, f2), -f3, f3);
    }

    private static double getWantedY(Entity entity) {
        if (entity instanceof LivingEntity) {
            return entity.getEyeY();
        }
        return (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0d;
    }
}
