package net.minecraft.world.entity.animal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Salmon.class */
public class Salmon extends AbstractSchoolingFish {
    public Salmon(EntityType<? extends Salmon> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.animal.AbstractSchoolingFish
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected ItemStack getBucketItemStack() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SALMON_HURT;
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected SoundEvent getFlopSound() {
        return SoundEvents.SALMON_FLOP;
    }
}
