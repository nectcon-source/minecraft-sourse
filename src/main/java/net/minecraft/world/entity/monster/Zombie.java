package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Zombie.class */
public class Zombie extends Monster {
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5d, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = difficulty -> {
        return difficulty == Difficulty.HARD;
    };
    private final BreakDoorGoal breakDoorGoal;
    private boolean canBreakDoors;
    private int inWaterTime;
    private int conversionTime;

    public Zombie(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
        this.breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
    }

    public Zombie(Level level) {
        this(EntityType.ZOMBIE, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new ZombieAttackTurtleEggGoal(this, 1.0d, 3));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0d, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0d, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0d).add(Attributes.MOVEMENT_SPEED, 0.23000000417232513d).add(Attributes.ATTACK_DAMAGE, 3.0d).add(Attributes.ARMOR, 2.0d).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(DATA_BABY_ID, false);
        getEntityData().define(DATA_SPECIAL_TYPE_ID, 0);
        getEntityData().define(DATA_DROWNED_CONVERSION_ID, false);
    }

    public boolean isUnderWaterConverting() {
        return ((Boolean) getEntityData().get(DATA_DROWNED_CONVERSION_ID)).booleanValue();
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    public void setCanBreakDoors(boolean z) {
        if (supportsBreakDoorGoal() && GoalUtils.hasGroundPathNavigation(this)) {
            if (this.canBreakDoors != z) {
                this.canBreakDoors = z;
                ((GroundPathNavigation) getNavigation()).setCanOpenDoors(z);
                if (z) {
                    this.goalSelector.addGoal(1, this.breakDoorGoal);
                    return;
                } else {
                    this.goalSelector.removeGoal(this.breakDoorGoal);
                    return;
                }
            }
            return;
        }
        if (this.canBreakDoors) {
            this.goalSelector.removeGoal(this.breakDoorGoal);
            this.canBreakDoors = false;
        }
    }

    protected boolean supportsBreakDoorGoal() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isBaby() {
        return ((Boolean) getEntityData().get(DATA_BABY_ID)).booleanValue();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected int getExperienceReward(Player player) {
        if (isBaby()) {
            this.xpReward = (int) (this.xpReward * 2.5f);
        }
        return super.getExperienceReward(player);
    }

    @Override // net.minecraft.world.entity.Mob
    public void setBaby(boolean z) {
        getEntityData().set(DATA_BABY_ID, Boolean.valueOf(z));
        if (this.level != null && !this.level.isClientSide) {
            AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(SPEED_MODIFIER_BABY);
            if (z) {
                attribute.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    protected boolean convertsInWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        if (!this.level.isClientSide && isAlive() && !isNoAi()) {
            if (isUnderWaterConverting()) {
                this.conversionTime--;
                if (this.conversionTime < 0) {
                    doUnderWaterConversion();
                }
            } else if (convertsInWater()) {
                if (isEyeInFluid(FluidTags.WATER)) {
                    this.inWaterTime++;
                    if (this.inWaterTime >= 600) {
                        startUnderWaterConversion(300);
                    }
                } else {
                    this.inWaterTime = -1;
                }
            }
        }
        super.tick();
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (isAlive()) {
            boolean z = isSunSensitive() && isSunBurnTick();
            if (z) {
                ItemStack itemBySlot = getItemBySlot(EquipmentSlot.HEAD);
                if (!itemBySlot.isEmpty()) {
                    if (itemBySlot.isDamageableItem()) {
                        itemBySlot.setDamageValue(itemBySlot.getDamageValue() + this.random.nextInt(2));
                        if (itemBySlot.getDamageValue() >= itemBySlot.getMaxDamage()) {
                            broadcastBreakEvent(EquipmentSlot.HEAD);
                            setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                    z = false;
                }
                if (z) {
                    setSecondsOnFire(8);
                }
            }
        }
        super.aiStep();
    }

    private void startUnderWaterConversion(int i) {
        this.conversionTime = i;
        getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
    }

    protected void doUnderWaterConversion() {
        convertToZombieType(EntityType.DROWNED);
        if (!isSilent()) {
            this.level.levelEvent(null, 1040, blockPosition(), 0);
        }
    }

    protected void convertToZombieType(EntityType<? extends Zombie> entityType) {
        Zombie zombie = (Zombie) convertTo(entityType, true);
        if (zombie != null) {
            zombie.handleAttributes(zombie.level.getCurrentDifficultyAt(zombie.blockPosition()).getSpecialMultiplier());
            zombie.setCanBreakDoors(zombie.supportsBreakDoorGoal() && canBreakDoors());
        }
    }

    protected boolean isSunSensitive() {
        return true;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (!super.hurt(damageSource, f) || !(this.level instanceof ServerLevel)) {
            return false;
        }
        ServerLevelAccessor serverLevelAccessor = (ServerLevel) this.level;
        LivingEntity target = getTarget();
        if (target == null && (damageSource.getEntity() instanceof LivingEntity)) {
            target = (LivingEntity) damageSource.getEntity();
        }
        if (target != null && this.level.getDifficulty() == Difficulty.HARD && this.random.nextFloat() < getAttributeValue(Attributes.SPAWN_REINFORCEMENTS_CHANCE) && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            int floor = Mth.floor(getX());
            int floor2 = Mth.floor(getY());
            int floor3 = Mth.floor(getZ());
            Zombie zombie = new Zombie(this.level);
            for (int i = 0; i < 50; i++) {
                int nextInt = floor + (Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1));
                int nextInt2 = floor2 + (Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1));
                int nextInt3 = floor3 + (Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1));
                BlockPos blockPos = new BlockPos(nextInt, nextInt2, nextInt3);
                EntityType<?> type = zombie.getType();
                if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(type), this.level, blockPos, type) && SpawnPlacements.checkSpawnRules(type, serverLevelAccessor, MobSpawnType.REINFORCEMENT, blockPos, this.level.random)) {
                    zombie.setPos(nextInt, nextInt2, nextInt3);
                    if (!this.level.hasNearbyAlivePlayer(nextInt, nextInt2, nextInt3, 7.0d) && this.level.isUnobstructed(zombie) && this.level.noCollision(zombie) && !this.level.containsAnyLiquid(zombie.getBoundingBox())) {
                        zombie.setTarget(target);
                        zombie.finalizeSpawn(serverLevelAccessor, this.level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.REINFORCEMENT, null, null);
                        serverLevelAccessor.addFreshEntityWithPassengers(zombie);
                        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806d, AttributeModifier.Operation.ADDITION));
                        zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806d, AttributeModifier.Operation.ADDITION));
                        return true;
                    }
                }
            }
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        boolean doHurtTarget = super.doHurtTarget(entity);
        if (doHurtTarget) {
            float effectiveDifficulty = this.level.getCurrentDifficultyAt(blockPosition()).getEffectiveDifficulty();
            if (getMainHandItem().isEmpty() && isOnFire() && this.random.nextFloat() < effectiveDifficulty * 0.3f) {
                entity.setSecondsOnFire(2 * ((int) effectiveDifficulty));
            }
        }
        return doHurtTarget;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(getStepSound(), 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        super.populateDefaultEquipmentSlots(difficultyInstance);
        if (this.random.nextFloat() < (this.level.getDifficulty() == Difficulty.HARD ? 0.05f : 0.01f)) {
            if (this.random.nextInt(3) == 0) {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("IsBaby", isBaby());
        compoundTag.putBoolean("CanBreakDoors", canBreakDoors());
        compoundTag.putInt("InWaterTime", isInWater() ? this.inWaterTime : -1);
        compoundTag.putInt("DrownedConversionTime", isUnderWaterConverting() ? this.conversionTime : -1);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setBaby(compoundTag.getBoolean("IsBaby"));
        setCanBreakDoors(compoundTag.getBoolean("CanBreakDoors"));
        this.inWaterTime = compoundTag.getInt("InWaterTime");
        if (compoundTag.contains("DrownedConversionTime", 99) && compoundTag.getInt("DrownedConversionTime") > -1) {
            startUnderWaterConversion(compoundTag.getInt("DrownedConversionTime"));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void killed(ServerLevel serverLevel, LivingEntity livingEntity) {
        super.killed(serverLevel, livingEntity);
        if ((serverLevel.getDifficulty() == Difficulty.NORMAL || serverLevel.getDifficulty() == Difficulty.HARD) && (livingEntity instanceof Villager)) {
            if (serverLevel.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return;
            }
            Villager villager = (Villager) livingEntity;
            ZombieVillager zombieVillager = (ZombieVillager) villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            zombieVillager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new ZombieGroupData(false, true), null);
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossips((Tag) villager.getGossips().store(NbtOps.INSTANCE).getValue());
            zombieVillager.setTradeOffers(villager.getOffers().createTag());
            zombieVillager.setVillagerXp(villager.getVillagerXp());
            if (!isSilent()) {
                serverLevel.levelEvent(null, 1026, blockPosition(), 0);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return isBaby() ? 0.93f : 1.74f;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canHoldItem(ItemStack itemStack) {
        if (itemStack.getItem() == Items.EGG && isBaby() && isPassenger()) {
            return false;
        }
        return super.canHoldItem(itemStack);
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData finalizeSpawn = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        float specialMultiplier = difficultyInstance.getSpecialMultiplier();
        setCanPickUpLoot(this.random.nextFloat() < 0.55f * specialMultiplier);
        if (finalizeSpawn == null) {
            finalizeSpawn = new ZombieGroupData(getSpawnAsBabyOdds(serverLevelAccessor.getRandom()), true);
        }
        if (finalizeSpawn instanceof ZombieGroupData) {
            ZombieGroupData zombieGroupData = (ZombieGroupData) finalizeSpawn;
            if (zombieGroupData.isBaby) {
                setBaby(true);
                if (zombieGroupData.canSpawnJockey) {
                    if (serverLevelAccessor.getRandom().nextFloat() < 0.05d) {
                        List<Chicken> entitiesOfClass = serverLevelAccessor.getEntitiesOfClass(Chicken.class, getBoundingBox().inflate(5.0d, 3.0d, 5.0d), EntitySelector.ENTITY_NOT_BEING_RIDDEN);
                        if (!entitiesOfClass.isEmpty()) {
                            Chicken chicken = entitiesOfClass.get(0);
                            chicken.setChickenJockey(true);
                            startRiding(chicken);
                        }
                    } else if (serverLevelAccessor.getRandom().nextFloat() < 0.05d) {
                        Chicken create = EntityType.CHICKEN.create(this.level);
                        create.moveTo(getX(), getY(), getZ(), this.yRot, 0.0f);
                        create.finalizeSpawn(serverLevelAccessor, difficultyInstance, MobSpawnType.JOCKEY, null, null);
                        create.setChickenJockey(true);
                        startRiding(create);
                        serverLevelAccessor.addFreshEntity(create);
                    }
                }
            }
            setCanBreakDoors(supportsBreakDoorGoal() && this.random.nextFloat() < specialMultiplier * 0.1f);
            populateDefaultEquipmentSlots(difficultyInstance);
            populateDefaultEquipmentEnchantments(difficultyInstance);
        }
        if (getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate now = LocalDate.now();
            int i = now.get(ChronoField.DAY_OF_MONTH);
            if (now.get(ChronoField.MONTH_OF_YEAR) == 10 && i == 31 && this.random.nextFloat() < 0.25f) {
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0f;
            }
        }
        handleAttributes(specialMultiplier);
        return finalizeSpawn;
    }

    public static boolean getSpawnAsBabyOdds(Random random) {
        return random.nextFloat() < 0.05f;
    }

    protected void handleAttributes(float f) {
        randomizeReinforcementsChance();
        getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05000000074505806d, AttributeModifier.Operation.ADDITION));
        double nextDouble = this.random.nextDouble() * 1.5d * f;
        if (nextDouble > 1.0d) {
            getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random zombie-spawn bonus", nextDouble, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (this.random.nextFloat() < f * 0.05f) {
            getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Leader zombie bonus", (this.random.nextDouble() * 0.25d) + 0.5d, AttributeModifier.Operation.ADDITION));
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Leader zombie bonus", (this.random.nextDouble() * 3.0d) + 1.0d, AttributeModifier.Operation.MULTIPLY_TOTAL));
            setCanBreakDoors(supportsBreakDoorGoal());
        }
    }

    protected void randomizeReinforcementsChance() {
        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * 0.10000000149011612d);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Zombie$ZombieGroupData.class */
    public static class ZombieGroupData implements SpawnGroupData {
        public final boolean isBaby;
        public final boolean canSpawnJockey;

        public ZombieGroupData(boolean z, boolean z2) {
            this.isBaby = z;
            this.canSpawnJockey = z2;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return isBaby() ? 0.0d : -0.45d;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        Entity entity = damageSource.getEntity();
        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;
            if (creeper.canDropMobsSkull()) {
                ItemStack skull = getSkull();
                if (!skull.isEmpty()) {
                    creeper.increaseDroppedSkulls();
                    spawnAtLocation(skull);
                }
            }
        }
    }

    protected ItemStack getSkull() {
        return new ItemStack(Items.ZOMBIE_HEAD);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Zombie$ZombieAttackTurtleEggGoal.class */
    class ZombieAttackTurtleEggGoal extends RemoveBlockGoal {
        ZombieAttackTurtleEggGoal(PathfinderMob pathfinderMob, double d, int i) {
            super(Blocks.TURTLE_EGG, pathfinderMob, d, i);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RemoveBlockGoal
        public void playDestroyProgressSound(LevelAccessor levelAccessor, BlockPos blockPos) {
            levelAccessor.playSound(null, blockPos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5f, 0.9f + (Zombie.this.random.nextFloat() * 0.2f));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RemoveBlockGoal
        public void playBreakSound(Level level, BlockPos blockPos) {
            level.playSound((Player) null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7f, 0.9f + (level.random.nextFloat() * 0.2f));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        public double acceptedDistance() {
            return 1.14d;
        }
    }
}
