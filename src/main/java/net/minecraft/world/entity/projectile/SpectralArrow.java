package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/SpectralArrow.class */
public class SpectralArrow extends AbstractArrow {
    private int duration;

    public SpectralArrow(EntityType<? extends SpectralArrow> entityType, Level level) {
        super(entityType, level);
        this.duration = 200;
    }

    public SpectralArrow(Level level, LivingEntity livingEntity) {
        super(EntityType.SPECTRAL_ARROW, livingEntity, level);
        this.duration = 200;
    }

    public SpectralArrow(Level level, double d, double d2, double d3) {
        super(EntityType.SPECTRAL_ARROW, d, d2, d3, level);
        this.duration = 200;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.level.isClientSide && !this.inGround) {
            this.level.addParticle(ParticleTypes.INSTANT_EFFECT, getX(), getY(), getZ(), 0.0d, 0.0d, 0.0d);
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.SPECTRAL_ARROW);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected void doPostHurtEffects(LivingEntity livingEntity) {
        super.doPostHurtEffects(livingEntity);
        livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, this.duration, 0));
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Duration")) {
            this.duration = compoundTag.getInt("Duration");
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Duration", this.duration);
    }
}
