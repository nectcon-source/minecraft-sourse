package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/EntityDimensions.class */
public class EntityDimensions {
    public final float width;
    public final float height;
    public final boolean fixed;

    public EntityDimensions(float f, float f2, boolean z) {
        this.width = f;
        this.height = f2;
        this.fixed = z;
    }

    public AABB makeBoundingBox(Vec3 vec3) {
        return makeBoundingBox(vec3.x, vec3.y, vec3.z);
    }

    public AABB makeBoundingBox(double d, double d2, double d3) {
        float f = this.width / 2.0f;
        return new AABB(d - f, d2, d3 - f, d + f, d2 + this.height, d3 + f);
    }

    public EntityDimensions scale(float f) {
        return scale(f, f);
    }

    public EntityDimensions scale(float f, float f2) {
        if (this.fixed || (f == 1.0f && f2 == 1.0f)) {
            return this;
        }
        return scalable(this.width * f, this.height * f2);
    }

    public static EntityDimensions scalable(float f, float f2) {
        return new EntityDimensions(f, f2, false);
    }

    public static EntityDimensions fixed(float f, float f2) {
        return new EntityDimensions(f, f2, true);
    }

    public String toString() {
        return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
    }
}
