package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Guardian.class */
public class Guardian extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.INT);
    private float clientSideTailAnimation;
    private float clientSideTailAnimationO;
    private float clientSideTailAnimationSpeed;
    private float clientSideSpikesAnimation;
    private float clientSideSpikesAnimationO;
    private LivingEntity clientSideCachedAttackTarget;
    private int clientSideAttackTime;
    private boolean clientSideTouchedGround;
    protected RandomStrollGoal randomStrollGoal;

    public Guardian(EntityType<? extends Guardian> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
        this.moveControl = new GuardianMoveControl(this);
        this.clientSideTailAnimation = this.random.nextFloat();
        this.clientSideTailAnimationO = this.clientSideTailAnimation;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        MoveTowardsRestrictionGoal moveTowardsRestrictionGoal = new MoveTowardsRestrictionGoal(this, 1.0d);
        this.randomStrollGoal = new RandomStrollGoal(this, 1.0d, 80);
        this.goalSelector.addGoal(4, new GuardianAttackGoal(this));
        this.goalSelector.addGoal(5, moveTowardsRestrictionGoal);
        this.goalSelector.addGoal(7, this.randomStrollGoal);
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Guardian.class, 12.0f, 0.01f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        moveTowardsRestrictionGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, LivingEntity.class, 10, true, false, new GuardianAttackSelector(this)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0d).add(Attributes.MOVEMENT_SPEED, 0.5d).add(Attributes.FOLLOW_RANGE, 16.0d).add(Attributes.MAX_HEALTH, 30.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_MOVING, false);
        this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.WATER;
    }

    public boolean isMoving() {
        return ((Boolean) this.entityData.get(DATA_ID_MOVING)).booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setMoving(boolean z) {
        this.entityData.set(DATA_ID_MOVING, Boolean.valueOf(z));
    }

    public int getAttackDuration() {
        return 80;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setActiveAttackTarget(int i) {
        this.entityData.set(DATA_ID_ATTACK_TARGET, Integer.valueOf(i));
    }

    public boolean hasActiveAttackTarget() {
        return ((Integer) this.entityData.get(DATA_ID_ATTACK_TARGET)).intValue() != 0;
    }

    @Nullable
    public LivingEntity getActiveAttackTarget() {
        if (!hasActiveAttackTarget()) {
            return null;
        }
        if (this.level.isClientSide) {
            if (this.clientSideCachedAttackTarget != null) {
                return this.clientSideCachedAttackTarget;
            }
            Entity entity = this.level.getEntity(((Integer) this.entityData.get(DATA_ID_ATTACK_TARGET)).intValue());
            if (entity instanceof LivingEntity) {
                this.clientSideCachedAttackTarget = (LivingEntity) entity;
                return this.clientSideCachedAttackTarget;
            }
            return null;
        }
        return getTarget();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_ID_ATTACK_TARGET.equals(entityDataAccessor)) {
            this.clientSideAttackTime = 0;
            this.clientSideCachedAttackTarget = null;
        }
    }

    @Override // net.minecraft.world.entity.Mob
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return isInWaterOrBubble() ? SoundEvents.GUARDIAN_AMBIENT : SoundEvents.GUARDIAN_AMBIENT_LAND;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return isInWaterOrBubble() ? SoundEvents.GUARDIAN_HURT : SoundEvents.GUARDIAN_HURT_LAND;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return isInWaterOrBubble() ? SoundEvents.GUARDIAN_DEATH : SoundEvents.GUARDIAN_DEATH_LAND;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.5f;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getFluidState(blockPos).is(FluidTags.WATER)) {
            return (10.0f + levelReader.getBrightness(blockPos)) - 0.5f;
        }
        return super.getWalkTargetValue(blockPos, levelReader);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (isAlive()) {
            if (this.level.isClientSide) {
                this.clientSideTailAnimationO = this.clientSideTailAnimation;
                if (!isInWater()) {
                    this.clientSideTailAnimationSpeed = 2.0f;
                    Vec3 deltaMovement = getDeltaMovement();
                    if (deltaMovement.y > 0.0d && this.clientSideTouchedGround && !isSilent()) {
                        this.level.playLocalSound(getX(), getY(), getZ(), getFlopSound(), getSoundSource(), 1.0f, 1.0f, false);
                    }
                    this.clientSideTouchedGround = deltaMovement.y < 0.0d && this.level.loadedAndEntityCanStandOn(blockPosition().below(), this);
                } else if (!isMoving()) {
                    this.clientSideTailAnimationSpeed += (0.125f - this.clientSideTailAnimationSpeed) * 0.2f;
                } else if (this.clientSideTailAnimationSpeed < 0.5f) {
                    this.clientSideTailAnimationSpeed = 4.0f;
                } else {
                    this.clientSideTailAnimationSpeed += (0.5f - this.clientSideTailAnimationSpeed) * 0.1f;
                }
                this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
                this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
                if (!isInWaterOrBubble()) {
                    this.clientSideSpikesAnimation = this.random.nextFloat();
                } else if (isMoving()) {
                    this.clientSideSpikesAnimation += (0.0f - this.clientSideSpikesAnimation) * 0.25f;
                } else {
                    this.clientSideSpikesAnimation += (1.0f - this.clientSideSpikesAnimation) * 0.06f;
                }
                if (isMoving() && isInWater()) {
                    Vec3 viewVector = getViewVector(0.0f);
                    for (int i = 0; i < 2; i++) {
                        this.level.addParticle(ParticleTypes.BUBBLE, getRandomX(0.5d) - (viewVector.x * 1.5d), getRandomY() - (viewVector.y * 1.5d), getRandomZ(0.5d) - (viewVector.z * 1.5d), 0.0d, 0.0d, 0.0d);
                    }
                }
                if (hasActiveAttackTarget()) {
                    if (this.clientSideAttackTime < getAttackDuration()) {
                        this.clientSideAttackTime++;
                    }
                    LivingEntity activeAttackTarget = getActiveAttackTarget();
                    if (activeAttackTarget != null) {
                        getLookControl().setLookAt(activeAttackTarget, 90.0f, 90.0f);
                        getLookControl().tick();
                        double attackAnimationScale = getAttackAnimationScale(0.0f);
                        double x = activeAttackTarget.getX() - getX();
                        double y = activeAttackTarget.getY(0.5d) - getEyeY();
                        double z = activeAttackTarget.getZ() - getZ();
                        double sqrt = Math.sqrt((x * x) + (y * y) + (z * z));
                        double d = x / sqrt;
                        double d2 = y / sqrt;
                        double d3 = z / sqrt;
                        double nextDouble = this.random.nextDouble();
                        while (nextDouble < sqrt) {
                            nextDouble += (1.8d - attackAnimationScale) + (this.random.nextDouble() * (1.7d - attackAnimationScale));
                            this.level.addParticle(ParticleTypes.BUBBLE, getX() + (d * nextDouble), getEyeY() + (d2 * nextDouble), getZ() + (d3 * nextDouble), 0.0d, 0.0d, 0.0d);
                        }
                    }
                }
            }
            if (isInWaterOrBubble()) {
                setAirSupply(300);
            } else if (this.onGround) {
                setDeltaMovement(getDeltaMovement().add(((this.random.nextFloat() * 2.0f) - 1.0f) * 0.4f, 0.5d, ((this.random.nextFloat() * 2.0f) - 1.0f) * 0.4f));
                this.yRot = this.random.nextFloat() * 360.0f;
                this.onGround = false;
                this.hasImpulse = true;
            }
            if (hasActiveAttackTarget()) {
                this.yRot = this.yHeadRot;
            }
        }
        super.aiStep();
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.GUARDIAN_FLOP;
    }

    public float getTailAnimation(float f) {
        return Mth.lerp(f, this.clientSideTailAnimationO, this.clientSideTailAnimation);
    }

    public float getSpikesAnimation(float f) {
        return Mth.lerp(f, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
    }

    public float getAttackAnimationScale(float f) {
        return (this.clientSideAttackTime + f) / getAttackDuration();
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    public static boolean checkGuardianSpawnRules(EntityType<? extends Guardian> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return (random.nextInt(20) == 0 || !levelAccessor.canSeeSkyFromBelowWater(blockPos)) && levelAccessor.getDifficulty() != Difficulty.PEACEFUL && (mobSpawnType == MobSpawnType.SPAWNER || levelAccessor.getFluidState(blockPos).is(FluidTags.WATER));
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (!isMoving() && !damageSource.isMagic() && (damageSource.getDirectEntity() instanceof LivingEntity)) {
            LivingEntity livingEntity = (LivingEntity) damageSource.getDirectEntity();
            if (!damageSource.isExplosion()) {
                livingEntity.hurt(DamageSource.thorns(this), 2.0f);
            }
        }
        if (this.randomStrollGoal != null) {
            this.randomStrollGoal.trigger();
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(0.1f, vec3);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9d));
            if (!isMoving() && getTarget() == null) {
                setDeltaMovement(getDeltaMovement().add(0.0d, -0.005d, 0.0d));
                return;
            }
            return;
        }
        super.travel(vec3);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Guardian$GuardianAttackSelector.class */
    static class GuardianAttackSelector implements Predicate<LivingEntity> {
        private final Guardian guardian;

        public GuardianAttackSelector(Guardian guardian) {
            this.guardian = guardian;
        }

        @Override // java.util.function.Predicate
        public boolean test(@Nullable LivingEntity livingEntity) {
            return ((livingEntity instanceof Player) || (livingEntity instanceof Squid)) && livingEntity.distanceToSqr(this.guardian) > 9.0d;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Guardian$GuardianAttackGoal.class */
    static class GuardianAttackGoal extends Goal {
        private final Guardian guardian;
        private int attackTime;
        private final boolean elder;

        public GuardianAttackGoal(Guardian guardian) {
            this.guardian = guardian;
            this.elder = guardian instanceof ElderGuardian;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target = this.guardian.getTarget();
            return target != null && target.isAlive();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return super.canContinueToUse() && (this.elder || this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0d);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.attackTime = -10;
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(this.guardian.getTarget(), 90.0f, 90.0f);
            this.guardian.hasImpulse = true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.guardian.setActiveAttackTarget(0);
            this.guardian.setTarget(null);
            this.guardian.randomStrollGoal.trigger();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            LivingEntity target = this.guardian.getTarget();
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(target, 90.0f, 90.0f);
            if (!this.guardian.canSee(target)) {
                this.guardian.setTarget(null);
                return;
            }
            this.attackTime++;
            if (this.attackTime == 0) {
                this.guardian.setActiveAttackTarget(this.guardian.getTarget().getId());
                if (!this.guardian.isSilent()) {
                    this.guardian.level.broadcastEntityEvent(this.guardian, (byte) 21);
                }
            } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                float f = 1.0f;
                if (this.guardian.level.getDifficulty() == Difficulty.HARD) {
                    f = 1.0f + 2.0f;
                }
                if (this.elder) {
                    f += 2.0f;
                }
                target.hurt(DamageSource.indirectMagic(this.guardian, this.guardian), f);
                target.hurt(DamageSource.mobAttack(this.guardian), (float) this.guardian.getAttributeValue(Attributes.ATTACK_DAMAGE));
                this.guardian.setTarget(null);
            }
            super.tick();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Guardian$GuardianMoveControl.class */
    static class GuardianMoveControl extends MoveControl {
        private final Guardian guardian;

        public GuardianMoveControl(Guardian guardian) {
            super(guardian);
            this.guardian = guardian;
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO || this.guardian.getNavigation().isDone()) {
                this.guardian.setSpeed(0.0f);
                this.guardian.setMoving(false);
                return;
            }
            Vec3 vec3 = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double length = vec3.length();
            double d = vec3.x / length;
            double d2 = vec3.y / length;
            double d3 = vec3.z / length;
            this.guardian.yRot = rotlerp(this.guardian.yRot, ((float) (Mth.atan2(vec3.z, vec3.x) * 57.2957763671875d)) - 90.0f, 90.0f);
            this.guardian.yBodyRot = this.guardian.yRot;
            float lerp = Mth.lerp(0.125f, this.guardian.getSpeed(), (float) (this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            this.guardian.setSpeed(lerp);
            double sin = Math.sin((this.guardian.tickCount + this.guardian.getId()) * 0.5d) * 0.05d;
            double cos = Math.cos(this.guardian.yRot * 0.017453292f);
            double sin2 = Math.sin(this.guardian.yRot * 0.017453292f);
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add(sin * cos, (Math.sin((this.guardian.tickCount + this.guardian.getId()) * 0.75d) * 0.05d * (sin2 + cos) * 0.25d) + (lerp * d2 * 0.1d), sin * sin2));
            LookControl lookControl = this.guardian.getLookControl();
            double x = this.guardian.getX() + (d * 2.0d);
            double eyeY = this.guardian.getEyeY() + (d2 / length);
            double z = this.guardian.getZ() + (d3 * 2.0d);
            double wantedX = lookControl.getWantedX();
            double wantedY = lookControl.getWantedY();
            double wantedZ = lookControl.getWantedZ();
            if (!lookControl.isHasWanted()) {
                wantedX = x;
                wantedY = eyeY;
                wantedZ = z;
            }
            this.guardian.getLookControl().setLookAt(Mth.lerp(0.125d, wantedX, x), Mth.lerp(0.125d, wantedY, eyeY), Mth.lerp(0.125d, wantedZ, z), 10.0f, 40.0f);
            this.guardian.setMoving(true);
        }
    }
}
