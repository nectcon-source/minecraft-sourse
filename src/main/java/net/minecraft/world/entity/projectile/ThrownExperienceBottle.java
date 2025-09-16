package net.minecraft.world.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrownExperienceBottle.class */
public class ThrownExperienceBottle extends ThrowableItemProjectile {
    public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownExperienceBottle(Level level, LivingEntity livingEntity) {
        super(EntityType.EXPERIENCE_BOTTLE, livingEntity, level);
    }

    public ThrownExperienceBottle(Level level, double d, double d2, double d3) {
        super(EntityType.EXPERIENCE_BOTTLE, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableItemProjectile
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableProjectile
    protected float getGravity() {
        return 0.07f;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
            this.level.levelEvent(2002, blockPosition(), PotionUtils.getColor(Potions.WATER));
            int nextInt = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5);
            while (nextInt > 0) {
                int experienceValue = ExperienceOrb.getExperienceValue(nextInt);
                nextInt -= experienceValue;
                this.level.addFreshEntity(new ExperienceOrb(this.level, getX(), getY(), getZ(), experienceValue));
            }
            remove();
        }
    }
}
