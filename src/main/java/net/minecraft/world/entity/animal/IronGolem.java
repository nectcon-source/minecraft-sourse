package net.minecraft.world.entity.animal;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/IronGolem.class */
public class IronGolem extends AbstractGolem implements NeutralMob {
    private int attackAnimationTick;
    private int offerFlowerTick;
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.BYTE);
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    public IronGolem(EntityType<? extends IronGolem> entityType, Level level) {
        super(entityType, level);
        this.maxUpStep = 1.0f;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0d, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9d, 32.0f));
        this.goalSelector.addGoal(2, new MoveBackToVillageGoal(this, 0.6d, false));
        this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6d));
        this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Mob.class, 5, false, false, livingEntity -> {
            return (livingEntity instanceof Enemy) && !(livingEntity instanceof Creeper);
        }));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal(this, false));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100.0d).add(Attributes.MOVEMENT_SPEED, 0.25d).add(Attributes.KNOCKBACK_RESISTANCE, 1.0d).add(Attributes.ATTACK_DAMAGE, 15.0d);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected int decreaseAirSupply(int i) {
        return i;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void doPush(Entity entity) {
        if ((entity instanceof Enemy) && !(entity instanceof Creeper) && getRandom().nextInt(20) == 0) {
            setTarget((LivingEntity) entity);
        }
        super.doPush(entity);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
        }
        if (this.offerFlowerTick > 0) {
            this.offerFlowerTick--;
        }
        if (getHorizontalDistanceSqr(getDeltaMovement()) > 2.500000277905201E-7d && this.random.nextInt(5) == 0) {
            BlockState blockState = this.level.getBlockState(new BlockPos(Mth.floor(getX()), Mth.floor(getY() - 0.20000000298023224d), Mth.floor(getZ())));
            if (!blockState.isAir()) {
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), getX() + ((this.random.nextFloat() - 0.5d) * getBbWidth()), getY() + 0.1d, getZ() + ((this.random.nextFloat() - 0.5d) * getBbWidth()), 4.0d * (this.random.nextFloat() - 0.5d), 0.5d, (this.random.nextFloat() - 0.5d) * 4.0d);
            }
        }
        if (!this.level.isClientSide) {
            updatePersistentAnger((ServerLevel) this.level, true);
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean canAttackType(EntityType<?> entityType) {
        if ((isPlayerCreated() && entityType == EntityType.PLAYER) || entityType == EntityType.CREEPER) {
            return false;
        }
        return super.canAttackType(entityType);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("PlayerCreated", isPlayerCreated());
        addPersistentAngerSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setPlayerCreated(compoundTag.getBoolean("PlayerCreated"));
        readPersistentAngerSaveData((ServerLevel) this.level, compoundTag);
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

    private float getAttackDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        this.attackAnimationTick = 10;
        this.level.broadcastEntityEvent(this, (byte) 4);
        float attackDamage = getAttackDamage();
        boolean hurt = entity.hurt(DamageSource.mobAttack(this), ((int) attackDamage) > 0 ? (attackDamage / 2.0f) + this.random.nextInt((int) attackDamage) : attackDamage);
        if (hurt) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0d, 0.4000000059604645d, 0.0d));
            doEnchantDamageEffects(this, entity);
        }
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return hurt;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        Crackiness crackiness = getCrackiness();
        boolean hurt = super.hurt(damageSource, f);
        if (hurt && getCrackiness() != crackiness) {
            playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0f, 1.0f);
        }
        return hurt;
    }

    public Crackiness getCrackiness() {
        return Crackiness.byFraction(getHealth() / getMaxHealth());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 4) {
            this.attackAnimationTick = 10;
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        } else if (b == 11) {
            this.offerFlowerTick = 400;
        } else if (b == 34) {
            this.offerFlowerTick = 0;
        } else {
            super.handleEntityEvent(b);
        }
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    public void offerFlower(boolean z) {
        if (z) {
            this.offerFlowerTick = 400;
            this.level.broadcastEntityEvent(this, (byte) 11);
        } else {
            this.offerFlowerTick = 0;
            this.level.broadcastEntityEvent(this, (byte) 34);
        }
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override // net.minecraft.world.entity.Mob
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() != Items.IRON_INGOT) {
            return InteractionResult.PASS;
        }
        float health = getHealth();
        heal(25.0f);
        if (getHealth() == health) {
            return InteractionResult.PASS;
        }
        playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0f, 1.0f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f));
        if (!player.abilities.instabuild) {
            itemInHand.shrink(1);
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    public int getOfferFlowerTick() {
        return this.offerFlowerTick;
    }

    public boolean isPlayerCreated() {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & 1) != 0;
    }

    public void setPlayerCreated(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue();
        if (z) {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (byteValue | 1)));
        } else {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (byteValue & (-2))));
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        super.die(damageSource);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        BlockPos blockPosition = blockPosition();
        BlockPos below = blockPosition.below();
        if (levelReader.getBlockState(below).entityCanStandOn(levelReader, below, this)) {
            for (int i = 1; i < 3; i++) {
                BlockPos above = blockPosition.above(i);
                BlockState blockState = levelReader.getBlockState(above);
                if (!NaturalSpawner.isValidEmptySpawnBlock(levelReader, above, blockState, blockState.getFluidState(), EntityType.IRON_GOLEM)) {
                    return false;
                }
            }
            return NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPosition, levelReader.getBlockState(blockPosition), Fluids.EMPTY.defaultFluidState(), EntityType.IRON_GOLEM) && levelReader.isUnobstructed(this);
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.875f * getEyeHeight(), getBbWidth() * 0.4f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/IronGolem$Crackiness.class */
    public enum Crackiness {
        NONE(1.0f),
        LOW(0.75f),
        MEDIUM(0.5f),
        HIGH(0.25f);

        private static final List<IronGolem.Crackiness> BY_DAMAGE = Stream.of(values())
                .sorted(Comparator.comparingDouble(var0 -> (double)var0.fraction))
                .collect(ImmutableList.toImmutableList());
        private final float fraction;

        Crackiness(float f) {
            this.fraction = f;
        }

        public static Crackiness byFraction(float f) {
            for (Crackiness crackiness : BY_DAMAGE) {
                if (f < crackiness.fraction) {
                    return crackiness;
                }
            }
            return NONE;
        }
    }
}
