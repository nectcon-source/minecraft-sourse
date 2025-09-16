package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox.class */
public class Fox extends Animal {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> {
        return !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
    };
    private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = entity -> {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            return livingEntity.getLastHurtMob() != null && livingEntity.getLastHurtMobTimestamp() < livingEntity.tickCount + 600;
        }
        return false;
    };
    private static final Predicate<Entity> STALKABLE_PREY = entity -> {
        return (entity instanceof Chicken) || (entity instanceof Rabbit);
    };
    private static final Predicate<Entity> AVOID_PLAYERS = entity -> {
        return !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
    };
    private Goal landTargetGoal;
    private Goal turtleEggTargetGoal;
    private Goal fishTargetGoal;
    private float interestedAngle;
    private float interestedAngleO;
    private float crouchAmount;
    private float crouchAmountO;
    private int ticksSinceEaten;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$Type.class */
    public enum Type {
        RED(0, "red", Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.TAIGA_MOUNTAINS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.GIANT_SPRUCE_TAIGA_HILLS),
        SNOW(1, "snow", Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS);

        private static final Type[] BY_ID = (Type[]) Arrays.stream(values()).sorted(Comparator.comparingInt((v0) -> {
            return v0.getId();
        })).toArray(i -> {
            return new Type[i];
        });
        private static final Map<String, Type> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, type -> {
            return type;
        }));

        /* renamed from: id */
        private final int f441id;
        private final String name;
        private final List<ResourceKey<Biome>> biomes;

        Type(int i, String str, ResourceKey... resourceKeyArr) {
            this.f441id = i;
            this.name = str;
            this.biomes = Arrays.asList(resourceKeyArr);
        }

        public String getName() {
            return this.name;
        }

        public int getId() {
            return this.f441id;
        }

        public static Type byName(String str) {
            return BY_NAME.getOrDefault(str, RED);
        }

        public static Type byId(int i) {
            if (i < 0 || i > BY_ID.length) {
                i = 0;
            }
            return BY_ID[i];
        }

        public static Type byBiome(Optional<ResourceKey<Biome>> optional) {
            return (optional.isPresent() && SNOW.biomes.contains(optional.get())) ? SNOW : RED;
        }
    }

    public Fox(EntityType<? extends Fox> entityType, Level level) {
        super(entityType, level);
        this.lookControl = new FoxLookControl();
        this.moveControl = new FoxMoveControl();
        setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0f);
        setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0f);
        setCanPickUpLoot(true);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRUSTED_ID_0, Optional.empty());
        this.entityData.define(DATA_TRUSTED_ID_1, Optional.empty());
        this.entityData.define(DATA_TYPE_ID, 0);
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.landTargetGoal = new NearestAttackableTargetGoal(this, Animal.class, 10, false, false, livingEntity -> {
            return (livingEntity instanceof Chicken) || (livingEntity instanceof Rabbit);
        });
        this.turtleEggTargetGoal = new NearestAttackableTargetGoal(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
        this.fishTargetGoal = new NearestAttackableTargetGoal(this, AbstractFish.class, 20, false, false, livingEntity2 -> {
            return livingEntity2 instanceof AbstractSchoolingFish;
        });
        this.goalSelector.addGoal(0, new FoxFloatGoal());
        this.goalSelector.addGoal(1, new FaceplantGoal());
        this.goalSelector.addGoal(2, new FoxPanicGoal(2.2d));
        this.goalSelector.addGoal(3, new FoxBreedGoal(1.0d));
        this.goalSelector.addGoal(4, new AvoidEntityGoal(this, Player.class, 16.0F, 1.6, 1.4, (var1) -> AVOID_PLAYERS.test((Entity) var1) && !this.trusts(((Entity) var1).getUUID()) && !this.isDefending()));

        this.goalSelector.addGoal(4, new AvoidEntityGoal(this, Wolf.class, 8.0f, 1.6d, 1.4d, livingEntity4 -> {
            return (((Wolf) livingEntity4).isTame() || isDefending()) ? false : true;
        }));
        this.goalSelector.addGoal(4, new AvoidEntityGoal(this, PolarBear.class, 8.0f, 1.6d, 1.4d, livingEntity5 -> {
            return !isDefending();
        }));
        this.goalSelector.addGoal(5, new StalkPreyGoal());
        this.goalSelector.addGoal(6, new FoxPounceGoal());
        this.goalSelector.addGoal(6, new SeekShelterGoal(1.25d));
        this.goalSelector.addGoal(7, new FoxMeleeAttackGoal(1.2000000476837158d, true));
        this.goalSelector.addGoal(7, new SleepGoal());
        this.goalSelector.addGoal(8, new FoxFollowParentGoal(this, 1.25d));
        this.goalSelector.addGoal(9, new FoxStrollThroughVillageGoal(32, 200));
        this.goalSelector.addGoal(10, new FoxEatBerriesGoal(1.2000000476837158d, 12, 2));
        this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(11, new FoxSearchForItemsGoal());
        this.goalSelector.addGoal(12, new FoxLookAtPlayerGoal(this, Player.class, 24.0f));
        this.goalSelector.addGoal(13, new PerchAndSearchGoal());
        this.targetSelector.addGoal(3, new DefendTrustedTargetGoal(LivingEntity.class, false, false, livingEntity6 -> {
            return TRUSTED_TARGET_SELECTOR.test(livingEntity6) && !trusts(livingEntity6.getUUID());
        }));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public SoundEvent getEatingSound(ItemStack itemStack) {
        return SoundEvents.FOX_EAT;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (!this.level.isClientSide && isAlive() && isEffectiveAi()) {
            this.ticksSinceEaten++;
            ItemStack itemBySlot = getItemBySlot(EquipmentSlot.MAINHAND);
            if (canEat(itemBySlot)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack finishUsingItem = itemBySlot.finishUsingItem(this.level, this);
                    if (!finishUsingItem.isEmpty()) {
                        setItemSlot(EquipmentSlot.MAINHAND, finishUsingItem);
                    }
                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1f) {
                    playSound(getEatingSound(itemBySlot), 1.0f, 1.0f);
                    this.level.broadcastEntityEvent(this, (byte) 45);
                }
            }
            LivingEntity target = getTarget();
            if (target == null || !target.isAlive()) {
                setIsCrouching(false);
                setIsInterested(false);
            }
        }
        if (isSleeping() || isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0f;
            this.zza = 0.0f;
        }
        super.aiStep();
        if (isDefending() && this.random.nextFloat() < 0.05f) {
            playSound(SoundEvents.FOX_AGGRO, 1.0f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean isImmobile() {
        return isDeadOrDying();
    }

    private boolean canEat(ItemStack itemStack) {
        return itemStack.getItem().isEdible() && getTarget() == null && this.onGround && !isSleeping();
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        ItemStack itemStack;
        if (this.random.nextFloat() < 0.2f) {
            float nextFloat = this.random.nextFloat();
            if (nextFloat < 0.05f) {
                itemStack = new ItemStack(Items.EMERALD);
            } else if (nextFloat < 0.2f) {
                itemStack = new ItemStack(Items.EGG);
            } else if (nextFloat < 0.4f) {
                itemStack = this.random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
            } else if (nextFloat < 0.6f) {
                itemStack = new ItemStack(Items.WHEAT);
            } else if (nextFloat < 0.8f) {
                itemStack = new ItemStack(Items.LEATHER);
            } else {
                itemStack = new ItemStack(Items.FEATHER);
            }
            setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 45) {
            ItemStack itemBySlot = getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemBySlot.isEmpty()) {
                for (int i = 0; i < 8; i++) {
                    Vec3 yRot = new Vec3((this.random.nextFloat() - 0.5d) * 0.1d, (Math.random() * 0.1d) + 0.1d, 0.0d).xRot((-this.xRot) * 0.017453292f).yRot((-this.yRot) * 0.017453292f);
                    this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemBySlot), getX() + (getLookAngle().x / 2.0d), getY(), getZ() + (getLookAngle().z / 2.0d), yRot.x, yRot.y + 0.05d, yRot.z);
                }
                return;
            }
            return;
        }
        super.handleEntityEvent(b);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.MAX_HEALTH, 10.0d).add(Attributes.FOLLOW_RANGE, 32.0d).add(Attributes.ATTACK_DAMAGE, 2.0d);
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Fox getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Fox create = EntityType.FOX.create(serverLevel);
        create.setFoxType(this.random.nextBoolean() ? getFoxType() : ((Fox) agableMob).getFoxType());
        return create;
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        Type byBiome = Type.byBiome(serverLevelAccessor.getBiomeName(blockPosition()));
        boolean z = false;
        if (spawnGroupData instanceof FoxGroupData) {
            byBiome = ((FoxGroupData) spawnGroupData).type;
            if (((FoxGroupData) spawnGroupData).getGroupSize() >= 2) {
                z = true;
            }
        } else {
            spawnGroupData = new FoxGroupData(byBiome);
        }
        setFoxType(byBiome);
        if (z) {
            setAge(-24000);
        }
        if (serverLevelAccessor instanceof ServerLevel) {
            setTargetGoals();
        }
        populateDefaultEquipmentSlots(difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    private void setTargetGoals() {
        if (getFoxType() == Type.RED) {
            this.targetSelector.addGoal(4, this.landTargetGoal);
            this.targetSelector.addGoal(4, this.turtleEggTargetGoal);
            this.targetSelector.addGoal(6, this.fishTargetGoal);
        } else {
            this.targetSelector.addGoal(4, this.fishTargetGoal);
            this.targetSelector.addGoal(6, this.landTargetGoal);
            this.targetSelector.addGoal(6, this.turtleEggTargetGoal);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal
    protected void usePlayerItem(Player player, ItemStack itemStack) {
        if (isFood(itemStack)) {
            playSound(getEatingSound(itemStack), 1.0f, 1.0f);
        }
        super.usePlayerItem(player, itemStack);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        if (isBaby()) {
            return entityDimensions.height * 0.85f;
        }
        return 0.4f;
    }

    public Type getFoxType() {
        return Type.byId(((Integer) this.entityData.get(DATA_TYPE_ID)).intValue());
    }

    private void setFoxType(Type type) {
        this.entityData.set(DATA_TYPE_ID, Integer.valueOf(type.getId()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<UUID> getTrustedUUIDs() {
        ArrayList newArrayList = Lists.newArrayList();
        newArrayList.add(((Optional) this.entityData.get(DATA_TRUSTED_ID_0)).orElse(null));
        newArrayList.add(((Optional) this.entityData.get(DATA_TRUSTED_ID_1)).orElse(null));
        return newArrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addTrustedUUID(@Nullable UUID uuid) {
        if (((Optional) this.entityData.get(DATA_TRUSTED_ID_0)).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(uuid));
        } else {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(uuid));
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        List<UUID> trustedUUIDs = getTrustedUUIDs();
        ListTag listTag = new ListTag();
        for (UUID uuid : trustedUUIDs) {
            if (uuid != null) {
                listTag.add(NbtUtils.createUUID(uuid));
            }
        }
        compoundTag.put("Trusted", listTag);
        compoundTag.putBoolean("Sleeping", isSleeping());
        compoundTag.putString("Type", getFoxType().getName());
        compoundTag.putBoolean("Sitting", isSitting());
        compoundTag.putBoolean("Crouching", isCrouching());
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        ListTag list = compoundTag.getList("Trusted", 11);
        for (int i = 0; i < list.size(); i++) {
            addTrustedUUID(NbtUtils.loadUUID(list.get(i)));
        }
        setSleeping(compoundTag.getBoolean("Sleeping"));
        setFoxType(Type.byName(compoundTag.getString("Type")));
        setSitting(compoundTag.getBoolean("Sitting"));
        setIsCrouching(compoundTag.getBoolean("Crouching"));
        if (this.level instanceof ServerLevel) {
            setTargetGoals();
        }
    }

    public boolean isSitting() {
        return getFlag(1);
    }

    public void setSitting(boolean z) {
        setFlag(1, z);
    }

    public boolean isFaceplanted() {
        return getFlag(64);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setFaceplanted(boolean z) {
        setFlag(64, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDefending() {
        return getFlag(128);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDefending(boolean z) {
        setFlag(128, z);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isSleeping() {
        return getFlag(32);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSleeping(boolean z) {
        setFlag(32, z);
    }

    private void setFlag(int i, boolean z) {
        if (z) {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() | i)));
        } else {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & (i ^ (-1)))));
        }
    }

    private boolean getFlag(int i) {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & i) != 0;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlotForItem = Mob.getEquipmentSlotForItem(itemStack);
        return getItemBySlot(equipmentSlotForItem).isEmpty() && equipmentSlotForItem == EquipmentSlot.MAINHAND && super.canTakeItem(itemStack);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canHoldItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        ItemStack itemBySlot = getItemBySlot(EquipmentSlot.MAINHAND);
        return itemBySlot.isEmpty() || (this.ticksSinceEaten > 0 && item.isEdible() && !itemBySlot.getItem().isEdible());
    }

    private void spitOutItem(ItemStack itemStack) {
        if (itemStack.isEmpty() || this.level.isClientSide) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity(this.level, getX() + getLookAngle().x, getY() + 1.0d, getZ() + getLookAngle().z, itemStack);
        itemEntity.setPickUpDelay(40);
        itemEntity.setThrower(getUUID());
        playSound(SoundEvents.FOX_SPIT, 1.0f, 1.0f);
        this.level.addFreshEntity(itemEntity);
    }

    private void dropItemStack(ItemStack itemStack) {
        this.level.addFreshEntity(new ItemEntity(this.level, getX(), getY(), getZ(), itemStack));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (canHoldItem(item)) {
            int count = item.getCount();
            if (count > 1) {
                dropItemStack(item.split(count - 1));
            }
            spitOutItem(getItemBySlot(EquipmentSlot.MAINHAND));
            onItemPickup(itemEntity);
            setItemSlot(EquipmentSlot.MAINHAND, item.split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0f;
            take(itemEntity, item.getCount());
            itemEntity.remove();
            this.ticksSinceEaten = 0;
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (isEffectiveAi()) {
            boolean isInWater = isInWater();
            if (isInWater || getTarget() != null || this.level.isThundering()) {
                wakeUp();
            }
            if (isInWater || isSleeping()) {
                setSitting(false);
            }
            if (isFaceplanted() && this.level.random.nextFloat() < 0.2f) {
                BlockPos blockPosition = blockPosition();
                this.level.levelEvent(2001, blockPosition, Block.getId(this.level.getBlockState(blockPosition)));
            }
        }
        this.interestedAngleO = this.interestedAngle;
        if (isInterested()) {
            this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f;
        } else {
            this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f;
        }
        this.crouchAmountO = this.crouchAmount;
        if (isCrouching()) {
            this.crouchAmount += 0.2f;
            if (this.crouchAmount > 3.0f) {
                this.crouchAmount = 3.0f;
                return;
            }
            return;
        }
        this.crouchAmount = 0.0f;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return itemStack.getItem() == Items.SWEET_BERRIES;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
        ((Fox) mob).addTrustedUUID(player.getUUID());
    }

    public boolean isPouncing() {
        return getFlag(16);
    }

    public void setIsPouncing(boolean z) {
        setFlag(16, z);
    }

    public boolean isFullyCrouched() {
        return this.crouchAmount == 3.0f;
    }

    public void setIsCrouching(boolean z) {
        setFlag(4, z);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isCrouching() {
        return getFlag(4);
    }

    public void setIsInterested(boolean z) {
        setFlag(8, z);
    }

    public boolean isInterested() {
        return getFlag(8);
    }

    public float getHeadRollAngle(float f) {
        return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.11f * 3.1415927f;
    }

    public float getCrouchAmount(float f) {
        return Mth.lerp(f, this.crouchAmountO, this.crouchAmount);
    }

    @Override // net.minecraft.world.entity.Mob
    public void setTarget(@Nullable LivingEntity livingEntity) {
        if (isDefending() && livingEntity == null) {
            setDefending(false);
        }
        super.setTarget(livingEntity);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected int calculateFallDamage(float f, float f2) {
        return Mth.ceil((f - 5.0f) * f2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void wakeUp() {
        setSleeping(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearStates() {
        setIsInterested(false);
        setIsCrouching(false);
        setSitting(false);
        setSleeping(false);
        setDefending(false);
        setFaceplanted(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canMove() {
        return (isSleeping() || isSitting() || isFaceplanted()) ? false : true;
    }

    @Override // net.minecraft.world.entity.Mob
    public void playAmbientSound() {
        SoundEvent ambientSound = getAmbientSound();
        if (ambientSound == SoundEvents.FOX_SCREECH) {
            playSound(ambientSound, 2.0f, getVoicePitch());
        } else {
            super.playAmbientSound();
        }
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (isSleeping()) {
            return SoundEvents.FOX_SLEEP;
        }
        if (!this.level.isDay() && this.random.nextFloat() < 0.1f && this.level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(16.0d, 16.0d, 16.0d), EntitySelector.NO_SPECTATORS).isEmpty()) {
            return SoundEvents.FOX_SCREECH;
        }
        return SoundEvents.FOX_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean trusts(UUID uuid) {
        return getTrustedUUIDs().contains(uuid);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropAllDeathLoot(DamageSource damageSource) {
        ItemStack itemBySlot = getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemBySlot.isEmpty()) {
            spawnAtLocation(itemBySlot);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        super.dropAllDeathLoot(damageSource);
    }

    public static boolean isPathClear(Fox fox, LivingEntity livingEntity) {
        double z = livingEntity.getZ() - fox.getZ();
        double x = livingEntity.getX() - fox.getX();
        double d = z / x;
        for (int i = 0; i < 6; i++) {
            double d2 = d == 0.0d ? 0.0d : z * (i / 6.0f);
            double d3 = d == 0.0d ? x * (i / 6.0f) : d2 / d;
            for (int i2 = 1; i2 < 4; i2++) {
                if (!fox.level.getBlockState(new BlockPos(fox.getX() + d3, fox.getY() + i2, fox.getZ() + d2)).getMaterial().isReplaceable()) {
                    return false;
                }
            }
        }
        return true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxSearchForItemsGoal.class */
    class FoxSearchForItemsGoal extends Goal {
        public FoxSearchForItemsGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && Fox.this.getTarget() == null && Fox.this.getLastHurtByMob() == null && Fox.this.canMove() && Fox.this.getRandom().nextInt(10) == 0 && !Fox.this.level.getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), Fox.ALLOWED_ITEMS).isEmpty() && Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            List<ItemEntity> entitiesOfClass = Fox.this.level.getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), Fox.ALLOWED_ITEMS);
            if (Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && !entitiesOfClass.isEmpty()) {
                Fox.this.getNavigation().moveTo(entitiesOfClass.get(0), 1.2000000476837158d);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            List<ItemEntity> entitiesOfClass = Fox.this.level.getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), Fox.ALLOWED_ITEMS);
            if (!entitiesOfClass.isEmpty()) {
                Fox.this.getNavigation().moveTo(entitiesOfClass.get(0), 1.2000000476837158d);
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxMoveControl.class */
    class FoxMoveControl extends MoveControl {
        public FoxMoveControl() {
            super(Fox.this);
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            if (Fox.this.canMove()) {
                super.tick();
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$StalkPreyGoal.class */
    class StalkPreyGoal extends Goal {
        public StalkPreyGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target;
            return (Fox.this.isSleeping() || (target = Fox.this.getTarget()) == null || !target.isAlive() || !Fox.STALKABLE_PREY.test(target) || Fox.this.distanceToSqr(target) <= 36.0d || Fox.this.isCrouching() || Fox.this.isInterested() || Fox.this.jumping) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setFaceplanted(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            LivingEntity target = Fox.this.getTarget();
            if (target != null && Fox.isPathClear(Fox.this, target)) {
                Fox.this.setIsInterested(true);
                Fox.this.setIsCrouching(true);
                Fox.this.getNavigation().stop();
                Fox.this.getLookControl().setLookAt(target, Fox.this.getMaxHeadYRot(), Fox.this.getMaxHeadXRot());
                return;
            }
            Fox.this.setIsInterested(false);
            Fox.this.setIsCrouching(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            LivingEntity target = Fox.this.getTarget();
            Fox.this.getLookControl().setLookAt(target, Fox.this.getMaxHeadYRot(), Fox.this.getMaxHeadXRot());
            if (Fox.this.distanceToSqr(target) <= 36.0d) {
                Fox.this.setIsInterested(true);
                Fox.this.setIsCrouching(true);
                Fox.this.getNavigation().stop();
                return;
            }
            Fox.this.getNavigation().moveTo(target, 1.5d);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxMeleeAttackGoal.class */
    class FoxMeleeAttackGoal extends MeleeAttackGoal {
        public FoxMeleeAttackGoal(double d, boolean z) {
            super(Fox.this, d, z);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
            if (d <= getAttackReachSqr(livingEntity) && isTimeToAttack()) {
                resetAttackCooldown();
                this.mob.doHurtTarget(livingEntity);
                Fox.this.playSound(SoundEvents.FOX_BITE, 1.0f, 1.0f);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Fox.this.setIsInterested(false);
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return (Fox.this.isSitting() || Fox.this.isSleeping() || Fox.this.isCrouching() || Fox.this.isFaceplanted() || !super.canUse()) ? false : true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxBreedGoal.class */
    class FoxBreedGoal extends BreedGoal {
        public FoxBreedGoal(double d) {
            super(Fox.this, d);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            ((Fox) this.animal).clearStates();
            ((Fox) this.partner).clearStates();
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.BreedGoal
        protected void breed() {
            ServerLevel serverLevel = (ServerLevel) this.level;
            Fox fox = (Fox) this.animal.getBreedOffspring(serverLevel, this.partner);
            if (fox == null) {
                return;
            }
            ServerPlayer loveCause = this.animal.getLoveCause();
            ServerPlayer loveCause2 = this.partner.getLoveCause();
            ServerPlayer serverPlayer = loveCause;
            if (loveCause != null) {
                fox.addTrustedUUID(loveCause.getUUID());
            } else {
                serverPlayer = loveCause2;
            }
            if (loveCause2 != null && loveCause != loveCause2) {
                fox.addTrustedUUID(loveCause2.getUUID());
            }
            if (serverPlayer != null) {
                serverPlayer.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayer, this.animal, this.partner, fox);
            }
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            fox.setAge(-24000);
            fox.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0f, 0.0f);
            serverLevel.addFreshEntityWithPassengers(fox);
            this.level.broadcastEntityEvent(this.animal, (byte) 18);
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$DefendTrustedTargetGoal.class */
    class DefendTrustedTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

        @Nullable
        private LivingEntity trustedLastHurtBy;
        private LivingEntity trustedLastHurt;
        private int timestamp;

        public DefendTrustedTargetGoal(Class<LivingEntity> cls, boolean z, boolean z2, Predicate<LivingEntity> predicate) {
            super(Fox.this, cls, 10, z, z2, predicate);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (this.randomInterval <= 0 || this.mob.getRandom().nextInt(this.randomInterval) == 0) {
                for (UUID uuid : Fox.this.getTrustedUUIDs()) {
                    if (uuid != null && (Fox.this.level instanceof ServerLevel)) {
                        Entity entity = ((ServerLevel) Fox.this.level).getEntity(uuid);
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            this.trustedLastHurt = livingEntity;
                            this.trustedLastHurtBy = livingEntity.getLastHurtByMob();
                            return livingEntity.getLastHurtByMobTimestamp() != this.timestamp && canAttack(this.trustedLastHurtBy, this.targetConditions);
                        }
                    }
                }
                return false;
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            setTarget(this.trustedLastHurtBy);
            this.target = this.trustedLastHurtBy;
            if (this.trustedLastHurt != null) {
                this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
            }
            Fox.this.playSound(SoundEvents.FOX_AGGRO, 1.0f, 1.0f);
            Fox.this.setDefending(true);
            Fox.this.wakeUp();
            super.start();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$SeekShelterGoal.class */
    class SeekShelterGoal extends FleeSunGoal {
        private int interval;

        public SeekShelterGoal(double d) {
            super(Fox.this, d);
            this.interval = 100;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.FleeSunGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (Fox.this.isSleeping() || this.mob.getTarget() != null) {
                return false;
            }
            if (Fox.this.level.isThundering()) {
                return true;
            }
            if (this.interval > 0) {
                this.interval--;
                return false;
            }
            this.interval = 100;
            BlockPos blockPosition = this.mob.blockPosition();
            return Fox.this.level.isDay() && Fox.this.level.canSeeSky(blockPosition) && !((ServerLevel) Fox.this.level).isVillage(blockPosition) && setWantedPos();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.FleeSunGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Fox.this.clearStates();
            super.start();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxAlertableEntitiesSelector.class */
    public class FoxAlertableEntitiesSelector implements Predicate<LivingEntity> {
        public FoxAlertableEntitiesSelector() {
        }

        @Override // java.util.function.Predicate
        public boolean test(LivingEntity livingEntity) {
            if (livingEntity instanceof Fox) {
                return false;
            }
            if ((livingEntity instanceof Chicken) || (livingEntity instanceof Rabbit) || (livingEntity instanceof Monster)) {
                return true;
            }
            return livingEntity instanceof TamableAnimal ? !((TamableAnimal) livingEntity).isTame() : (((livingEntity instanceof Player) && (livingEntity.isSpectator() || ((Player) livingEntity).isCreative())) || Fox.this.trusts(livingEntity.getUUID()) || livingEntity.isSleeping() || livingEntity.isDiscrete()) ? false : true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxBehaviorGoal.class */
    abstract class FoxBehaviorGoal extends Goal {
        private final TargetingConditions alertableTargeting;

        private FoxBehaviorGoal() {
            this.alertableTargeting = new TargetingConditions().range(12.0d).allowUnseeable().selector(Fox.this.new FoxAlertableEntitiesSelector());
        }

        protected boolean hasShelter() {
            BlockPos blockPos = new BlockPos(Fox.this.getX(), Fox.this.getBoundingBox().maxY, Fox.this.getZ());
            return !Fox.this.level.canSeeSky(blockPos) && Fox.this.getWalkTargetValue(blockPos) >= 0.0f;
        }

        protected boolean alertable() {
            return !Fox.this.level.getNearbyEntities(LivingEntity.class, this.alertableTargeting, Fox.this, Fox.this.getBoundingBox().inflate(12.0d, 6.0d, 12.0d)).isEmpty();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$SleepGoal.class */
    class SleepGoal extends FoxBehaviorGoal {
        private int countdown;

        public SleepGoal() {
            super();
            this.countdown = Fox.this.random.nextInt(140);
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (Fox.this.xxa == 0.0f && Fox.this.yya == 0.0f && Fox.this.zza == 0.0f) {
                return canSleep() || Fox.this.isSleeping();
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return canSleep();
        }

        private boolean canSleep() {
            if (this.countdown <= 0) {
                return Fox.this.level.isDay() && hasShelter() && !alertable();
            }
            this.countdown--;
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.countdown = Fox.this.random.nextInt(140);
            Fox.this.clearStates();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setIsCrouching(false);
            Fox.this.setIsInterested(false);
            Fox.this.setJumping(false);
            Fox.this.setSleeping(true);
            Fox.this.getNavigation().stop();
            Fox.this.getMoveControl().setWantedPosition(Fox.this.getX(), Fox.this.getY(), Fox.this.getZ(), 0.0d);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$PerchAndSearchGoal.class */
    class PerchAndSearchGoal extends FoxBehaviorGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public PerchAndSearchGoal() {
            super();
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return Fox.this.getLastHurtByMob() == null && Fox.this.getRandom().nextFloat() < 0.02f && !Fox.this.isSleeping() && Fox.this.getTarget() == null && Fox.this.getNavigation().isDone() && !alertable() && !Fox.this.isPouncing() && !Fox.this.isCrouching();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return this.looksRemaining > 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            resetLook();
            this.looksRemaining = 2 + Fox.this.getRandom().nextInt(3);
            Fox.this.setSitting(true);
            Fox.this.getNavigation().stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            Fox.this.setSitting(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.lookTime--;
            if (this.lookTime <= 0) {
                this.looksRemaining--;
                resetLook();
            }
            Fox.this.getLookControl().setLookAt(Fox.this.getX() + this.relX, Fox.this.getEyeY(), Fox.this.getZ() + this.relZ, Fox.this.getMaxHeadYRot(), Fox.this.getMaxHeadXRot());
        }

        private void resetLook() {
            double nextDouble = 6.283185307179586d * Fox.this.getRandom().nextDouble();
            this.relX = Math.cos(nextDouble);
            this.relZ = Math.sin(nextDouble);
            this.lookTime = 80 + Fox.this.getRandom().nextInt(20);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxEatBerriesGoal.class */
    public class FoxEatBerriesGoal extends MoveToBlockGoal {
        protected int ticksWaited;

        public FoxEatBerriesGoal(double d, int i, int i2) {
            super(Fox.this, d, i, i2);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        public double acceptedDistance() {
            return 2.0d;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            return blockState.is(Blocks.SWEET_BERRY_BUSH) && ((Integer) blockState.getValue(SweetBerryBushBlock.AGE)).intValue() >= 2;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    onReachedTarget();
                } else {
                    this.ticksWaited++;
                }
            } else if (!isReachedTarget() && Fox.this.random.nextFloat() < 0.05f) {
                Fox.this.playSound(SoundEvents.FOX_SNIFF, 1.0f, 1.0f);
            }
            super.tick();
        }

        protected void onReachedTarget() {
            if (!Fox.this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return;
            }
            BlockState blockState = Fox.this.level.getBlockState(this.blockPos);
            if (!blockState.is(Blocks.SWEET_BERRY_BUSH)) {
                return;
            }
            int intValue = ((Integer) blockState.getValue(SweetBerryBushBlock.AGE)).intValue();
            blockState.setValue(SweetBerryBushBlock.AGE, 1);
            int nextInt = 1 + Fox.this.level.random.nextInt(2) + (intValue == 3 ? 1 : 0);
            if (Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Fox.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                nextInt--;
            }
            if (nextInt > 0) {
                Block.popResource(Fox.this.level, this.blockPos, new ItemStack(Items.SWEET_BERRIES, nextInt));
            }
            Fox.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 1.0f);
            Fox.this.level.setBlock(this.blockPos, (BlockState) blockState.setValue(SweetBerryBushBlock.AGE, 1), 2);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !Fox.this.isSleeping() && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.ticksWaited = 0;
            Fox.this.setSitting(false);
            super.start();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxGroupData.class */
    public static class FoxGroupData extends AgableMob.AgableMobGroupData {
        public final Type type;

        public FoxGroupData(Type type) {
            super(false);
            this.type = type;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FaceplantGoal.class */
    class FaceplantGoal extends Goal {
        int countdown;

        public FaceplantGoal() {
            setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return Fox.this.isFaceplanted();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return canUse() && this.countdown > 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.countdown = 40;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            Fox.this.setFaceplanted(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.countdown--;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxPanicGoal.class */
    class FoxPanicGoal extends PanicGoal {
        public FoxPanicGoal(double d) {
            super(Fox.this, d);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.PanicGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !Fox.this.isDefending() && super.canUse();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxStrollThroughVillageGoal.class */
    class FoxStrollThroughVillageGoal extends StrollThroughVillageGoal {
        public FoxStrollThroughVillageGoal(int i, int i2) {
            super(Fox.this, i2);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Fox.this.clearStates();
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.StrollThroughVillageGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && canFoxMove();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.StrollThroughVillageGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return super.canContinueToUse() && canFoxMove();
        }

        private boolean canFoxMove() {
            return (Fox.this.isSleeping() || Fox.this.isSitting() || Fox.this.isDefending() || Fox.this.getTarget() != null) ? false : true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxFloatGoal.class */
    class FoxFloatGoal extends FloatGoal {
        public FoxFloatGoal() {
            super(Fox.this);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            Fox.this.clearStates();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.FloatGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return (Fox.this.isInWater() && Fox.this.getFluidHeight(FluidTags.WATER) > 0.25d) || Fox.this.isInLava();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxPounceGoal.class */
    public class FoxPounceGoal extends JumpGoal {
        public FoxPounceGoal() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target;
            if (!Fox.this.isFullyCrouched() || (target = Fox.this.getTarget()) == null || !target.isAlive() || target.getMotionDirection() != target.getDirection()) {
                return false;
            }
            boolean isPathClear = Fox.isPathClear(Fox.this, target);
            if (!isPathClear) {
                Fox.this.getNavigation().createPath(target, 0);
                Fox.this.setIsCrouching(false);
                Fox.this.setIsInterested(false);
            }
            return isPathClear;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            LivingEntity target = Fox.this.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double d = Fox.this.getDeltaMovement().y;
            return (d * d >= 0.05000000074505806d || Math.abs(Fox.this.xRot) >= 15.0f || !Fox.this.onGround) && !Fox.this.isFaceplanted();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean isInterruptable() {
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Fox.this.setJumping(true);
            Fox.this.setIsPouncing(true);
            Fox.this.setIsInterested(false);
            LivingEntity target = Fox.this.getTarget();
            Fox.this.getLookControl().setLookAt(target, 60.0f, 30.0f);
            Vec3 normalize = new Vec3(target.getX() - Fox.this.getX(), target.getY() - Fox.this.getY(), target.getZ() - Fox.this.getZ()).normalize();
            Fox.this.setDeltaMovement(Fox.this.getDeltaMovement().add(normalize.x * 0.8d, 0.9d, normalize.z * 0.8d));
            Fox.this.getNavigation().stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            Fox.this.setIsCrouching(false);
            Fox.this.crouchAmount = 0.0f;
            Fox.this.crouchAmountO = 0.0f;
            Fox.this.setIsInterested(false);
            Fox.this.setIsPouncing(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            LivingEntity target = Fox.this.getTarget();
            if (target != null) {
                Fox.this.getLookControl().setLookAt(target, 60.0f, 30.0f);
            }
            if (!Fox.this.isFaceplanted()) {
                Vec3 deltaMovement = Fox.this.getDeltaMovement();
                if (deltaMovement.y * deltaMovement.y < 0.029999999329447746d && Fox.this.xRot != 0.0f) {
                    Fox.this.xRot = Mth.rotlerp(Fox.this.xRot, 0.0f, 0.2f);
                } else {
                    Fox.this.xRot = (float) (Math.signum(-deltaMovement.y) * Math.acos(Math.sqrt(Entity.getHorizontalDistanceSqr(deltaMovement)) / deltaMovement.length()) * 57.2957763671875d);
                }
            }
            if (target == null || Fox.this.distanceTo(target) > 2.0f) {
                if (Fox.this.xRot > 0.0f && Fox.this.onGround && ((float) Fox.this.getDeltaMovement().y) != 0.0f && Fox.this.level.getBlockState(Fox.this.blockPosition()).is(Blocks.SNOW)) {
                    Fox.this.xRot = 60.0f;
                    Fox.this.setTarget(null);
                    Fox.this.setFaceplanted(true);
                    return;
                }
                return;
            }
            Fox.this.doHurtTarget(target);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.55f * getEyeHeight(), getBbWidth() * 0.4f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxLookControl.class */
    public class FoxLookControl extends LookControl {
        public FoxLookControl() {
            super(Fox.this);
        }

        @Override // net.minecraft.world.entity.p000ai.control.LookControl
        public void tick() {
            if (!Fox.this.isSleeping()) {
                super.tick();
            }
        }

        @Override // net.minecraft.world.entity.p000ai.control.LookControl
        protected boolean resetXRotOnTick() {
            if (!Fox.this.isPouncing() && !Fox.this.isCrouching()) {
                if ((!Fox.this.isInterested()) & (!Fox.this.isFaceplanted())) {
                    return true;
                }
            }
            return false;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxFollowParentGoal.class */
    class FoxFollowParentGoal extends FollowParentGoal {
        private final Fox fox;

        public FoxFollowParentGoal(Fox fox, double d) {
            super(fox, d);
            this.fox = fox;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.FollowParentGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.fox.isDefending() && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.FollowParentGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return !this.fox.isDefending() && super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.FollowParentGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.fox.clearStates();
            super.start();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Fox$FoxLookAtPlayerGoal.class */
    class FoxLookAtPlayerGoal extends LookAtPlayerGoal {
        public FoxLookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> cls, float f) {
            super(mob, cls, f);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.LookAtPlayerGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return (!super.canUse() || Fox.this.isFaceplanted() || Fox.this.isInterested()) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.LookAtPlayerGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return (!super.canContinueToUse() || Fox.this.isFaceplanted() || Fox.this.isInterested()) ? false : true;
        }
    }
}
