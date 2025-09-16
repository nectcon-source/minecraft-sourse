package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrownEnderpearl.class */
public class ThrownEnderpearl extends ThrowableItemProjectile {
    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownEnderpearl(Level level, LivingEntity livingEntity) {
        super(EntityType.ENDER_PEARL, livingEntity, level);
    }

    public ThrownEnderpearl(Level level, double d, double d2, double d3) {
        super(EntityType.ENDER_PEARL, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableItemProjectile
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(DamageSource.thrown(this, getOwner()), 0.0f);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        Entity owner = getOwner();
        for (int i = 0; i < 32; i++) {
            this.level.addParticle(ParticleTypes.PORTAL, getX(), getY() + (this.random.nextDouble() * 2.0d), getZ(), this.random.nextGaussian(), 0.0d, this.random.nextGaussian());
        }
        if (!this.level.isClientSide && !this.removed) {
            if (owner instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) owner;
                if (serverPlayer.connection.getConnection().isConnected() && serverPlayer.level == this.level && !serverPlayer.isSleeping()) {
                    if (this.random.nextFloat() < 0.05f && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Endermite create = EntityType.ENDERMITE.create(this.level);
                        create.setPlayerSpawned(true);
                        create.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.yRot, owner.xRot);
                        this.level.addFreshEntity(create);
                    }
                    if (owner.isPassenger()) {
                        owner.stopRiding();
                    }
                    owner.teleportTo(getX(), getY(), getZ());
                    owner.fallDistance = 0.0f;
                    owner.hurt(DamageSource.FALL, 5.0f);
                }
            } else if (owner != null) {
                owner.teleportTo(getX(), getY(), getZ());
                owner.fallDistance = 0.0f;
            }
            remove();
        }
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableProjectile, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        Entity owner = getOwner();
        if ((owner instanceof Player) && !owner.isAlive()) {
            remove();
        } else {
            super.tick();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        Entity owner = getOwner();
        if (owner != null && owner.level.dimension() != serverLevel.dimension()) {
            setOwner(null);
        }
        return super.changeDimension(serverLevel);
    }
}
