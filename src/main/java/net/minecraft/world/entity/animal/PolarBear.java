package net.minecraft.world.entity.animal;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/PolarBear.class */
public class PolarBear extends Animal implements NeutralMob {
    private float clientSideStandAnimationO;
    private float clientSideStandAnimation;
    private int warningSoundTicks;
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    private static final EntityDataAccessor<Boolean> DATA_STANDING_ID = SynchedEntityData.defineId(PolarBear.class, EntityDataSerializers.BOOLEAN);
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    public PolarBear(EntityType<? extends PolarBear> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.AgableMob
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return EntityType.POLAR_BEAR.create(serverLevel);
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PolarBearMeleeAttackGoal());
        this.goalSelector.addGoal(1, new PolarBearPanicGoal());
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25d));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new PolarBearHurtByTargetGoal());
        this.targetSelector.addGoal(2, new PolarBearAttackPlayersGoal());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Fox.class, 10, true, true, null));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0d).add(Attributes.FOLLOW_RANGE, 20.0d).add(Attributes.MOVEMENT_SPEED, 0.25d).add(Attributes.ATTACK_DAMAGE, 6.0d);
    }

    public static boolean checkPolarBearSpawnRules(EntityType<PolarBear> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        Optional<ResourceKey<Biome>> biomeName = levelAccessor.getBiomeName(blockPos);
        if (Objects.equals(biomeName, Optional.of(Biomes.FROZEN_OCEAN)) || Objects.equals(biomeName, Optional.of(Biomes.DEEP_FROZEN_OCEAN))) {
            return levelAccessor.getRawBrightness(blockPos, 0) > 8 && levelAccessor.getBlockState(blockPos.below()).is(Blocks.ICE);
        }
        return checkAnimalSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        readPersistentAngerSaveData((ServerLevel) this.level, compoundTag);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        addPersistentAngerSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setRemainingPersistentAngerTime(int i) {
        this.remainingPersistentAngerTime = i;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (isBaby()) {
            return SoundEvents.POLAR_BEAR_AMBIENT_BABY;
        }
        return SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.POLAR_BEAR_STEP, 0.15f, 1.0f);
    }

    protected void playWarningSound() {
        if (this.warningSoundTicks <= 0) {
            playSound(SoundEvents.POLAR_BEAR_WARNING, 1.0f, getVoicePitch());
            this.warningSoundTicks = 40;
        }
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STANDING_ID, false);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.clientSideStandAnimation != this.clientSideStandAnimationO) {
                refreshDimensions();
            }
            this.clientSideStandAnimationO = this.clientSideStandAnimation;
            if (isStanding()) {
                this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation + 1.0f, 0.0f, 6.0f);
            } else {
                this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation - 1.0f, 0.0f, 6.0f);
            }
        }
        if (this.warningSoundTicks > 0) {
            this.warningSoundTicks--;
        }
        if (!this.level.isClientSide) {
            updatePersistentAnger((ServerLevel) this.level, true);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        if (this.clientSideStandAnimation > 0.0f) {
            return super.getDimensions(pose).scale(1.0f, 1.0f + (this.clientSideStandAnimation / 6.0f));
        }
        return super.getDimensions(pose);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = entity.hurt(DamageSource.mobAttack(this), (int) getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            doEnchantDamageEffects(this, entity);
        }
        return hurt;
    }

    public boolean isStanding() {
        return ((Boolean) this.entityData.get(DATA_STANDING_ID)).booleanValue();
    }

    public void setStanding(boolean z) {
        this.entityData.set(DATA_STANDING_ID, Boolean.valueOf(z));
    }

    public float getStandingAnimationScale(float f) {
        return Mth.lerp(f, this.clientSideStandAnimationO, this.clientSideStandAnimation) / 6.0f;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getWaterSlowDown() {
        return 0.98f;
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData(1.0f);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/PolarBear$PolarBearHurtByTargetGoal.class */
    class PolarBearHurtByTargetGoal extends HurtByTargetGoal {
        public PolarBearHurtByTargetGoal() {
            super(PolarBear.this, new Class[0]);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.HurtByTargetGoal, net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            if (PolarBear.this.isBaby()) {
                alertOthers();
                stop();
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.HurtByTargetGoal
        protected void alertOther(Mob mob, LivingEntity livingEntity) {
            if ((mob instanceof PolarBear) && !mob.isBaby()) {
                super.alertOther(mob, livingEntity);
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/PolarBear$PolarBearAttackPlayersGoal.class */
    class PolarBearAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
        public PolarBearAttackPlayersGoal() {
            super(PolarBear.this, Player.class, 20, true, true, null);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (!PolarBear.this.isBaby() && super.canUse()) {
                Iterator<PolarBear> it = PolarBear.this.level.getEntitiesOfClass(PolarBear.class, PolarBear.this.getBoundingBox().inflate(8.0d, 4.0d, 8.0d)).iterator();
                while (it.hasNext()) {
                    if (it.next().isBaby()) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.5d;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/PolarBear$PolarBearMeleeAttackGoal.class */
    class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
        public PolarBearMeleeAttackGoal() {
            super(PolarBear.this, 1.25d, true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
            double attackReachSqr = getAttackReachSqr(livingEntity);
            if (d <= attackReachSqr && isTimeToAttack()) {
                resetAttackCooldown();
                this.mob.doHurtTarget(livingEntity);
                PolarBear.this.setStanding(false);
            } else {
                if (d <= attackReachSqr * 2.0d) {
                    if (isTimeToAttack()) {
                        PolarBear.this.setStanding(false);
                        resetAttackCooldown();
                    }
                    if (getTicksUntilNextAttack() <= 10) {
                        PolarBear.this.setStanding(true);
                        PolarBear.this.playWarningSound();
                        return;
                    }
                    return;
                }
                resetAttackCooldown();
                PolarBear.this.setStanding(false);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            PolarBear.this.setStanding(false);
            super.stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            return 4.0f + livingEntity.getBbWidth();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/PolarBear$PolarBearPanicGoal.class */
    class PolarBearPanicGoal extends PanicGoal {
        public PolarBearPanicGoal() {
            super(PolarBear.this, 2.0d);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.PanicGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (!PolarBear.this.isBaby() && !PolarBear.this.isOnFire()) {
                return false;
            }
            return super.canUse();
        }
    }
}
