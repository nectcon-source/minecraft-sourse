package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/LlamaSpit.class */
public class LlamaSpit extends Projectile {
    public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
        super(entityType, level);
    }

    public LlamaSpit(Level level, Llama llama) {
        this(EntityType.LLAMA_SPIT, level);
        super.setOwner(llama);
        setPos(llama.getX() - (((llama.getBbWidth() + 1.0f) * 0.5d) * Mth.sin(llama.yBodyRot * 0.017453292f)), llama.getEyeY() - 0.10000000149011612d, llama.getZ() + ((llama.getBbWidth() + 1.0f) * 0.5d * Mth.cos(llama.yBodyRot * 0.017453292f)));
    }

    public LlamaSpit(Level level, double d, double d2, double d3, double d4, double d5, double d6) {
        this(EntityType.LLAMA_SPIT, level);
        setPos(d, d2, d3);
        for (int i = 0; i < 7; i++) {
            double d7 = 0.4d + (0.1d * i);
            level.addParticle(ParticleTypes.SPIT, d, d2, d3, d4 * d7, d5, d6 * d7);
        }
        setDeltaMovement(d4, d5, d6);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        Vec3 deltaMovement = getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitResult != null) {
            onHit(hitResult);
        }
        double x = getX() + deltaMovement.x;
        double y = getY() + deltaMovement.y;
        double z = getZ() + deltaMovement.z;
        updateRotation();
        if (this.level.getBlockStates(getBoundingBox()).noneMatch((v0) -> {
            return v0.isAir();
        })) {
            remove();
            return;
        }
        if (isInWaterOrBubble()) {
            remove();
            return;
        }
        setDeltaMovement(deltaMovement.scale(0.9900000095367432d));
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.05999999865889549d, 0.0d));
        }
        setPos(x, y, z);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity owner = getOwner();
        if (owner instanceof LivingEntity) {
            entityHitResult.getEntity().hurt(DamageSource.indirectMobAttack(this, (LivingEntity) owner).setProjectile(), 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (!this.level.isClientSide) {
            remove();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
