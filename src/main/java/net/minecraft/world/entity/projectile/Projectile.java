package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/Projectile.class */
public abstract class Projectile extends Entity {
    private UUID ownerUUID;
    private int ownerNetworkId;
    private boolean leftOwner;

    Projectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.ownerNetworkId = entity.getId();
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.ownerUUID != null && (this.level instanceof ServerLevel)) {
            return ((ServerLevel) this.level).getEntity(this.ownerUUID);
        }
        if (this.ownerNetworkId != 0) {
            return this.level.getEntity(this.ownerNetworkId);
        }
        return null;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (this.leftOwner) {
            compoundTag.putBoolean("LeftOwner", true);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
        this.leftOwner = compoundTag.getBoolean("LeftOwner");
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        if (!this.leftOwner) {
            this.leftOwner = checkLeftOwner();
        }
        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity owner = getOwner();
        if (owner != null) {
            Iterator<Entity> it = this.level.getEntities(this, getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0d), entity -> {
                return !entity.isSpectator() && entity.isPickable();
            }).iterator();
            while (it.hasNext()) {
                if (it.next().getRootVehicle() == owner.getRootVehicle()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public void shoot(double d, double d2, double d3, float f, float f2) {
        Vec3 scale = new Vec3(d, d2, d3).normalize().add(this.random.nextGaussian() * 0.007499999832361937d * f2, this.random.nextGaussian() * 0.007499999832361937d * f2, this.random.nextGaussian() * 0.007499999832361937d * f2).scale(f);
        setDeltaMovement(scale);
        float sqrt = Mth.sqrt(getHorizontalDistanceSqr(scale));
        this.yRot = (float) (Mth.atan2(scale.x, scale.z) * 57.2957763671875d);
        this.xRot = (float) (Mth.atan2(scale.y, sqrt) * 57.2957763671875d);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    public void shootFromRotation(Entity entity, float f, float f2, float f3, float f4, float f5) {
        shoot((-Mth.sin(f2 * 0.017453292f)) * Mth.cos(f * 0.017453292f), -Mth.sin((f + f3) * 0.017453292f), Mth.cos(f2 * 0.017453292f) * Mth.cos(f * 0.017453292f), f4, f5);
        Vec3 deltaMovement = entity.getDeltaMovement();
        setDeltaMovement(getDeltaMovement().add(deltaMovement.x, entity.isOnGround() ? 0.0d : deltaMovement.y, deltaMovement.z));
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitResult);
        } else if (type == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) hitResult);
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.level, blockState, blockHitResult, this);
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpMotion(double d, double d2, double d3) {
        setDeltaMovement(d, d2, d3);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            this.xRot = (float) (Mth.atan2(d2, Mth.sqrt((d * d) + (d3 * d3))) * 57.2957763671875d);
            this.yRot = (float) (Mth.atan2(d, d3) * 57.2957763671875d);
            this.xRotO = this.xRot;
            this.yRotO = this.yRot;
            moveTo(getX(), getY(), getZ(), this.yRot, this.xRot);
        }
    }

    protected boolean canHitEntity(Entity entity) {
        if (entity.isSpectator() || !entity.isAlive() || !entity.isPickable()) {
            return false;
        }
        Entity owner = getOwner();
        return owner == null || this.leftOwner || !owner.isPassengerOfSameVehicle(entity);
    }

    protected void updateRotation() {
        Vec3 deltaMovement = getDeltaMovement();
        this.xRot = lerpRotation(this.xRotO, (float) (Mth.atan2(deltaMovement.y, Mth.sqrt(getHorizontalDistanceSqr(deltaMovement))) * 57.2957763671875d));
        this.yRot = lerpRotation(this.yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 57.2957763671875d));
    }

    protected static float lerpRotation(float f, float f2) {
        while (f2 - f < -180.0f) {
            f -= 360.0f;
        }
        while (f2 - f >= 180.0f) {
            f += 360.0f;
        }
        return Mth.lerp(0.2f, f, f2);
    }
}
