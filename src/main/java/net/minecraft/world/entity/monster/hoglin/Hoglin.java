package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/hoglin/Hoglin.class */
public class Hoglin extends Animal implements Enemy, HoglinBase {
    private int attackAnimationRemainingTicks;
    private int timeInOverworld;
    private boolean cannotBeHunted;
    private static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(Hoglin.class, EntityDataSerializers.BOOLEAN);
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ADULT, SensorType.HOGLIN_SPECIFIC_SENSOR);
//    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = (ImmutableList<? extends MemoryModuleType<?>>) ImmutableList.of(MemoryModuleType.BREED_TARGET, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, new MemoryModuleType[]{MemoryModuleType.AVOID_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.PACIFIED});
protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.<MemoryModuleType<?>>builder()
        .add(MemoryModuleType.BREED_TARGET)
        .add(MemoryModuleType.LIVING_ENTITIES)
        .add(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
        .add(MemoryModuleType.NEAREST_VISIBLE_PLAYER)
        .add(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER)
        .add(MemoryModuleType.LOOK_TARGET)
        .add(MemoryModuleType.WALK_TARGET)
        .add(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        .add(MemoryModuleType.PATH)
        .add(MemoryModuleType.ATTACK_TARGET)
        .add(MemoryModuleType.ATTACK_COOLING_DOWN)
        .add(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN)
        .add(MemoryModuleType.AVOID_TARGET)
        .add(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT)
        .add(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT)
        .add(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS)
        .add(MemoryModuleType.NEAREST_VISIBLE_ADULT)
        .add(MemoryModuleType.NEAREST_REPELLENT)
        .add(MemoryModuleType.PACIFIED)
        .build();
    public Hoglin(EntityType<? extends Hoglin> entityType, Level level) {
        super(entityType, level);
        this.timeInOverworld = 0;
        this.cannotBeHunted = false;
        this.xpReward = 5;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return !isLeashed();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.KNOCKBACK_RESISTANCE, 0.6000000238418579d).add(Attributes.ATTACK_KNOCKBACK, 1.0d).add(Attributes.ATTACK_DAMAGE, 6.0d);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        this.attackAnimationRemainingTicks = 10;
        this.level.broadcastEntityEvent(this, (byte) 4);
        playSound(SoundEvents.HOGLIN_ATTACK, 1.0f, getVoicePitch());
        HoglinAi.onHitTarget(this, (LivingEntity) entity);
        return HoglinBase.hurtAndThrowTarget(this, (LivingEntity) entity);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void blockedByShield(LivingEntity livingEntity) {
        if (isAdult()) {
            HoglinBase.throwTarget(this, livingEntity);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        boolean hurt = super.hurt(damageSource, f);
        if (this.level.isClientSide) {
            return false;
        }
        if (hurt && (damageSource.getEntity() instanceof LivingEntity)) {
            HoglinAi.wasHurtBy(this, (LivingEntity) damageSource.getEntity());
        }
        return hurt;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain.Provider<Hoglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return HoglinAi.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public Brain<Hoglin> getBrain() {
        return (Brain<Hoglin>) super.getBrain();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        this.level.getProfiler().push("hoglinBrain");
        getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        HoglinAi.updateActivity(this);
        if (isConverting()) {
            this.timeInOverworld++;
            if (this.timeInOverworld > 300) {
                playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED);
                finishConversion((ServerLevel) this.level);
                return;
            }
            return;
        }
        this.timeInOverworld = 0;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            this.attackAnimationRemainingTicks--;
        }
        super.aiStep();
    }

    @Override // net.minecraft.world.entity.AgableMob
    protected void ageBoundaryReached() {
        if (isBaby()) {
            this.xpReward = 3;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5d);
        } else {
            this.xpReward = 5;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0d);
        }
    }

    public static boolean checkHoglinSpawnRules(EntityType<Hoglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (serverLevelAccessor.getRandom().nextFloat() < 0.2f) {
            setBaby(true);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return !isPersistenceRequired();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (HoglinAi.isPosNearNearestRepellent(this, blockPos)) {
            return -1.0f;
        }
        if (levelReader.getBlockState(blockPos.below()).is(Blocks.CRIMSON_NYLIUM)) {
            return 10.0f;
        }
        return 0.0f;
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return getBbHeight() - (isBaby() ? 0.2d : 0.15d);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        InteractionResult mobInteract = super.mobInteract(player, interactionHand);
        if (mobInteract.consumesAction()) {
            setPersistenceRequired();
        }
        return mobInteract;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 4) {
            this.attackAnimationRemainingTicks = 10;
            playSound(SoundEvents.HOGLIN_ATTACK, 1.0f, getVoicePitch());
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override // net.minecraft.world.entity.monster.hoglin.HoglinBase
    public int getAttackAnimationRemainingTicks() {
        return this.attackAnimationRemainingTicks;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean shouldDropExperience() {
        return true;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected int getExperienceReward(Player player) {
        return this.xpReward;
    }

    private void finishConversion(ServerLevel serverLevel) {
        Zoglin zoglin = (Zoglin) convertTo(EntityType.ZOGLIN, true);
        if (zoglin != null) {
            zoglin.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return itemStack.getItem() == Items.CRIMSON_FUNGUS;
    }

    public boolean isAdult() {
        return !isBaby();
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (isImmuneToZombification()) {
            compoundTag.putBoolean("IsImmuneToZombification", true);
        }
        compoundTag.putInt("TimeInOverworld", this.timeInOverworld);
        if (this.cannotBeHunted) {
            compoundTag.putBoolean("CannotBeHunted", true);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setImmuneToZombification(compoundTag.getBoolean("IsImmuneToZombification"));
        this.timeInOverworld = compoundTag.getInt("TimeInOverworld");
        setCannotBeHunted(compoundTag.getBoolean("CannotBeHunted"));
    }

    public void setImmuneToZombification(boolean z) {
        getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, Boolean.valueOf(z));
    }

    private boolean isImmuneToZombification() {
        return ((Boolean) getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION)).booleanValue();
    }

    public boolean isConverting() {
        return (this.level.dimensionType().piglinSafe() || isImmuneToZombification() || isNoAi()) ? false : true;
    }

    private void setCannotBeHunted(boolean z) {
        this.cannotBeHunted = z;
    }

    public boolean canBeHunted() {
        return isAdult() && !this.cannotBeHunted;
    }

    @Override // net.minecraft.world.entity.AgableMob
    @Nullable
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Hoglin create = EntityType.HOGLIN.create(serverLevel);
        if (create != null) {
            create.setPersistenceRequired();
        }
        return create;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean canFallInLove() {
        return !HoglinAi.isPacified(this) && super.canFallInLove();
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (this.level.isClientSide) {
            return null;
        }
        return HoglinAi.getSoundForCurrentActivity(this).orElse(null);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSound() {
        return SoundEvents.HOSTILE_SWIM;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.HOSTILE_SPLASH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.HOGLIN_STEP, 0.15f, 1.0f);
    }

    protected void playSound(SoundEvent soundEvent) {
        playSound(soundEvent, getSoundVolume(), getVoicePitch());
    }

    @Override // net.minecraft.world.entity.Mob
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }
}
