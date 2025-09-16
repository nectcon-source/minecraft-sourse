package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrowableProjectile.class */
public abstract class ThrowableProjectile extends Projectile {
    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, double d, double d2, double d3, Level level) {
        this(entityType, level);
        setPos(d, d2, d3);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, LivingEntity livingEntity, Level level) {
        this(entityType, livingEntity.getX(), livingEntity.getEyeY() - 0.10000000149011612d, livingEntity.getZ(), level);
        setOwner(livingEntity);
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
        float f;
        super.tick();
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        boolean z = false;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState blockState = this.level.getBlockState(blockPos);
            if (blockState.is(Blocks.NETHER_PORTAL)) {
                handleInsidePortal(blockPos);
                z = true;
            } else if (blockState.is(Blocks.END_GATEWAY)) {
                BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
                if ((blockEntity instanceof TheEndGatewayBlockEntity) && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                    ((TheEndGatewayBlockEntity) blockEntity).teleportEntity(this);
                }
                z = true;
            }
        }
        if (hitResult.getType() != HitResult.Type.MISS && !z) {
            onHit(hitResult);
        }
        checkInsideBlocks();
        Vec3 deltaMovement = getDeltaMovement();
        double x = getX() + deltaMovement.x;
        double y = getY() + deltaMovement.y;
        double z2 = getZ() + deltaMovement.z;
        updateRotation();
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level.addParticle(ParticleTypes.BUBBLE, x - (deltaMovement.x * 0.25d), y - (deltaMovement.y * 0.25d), z2 - (deltaMovement.z * 0.25d), deltaMovement.x, deltaMovement.y, deltaMovement.z);
            }
            f = 0.8f;
        } else {
            f = 0.99f;
        }
        setDeltaMovement(deltaMovement.scale(f));
        if (!isNoGravity()) {
            Vec3 deltaMovement2 = getDeltaMovement();
            setDeltaMovement(deltaMovement2.x, deltaMovement2.y - getGravity(), deltaMovement2.z);
        }
        setPos(x, y, z2);
    }

    protected float getGravity() {
        return 0.03f;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
