package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/FlyingMob.class */
public abstract class FlyingMob extends Mob {
    protected FlyingMob(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void checkFallDamage(double d, boolean z, BlockState blockState, BlockPos blockPos) {
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        if (isInWater()) {
            moveRelative(0.02f, vec3);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.800000011920929d));
        } else if (isInLava()) {
            moveRelative(0.02f, vec3);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.5d));
        } else {
            float f = 0.91f;
            if (this.onGround) {
                f = this.level.getBlockState(new BlockPos(getX(), getY() - 1.0d, getZ())).getBlock().getFriction() * 0.91f;
            }
            float f2 = 0.16277137f / ((f * f) * f);
            float f3 = 0.91f;
            if (this.onGround) {
                f3 = this.level.getBlockState(new BlockPos(getX(), getY() - 1.0d, getZ())).getBlock().getFriction() * 0.91f;
            }
            moveRelative(this.onGround ? 0.1f * f2 : 0.02f, vec3);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(f3));
        }
        calculateEntityAnimation(this, false);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean onClimbable() {
        return false;
    }
}
