package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Squid.class */
public class Squid extends WaterAnimal {
    public float xBodyRot;
    public float xBodyRotO;
    public float zBodyRot;
    public float zBodyRotO;
    public float tentacleMovement;
    public float oldTentacleMovement;
    public float tentacleAngle;
    public float oldTentacleAngle;
    private float speed;
    private float tentacleSpeed;
    private float rotateSpeed;

    /* renamed from: tx */
    private float tx;

    /* renamed from: ty */
    private float ty;

    /* renamed from: tz */
    private float tz;

    public Squid(EntityType<? extends Squid> entityType, Level level) {
        super(entityType, level);
        this.random.setSeed(getId());
        this.tentacleSpeed = (1.0f / (this.random.nextFloat() + 1.0f)) * 0.2f;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SquidRandomMovementGoal(this));
        this.goalSelector.addGoal(1, new SquidFleeGoal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0d);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.5f;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SQUID_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SQUID_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SQUID_DEATH;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.LivingEntity
    public float getSoundVolume() {
        return 0.4f;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        this.xBodyRotO = this.xBodyRot;
        this.zBodyRotO = this.zBodyRot;
        this.oldTentacleMovement = this.tentacleMovement;
        this.oldTentacleAngle = this.tentacleAngle;
        this.tentacleMovement += this.tentacleSpeed;
        if (this.tentacleMovement > 6.283185307179586d) {
            if (this.level.isClientSide) {
                this.tentacleMovement = 6.2831855f;
            } else {
                this.tentacleMovement = (float) (this.tentacleMovement - 6.283185307179586d);
                if (this.random.nextInt(10) == 0) {
                    this.tentacleSpeed = (1.0f / (this.random.nextFloat() + 1.0f)) * 0.2f;
                }
                this.level.broadcastEntityEvent(this, (byte) 19);
            }
        }
        if (isInWaterOrBubble()) {
            if (this.tentacleMovement < 3.1415927f) {
                float f = this.tentacleMovement / 3.1415927f;
                this.tentacleAngle = Mth.sin(f * f * 3.1415927f) * 3.1415927f * 0.25f;
                if (f > 0.75d) {
                    this.speed = 1.0f;
                    this.rotateSpeed = 1.0f;
                } else {
                    this.rotateSpeed *= 0.8f;
                }
            } else {
                this.tentacleAngle = 0.0f;
                this.speed *= 0.9f;
                this.rotateSpeed *= 0.99f;
            }
            if (!this.level.isClientSide) {
                setDeltaMovement(this.tx * this.speed, this.ty * this.speed, this.tz * this.speed);
            }
            Vec3 deltaMovement = getDeltaMovement();
            float sqrt = Mth.sqrt(getHorizontalDistanceSqr(deltaMovement));
            this.yBodyRot += (((-((float) Mth.atan2(deltaMovement.x, deltaMovement.z))) * 57.295776f) - this.yBodyRot) * 0.1f;
            this.yRot = this.yBodyRot;
            this.zBodyRot = (float) (this.zBodyRot + (3.141592653589793d * this.rotateSpeed * 1.5d));
            this.xBodyRot += (((-((float) Mth.atan2(sqrt, deltaMovement.y))) * 57.295776f) - this.xBodyRot) * 0.1f;
            return;
        }
        this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * 3.1415927f * 0.25f;
        if (!this.level.isClientSide) {
            double d = getDeltaMovement().y;
            if (hasEffect(MobEffects.LEVITATION)) {
                d = 0.05d * (getEffect(MobEffects.LEVITATION).getAmplifier() + 1);
            } else if (!isNoGravity()) {
                d -= 0.08d;
            }
            setDeltaMovement(0.0d, d * 0.9800000190734863d, 0.0d);
        }
        this.xBodyRot = (float) (this.xBodyRot + (((-90.0f) - this.xBodyRot) * 0.02d));
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (super.hurt(damageSource, f) && getLastHurtByMob() != null) {
            spawnInk();
            return true;
        }
        return false;
    }

    private Vec3 rotateVector(Vec3 vec3) {
        return vec3.xRot(this.xBodyRotO * 0.017453292f).yRot((-this.yBodyRotO) * 0.017453292f);
    }

