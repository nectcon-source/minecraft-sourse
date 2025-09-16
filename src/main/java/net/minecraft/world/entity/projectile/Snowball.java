package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/Snowball.class */
public class Snowball extends ThrowableItemProjectile {
    public Snowball(EntityType<? extends Snowball> entityType, Level level) {
        super(entityType, level);
    }

    public Snowball(Level level, LivingEntity livingEntity) {
        super(EntityType.SNOWBALL, livingEntity, level);
    }

    public Snowball(Level level, double d, double d2, double d3) {
        super(EntityType.SNOWBALL, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableItemProjectile
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    private ParticleOptions getParticle() {
        ItemStack itemRaw = getItemRaw();
        return itemRaw.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, itemRaw);
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 3) {
            ParticleOptions particle = getParticle();
            for (int i = 0; i < 8; i++) {
                this.level.addParticle(particle, getX(), getY(), getZ(), 0.0d, 0.0d, 0.0d);
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.hurt(DamageSource.thrown(this, getOwner()), entity instanceof Blaze ? 3 : 0);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 3);
            remove();
        }
    }
}
