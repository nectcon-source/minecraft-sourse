package net.minecraft.world.entity.animal.horse;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/AbstractHorse.class */
public abstract class AbstractHorse extends Animal implements ContainerListener, PlayerRideableJumping, Saddleable {
    private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = livingEntity -> {
        return (livingEntity instanceof AbstractHorse) && ((AbstractHorse) livingEntity).isBred();
    };
    private static final TargetingConditions MOMMY_TARGETING = new TargetingConditions().range(16.0d).allowInvulnerable().allowSameTeam().allowUnseeable().selector(PARENT_HORSE_SELECTOR);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<UUID>> DATA_ID_OWNER_UUID = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.OPTIONAL_UUID);
    private int eatingCounter;
    private int mouthCounter;
    private int standCounter;
    public int tailCounter;
    public int sprintCounter;
    protected boolean isJumping;
    protected SimpleContainer inventory;
    protected int temper;
    protected float playerJumpPendingScale;
    private boolean allowStandSliding;
    private float eatAnim;
    private float eatAnimO;
    private float standAnim;
    private float standAnimO;
    private float mouthAnim;
    private float mouthAnimO;
    protected boolean canGallop;
    protected int gallopSoundCounter;

    protected AbstractHorse(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
        this.canGallop = true;
        this.maxUpStep = 1.0f;
        createInventory();
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2d));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2d));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0d, AbstractHorse.class));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0d));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7d));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FLAGS, (byte) 0);
        this.entityData.define(DATA_ID_OWNER_UUID, Optional.empty());
    }

    protected boolean getFlag(int i) {
        return (((Byte) this.entityData.get(DATA_ID_FLAGS)).byteValue() & i) != 0;
    }

    protected void setFlag(int i, boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_ID_FLAGS)).byteValue();
        if (z) {
            this.entityData.set(DATA_ID_FLAGS, Byte.valueOf((byte) (byteValue | i)));
        } else {
            this.entityData.set(DATA_ID_FLAGS, Byte.valueOf((byte) (byteValue & (i ^ (-1)))));
        }
    }

    public boolean isTamed() {
        return getFlag(2);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return (UUID) ((Optional) this.entityData.get(DATA_ID_OWNER_UUID)).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_ID_OWNER_UUID, Optional.ofNullable(uuid));
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public void setTamed(boolean z) {
        setFlag(2, z);
    }

    public void setIsJumping(boolean z) {
        this.isJumping = z;
    }

    @Override // net.minecraft.world.entity.PathfinderMob
    protected void onLeashDistance(float f) {
        if (f > 6.0f && isEating()) {
            setEating(false);
        }
    }

    public boolean isEating() {
        return getFlag(16);
    }

    public boolean isStanding() {
        return getFlag(32);
    }

    public boolean isBred() {
        return getFlag(8);
    }

    public void setBred(boolean z) {
        setFlag(8, z);
    }

    @Override // net.minecraft.world.entity.Saddleable
    public boolean isSaddleable() {
        return isAlive() && !isBaby() && isTamed();
    }

    @Override // net.minecraft.world.entity.Saddleable
    public void equipSaddle(@Nullable SoundSource soundSource) {
        this.inventory.setItem(0, new ItemStack(Items.SADDLE));
        if (soundSource != null) {
            this.level.playSound((Player) null, this, SoundEvents.HORSE_SADDLE, soundSource, 0.5f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.Saddleable
    public boolean isSaddled() {
        return getFlag(4);
    }

    public int getTemper() {
        return this.temper;
    }

    public void setTemper(int i) {
        this.temper = i;
    }

    public int modifyTemper(int i) {
        int clamp = Mth.clamp(getTemper() + i, 0, getMaxTemper());
        setTemper(clamp);
        return clamp;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean isPushable() {
        return !isVehicle();
    }

    private void eating() {
        SoundEvent eatingSound;
        openMouth();
        if (!isSilent() && (eatingSound = getEatingSound()) != null) {
            this.level.playSound(null, getX(), getY(), getZ(), eatingSound, getSoundSource(), 1.0f, 1.0f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f));
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        if (f > 1.0f) {
            playSound(SoundEvents.HORSE_LAND, 0.4f, 1.0f);
        }
        int calculateFallDamage = calculateFallDamage(f, f2);
        if (calculateFallDamage <= 0) {
            return false;
        }
        hurt(DamageSource.FALL, calculateFallDamage);
        if (isVehicle()) {
            Iterator<Entity> it = getIndirectPassengers().iterator();
            while (it.hasNext()) {
                it.next().hurt(DamageSource.FALL, calculateFallDamage);
            }
        }
        playBlockFallSound();
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected int calculateFallDamage(float f, float f2) {
        return Mth.ceil(((f * 0.5f) - 3.0f) * f2);
    }

    protected int getInventorySize() {
        return 2;
    }

    protected void createInventory() {
        SimpleContainer simpleContainer = this.inventory;
        this.inventory = new SimpleContainer(getInventorySize());
        if (simpleContainer != null) {
            simpleContainer.removeListener(this);
            int min = Math.min(simpleContainer.getContainerSize(), this.inventory.getContainerSize());
            for (int i = 0; i < min; i++) {
                ItemStack item = simpleContainer.getItem(i);
                if (!item.isEmpty()) {
                    this.inventory.setItem(i, item.copy());
                }
            }
        }
        this.inventory.addListener(this);
        updateContainerEquipment();
    }

    protected void updateContainerEquipment() {
        if (this.level.isClientSide) {
            return;
        }
        setFlag(4, !this.inventory.getItem(0).isEmpty());
    }

    @Override // net.minecraft.world.ContainerListener
    public void containerChanged(Container container) {
        boolean isSaddled = isSaddled();
        updateContainerEquipment();
        if (this.tickCount > 20 && !isSaddled && isSaddled()) {
            playSound(SoundEvents.HORSE_SADDLE, 0.5f, 1.0f);
        }
    }

    public double getCustomJump() {
        return getAttributeValue(Attributes.JUMP_STRENGTH);
    }

    @Nullable
    protected SoundEvent getEatingSound() {
        return null;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.random.nextInt(3) == 0) {
            stand();
            return null;
        }
        return null;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(10) == 0 && !isImmobile()) {
            stand();
            return null;
        }
        return null;
    }

    @Nullable
    protected SoundEvent getAngrySound() {
        stand();
        return null;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (blockState.getMaterial().isLiquid()) {
            return;
        }
        BlockState blockState2 = this.level.getBlockState(blockPos.above());
        SoundType soundType = blockState.getSoundType();
        if (blockState2.is(Blocks.SNOW)) {
            soundType = blockState2.getSoundType();
        }
        if (isVehicle() && this.canGallop) {
            this.gallopSoundCounter++;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                playGallopSound(soundType);
                return;
            } else {
                if (this.gallopSoundCounter <= 5) {
                    playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15f, soundType.getPitch());
                    return;
                }
                return;
            }
        }
        if (soundType == SoundType.WOOD) {
            playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15f, soundType.getPitch());
        } else {
            playSound(SoundEvents.HORSE_STEP, soundType.getVolume() * 0.15f, soundType.getPitch());
        }
    }

    protected void playGallopSound(SoundType soundType) {
        playSound(SoundEvents.HORSE_GALLOP, soundType.getVolume() * 0.15f, soundType.getPitch());
    }

    public static AttributeSupplier.Builder createBaseHorseAttributes() {
        return Mob.createMobAttributes().add(Attributes.JUMP_STRENGTH).add(Attributes.MAX_HEALTH, 53.0d).add(Attributes.MOVEMENT_SPEED, 0.22499999403953552d);
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxSpawnClusterSize() {
        return 6;
    }

    public int getMaxTemper() {
        return 100;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getSoundVolume() {
        return 0.8f;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public int getAmbientSoundInterval() {
        return 400;
    }

    public void openInventory(Player player) {
        if (this.level.isClientSide) {
            return;
        }
        if ((!isVehicle() || hasPassenger(player)) && isTamed()) {
            player.openHorseInventory(this, this.inventory);
        }
    }

    public InteractionResult fedFood(Player player, ItemStack itemStack) {
        boolean handleEating = handleEating(player, itemStack);
        if (!player.abilities.instabuild) {
            itemStack.shrink(1);
        }
        if (this.level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        return handleEating ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    protected boolean handleEating(Player player, ItemStack itemStack) {
        boolean z = false;
        float f = 0.0f;
        int i = 0;
        int i2 = 0;
        Item item = itemStack.getItem();
        if (item == Items.WHEAT) {
            f = 2.0f;
            i = 20;
            i2 = 3;
        } else if (item == Items.SUGAR) {
            f = 1.0f;
            i = 30;
            i2 = 3;
        } else if (item == Blocks.HAY_BLOCK.asItem()) {
            f = 20.0f;
            i = 180;
        } else if (item == Items.APPLE) {
            f = 3.0f;
            i = 60;
            i2 = 3;
        } else if (item == Items.GOLDEN_CARROT) {
            f = 4.0f;
            i = 60;
            i2 = 5;
            if (!this.level.isClientSide && isTamed() && getAge() == 0 && !isInLove()) {
                z = true;
                setInLove(player);
            }
        } else if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
            f = 10.0f;
            i = 240;
            i2 = 10;
            if (!this.level.isClientSide && isTamed() && getAge() == 0 && !isInLove()) {
                z = true;
                setInLove(player);
            }
        }
        if (getHealth() < getMaxHealth() && f > 0.0f) {
            heal(f);
            z = true;
        }
        if (isBaby() && i > 0) {
            this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, getRandomX(1.0d), getRandomY() + 0.5d, getRandomZ(1.0d), 0.0d, 0.0d, 0.0d);
            if (!this.level.isClientSide) {
                ageUp(i);
            }
            z = true;
        }
        if (i2 > 0 && ((z || !isTamed()) && getTemper() < getMaxTemper())) {
            z = true;
            if (!this.level.isClientSide) {
                modifyTemper(i2);
            }
        }
        if (z) {
            eating();
        }
        return z;
    }

    protected void doPlayerRide(Player player) {
        setEating(false);
        setStanding(false);
        if (!this.level.isClientSide) {
            player.yRot = this.yRot;
            player.xRot = this.xRot;
            player.startRiding(this);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean isImmobile() {
        return (super.isImmobile() && isVehicle() && isSaddled()) || isEating() || isStanding();
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.inventory == null) {
            return;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack item = this.inventory.getItem(i);
            if (!item.isEmpty() && !EnchantmentHelper.hasVanishingCurse(item)) {
                spawnAtLocation(item);
            }
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.random.nextInt(200) == 0) {
            moveTail();
        }
        super.aiStep();
        if (this.level.isClientSide || !isAlive()) {
            return;
        }
        if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            heal(1.0f);
        }
        if (canEatGrass()) {
            if (!isEating() && !isVehicle() && this.random.nextInt(300) == 0 && this.level.getBlockState(blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                setEating(true);
            }
            if (isEating()) {
                int i = this.eatingCounter + 1;
                this.eatingCounter = i;
                if (i > 50) {
                    this.eatingCounter = 0;
                    setEating(false);
                }
            }
        }
        followMommy();
    }

    protected void followMommy() {
        LivingEntity nearestEntity;
        if (isBred() && isBaby() && !isEating() && (nearestEntity = this.level.getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, getX(), getY(), getZ(), getBoundingBox().inflate(16.0d))) != null && distanceToSqr(nearestEntity) > 4.0d) {
            this.navigation.createPath(nearestEntity, 0);
        }
    }

    public boolean canEatGrass() {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.mouthCounter > 0) {
            int i = this.mouthCounter + 1;
            this.mouthCounter = i;
            if (i > 30) {
                this.mouthCounter = 0;
                setFlag(64, false);
            }
        }
        if ((isControlledByLocalInstance() || isEffectiveAi()) && this.standCounter > 0) {
            int i2 = this.standCounter + 1;
            this.standCounter = i2;
            if (i2 > 20) {
                this.standCounter = 0;
                setStanding(false);
            }
        }
        if (this.tailCounter > 0) {
            int i3 = this.tailCounter + 1;
            this.tailCounter = i3;
            if (i3 > 8) {
                this.tailCounter = 0;
            }
        }
        if (this.sprintCounter > 0) {
            this.sprintCounter++;
            if (this.sprintCounter > 300) {
                this.sprintCounter = 0;
            }
        }
        this.eatAnimO = this.eatAnim;
        if (isEating()) {
            this.eatAnim += ((1.0f - this.eatAnim) * 0.4f) + 0.05f;
            if (this.eatAnim > 1.0f) {
                this.eatAnim = 1.0f;
            }
        } else {
            this.eatAnim += ((0.0f - this.eatAnim) * 0.4f) - 0.05f;
            if (this.eatAnim < 0.0f) {
                this.eatAnim = 0.0f;
            }
        }
        this.standAnimO = this.standAnim;
        if (isStanding()) {
            this.eatAnim = 0.0f;
            this.eatAnimO = this.eatAnim;
            this.standAnim += ((1.0f - this.standAnim) * 0.4f) + 0.05f;
            if (this.standAnim > 1.0f) {
                this.standAnim = 1.0f;
            }
        } else {
            this.allowStandSliding = false;
            this.standAnim += (((((0.8f * this.standAnim) * this.standAnim) * this.standAnim) - this.standAnim) * 0.6f) - 0.05f;
            if (this.standAnim < 0.0f) {
                this.standAnim = 0.0f;
            }
        }
        this.mouthAnimO = this.mouthAnim;
        if (getFlag(64)) {
            this.mouthAnim += ((1.0f - this.mouthAnim) * 0.7f) + 0.05f;
            if (this.mouthAnim > 1.0f) {
                this.mouthAnim = 1.0f;
                return;
            }
            return;
        }
        this.mouthAnim += ((0.0f - this.mouthAnim) * 0.7f) - 0.05f;
        if (this.mouthAnim < 0.0f) {
            this.mouthAnim = 0.0f;
        }
    }

    private void openMouth() {
        if (!this.level.isClientSide) {
            this.mouthCounter = 1;
            setFlag(64, true);
        }
    }

    public void setEating(boolean z) {
        setFlag(16, z);
    }

    public void setStanding(boolean z) {
        if (z) {
            setEating(false);
        }
        setFlag(32, z);
    }

    private void stand() {
        if (isControlledByLocalInstance() || isEffectiveAi()) {
            this.standCounter = 1;
            setStanding(true);
        }
    }

    public void makeMad() {
        if (!isStanding()) {
            stand();
            SoundEvent angrySound = getAngrySound();
            if (angrySound != null) {
                playSound(angrySound, getSoundVolume(), getVoicePitch());
            }
        }
    }

    public boolean tameWithName(Player player) {
        setOwnerUUID(player.getUUID());
        setTamed(true);
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer) player, this);
        }
        this.level.broadcastEntityEvent(this, (byte) 7);
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        double d;
        if (!isAlive()) {
            return;
        }
        if (!isVehicle() || !canBeControlledByRider() || !isSaddled()) {
            this.flyingSpeed = 0.02f;
            super.travel(vec3);
            return;
        }
        LivingEntity livingEntity = (LivingEntity) getControllingPassenger();
        this.yRot = livingEntity.yRot;
        this.yRotO = this.yRot;
        this.xRot = livingEntity.xRot * 0.5f;
        setRot(this.yRot, this.xRot);
        this.yBodyRot = this.yRot;
        this.yHeadRot = this.yBodyRot;
        float f = livingEntity.xxa * 0.5f;
        float f2 = livingEntity.zza;
        if (f2 <= 0.0f) {
            f2 *= 0.25f;
            this.gallopSoundCounter = 0;
        }
        if (this.onGround && this.playerJumpPendingScale == 0.0f && isStanding() && !this.allowStandSliding) {
            f = 0.0f;
            f2 = 0.0f;
        }
        if (this.playerJumpPendingScale > 0.0f && !isJumping() && this.onGround) {
            double customJump = getCustomJump() * this.playerJumpPendingScale * getBlockJumpFactor();
            if (hasEffect(MobEffects.JUMP)) {
                d = customJump + ((getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1f);
            } else {
                d = customJump;
            }
            Vec3 deltaMovement = getDeltaMovement();
            setDeltaMovement(deltaMovement.x, d, deltaMovement.z);
            setIsJumping(true);
            this.hasImpulse = true;
            if (f2 > 0.0f) {
                setDeltaMovement(getDeltaMovement().add((-0.4f) * Mth.sin(this.yRot * 0.017453292f) * this.playerJumpPendingScale, 0.0d, 0.4f * Mth.cos(this.yRot * 0.017453292f) * this.playerJumpPendingScale));
            }
            this.playerJumpPendingScale = 0.0f;
        }
        this.flyingSpeed = getSpeed() * 0.1f;
        if (isControlledByLocalInstance()) {
            setSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new Vec3(f, vec3.y, f2));
        } else if (livingEntity instanceof Player) {
            setDeltaMovement(Vec3.ZERO);
        }
        if (this.onGround) {
            this.playerJumpPendingScale = 0.0f;
            setIsJumping(false);
        }
        calculateEntityAnimation(this, false);
    }

    protected void playJumpSound() {
        playSound(SoundEvents.HORSE_JUMP, 0.4f, 1.0f);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("EatingHaystack", isEating());
        compoundTag.putBoolean("Bred", isBred());
        compoundTag.putInt("Temper", getTemper());
        compoundTag.putBoolean("Tame", isTamed());
        if (getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", getOwnerUUID());
        }
        if (!this.inventory.getItem(0).isEmpty()) {
            compoundTag.put("SaddleItem", this.inventory.getItem(0).save(new CompoundTag()));
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        UUID convertMobOwnerIfNecessary;
        super.readAdditionalSaveData(compoundTag);
        setEating(compoundTag.getBoolean("EatingHaystack"));
        setBred(compoundTag.getBoolean("Bred"));
        setTemper(compoundTag.getInt("Temper"));
        setTamed(compoundTag.getBoolean("Tame"));
        if (compoundTag.hasUUID("Owner")) {
            convertMobOwnerIfNecessary = compoundTag.getUUID("Owner");
        } else {
            convertMobOwnerIfNecessary = OldUsersConverter.convertMobOwnerIfNecessary(getServer(), compoundTag.getString("Owner"));
        }
        if (convertMobOwnerIfNecessary != null) {
            setOwnerUUID(convertMobOwnerIfNecessary);
        }
        if (compoundTag.contains("SaddleItem", 10)) {
            ItemStack m66of = ItemStack.of(compoundTag.getCompound("SaddleItem"));
            if (m66of.getItem() == Items.SADDLE) {
                this.inventory.setItem(0, m66of);
            }
        }
        updateContainerEquipment();
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        return false;
    }

    protected boolean canParent() {
        return !isVehicle() && !isPassenger() && isTamed() && !isBaby() && getHealth() >= getMaxHealth() && isInLove();
    }

    @Override // net.minecraft.world.entity.AgableMob
    @Nullable
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return null;
    }

    protected void setOffspringAttributes(AgableMob agableMob, AbstractHorse abstractHorse) {
        abstractHorse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(((getAttributeBaseValue(Attributes.MAX_HEALTH) + agableMob.getAttributeBaseValue(Attributes.MAX_HEALTH)) + generateRandomMaxHealth()) / 3.0d);
        abstractHorse.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(((getAttributeBaseValue(Attributes.JUMP_STRENGTH) + agableMob.getAttributeBaseValue(Attributes.JUMP_STRENGTH)) + generateRandomJumpStrength()) / 3.0d);
        abstractHorse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(((getAttributeBaseValue(Attributes.MOVEMENT_SPEED) + agableMob.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)) + generateRandomSpeed()) / 3.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeControlledByRider() {
        return getControllingPassenger() instanceof LivingEntity;
    }

    public float getEatAnim(float f) {
        return Mth.lerp(f, this.eatAnimO, this.eatAnim);
    }

    public float getStandAnim(float f) {
        return Mth.lerp(f, this.standAnimO, this.standAnim);
    }

    public float getMouthAnim(float f) {
        return Mth.lerp(f, this.mouthAnimO, this.mouthAnim);
    }

    @Override // net.minecraft.world.entity.PlayerRideableJumping
    public void onPlayerJump(int i) {
        if (!isSaddled()) {
            return;
        }
        if (i < 0) {
            i = 0;
        } else {
            this.allowStandSliding = true;
            stand();
        }
        if (i >= 90) {
            this.playerJumpPendingScale = 1.0f;
        } else {
            this.playerJumpPendingScale = 0.4f + ((0.4f * i) / 90.0f);
        }
    }

    @Override // net.minecraft.world.entity.PlayerRideableJumping
    public boolean canJump() {
        return isSaddled();
    }

    @Override // net.minecraft.world.entity.PlayerRideableJumping
    public void handleStartJump(int i) {
        this.allowStandSliding = true;
        stand();
        playJumpSound();
    }

    @Override // net.minecraft.world.entity.PlayerRideableJumping
    public void handleStopJump() {
    }

    protected void spawnTamingParticles(boolean z) {
        ParticleOptions particleOptions = z ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; i++) {
            this.level.addParticle(particleOptions, getRandomX(1.0d), getRandomY() + 0.5d, getRandomZ(1.0d), this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 7) {
            spawnTamingParticles(true);
        } else if (b == 6) {
            spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void positionRider(Entity entity) {
        super.positionRider(entity);
        if (entity instanceof Mob) {
            this.yBodyRot = ((Mob) entity).yBodyRot;
        }
        if (this.standAnimO > 0.0f) {
            float sin = Mth.sin(this.yBodyRot * 0.017453292f);
            float cos = Mth.cos(this.yBodyRot * 0.017453292f);
            float f = 0.7f * this.standAnimO;
            entity.setPos(getX() + (f * sin), getY() + getPassengersRidingOffset() + entity.getMyRidingOffset() + (0.15f * this.standAnimO), getZ() - (f * cos));
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).yBodyRot = this.yBodyRot;
            }
        }
    }

    protected float generateRandomMaxHealth() {
        return 15.0f + this.random.nextInt(8) + this.random.nextInt(9);
    }

    protected double generateRandomJumpStrength() {
        return 0.4000000059604645d + (this.random.nextDouble() * 0.2d) + (this.random.nextDouble() * 0.2d) + (this.random.nextDouble() * 0.2d);
    }

    protected double generateRandomSpeed() {
        return (0.44999998807907104d + (this.random.nextDouble() * 0.3d) + (this.random.nextDouble() * 0.3d) + (this.random.nextDouble() * 0.3d)) * 0.25d;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean onClimbable() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.95f;
    }

    public boolean canWearArmor() {
        return false;
    }

    public boolean isWearingArmor() {
        return !getItemBySlot(EquipmentSlot.CHEST).isEmpty();
    }

    public boolean isArmor(ItemStack itemStack) {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        int i2 = i - 400;
        if (i2 >= 0 && i2 < 2 && i2 < this.inventory.getContainerSize()) {
            if (i2 == 0 && itemStack.getItem() != Items.SADDLE) {
                return false;
            }
            if (i2 == 1 && (!canWearArmor() || !isArmor(itemStack))) {
                return false;
            }
            this.inventory.setItem(i2, itemStack);
            updateContainerEquipment();
            return true;
        }
        int i3 = (i - 500) + 2;
        if (i3 >= 2 && i3 < this.inventory.getContainerSize()) {
            this.inventory.setItem(i3, itemStack);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity getControllingPassenger() {
        if (getPassengers().isEmpty()) {
            return null;
        }
        return getPassengers().get(0);
    }

    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 vec3, LivingEntity livingEntity) {
        double x = getX() + vec3.x;
        double d = getBoundingBox().minY;
        double z = getZ() + vec3.z;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        UnmodifiableIterator it = livingEntity.getDismountPoses().iterator();
        while (it.hasNext()) {
            Pose pose = (Pose) it.next();
            mutableBlockPos.set(x, d, z);
            double d2 = getBoundingBox().maxY + 0.75d;
            do {
                double blockFloorHeight = this.level.getBlockFloorHeight(mutableBlockPos);
                if (mutableBlockPos.getY() + blockFloorHeight <= d2) {
                    if (DismountHelper.isBlockFloorValid(blockFloorHeight)) {
                        AABB localBoundsForPose = livingEntity.getLocalBoundsForPose(pose);
                        Vec3 vec32 = new Vec3(x, mutableBlockPos.getY() + blockFloorHeight, z);
                        if (DismountHelper.canDismountTo(this.level, livingEntity, localBoundsForPose.move(vec32))) {
                            livingEntity.setPose(pose);
                            return vec32;
                        }
                    }
                    mutableBlockPos.move(Direction.UP);
                }
            } while (mutableBlockPos.getY() < d2);
        }
        return null;
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 dismountLocationInDirection = getDismountLocationInDirection(getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), this.yRot + (livingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0f : -90.0f)), livingEntity);
        if (dismountLocationInDirection != null) {
            return dismountLocationInDirection;
        }
        Vec3 dismountLocationInDirection2 = getDismountLocationInDirection(getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), this.yRot + (livingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0f : -90.0f)), livingEntity);
        if (dismountLocationInDirection2 != null) {
            return dismountLocationInDirection2;
        }
        return position();
    }

    protected void randomizeAttributes() {
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData(0.2f);
        }
        randomizeAttributes();
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }
}
