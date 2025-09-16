package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/WitherSkeleton.class */
public class WitherSkeleton extends AbstractSkeleton {
    public WitherSkeleton(EntityType<? extends WitherSkeleton> entityType, Level level) {
        super(entityType, level);
        setPathfindingMalus(BlockPathTypes.LAVA, 8.0f);
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractPiglin.class, true));
        super.registerGoals();
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton
    SoundEvent getStepSound() {
        return SoundEvents.WITHER_SKELETON_STEP;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        Entity entity = damageSource.getEntity();
        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;
            if (creeper.canDropMobsSkull()) {
                creeper.increaseDroppedSkulls();
                spawnAtLocation(Items.WITHER_SKELETON_SKULL);
            }
        }
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton, net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentEnchantments(DifficultyInstance difficultyInstance) {
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData finalizeSpawn = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0d);
        reassessWeaponGoal();
        return finalizeSpawn;
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton, net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 2.1f;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        if (!super.doHurtTarget(entity)) {
            return false;
        }
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton
    protected AbstractArrow getArrow(ItemStack itemStack, float f) {
        AbstractArrow arrow = super.getArrow(itemStack, f);
        arrow.setSecondsOnFire(100);
        return arrow;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }
}
