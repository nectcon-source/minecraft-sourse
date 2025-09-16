package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/SmallFireball.class */
public class SmallFireball extends Fireball {
    public SmallFireball(EntityType<? extends SmallFireball> entityType, Level level) {
        super(entityType, level);
    }

    public SmallFireball(Level level, LivingEntity livingEntity, double d, double d2, double d3) {
        super(EntityType.SMALL_FIREBALL, livingEntity, d, d2, d3, level);
    }

    public SmallFireball(Level level, double d, double d2, double d3, double d4, double d5, double d6) {
        super(EntityType.SMALL_FIREBALL, d, d2, d3, d4, d5, d6, level);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level.isClientSide) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        if (!entity.fireImmune()) {
            Entity owner = getOwner();
            int remainingFireTicks = entity.getRemainingFireTicks();
            entity.setSecondsOnFire(5);
            if (!entity.hurt(DamageSource.fireball(this, owner), 5.0f)) {
                entity.setRemainingFireTicks(remainingFireTicks);
            } else if (owner instanceof LivingEntity) {
                doEnchantDamageEffects((LivingEntity) owner, entity);
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level.isClientSide) {
            return;
        }
        Entity owner = getOwner();
        if (owner == null || !(owner instanceof Mob) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos relative = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            if (this.level.isEmptyBlock(relative)) {
                this.level.setBlockAndUpdate(relative, BaseFireBlock.getState(this.level, relative));
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
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
}
