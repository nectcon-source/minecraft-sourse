package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/AbstractHurtingProjectile.class */
public abstract class AbstractHurtingProjectile extends Projectile {
    public double xPower;
    public double yPower;
    public double zPower;

    protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, double d, double d2, double d3, double d4, double d5, double d6, Level level) {
        this(entityType, level);
        moveTo(d, d2, d3, this.yRot, this.xRot);
        reapplyPosition();
        double sqrt = Mth.sqrt((d4 * d4) + (d5 * d5) + (d6 * d6));
        if (sqrt != 0.0d) {
            this.xPower = (d4 / sqrt) * 0.1d;
            this.yPower = (d5 / sqrt) * 0.1d;
            this.zPower = (d6 / sqrt) * 0.1d;
        }
    }

    public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, LivingEntity livingEntity, double d, double d2, double d3, Level level) {
        this(entityType, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), d, d2, d3, level);
        setOwner(livingEntity);
        setRot(livingEntity.yRot, livingEntity.xRot);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        double size = getBoundingBox().getSize() * 4.0d;
        if (Double.isNaN(size)) {
            size = 4.0d;
        }
        double d2 = size * 64.0d;
        return d < d2 * d2;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        Entity owner = getOwner();
        if (!this.level.isClientSide && ((owner != null && owner.removed) || !this.level.hasChunkAt(blockPosition()))) {
            remove();
            return;
        }
        super.tick();
        if (shouldBurn()) {
            setSecondsOnFire(1);
        }
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            onHit(hitResult);
        }
        checkInsideBlocks();
        Vec3 deltaMovement = getDeltaMovement();
        double x = getX() + deltaMovement.x;
        double y = getY() + deltaMovement.y;
        double z = getZ() + deltaMovement.z;
        ProjectileUtil.rotateTowardsMovement(this, 0.2f);
        float inertia = getInertia();
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level.addParticle(ParticleTypes.BUBBLE, x - (deltaMovement.x * 0.25d), y - (deltaMovement.y * 0.25d), z - (deltaMovement.z * 0.25d), deltaMovement.x, deltaMovement.y, deltaMovement.z);
            }
            inertia = 0.8f;
        }
        setDeltaMovement(deltaMovement.add(this.xPower, this.yPower, this.zPower).scale(inertia));
        this.level.addParticle(getTrailParticle(), x, y + 0.5d, z, 0.0d, 0.0d, 0.0d);
        setPos(x, y, z);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !entity.noPhysics;
    }

    protected boolean shouldBurn() {
        return true;
    }

    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SMOKE;
    }

    protected float getInertia() {
        return 0.95f;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("power", newDoubleList(this.xPower, this.yPower, this.zPower));
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("power", 9)) {
            ListTag list = compoundTag.getList("power", 6);
            if (list.size() == 3) {
                this.xPower = list.getDouble(0);
                this.yPower = list.getDouble(1);
                this.zPower = list.getDouble(2);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getPickRadius() {
        return 1.0f;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        markHurt();
        Entity entity = damageSource.getEntity();
        if (entity != null) {
            Vec3 lookAngle = entity.getLookAngle();
            setDeltaMovement(lookAngle);
            this.xPower = lookAngle.x * 0.1d;
            this.yPower = lookAngle.y * 0.1d;
            this.zPower = lookAngle.z * 0.1d;
            setOwner(entity);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBrightness() {
        return 1.0f;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        Entity owner = getOwner();
        return new ClientboundAddEntityPacket(getId(), getUUID(), getX(), getY(), getZ(), this.xRot, this.yRot, getType(), owner == null ? 0 : owner.getId(), new Vec3(this.xPower, this.yPower, this.zPower));
    }
}
