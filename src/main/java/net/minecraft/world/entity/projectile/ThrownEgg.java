package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrownEgg.class */
public class ThrownEgg extends ThrowableItemProjectile {
    public ThrownEgg(EntityType<? extends ThrownEgg> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownEgg(Level level, LivingEntity livingEntity) {
        super(EntityType.EGG, livingEntity, level);
    }

    public ThrownEgg(Level level, double d, double d2, double d3) {
        super(EntityType.EGG, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 3) {
            for (int i = 0; i < 8; i++) {
                this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, getItem()), getX(), getY(), getZ(), (this.random.nextFloat() - 0.5d) * 0.08d, (this.random.nextFloat() - 0.5d) * 0.08d, (this.random.nextFloat() - 0.5d) * 0.08d);
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(DamageSource.thrown(this, getOwner()), 0.0f);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int i = 1;
                if (this.random.nextInt(32) == 0) {
                    i = 4;
                }
                for (int i2 = 0; i2 < i; i2++) {
                    Chicken create = EntityType.CHICKEN.create(this.level);
                    create.setAge(-24000);
                    create.moveTo(getX(), getY(), getZ(), this.yRot, 0.0f);
                    this.level.addFreshEntity(create);
                }
            }
            this.level.broadcastEntityEvent(this, (byte) 3);
            remove();
        }
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableItemProjectile
    protected Item getDefaultItem() {
        return Items.EGG;
    }
}
