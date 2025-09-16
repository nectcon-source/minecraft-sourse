package net.minecraft.world.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Zoglin.class */
public class Zoglin extends Monster implements Enemy, HoglinBase {
    private int attackAnimationRemainingTicks;
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zoglin.class, EntityDataSerializers.BOOLEAN);
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Zoglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

    public Zoglin(EntityType<? extends Zoglin> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain.Provider<Zoglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<Zoglin> makeBrain = brainProvider().makeBrain(dynamic);
        initCoreActivity(makeBrain);
        initIdleActivity(makeBrain);
        initFightActivity(makeBrain);
        makeBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        makeBrain.setDefaultActivity(Activity.IDLE);
        makeBrain.useDefaultActivity();
        return makeBrain;
    }

    private static void initCoreActivity(Brain<Zoglin> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

//    private static void initIdleActivity(Brain<Zoglin> brain) {
//        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new StartAttacking((v0) -> {
//            return v0.findNearestValidAttackTarget();
//        }), new RunSometimes(new SetEntityLookTarget(8.0f), IntRange.of(30, 60)), new RunOne(ImmutableList.of(Pair.of(new RandomStroll(0.4f), 2), Pair.of(new SetWalkTargetFromLookTarget(0.4f, 3), 2), Pair.of(new DoNothing(30, 60), 1)))));
//    }
//
//    private static void initFightActivity(Brain<Zoglin> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0f), new RunIf((v0) -> {
//            return v0.isAdult();
//        }, new MeleeAttack(40)), new RunIf((v0) -> {
//            return v0.isBaby();
//        }, new MeleeAttack(15)), new StopAttackingIfTargetInvalid()), MemoryModuleType.ATTACK_TARGET);
//    }

    private static void initIdleActivity(Brain<Zoglin> brain) {
        brain.addActivity(
                Activity.IDLE,
                10,
                 ImmutableList.<net.minecraft.world.entity.ai.behavior.Behavior<? super Zoglin>>of(
                        new StartAttacking<>(Zoglin::findNearestValidAttackTarget),
                        new RunSometimes<>(new SetEntityLookTarget(8.0F), IntRange.of(30, 60)),
                        new RunOne(
                                ImmutableList.of(Pair.of(new RandomStroll(0.4F), 2), Pair.of(new SetWalkTargetFromLookTarget(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
                        )
                )
        );
    }

    private static void initFightActivity(Brain<Zoglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.FIGHT, 10,
                ImmutableList.<net.minecraft.world.entity.ai.behavior.Behavior<? super Zoglin>>of(
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
                        new RunIf<>(Zoglin::isAdult, new MeleeAttack(40)),
                        new RunIf<>(Zoglin::isBaby, new MeleeAttack(15)),
                        new StopAttackingIfTargetInvalid()
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    //

    private Optional<? extends LivingEntity> findNearestValidAttackTarget() {
        return this.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of()).stream().filter(Zoglin::isTargetable).findFirst();
    }

    private static boolean isTargetable(LivingEntity livingEntity) {
        EntityType<?> type = livingEntity.getType();
        return (type == EntityType.ZOGLIN || type == EntityType.CREEPER || !EntitySelector.ATTACK_ALLOWED.test(livingEntity)) ? false : true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            refreshDimensions();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.KNOCKBACK_RESISTANCE, 0.6000000238418579d).add(Attributes.ATTACK_KNOCKBACK, 1.0d).add(Attributes.ATTACK_DAMAGE, 6.0d);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        this.attackAnimationRemainingTicks = 10;
        this.level.broadcastEntityEvent(this, (byte) 4);
        playSound(SoundEvents.ZOGLIN_ATTACK, 1.0f, getVoicePitch());
        return HoglinBase.hurtAndThrowTarget(this, (LivingEntity) entity);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return !isLeashed();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void blockedByShield(LivingEntity livingEntity) {
        if (!isBaby()) {
            HoglinBase.throwTarget(this, livingEntity);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return getBbHeight() - (isBaby() ? 0.2d : 0.15d);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        boolean hurt = super.hurt(damageSource, f);
        if (this.level.isClientSide) {
            return false;
        }
        if (!hurt || !(damageSource.getEntity() instanceof LivingEntity)) {
            return hurt;
        }
        LivingEntity livingEntity = (LivingEntity) damageSource.getEntity();
        if (EntitySelector.ATTACK_ALLOWED.test(livingEntity) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, livingEntity, 4.0d)) {
            setAttackTarget(livingEntity);
        }
        return hurt;
    }

    private void setAttackTarget(LivingEntity livingEntity) {
        this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, livingEntity, 200L);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public Brain<Zoglin> getBrain() {
        return (Brain<Zoglin>) super.getBrain();
    }

    protected void updateActivity() {
        Activity orElse = this.brain.getActiveNonCoreActivity().orElse(null);
        this.brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        if (this.brain.getActiveNonCoreActivity().orElse(null) == Activity.FIGHT && orElse != Activity.FIGHT) {
            playAngrySound();
        }
        setAggressive(this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        this.level.getProfiler().push("zoglinBrain");
        getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        updateActivity();
    }

    @Override // net.minecraft.world.entity.Mob
    public void setBaby(boolean z) {
        getEntityData().set(DATA_BABY_ID, Boolean.valueOf(z));
        if (!this.level.isClientSide && z) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5d);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isBaby() {
        return ((Boolean) getEntityData().get(DATA_BABY_ID)).booleanValue();
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            this.attackAnimationRemainingTicks--;
        }
        super.aiStep();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 4) {
            this.attackAnimationRemainingTicks = 10;
            playSound(SoundEvents.ZOGLIN_ATTACK, 1.0f, getVoicePitch());
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override // net.minecraft.world.entity.monster.hoglin.HoglinBase
    public int getAttackAnimationRemainingTicks() {
        return this.attackAnimationRemainingTicks;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (this.level.isClientSide) {
            return null;
        }
        if (this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return SoundEvents.ZOGLIN_ANGRY;
        }
        return SoundEvents.ZOGLIN_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOGLIN_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOGLIN_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.ZOGLIN_STEP, 0.15f, 1.0f);
    }

    protected void playAngrySound() {
        playSound(SoundEvents.ZOGLIN_ANGRY, 1.0f, getVoicePitch());
    }

    @Override // net.minecraft.world.entity.Mob
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (isBaby()) {
            compoundTag.putBoolean("IsBaby", true);
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.getBoolean("IsBaby")) {
            setBaby(true);
        }
    }
}
