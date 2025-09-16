package net.minecraft.world.entity.monster;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Skeleton.class */
public class Skeleton extends AbstractSkeleton {
    public Skeleton(EntityType<? extends Skeleton> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton
    SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        Entity entity = damageSource.getEntity();
        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;
            if (creeper.canDropMobsSkull()) {
                creeper.increaseDroppedSkulls();
                spawnAtLocation(Items.SKELETON_SKULL);
            }
        }
    }
}
