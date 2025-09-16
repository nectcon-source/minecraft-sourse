package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/Piglin.class */
public class Piglin extends AbstractPiglin implements CrossbowAttackMob {
    private final SimpleContainer inventory;
    private boolean cannotHunt;
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.20000000298023224d, AttributeModifier.Operation.MULTIPLY_BASE);
    protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, new MemoryModuleType[]{MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT});

    public Piglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(8);
        this.cannotHunt = false;
        this.xpReward = 5;
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (isBaby()) {
            compoundTag.putBoolean("IsBaby", true);
        }
        if (this.cannotHunt) {
            compoundTag.putBoolean("CannotHunt", true);
        }
        compoundTag.put("Inventory", this.inventory.createTag());
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setBaby(compoundTag.getBoolean("IsBaby"));
        setCannotHunt(compoundTag.getBoolean("CannotHunt"));
        this.inventory.fromTag(compoundTag.getList("Inventory", 10));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
    }

    protected ItemStack addToInventory(ItemStack itemStack) {
        return this.inventory.addItem(itemStack);
    }

    protected boolean canAddToInventory(ItemStack itemStack) {
        return this.inventory.canAddItem(itemStack);
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
        this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
        this.entityData.define(DATA_IS_DANCING, false);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            refreshDimensions();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0d).add(Attributes.MOVEMENT_SPEED, 0.3499999940395355d).add(Attributes.ATTACK_DAMAGE, 5.0d);
    }

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (mobSpawnType != MobSpawnType.STRUCTURE) {
            if (serverLevelAccessor.getRandom().nextFloat() < 0.2f) {
                setBaby(true);
            } else if (isAdult()) {
                setItemSlot(EquipmentSlot.MAINHAND, createSpawnWeapon());
            }
        }
        PiglinAi.initMemories(this);
        populateDefaultEquipmentSlots(difficultyInstance);
        populateDefaultEquipmentEnchantments(difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return !isPersistenceRequired();
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        if (isAdult()) {
            maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
            maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
            maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }
    }

    private void maybeWearArmor(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        if (this.level.random.nextFloat() < 0.1f) {
            setItemSlot(equipmentSlot, itemStack);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain.Provider<Piglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinAi.makeBrain(this, brainProvider().makeBrain(dynamic));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public Brain<Piglin> getBrain() {
        return (Brain<Piglin>) super.getBrain();
    }

    @Override // net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        InteractionResult mobInteract = super.mobInteract(player, interactionHand);
        if (mobInteract.consumesAction()) {
            return mobInteract;
        }
        if (this.level.isClientSide) {
            return PiglinAi.canAdmire(this, player.getItemInHand(interactionHand)) && getArmPose() != PiglinArmPose.ADMIRING_ITEM ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return PiglinAi.mobInteract(this, player, interactionHand);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return isBaby() ? 0.93f : 1.74f;
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return getBbHeight() * 0.92d;
    }

    @Override // net.minecraft.world.entity.Mob
    public void setBaby(boolean z) {
        getEntityData().set(DATA_BABY_ID, Boolean.valueOf(z));
        if (!this.level.isClientSide) {
            AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(SPEED_MODIFIER_BABY);
            if (z) {
                attribute.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isBaby() {
        return ((Boolean) getEntityData().get(DATA_BABY_ID)).booleanValue();
    }

    private void setCannotHunt(boolean z) {
        this.cannotHunt = z;
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin
    protected boolean canHunt() {
        return !this.cannotHunt;
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin, net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        this.level.getProfiler().push("piglinBrain");
        getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        PiglinAi.updateActivity(this);
        super.customServerAiStep();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected int getExperienceReward(Player player) {
        return this.xpReward;
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin
    protected void finishConversion(ServerLevel serverLevel) {
        PiglinAi.cancelAdmiring(this);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
        super.finishConversion(serverLevel);
    }

    private ItemStack createSpawnWeapon() {
        if (this.random.nextFloat() < 0.5d) {
            return new ItemStack(Items.CROSSBOW);
        }
        return new ItemStack(Items.GOLDEN_SWORD);
    }

    private boolean isChargingCrossbow() {
        return ((Boolean) this.entityData.get(DATA_IS_CHARGING_CROSSBOW)).booleanValue();
    }

    @Override // net.minecraft.world.entity.monster.CrossbowAttackMob
    public void setChargingCrossbow(boolean z) {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.monster.CrossbowAttackMob
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin
    public PiglinArmPose getArmPose() {
        if (isDancing()) {
            return PiglinArmPose.DANCING;
        }
        if (PiglinAi.isLovedItem(getOffhandItem().getItem())) {
            return PiglinArmPose.ADMIRING_ITEM;
        }
        if (isAggressive() && isHoldingMeleeWeapon()) {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        }
        if (isChargingCrossbow()) {
            return PiglinArmPose.CROSSBOW_CHARGE;
        }
        if (isAggressive() && isHolding(Items.CROSSBOW)) {
            return PiglinArmPose.CROSSBOW_HOLD;
        }
        return PiglinArmPose.DEFAULT;
    }

    public boolean isDancing() {
        return ((Boolean) this.entityData.get(DATA_IS_DANCING)).booleanValue();
    }

    public void setDancing(boolean z) {
        this.entityData.set(DATA_IS_DANCING, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        boolean hurt = super.hurt(damageSource, f);
        if (this.level.isClientSide) {
            return false;
        }
        if (hurt && (damageSource.getEntity() instanceof LivingEntity)) {
            PiglinAi.wasHurtBy(this, (LivingEntity) damageSource.getEntity());
        }
        return hurt;
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        performCrossbowAttack(this, 1.6f);
    }

    @Override // net.minecraft.world.entity.monster.CrossbowAttackMob
    public void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f) {
        shootCrossbowProjectile(this, livingEntity, projectile, f, 1.6f);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return projectileWeaponItem == Items.CROSSBOW;
    }

    protected void holdInMainHand(ItemStack itemStack) {
        setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemStack);
    }

    protected void holdInOffHand(ItemStack itemStack) {
        if (itemStack.getItem() == PiglinAi.BARTERING_ITEM) {
            setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, itemStack);
        }
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean wantsToPickUp(ItemStack itemStack) {
        return this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && canPickUpLoot() && PiglinAi.wantsToPickup(this, itemStack);
    }

    protected boolean canReplaceCurrentItem(ItemStack itemStack) {
        return canReplaceCurrentItem(itemStack, getItemBySlot(Mob.getEquipmentSlotForItem(itemStack)));
    }

    @Override // net.minecraft.world.entity.Mob
    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
        if (EnchantmentHelper.hasBindingCurse(itemStack2)) {
            return false;
        }
        boolean z = PiglinAi.isLovedItem(itemStack.getItem()) || itemStack.getItem() == Items.CROSSBOW;
        boolean z2 = PiglinAi.isLovedItem(itemStack2.getItem()) || itemStack2.getItem() == Items.CROSSBOW;
        if (z && !z2) {
            return true;
        }
        if (!z && z2) {
            return false;
        }
        if (isAdult() && itemStack.getItem() != Items.CROSSBOW && itemStack2.getItem() == Items.CROSSBOW) {
            return false;
        }
        return super.canReplaceCurrentItem(itemStack, itemStack2);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void pickUpItem(ItemEntity itemEntity) {
        onItemPickup(itemEntity);
        PiglinAi.pickUpItem(this, itemEntity);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.Entity
    public boolean startRiding(Entity entity, boolean z) {
        if (isBaby() && entity.getType() == EntityType.HOGLIN) {
            entity = getTopPassenger(entity, 3);
        }
        return super.startRiding(entity, z);
    }

    private Entity getTopPassenger(Entity entity, int i) {
        List<Entity> passengers = entity.getPassengers();
        if (i == 1 || passengers.isEmpty()) {
            return entity;
        }
        return getTopPassenger(passengers.get(0), i - 1);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (this.level.isClientSide) {
            return null;
        }
        return PiglinAi.getSoundForCurrentActivity(this).orElse(null);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.PIGLIN_STEP, 0.15f, 1.0f);
    }

    protected void playSound(SoundEvent soundEvent) {
        playSound(soundEvent, getSoundVolume(), getVoicePitch());
    }

    @Override // net.minecraft.world.entity.monster.piglin.AbstractPiglin
    protected void playConvertedSound() {
        playSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }
}