    private void spawnInk() {
        playSound(SoundEvents.SQUID_SQUIRT, getSoundVolume(), getVoicePitch());
        Vec3 add = rotateVector(new Vec3(0.0d, -1.0d, 0.0d)).add(getX(), getY(), getZ());
        for (int i = 0; i < 30; i++) {
            Vec3 scale = rotateVector(new Vec3((this.random.nextFloat() * 0.6d) - 0.3d, -1.0d, (this.random.nextFloat() * 0.6d) - 0.3d)).scale(0.3d + (this.random.nextFloat() * 2.0f));
            ((ServerLevel) this.level).sendParticles(ParticleTypes.SQUID_INK, add.x, add.y + 0.5d, add.z, 0, scale.x, scale.y, scale.z, 0.10000000149011612d);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        move(MoverType.SELF, getDeltaMovement());
    }

    public static boolean checkSquidSpawnRules(EntityType<Squid> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return blockPos.getY() > 45 && blockPos.getY() < levelAccessor.getSeaLevel();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 19) {
            this.tentacleMovement = 0.0f;
        } else {
            super.handleEntityEvent(b);
        }
    }

    public void setMovementVector(float f, float f2, float f3) {
        this.tx = f;
        this.ty = f2;
        this.tz = f3;
    }

    public boolean hasMovementVector() {
        return (this.tx == 0.0f && this.ty == 0.0f && this.tz == 0.0f) ? false : true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Squid$SquidRandomMovementGoal.class */
    class SquidRandomMovementGoal extends Goal {
        private final Squid squid;

        public SquidRandomMovementGoal(Squid squid) {
            this.squid = squid;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.squid.getNoActionTime() > 100) {
                this.squid.setMovementVector(0.0f, 0.0f, 0.0f);
                return;
            }
            if (this.squid.getRandom().nextInt(50) == 0 || !this.squid.wasTouchingWater || !this.squid.hasMovementVector()) {
                float nextFloat = this.squid.getRandom().nextFloat() * 6.2831855f;
                this.squid.setMovementVector(Mth.cos(nextFloat) * 0.2f, (-0.1f) + (this.squid.getRandom().nextFloat() * 0.2f), Mth.sin(nextFloat) * 0.2f);
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Squid$SquidFleeGoal.class */
    class SquidFleeGoal extends Goal {
        private int fleeTicks;

        private SquidFleeGoal() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity lastHurtByMob = Squid.this.getLastHurtByMob();
            return Squid.this.isInWater() && lastHurtByMob != null && Squid.this.distanceToSqr(lastHurtByMob) < 100.0d;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.fleeTicks = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.fleeTicks++;
            LivingEntity lastHurtByMob = Squid.this.getLastHurtByMob();
            if (lastHurtByMob == null) {
                return;
            }
            Vec3 vec3 = new Vec3(Squid.this.getX() - lastHurtByMob.getX(), Squid.this.getY() - lastHurtByMob.getY(), Squid.this.getZ() - lastHurtByMob.getZ());
            BlockState blockState = Squid.this.level.getBlockState(new BlockPos(Squid.this.getX() + vec3.x, Squid.this.getY() + vec3.y, Squid.this.getZ() + vec3.z));
            if (Squid.this.level.getFluidState(new BlockPos(Squid.this.getX() + vec3.x, Squid.this.getY() + vec3.y, Squid.this.getZ() + vec3.z)).is(FluidTags.WATER) || blockState.isAir()) {
                double length = vec3.length();
                if (length > 0.0d) {
                    vec3.normalize();
                    float f = 3.0f;
                    if (length > 5.0d) {
                        f = (float) (3.0f - ((length - 5.0d) / 5.0d));
                    }
                    if (f > 0.0f) {
                        vec3 = vec3.scale(f);
                    }
                }
                if (blockState.isAir()) {
                    vec3 = vec3.subtract(0.0d, vec3.y, 0.0d);
                }
                Squid.this.setMovementVector(((float) vec3.x) / 20.0f, ((float) vec3.y) / 20.0f, ((float) vec3.z) / 20.0f);
            }
            if (this.fleeTicks % 10 == 5) {
                Squid.this.level.addParticle(ParticleTypes.BUBBLE, Squid.this.getX(), Squid.this.getY(), Squid.this.getZ(), 0.0d, 0.0d, 0.0d);
            }
        }
    }
}
