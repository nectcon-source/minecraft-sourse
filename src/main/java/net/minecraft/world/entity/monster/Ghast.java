package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ghast.class */
public class Ghast extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(Ghast.class, EntityDataSerializers.BOOLEAN);
    private int explosionPower;

    public Ghast(EntityType<? extends Ghast> entityType, Level level) {
        super(entityType, level);
        this.explosionPower = 1;
        this.xpReward = 5;
        this.moveControl = new GhastMoveControl(this);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
        this.goalSelector.addGoal(7, new Ghast.GhastShootFireballGoal(this));
        this.targetSelector
                .addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, var1 -> Math.abs(var1.getY() - this.getY()) <= 4.0));
        }

    public boolean isCharging() {
        return ((Boolean) this.entityData.get(DATA_IS_CHARGING)).booleanValue();
    }

    public void setCharging(boolean z) {
        this.entityData.set(DATA_IS_CHARGING, Boolean.valueOf(z));
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    @Override // net.minecraft.world.entity.Mob
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if ((damageSource.getDirectEntity() instanceof LargeFireball) && (damageSource.getEntity() instanceof Player)) {
            super.hurt(damageSource, 1000.0f);
            return true;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0d).add(Attributes.FOLLOW_RANGE, 100.0d);
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GHAST_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getSoundVolume() {
        return 5.0f;
    }

    public static boolean checkGhastSpawnRules(EntityType<Ghast> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(20) == 0 && checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("ExplosionPower", this.explosionPower);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("ExplosionPower", 99)) {
            this.explosionPower = compoundTag.getInt("ExplosionPower");
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ghast$GhastMoveControl.class */
    static class GhastMoveControl extends MoveControl {
        private final Ghast ghast;
        private int floatDuration;

        public GhastMoveControl(Ghast ghast) {
            super(ghast);
            this.ghast = ghast;
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                return;
            }
            int i = this.floatDuration;
            this.floatDuration = i - 1;
            if (i <= 0) {
                this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
                Vec3 vec3 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
                double length = vec3.length();
                Vec3 normalize = vec3.normalize();
                if (canReach(normalize, Mth.ceil(length))) {
                    this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(normalize.scale(0.1d)));
                } else {
                    this.operation = MoveControl.Operation.WAIT;
                }
            }
        }

        private boolean canReach(Vec3 vec3, int i) {
            AABB boundingBox = this.ghast.getBoundingBox();
            for (int i2 = 1; i2 < i; i2++) {
                boundingBox = boundingBox.move(vec3);
                if (!this.ghast.level.noCollision(this.ghast, boundingBox)) {
                    return false;
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ghast$RandomFloatAroundGoal.class */
    static class RandomFloatAroundGoal extends Goal {
        private final Ghast ghast;

        public RandomFloatAroundGoal(Ghast ghast) {
            this.ghast = ghast;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            MoveControl moveControl = this.ghast.getMoveControl();
            if (!moveControl.hasWanted()) {
                return true;
            }
            double wantedX = moveControl.getWantedX() - this.ghast.getX();
            double wantedY = moveControl.getWantedY() - this.ghast.getY();
            double wantedZ = moveControl.getWantedZ() - this.ghast.getZ();
            double d = (wantedX * wantedX) + (wantedY * wantedY) + (wantedZ * wantedZ);
            if (d < 1.0d || d > 3600.0d) {
                return true;
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Random random = this.ghast.getRandom();
            this.ghast.getMoveControl().setWantedPosition(this.ghast.getX() + (((random.nextFloat() * 2.0f) - 1.0f) * 16.0f), this.ghast.getY() + (((random.nextFloat() * 2.0f) - 1.0f) * 16.0f), this.ghast.getZ() + (((random.nextFloat() * 2.0f) - 1.0f) * 16.0f), 1.0d);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ghast$GhastLookGoal.class */
    static class GhastLookGoal extends Goal {
        private final Ghast ghast;

        public GhastLookGoal(Ghast ghast) {
            this.ghast = ghast;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.ghast.getTarget() == null) {
                Vec3 deltaMovement = this.ghast.getDeltaMovement();
                this.ghast.yRot = (-((float) Mth.atan2(deltaMovement.x, deltaMovement.z))) * 57.295776f;
                this.ghast.yBodyRot = this.ghast.yRot;
                return;
            }
            LivingEntity target = this.ghast.getTarget();
            if (target.distanceToSqr(this.ghast) < 4096.0d) {
                double x = target.getX() - this.ghast.getX();
                double z = target.getZ() - this.ghast.getZ();
                this.ghast.yRot = (-((float) Mth.atan2(x, z))) * 57.295776f;
                this.ghast.yBodyRot = this.ghast.yRot;
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ghast$GhastShootFireballGoal.class */
    static class GhastShootFireballGoal extends Goal {
        private final Ghast ghast;
        public int chargeTime;

        public GhastShootFireballGoal(Ghast ghast) {
            this.ghast = ghast;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.chargeTime = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.ghast.setCharging(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            LivingEntity target = this.ghast.getTarget();
            if (target.distanceToSqr(this.ghast) < 4096.0d && this.ghast.canSee(target)) {
                Level level = this.ghast.level;
                this.chargeTime++;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                    level.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
                }
                if (this.chargeTime == 20) {
                    Vec3 viewVector = this.ghast.getViewVector(1.0f);
                    double x = target.getX() - (this.ghast.getX() + (viewVector.x * 4.0d));
                    double y = target.getY(0.5d) - (0.5d + this.ghast.getY(0.5d));
                    double z = target.getZ() - (this.ghast.getZ() + (viewVector.z * 4.0d));
                    if (!this.ghast.isSilent()) {
                        level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                    }
                    LargeFireball largeFireball = new LargeFireball(level, this.ghast, x, y, z);
                    largeFireball.explosionPower = this.ghast.getExplosionPower();
                    largeFireball.setPos(this.ghast.getX() + (viewVector.x * 4.0d), this.ghast.getY(0.5d) + 0.5d, largeFireball.getZ() + (viewVector.z * 4.0d));
                    level.addFreshEntity(largeFireball);
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                this.chargeTime--;
            }
            this.ghast.setCharging(this.chargeTime > 10);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 2.6f;
    }
}
