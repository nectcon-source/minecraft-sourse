package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Blaze.class */
public class Blaze extends Monster {
    private float allowedHeightOffset;
    private int nextHeightOffsetChangeTick;
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Blaze.class, EntityDataSerializers.BYTE);

    public Blaze(EntityType<? extends Blaze> entityType, Level level) {
        super(entityType, level);
        this.allowedHeightOffset = 0.5f;
        setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
        setPathfindingMalus(BlockPathTypes.LAVA, 8.0f);
        setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0f);
        setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0f);
        this.xpReward = 10;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new BlazeAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0d));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0d, 0.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0d).add(Attributes.MOVEMENT_SPEED, 0.23000000417232513d).add(Attributes.FOLLOW_RANGE, 48.0d);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBrightness() {
        return 1.0f;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (!this.onGround && getDeltaMovement().y < 0.0d) {
            setDeltaMovement(getDeltaMovement().multiply(1.0d, 0.6d, 1.0d));
        }
        if (this.level.isClientSide) {
            if (this.random.nextInt(24) == 0 && !isSilent()) {
                this.level.playLocalSound(getX() + 0.5d, getY() + 0.5d, getZ() + 0.5d, SoundEvents.BLAZE_BURN, getSoundSource(), 1.0f + this.random.nextFloat(), (this.random.nextFloat() * 0.7f) + 0.3f, false);
            }
            for (int i = 0; i < 2; i++) {
                this.level.addParticle(ParticleTypes.LARGE_SMOKE, getRandomX(0.5d), getRandomY(), getRandomZ(0.5d), 0.0d, 0.0d, 0.0d);
            }
        }
        super.aiStep();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        this.nextHeightOffsetChangeTick--;
        if (this.nextHeightOffsetChangeTick <= 0) {
            this.nextHeightOffsetChangeTick = 100;
            this.allowedHeightOffset = 0.5f + (((float) this.random.nextGaussian()) * 3.0f);
        }
        LivingEntity target = getTarget();
        if (target != null && target.getEyeY() > getEyeY() + this.allowedHeightOffset && canAttack(target)) {
            setDeltaMovement(getDeltaMovement().add(0.0d, (0.30000001192092896d - getDeltaMovement().y) * 0.30000001192092896d, 0.0d));
            this.hasImpulse = true;
        }
        super.customServerAiStep();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isOnFire() {
        return isCharged();
    }

    private boolean isCharged() {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & 1) != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCharged(boolean z) {
        byte b;
        byte byteValue = ((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue();
        if (z) {
            b = (byte) (byteValue | 1);
        } else {
            b = (byte) (byteValue & (-2));
        }
        this.entityData.set(DATA_FLAGS_ID, Byte.valueOf(b));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Blaze$BlazeAttackGoal.class */
    static class BlazeAttackGoal extends Goal {
        private final Blaze blaze;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public BlazeAttackGoal(Blaze blaze) {
            this.blaze = blaze;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target = this.blaze.getTarget();
            return target != null && target.isAlive() && this.blaze.canAttack(target);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.attackStep = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.blaze.setCharged(false);
            this.lastSeen = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.attackTime--;
            LivingEntity target = this.blaze.getTarget();
            if (target == null) {
                return;
            }
            boolean canSee = this.blaze.getSensing().canSee(target);
            if (canSee) {
                this.lastSeen = 0;
            } else {
                this.lastSeen++;
            }
            double distanceToSqr = this.blaze.distanceToSqr(target);
            if (distanceToSqr < 4.0d) {
                if (!canSee) {
                    return;
                }
                if (this.attackTime <= 0) {
                    this.attackTime = 20;
                    this.blaze.doHurtTarget(target);
                }
                this.blaze.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0d);
            } else if (distanceToSqr < getFollowDistance() * getFollowDistance() && canSee) {
                double x = target.getX() - this.blaze.getX();
                double y = target.getY(0.5d) - this.blaze.getY(0.5d);
                double z = target.getZ() - this.blaze.getZ();
                if (this.attackTime <= 0) {
                    this.attackStep++;
                    if (this.attackStep == 1) {
                        this.attackTime = 60;
                        this.blaze.setCharged(true);
                    } else if (this.attackStep <= 4) {
                        this.attackTime = 6;
                    } else {
                        this.attackTime = 100;
                        this.attackStep = 0;
                        this.blaze.setCharged(false);
                    }
                    if (this.attackStep > 1) {
                        float sqrt = Mth.sqrt(Mth.sqrt(distanceToSqr)) * 0.5f;
                        if (!this.blaze.isSilent()) {
                            this.blaze.level.levelEvent(null, 1018, this.blaze.blockPosition(), 0);
                        }
                        for (int i = 0; i < 1; i++) {
                            SmallFireball smallFireball = new SmallFireball(this.blaze.level, this.blaze, x + (this.blaze.getRandom().nextGaussian() * sqrt), y, z + (this.blaze.getRandom().nextGaussian() * sqrt));
                            smallFireball.setPos(smallFireball.getX(), this.blaze.getY(0.5d) + 0.5d, smallFireball.getZ());
                            this.blaze.level.addFreshEntity(smallFireball);
                        }
                    }
                }
                this.blaze.getLookControl().setLookAt(target, 10.0f, 10.0f);
            } else if (this.lastSeen < 5) {
                this.blaze.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0d);
            }
            super.tick();
        }

        private double getFollowDistance() {
            return this.blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
        }
    }
}
