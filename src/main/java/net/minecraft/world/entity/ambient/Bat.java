package net.minecraft.world.entity.ambient;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Random;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ambient/Bat.class */
public class Bat extends AmbientCreature {
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Bat.class, EntityDataSerializers.BYTE);
    private static final TargetingConditions BAT_RESTING_TARGETING = new TargetingConditions().range(4.0d).allowSameTeam();
    private BlockPos targetPosition;

    public Bat(EntityType<? extends Bat> entityType, Level level) {
        super(entityType, level);
        setResting(true);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FLAGS, (byte) 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.LivingEntity
    public float getSoundVolume() {
        return 0.1f;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.LivingEntity
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SoundEvent getAmbientSound() {
        if (isResting() && this.random.nextInt(4) != 0) {
            return null;
        }
        return SoundEvents.BAT_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BAT_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean isPushable() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void doPush(Entity entity) {
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void pushEntities() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0d);
    }

    public boolean isResting() {
        return (((Byte) this.entityData.get(DATA_ID_FLAGS)).byteValue() & 1) != 0;
    }

    public void setResting(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_ID_FLAGS)).byteValue();
        if (z) {
            this.entityData.set(DATA_ID_FLAGS, Byte.valueOf((byte) (byteValue | 1)));
        } else {
            this.entityData.set(DATA_ID_FLAGS, Byte.valueOf((byte) (byteValue & (-2))));
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick(){
        super.tick();
        if (isResting()) {
            setDeltaMovement(Vec3.ZERO);
            setPosRaw(getX(), (Mth.floor(getY()) + 1.0d) - getBbHeight(), getZ());
        } else {
            setDeltaMovement(getDeltaMovement().multiply(1.0d, 0.6d, 1.0d));
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        super.customServerAiStep();
        BlockPos blockPosition = blockPosition();
        BlockPos above = blockPosition.above();
        if (isResting()) {
            boolean isSilent = isSilent();
            if (this.level.getBlockState(above).isRedstoneConductor(this.level, blockPosition)) {
                if (this.random.nextInt(200) == 0) {
                    this.yHeadRot = this.random.nextInt(360);
                }
                if (this.level.getNearestPlayer(BAT_RESTING_TARGETING, this) != null) {
                    setResting(false);
                    if (!isSilent) {
                        this.level.levelEvent(null, 1025, blockPosition, 0);
                        return;
                    }
                    return;
                }
                return;
            }
            setResting(false);
            if (!isSilent) {
                this.level.levelEvent(null, 1025, blockPosition, 0);
                return;
            }
            return;
        }
        if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() < 1)) {
            this.targetPosition = null;
        }
        if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerThan(position(), 2.0d)) {
            this.targetPosition = new BlockPos((getX() + this.random.nextInt(7)) - this.random.nextInt(7), (getY() + this.random.nextInt(6)) - 2.0d, (getZ() + this.random.nextInt(7)) - this.random.nextInt(7));
        }
        double x = (this.targetPosition.getX() + 0.5d) - getX();
        double y = (this.targetPosition.getY() + 0.1d) - getY();
        double z = (this.targetPosition.getZ() + 0.5d) - getZ();
        Vec3 deltaMovement = getDeltaMovement();
        Vec3 add = deltaMovement.add(((Math.signum(x) * 0.5d) - deltaMovement.x) * 0.10000000149011612d, ((Math.signum(y) * 0.699999988079071d) - deltaMovement.y) * 0.10000000149011612d, ((Math.signum(z) * 0.5d) - deltaMovement.z) * 0.10000000149011612d);
        setDeltaMovement(add);
        float wrapDegrees = Mth.wrapDegrees((((float) (Mth.atan2(add.z, add.x) * 57.2957763671875d)) - 90.0f) - this.yRot);
        this.zza = 0.5f;
        this.yRot += wrapDegrees;
        if (this.random.nextInt(100) == 0 && this.level.getBlockState(above).isRedstoneConductor(this.level, above)) {
            setResting(true);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void checkFallDamage(double d, boolean z, BlockState blockState, BlockPos blockPos) {
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!this.level.isClientSide && isResting()) {
            setResting(false);
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_ID_FLAGS, Byte.valueOf(compoundTag.getByte("BatFlags")));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putByte("BatFlags", ((Byte) this.entityData.get(DATA_ID_FLAGS)).byteValue());
    }

    public static boolean checkBatSpawnRules(EntityType<Bat> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        if (blockPos.getY() >= levelAccessor.getSeaLevel()) {
            return false;
        }
        int maxLocalRawBrightness = levelAccessor.getMaxLocalRawBrightness(blockPos);
        int i = 4;
        if (isHalloween()) {
            i = 7;
        } else if (random.nextBoolean()) {
            return false;
        }
        if (maxLocalRawBrightness > random.nextInt(i)) {
            return false;
        }
        return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
    }

    private static boolean isHalloween() {
        LocalDate now = LocalDate.now();
        int i = now.get(ChronoField.DAY_OF_MONTH);
        int i2 = now.get(ChronoField.MONTH_OF_YEAR);
        return (i2 == 10 && i >= 20) || (i2 == 11 && i <= 3);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height / 2.0f;
    }
}
