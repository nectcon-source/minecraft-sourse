package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/DragonFireball.class */
public class DragonFireball extends AbstractHurtingProjectile {
    public DragonFireball(EntityType<? extends DragonFireball> entityType, Level level) {
        super(entityType, level);
    }

    public DragonFireball(Level level, double d, double d2, double d3, double d4, double d5, double d6) {
        super(EntityType.DRAGON_FIREBALL, d, d2, d3, d4, d5, d6, level);
    }

    public DragonFireball(Level level, LivingEntity livingEntity, double d, double d2, double d3) {
        super(EntityType.DRAGON_FIREBALL, livingEntity, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        Entity owner = getOwner();
        if ((hitResult.getType() != HitResult.Type.ENTITY || !((EntityHitResult) hitResult).getEntity().is(owner)) && !this.level.isClientSide) {
            List<LivingEntity> entitiesOfClass = this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0d, 2.0d, 4.0d));
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level, getX(), getY(), getZ());
            if (owner instanceof LivingEntity) {
                areaEffectCloud.setOwner((LivingEntity) owner);
            }
            areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
            areaEffectCloud.setRadius(3.0f);
            areaEffectCloud.setDuration(600);
            areaEffectCloud.setRadiusPerTick((7.0f - areaEffectCloud.getRadius()) / areaEffectCloud.getDuration());
            areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
            if (!entitiesOfClass.isEmpty()) {
                Iterator<LivingEntity> it = entitiesOfClass.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    LivingEntity next = it.next();
                    if (distanceToSqr(next) < 16.0d) {
                        areaEffectCloud.setPos(next.getX(), next.getY(), next.getZ());
                        break;
                    }
                }
            }
            this.level.levelEvent(2006, blockPosition(), isSilent() ? -1 : 1);
            this.level.addFreshEntity(areaEffectCloud);
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

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile
    protected boolean shouldBurn() {
        return false;
    }
}
