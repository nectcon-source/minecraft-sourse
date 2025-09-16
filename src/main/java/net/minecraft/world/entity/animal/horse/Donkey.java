package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Donkey.class */
public class Donkey extends AbstractChestedHorse {
    public Donkey(EntityType<? extends Donkey> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.DONKEY_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.DONKEY_ANGRY;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.DONKEY_DEATH;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.DONKEY_EAT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        super.getHurtSound(damageSource);
        return SoundEvents.DONKEY_HURT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        return ((animal instanceof Donkey) || (animal instanceof Horse)) && canParent() && ((AbstractHorse) animal).canParent();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        AbstractHorse abstractHorse = (AbstractHorse) (agableMob instanceof Horse ? EntityType.MULE : EntityType.DONKEY).create(serverLevel);
        setOffspringAttributes(agableMob, abstractHorse);
        return abstractHorse;
    }
}
