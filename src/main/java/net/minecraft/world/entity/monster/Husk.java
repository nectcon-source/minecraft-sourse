package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Husk.class */
public class Husk extends Zombie {
    public Husk(EntityType<? extends Husk> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkHuskSpawnRules(EntityType<Husk> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return checkMonsterSpawnRules(entityType, serverLevelAccessor, mobSpawnType, blockPos, random) && (mobSpawnType == MobSpawnType.SPAWNER || serverLevelAccessor.canSeeSky(blockPos));
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected boolean isSunSensitive() {
        return false;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HUSK_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected SoundEvent getStepSound() {
        return SoundEvents.HUSK_STEP;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        boolean doHurtTarget = super.doHurtTarget(entity);
        if (doHurtTarget && getMainHandItem().isEmpty() && (entity instanceof LivingEntity)) {
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * ((int) this.level.getCurrentDifficultyAt(blockPosition()).getEffectiveDifficulty())));
        }
        return doHurtTarget;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected boolean convertsInWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected void doUnderWaterConversion() {
        convertToZombieType(EntityType.ZOMBIE);
        if (!isSilent()) {
            this.level.levelEvent(null, 1041, blockPosition(), 0);
        }
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
}
