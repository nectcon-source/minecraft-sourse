package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/LargeFireball.class */
public class LargeFireball extends Fireball {
    public int explosionPower;

    public LargeFireball(EntityType<? extends LargeFireball> entityType, Level level) {
        super(entityType, level);
        this.explosionPower = 1;
    }

    public LargeFireball(Level level, double d, double d2, double d3, double d4, double d5, double d6) {
        super(EntityType.FIREBALL, d, d2, d3, d4, d5, d6, level);
        this.explosionPower = 1;
    }

    public LargeFireball(Level level, LivingEntity livingEntity, double d, double d2, double d3) {
        super(EntityType.FIREBALL, livingEntity, d, d2, d3, level);
        this.explosionPower = 1;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
            boolean z = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level.explode(null, getX(), getY(), getZ(), this.explosionPower, z, z ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
            remove();
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level.isClientSide) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        Entity owner = getOwner();
        entity.hurt(DamageSource.fireball(this, owner), 6.0f);
        if (owner instanceof LivingEntity) {
            doEnchantDamageEffects((LivingEntity) owner, entity);
        }
    }

    @Override // net.minecraft.world.entity.projectile.Fireball, net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("ExplosionPower", this.explosionPower);
    }

    @Override // net.minecraft.world.entity.projectile.Fireball, net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("ExplosionPower", 99)) {
            this.explosionPower = compoundTag.getInt("ExplosionPower");
        }
    }
}
