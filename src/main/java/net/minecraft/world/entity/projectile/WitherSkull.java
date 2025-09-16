package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/WitherSkull.class */
public class WitherSkull extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);

    public WitherSkull(EntityType<? extends WitherSkull> entityType, Level level) {
        super(entityType, level);
    }

    public WitherSkull(Level level, LivingEntity livingEntity, double d, double d2, double d3) {
        super(EntityType.WITHER_SKULL, livingEntity, d, d2, d3, level);
    }

    public WitherSkull(Level level, double d, double d2, double d3, double d4, double d5, double d6) {
        super(EntityType.WITHER_SKULL, d, d2, d3, d4, d5, d6, level);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile
    protected float getInertia() {
        if (isDangerous()) {
            return 0.73f;
        }
        return super.getInertia();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isOnFire() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f) {
        if (isDangerous() && WitherBoss.canDestroy(blockState)) {
            return Math.min(0.8f, f);
        }
        return f;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        boolean hurt;
        super.onHitEntity(entityHitResult);
        if (this.level.isClientSide) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        Entity owner = getOwner();
        if (owner instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) owner;
            hurt = entity.hurt(DamageSource.witherSkull(this, livingEntity), 8.0f);
            if (hurt) {
                if (entity.isAlive()) {
                    doEnchantDamageEffects(livingEntity, entity);
                } else {
                    livingEntity.heal(5.0f);
                }
            }
        } else {
            hurt = entity.hurt(DamageSource.MAGIC, 5.0f);
        }
        if (hurt && (entity instanceof LivingEntity)) {
            int i = 0;
            if (this.level.getDifficulty() == Difficulty.NORMAL) {
                i = 10;
            } else if (this.level.getDifficulty() == Difficulty.HARD) {
                i = 40;
            }
            if (i > 0) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1));
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
            this.level.explode(this, getX(), getY(), getZ(), 1.0f, false, this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
            remove();
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return false;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        return false;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        this.entityData.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return ((Boolean) this.entityData.get(DATA_DANGEROUS)).booleanValue();
    }

    public void setDangerous(boolean z) {
        this.entityData.set(DATA_DANGEROUS, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile
    protected boolean shouldBurn() {
        return false;
    }
}
