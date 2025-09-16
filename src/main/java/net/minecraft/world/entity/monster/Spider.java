package net.minecraft.world.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Spider.class */
public class Spider extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);

    public Spider(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(4, new SpiderAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8d));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new SpiderTargetGoal(this, Player.class));
        this.targetSelector.addGoal(3, new SpiderTargetGoal(this, IronGolem.class));
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return getBbHeight() * 0.5f;
    }

    @Override // net.minecraft.world.entity.Mob
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            setClimbing(this.horizontalCollision);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean onClimbable() {
        return isClimbing();
    }

    @Override // net.minecraft.world.entity.Entity
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
        if (!blockState.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(blockState, vec3);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.POISON) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    public boolean isClimbing() {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & 1) != 0;
    }

    public void setClimbing(boolean z) {
        byte b;
        byte byteValue = ((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue();
        if (z) {
            b = (byte) (byteValue | 1);
        } else {
            b = (byte) (byteValue & (-2));
        }
        this.entityData.set(DATA_FLAGS_ID, Byte.valueOf(b));
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        MobEffect mobEffect;
        SpawnGroupData finalizeSpawn = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        if (serverLevelAccessor.getRandom().nextInt(100) == 0) {
            Skeleton create = EntityType.SKELETON.create(this.level);
            create.moveTo(getX(), getY(), getZ(), this.yRot, 0.0f);
            create.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, null, null);
            create.startRiding(this);
        }
        if (finalizeSpawn == null) {
            finalizeSpawn = new SpiderEffectsGroupData();
            if (serverLevelAccessor.getDifficulty() == Difficulty.HARD && serverLevelAccessor.getRandom().nextFloat() < 0.1f * difficultyInstance.getSpecialMultiplier()) {
                ((SpiderEffectsGroupData) finalizeSpawn).setRandomEffect(serverLevelAccessor.getRandom());
            }
        }
        if ((finalizeSpawn instanceof SpiderEffectsGroupData) && (mobEffect = ((SpiderEffectsGroupData) finalizeSpawn).effect) != null) {
            addEffect(new MobEffectInstance(mobEffect, Integer.MAX_VALUE));
        }
        return finalizeSpawn;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.65f;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Spider$SpiderEffectsGroupData.class */
    public static class SpiderEffectsGroupData implements SpawnGroupData {
        public MobEffect effect;

        public void setRandomEffect(Random random) {
            int nextInt = random.nextInt(5);
            if (nextInt <= 1) {
                this.effect = MobEffects.MOVEMENT_SPEED;
                return;
            }
            if (nextInt <= 2) {
                this.effect = MobEffects.DAMAGE_BOOST;
            } else if (nextInt <= 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (nextInt <= 4) {
                this.effect = MobEffects.INVISIBILITY;
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Spider$SpiderAttackGoal.class */
    static class SpiderAttackGoal extends MeleeAttackGoal {
        public SpiderAttackGoal(Spider spider) {
            super(spider, 1.0d, true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            if (this.mob.getBrightness() >= 0.5f && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            return 4.0f + livingEntity.getBbWidth();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Spider$SpiderTargetGoal.class */
    static class SpiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public SpiderTargetGoal(Spider spider, Class<T> cls) {
            super(spider, cls, true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (this.mob.getBrightness() >= 0.5f) {
                return false;
            }
            return super.canUse();
        }
    }
}
