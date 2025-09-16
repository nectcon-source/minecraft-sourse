package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/Player.class */
public abstract class Player extends LivingEntity {
    public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6f, 1.8f);
    private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder().put(Pose.STANDING, STANDING_DIMENSIONS).put(Pose.SLEEPING, SLEEPING_DIMENSIONS).put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6f, 0.6f)).put(Pose.SWIMMING, EntityDimensions.scalable(0.6f, 0.6f)).put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6f, 0.6f)).put(Pose.CROUCHING, EntityDimensions.scalable(0.6f, 1.5f)).put(Pose.DYING, EntityDimensions.fixed(0.2f, 0.2f)).build();
    private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_SCORE_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    private long timeEntitySatOnShoulder;
    public final Inventory inventory;
    protected PlayerEnderChestContainer enderChestInventory;
    public final InventoryMenu inventoryMenu;
    public AbstractContainerMenu containerMenu;
    protected FoodData foodData;
    protected int jumpTriggerTime;
    public float oBob;
    public float bob;
    public int takeXpDelay;
    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;
    private int sleepCounter;
    protected boolean wasUnderwater;
    public final Abilities abilities;
    public int experienceLevel;
    public int totalExperience;
    public float experienceProgress;
    protected int enchantmentSeed;
    protected float defaultFlySpeed = 0.02f;
    private int lastLevelUpTime;
    private final GameProfile gameProfile;
    private boolean reducedDebugInfo;
    private ItemStack lastItemInMainHand;
    private final ItemCooldowns cooldowns;

    @Nullable
    public FishingHook fishing;

    @Override // net.minecraft.world.entity.Entity
    public abstract boolean isSpectator();

    public abstract boolean isCreative();

    public Player(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(EntityType.PLAYER, level);
        this.inventory = new Inventory(this);
        this.enderChestInventory = new PlayerEnderChestContainer();
        this.foodData = new FoodData();
        this.abilities = new Abilities();
        this.defaultFlySpeed = 0.02f;
        this.lastItemInMainHand = ItemStack.EMPTY;
        this.cooldowns = createItemCooldowns();
        setUUID(createPlayerUUID(gameProfile));
        this.gameProfile = gameProfile;
        this.inventoryMenu = new InventoryMenu(this.inventory, !level.isClientSide, this);
        this.containerMenu = this.inventoryMenu;
        moveTo(blockPos.getX() + 0.5d, blockPos.getY() + 1, blockPos.getZ() + 0.5d, f, 0.0f);
        this.rotOffs = 180.0f;
    }

    public boolean blockActionRestricted(Level level, BlockPos blockPos, GameType gameType) {
        if (!gameType.isBlockPlacingRestricted()) {
            return false;
        }
        if (gameType == GameType.SPECTATOR) {
            return true;
        }
        if (mayBuild()) {
            return false;
        }
        ItemStack mainHandItem = getMainHandItem();
        return mainHandItem.isEmpty() || !mainHandItem.hasAdventureModeBreakTagForBlock(level.getTagManager(), new BlockInWorld(level, blockPos, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 1.0d).add(Attributes.MOVEMENT_SPEED, 0.10000000149011612d).add(Attributes.ATTACK_SPEED).add(Attributes.LUCK);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PLAYER_ABSORPTION_ID, Float.valueOf(0.0f));
        this.entityData.define(DATA_SCORE_ID, 0);
        this.entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0);
        this.entityData.define(DATA_PLAYER_MAIN_HAND, (byte) 1);
        this.entityData.define(DATA_SHOULDER_LEFT, new CompoundTag());
        this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundTag());
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick()  {
        this.noPhysics = isSpectator();
        if (isSpectator()) {
            this.onGround = false;
        }
        if (this.takeXpDelay > 0) {
            this.takeXpDelay--;
        }
        if (isSleeping()) {
            this.sleepCounter++;
            if (this.sleepCounter > 100) {
                this.sleepCounter = 100;
            }
            if (!this.level.isClientSide && this.level.isDay()) {
                stopSleepInBed(false, true);
            }
        } else if (this.sleepCounter > 0) {
            this.sleepCounter++;
            if (this.sleepCounter >= 110) {
                this.sleepCounter = 0;
            }
        }
        updateIsUnderwater();
        super.tick();
        if (!this.level.isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
            closeContainer();
            this.containerMenu = this.inventoryMenu;
        }
        moveCloak();
        if (!this.level.isClientSide) {
            this.foodData.tick(this);
            awardStat(Stats.PLAY_ONE_MINUTE);
            if (isAlive()) {
                awardStat(Stats.TIME_SINCE_DEATH);
            }
            if (isDiscrete()) {
                awardStat(Stats.CROUCH_TIME);
            }
            if (!isSleeping()) {
                awardStat(Stats.TIME_SINCE_REST);
            }
        }
        double clamp = Mth.clamp(getX(), -2.9999999E7d, 2.9999999E7d);
        double clamp2 = Mth.clamp(getZ(), -2.9999999E7d, 2.9999999E7d);
        if (clamp != getX() || clamp2 != getZ()) {
            setPos(clamp, getY(), clamp2);
        }
        this.attackStrengthTicker++;
        ItemStack mainHandItem = getMainHandItem();
        if (!ItemStack.matches(this.lastItemInMainHand, mainHandItem)) {
            if (!ItemStack.isSameIgnoreDurability(this.lastItemInMainHand, mainHandItem)) {
                resetAttackStrengthTicker();
            }
            this.lastItemInMainHand = mainHandItem.copy();
        }
        turtleHelmetTick();
        this.cooldowns.tick();
        updatePlayerPose();
    }

    public boolean isSecondaryUseActive() {
        return isShiftKeyDown();
    }

    protected boolean wantsToStopRiding() {
        return isShiftKeyDown();
    }

    protected boolean isStayingOnGroundSurface() {
        return isShiftKeyDown();
    }

    protected boolean updateIsUnderwater() {
        this.wasUnderwater = isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    private void turtleHelmetTick() {
        if (getItemBySlot(EquipmentSlot.HEAD).getItem() == Items.TURTLE_HELMET && !isEyeInFluid(FluidTags.WATER)) {
            addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
        }
    }

    protected ItemCooldowns createItemCooldowns() {
        return new ItemCooldowns();
    }

    private void moveCloak() {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double x = getX() - this.xCloak;
        double y = getY() - this.yCloak;
        double z = getZ() - this.zCloak;
        if (x > 10.0d) {
            this.xCloak = getX();
            this.xCloakO = this.xCloak;
        }
        if (z > 10.0d) {
            this.zCloak = getZ();
            this.zCloakO = this.zCloak;
        }
        if (y > 10.0d) {
            this.yCloak = getY();
            this.yCloakO = this.yCloak;
        }
        if (x < -10.0d) {
            this.xCloak = getX();
            this.xCloakO = this.xCloak;
        }
        if (z < -10.0d) {
            this.zCloak = getZ();
            this.zCloakO = this.zCloak;
        }
        if (y < -10.0d) {
            this.yCloak = getY();
            this.yCloakO = this.yCloak;
        }
        this.xCloak += x * 0.25d;
        this.zCloak += z * 0.25d;
        this.yCloak += y * 0.25d;
    }

    protected void updatePlayerPose() {
        Pose pose;
        Pose pose2;
        if (!canEnterPose(Pose.SWIMMING)) {
            return;
        }
        if (isFallFlying()) {
            pose = Pose.FALL_FLYING;
        } else if (isSleeping()) {
            pose = Pose.SLEEPING;
        } else if (isSwimming()) {
            pose = Pose.SWIMMING;
        } else if (isAutoSpinAttack()) {
            pose = Pose.SPIN_ATTACK;
        } else if (isShiftKeyDown() && !this.abilities.flying) {
            pose = Pose.CROUCHING;
        } else {
            pose = Pose.STANDING;
        }
        if (isSpectator() || isPassenger() || canEnterPose(pose)) {
            pose2 = pose;
        } else if (canEnterPose(Pose.CROUCHING)) {
            pose2 = Pose.CROUCHING;
        } else {
            pose2 = Pose.SWIMMING;
        }
        setPose(pose2);
    }

    @Override // net.minecraft.world.entity.Entity
    public int getPortalWaitTime() {
        return this.abilities.invulnerable ? 1 : 80;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSound() {
        return SoundEvents.PLAYER_SWIM;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.PLAYER_SPLASH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimHighSpeedSplashSound() {
        return SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
    }

    @Override // net.minecraft.world.entity.Entity
    public int getDimensionChangingDelay() {
        return 10;
    }

    @Override // net.minecraft.world.entity.Entity
    public void playSound(SoundEvent soundEvent, float f, float f2) {
        this.level.playSound(this, getX(), getY(), getZ(), soundEvent, getSoundSource(), f, f2);
    }

    public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    @Override // net.minecraft.world.entity.Entity
    protected int getFireImmuneTicks() {
        return 20;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 9) {
            completeUsingItem();
            return;
        }
        if (b == 23) {
            this.reducedDebugInfo = false;
            return;
        }
        if (b == 22) {
            this.reducedDebugInfo = true;
        } else if (b == 43) {
            addParticlesAroundSelf(ParticleTypes.CLOUD);
        } else {
            super.handleEntityEvent(b);
        }
    }

    private void addParticlesAroundSelf(ParticleOptions particleOptions) {
        for (int i = 0; i < 5; i++) {
            this.level.addParticle(particleOptions, getRandomX(1.0d), getRandomY() + 1.0d, getRandomZ(1.0d), this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d);
        }
    }

    protected void closeContainer() {
        this.containerMenu = this.inventoryMenu;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void rideTick() throws CommandSyntaxException {
        if (wantsToStopRiding() && isPassenger()) {
            stopRiding();
            setShiftKeyDown(false);
            return;
        }
        double x = getX();
        double y = getY();
        double z = getZ();
        super.rideTick();
        this.oBob = this.bob;
        this.bob = 0.0f;
        checkRidingStatistics(getX() - x, getY() - y, getZ() - z);
    }

    @Override // net.minecraft.world.entity.Entity
    public void resetPos() {
        setPose(Pose.STANDING);
        super.resetPos();
        setHealth(getMaxHealth());
        this.deathTime = 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void serverAiStep() {
        super.serverAiStep();
        updateSwingTime();
        this.yHeadRot = this.yRot;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        float f;
        AABB inflate;
        if (this.jumpTriggerTime > 0) {
            this.jumpTriggerTime--;
        }
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (getHealth() < getMaxHealth() && this.tickCount % 20 == 0) {
                heal(1.0f);
            }
            if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
                this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
            }
        }
        this.inventory.tick();
        this.oBob = this.bob;
        super.aiStep();
        this.flyingSpeed = 0.02f;
        if (isSprinting()) {
            this.flyingSpeed = (float) (this.flyingSpeed + 0.005999999865889549d);
        }
        setSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));
        if (!this.onGround || isDeadOrDying() || isSwimming()) {
            f = 0.0f;
        } else {
            f = Math.min(0.1f, Mth.sqrt(getHorizontalDistanceSqr(getDeltaMovement())));
        }
        this.bob += (f - this.bob) * 0.4f;
        if (getHealth() > 0.0f && !isSpectator()) {
            if (isPassenger() && !getVehicle().removed) {
                inflate = getBoundingBox().minmax(getVehicle().getBoundingBox()).inflate(1.0d, 0.0d, 1.0d);
            } else {
                inflate = getBoundingBox().inflate(1.0d, 0.5d, 1.0d);
            }
            List<Entity> entities = this.level.getEntities(this, inflate);
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                if (!entity.removed) {
                    touch(entity);
                }
            }
        }
        playShoulderEntityAmbientSound(getShoulderEntityLeft());
        playShoulderEntityAmbientSound(getShoulderEntityRight());
        if ((!this.level.isClientSide && (this.fallDistance > 0.5f || isInWater())) || this.abilities.flying || isSleeping()) {
            removeEntitiesOnShoulder();
        }
    }

    private void playShoulderEntityAmbientSound(@Nullable CompoundTag compoundTag) {
        if (compoundTag != null) {
            if ((!compoundTag.contains("Silent") || !compoundTag.getBoolean("Silent")) && this.level.random.nextInt(200) == 0) {
                EntityType.byString(compoundTag.getString("id")).filter(entityType -> {
                    return entityType == EntityType.PARROT;
                }).ifPresent(entityType2 -> {
                    if (!Parrot.imitateNearbyMobs(this.level, this)) {
                        this.level.playSound(null, getX(), getY(), getZ(), Parrot.getAmbient(this.level, this.level.random), getSoundSource(), 1.0f, Parrot.getPitch(this.level.random));
                    }
                });
            }
        }
    }

    private void touch(Entity entity) {
        entity.playerTouch(this);
    }

    public int getScore() {
        return ((Integer) this.entityData.get(DATA_SCORE_ID)).intValue();
    }

    public void setScore(int i) {
        this.entityData.set(DATA_SCORE_ID, Integer.valueOf(i));
    }

    public void increaseScore(int i) {
        this.entityData.set(DATA_SCORE_ID, Integer.valueOf(getScore() + i));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        reapplyPosition();
        if (!isSpectator()) {
            dropAllDeathLoot(damageSource);
        }
        if (damageSource != null) {
            setDeltaMovement((-Mth.cos((this.hurtDir + this.yRot) * 0.017453292f)) * 0.1f, 0.10000000149011612d, (-Mth.sin((this.hurtDir + this.yRot) * 0.017453292f)) * 0.1f);
        } else {
            setDeltaMovement(0.0d, 0.1d, 0.0d);
        }
        awardStat(Stats.DEATHS);
        resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        clearFire();
        setSharedFlag(0, false);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropEquipment() {
        super.dropEquipment();
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            destroyVanishingCursedItems();
            this.inventory.dropAll();
        }
    }

    protected void destroyVanishingCursedItems() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack item = this.inventory.getItem(i);
            if (!item.isEmpty() && EnchantmentHelper.hasVanishingCurse(item)) {
                this.inventory.removeItemNoUpdate(i);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (damageSource == DamageSource.ON_FIRE) {
            return SoundEvents.PLAYER_HURT_ON_FIRE;
        }
        if (damageSource == DamageSource.DROWN) {
            return SoundEvents.PLAYER_HURT_DROWN;
        }
        if (damageSource == DamageSource.SWEET_BERRY_BUSH) {
            return SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH;
        }
        return SoundEvents.PLAYER_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    public boolean drop(boolean z) {
        return drop(this.inventory.removeItem(this.inventory.selected, (!z || this.inventory.getSelected().isEmpty()) ? 1 : this.inventory.getSelected().getCount()), false, true) != null;
    }

    @Nullable
    public ItemEntity drop(ItemStack itemStack, boolean z) {
        return drop(itemStack, false, z);
    }

    @Nullable
    public ItemEntity drop(ItemStack itemStack, boolean z, boolean z2) {
        if (itemStack.isEmpty()) {
            return null;
        }
        if (this.level.isClientSide) {
            swing(InteractionHand.MAIN_HAND);
        }
        ItemEntity itemEntity = new ItemEntity(this.level, getX(), getEyeY() - 0.30000001192092896d, getZ(), itemStack);
        itemEntity.setPickUpDelay(40);
        if (z2) {
            itemEntity.setThrower(getUUID());
        }
        if (z) {
            float nextFloat = this.random.nextFloat() * 0.5f;
            float nextFloat2 = this.random.nextFloat() * 6.2831855f;
            itemEntity.setDeltaMovement((-Mth.sin(nextFloat2)) * nextFloat, 0.20000000298023224d, Mth.cos(nextFloat2) * nextFloat);
        } else {
            float sin = Mth.sin(this.xRot * 0.017453292f);
            float cos = Mth.cos(this.xRot * 0.017453292f);
            float sin2 = Mth.sin(this.yRot * 0.017453292f);
            float cos2 = Mth.cos(this.yRot * 0.017453292f);
            float nextFloat3 = this.random.nextFloat() * 6.2831855f;
            float nextFloat4 = 0.02f * this.random.nextFloat();
            itemEntity.setDeltaMovement(((-sin2) * cos * 0.3f) + (Math.cos(nextFloat3) * nextFloat4), ((-sin) * 0.3f) + 0.1f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f), (cos2 * cos * 0.3f) + (Math.sin(nextFloat3) * nextFloat4));
        }
        return itemEntity;
    }

    public float getDestroySpeed(BlockState blockState) {
        float f;
        float destroySpeed = this.inventory.getDestroySpeed(blockState);
        if (destroySpeed > 1.0f) {
            int blockEfficiency = EnchantmentHelper.getBlockEfficiency(this);
            ItemStack mainHandItem = getMainHandItem();
            if (blockEfficiency > 0 && !mainHandItem.isEmpty()) {
                destroySpeed += (blockEfficiency * blockEfficiency) + 1;
            }
        }
        if (MobEffectUtil.hasDigSpeed(this)) {
            destroySpeed *= 1.0f + ((MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2f);
        }
        if (hasEffect(MobEffects.DIG_SLOWDOWN)) {
            switch (getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    f = 0.3f;
                    break;
                case 1:
                    f = 0.09f;
                    break;
                case 2:
                    f = 0.0027f;
                    break;
                case 3:
                default:
                    f = 8.1E-4f;
                    break;
            }
            destroySpeed *= f;
        }
        if (isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
            destroySpeed /= 5.0f;
        }
        if (!this.onGround) {
            destroySpeed /= 5.0f;
        }
        return destroySpeed;
    }

    public boolean hasCorrectToolForDrops(BlockState blockState) {
        return !blockState.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(blockState);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setUUID(createPlayerUUID(this.gameProfile));
        this.inventory.load(compoundTag.getList("Inventory", 10));
        this.inventory.selected = compoundTag.getInt("SelectedItemSlot");
        this.sleepCounter = compoundTag.getShort("SleepTimer");
        this.experienceProgress = compoundTag.getFloat("XpP");
        this.experienceLevel = compoundTag.getInt("XpLevel");
        this.totalExperience = compoundTag.getInt("XpTotal");
        this.enchantmentSeed = compoundTag.getInt("XpSeed");
        if (this.enchantmentSeed == 0) {
            this.enchantmentSeed = this.random.nextInt();
        }
        setScore(compoundTag.getInt("Score"));
        this.foodData.readAdditionalSaveData(compoundTag);
        this.abilities.loadSaveData(compoundTag);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.abilities.getWalkingSpeed());
        if (compoundTag.contains("EnderItems", 9)) {
            this.enderChestInventory.fromTag(compoundTag.getList("EnderItems", 10));
        }
        if (compoundTag.contains("ShoulderEntityLeft", 10)) {
            setShoulderEntityLeft(compoundTag.getCompound("ShoulderEntityLeft"));
        }
        if (compoundTag.contains("ShoulderEntityRight", 10)) {
            setShoulderEntityRight(compoundTag.getCompound("ShoulderEntityRight"));
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundTag.put("Inventory", this.inventory.save(new ListTag()));
        compoundTag.putInt("SelectedItemSlot", this.inventory.selected);
        compoundTag.putShort("SleepTimer", (short) this.sleepCounter);
        compoundTag.putFloat("XpP", this.experienceProgress);
        compoundTag.putInt("XpLevel", this.experienceLevel);
        compoundTag.putInt("XpTotal", this.totalExperience);
        compoundTag.putInt("XpSeed", this.enchantmentSeed);
        compoundTag.putInt("Score", getScore());
        this.foodData.addAdditionalSaveData(compoundTag);
        this.abilities.addSaveData(compoundTag);
        compoundTag.put("EnderItems", this.enderChestInventory.createTag());
        if (!getShoulderEntityLeft().isEmpty()) {
            compoundTag.put("ShoulderEntityLeft", getShoulderEntityLeft());
        }
        if (!getShoulderEntityRight().isEmpty()) {
            compoundTag.put("ShoulderEntityRight", getShoulderEntityRight());
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (super.isInvulnerableTo(damageSource)) {
            return true;
        }
        return damageSource == DamageSource.DROWN ? !this.level.getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE) : damageSource == DamageSource.FALL ? !this.level.getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE) : damageSource.isFire() && !this.level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (this.abilities.invulnerable && !damageSource.isBypassInvul()) {
            return false;
        }
        this.noActionTime = 0;
        if (isDeadOrDying()) {
            return false;
        }
        removeEntitiesOnShoulder();
        if (damageSource.scalesWithDifficulty()) {
            if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                f = 0.0f;
            }
            if (this.level.getDifficulty() == Difficulty.EASY) {
                f = Math.min((f / 2.0f) + 1.0f, f);
            }
            if (this.level.getDifficulty() == Difficulty.HARD) {
                f = (f * 3.0f) / 2.0f;
            }
        }
        if (f == 0.0f) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void blockUsingShield(LivingEntity livingEntity) {
        super.blockUsingShield(livingEntity);
        if (livingEntity.getMainHandItem().getItem() instanceof AxeItem) {
            disableShield(true);
        }
    }

    public boolean canHarmPlayer(Player player) {
        Team team = getTeam();
        Team team2 = player.getTeam();
        if (team == null || !team.isAlliedTo(team2)) {
            return true;
        }
        return team.isAllowFriendlyFire();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void hurtArmor(DamageSource damageSource, float f) {
        this.inventory.hurtArmor(damageSource, f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void hurtCurrentlyUsedShield(float f) {
        if (this.useItem.getItem() != Items.SHIELD) {
            return;
        }
        if (!this.level.isClientSide) {
            awardStat(Stats.ITEM_USED.get(this.useItem.getItem()));
        }
        if (f >= 3.0f) {
            int floor = 1 + Mth.floor(f);
            InteractionHand usedItemHand = getUsedItemHand();
            this.useItem.hurtAndBreak(floor, this, player -> {
                player.broadcastBreakEvent(usedItemHand);
            });
            if (this.useItem.isEmpty()) {
                if (usedItemHand == InteractionHand.MAIN_HAND) {
                    setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                this.useItem = ItemStack.EMPTY;
                playSound(SoundEvents.SHIELD_BREAK, 0.8f, 0.8f + (this.level.random.nextFloat() * 0.4f));
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void actuallyHurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return;
        }
        float damageAfterMagicAbsorb = getDamageAfterMagicAbsorb(damageSource, getDamageAfterArmorAbsorb(damageSource, f));
        float max = Math.max(damageAfterMagicAbsorb - getAbsorptionAmount(), 0.0f);
        setAbsorptionAmount(getAbsorptionAmount() - (damageAfterMagicAbsorb - max));
        float f2 = damageAfterMagicAbsorb - max;
        if (f2 > 0.0f && f2 < 3.4028235E37f) {
            awardStat(Stats.DAMAGE_ABSORBED, Math.round(f2 * 10.0f));
        }
        if (max == 0.0f) {
            return;
        }
        causeFoodExhaustion(damageSource.getFoodExhaustion());
        float health = getHealth();
        setHealth(getHealth() - max);
        getCombatTracker().recordDamage(damageSource, health, max);
        if (max < 3.4028235E37f) {
            awardStat(Stats.DAMAGE_TAKEN, Math.round(max * 10.0f));
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean onSoulSpeedBlock() {
        return !this.abilities.flying && super.onSoulSpeedBlock();
    }

    public void openTextEdit(SignBlockEntity signBlockEntity) {
    }

    public void openMinecartCommandBlock(BaseCommandBlock baseCommandBlock) {
    }

    public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
    }

    public void openStructureBlock(StructureBlockEntity structureBlockEntity) {
    }

    public void openJigsawBlock(JigsawBlockEntity jigsawBlockEntity) {
    }

    public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
    }

    public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
        return OptionalInt.empty();
    }

    public void sendMerchantOffers(int i, MerchantOffers merchantOffers, int i2, int i3, boolean z, boolean z2) {
    }

    public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
    }

    /* JADX WARN: Multi-variable type inference failed */
    public InteractionResult interactOn(Entity entity, InteractionHand interactionHand) {
        if (isSpectator()) {
            if (entity instanceof MenuProvider) {
                openMenu((MenuProvider) entity);
            }
            return InteractionResult.PASS;
        }
        ItemStack itemInHand = getItemInHand(interactionHand);
        ItemStack copy = itemInHand.copy();
        InteractionResult interact = entity.interact(this, interactionHand);
        if (interact.consumesAction()) {
            if (this.abilities.instabuild && itemInHand == getItemInHand(interactionHand) && itemInHand.getCount() < copy.getCount()) {
                itemInHand.setCount(copy.getCount());
            }
            return interact;
        }
        if (!itemInHand.isEmpty() && (entity instanceof LivingEntity)) {
            if (this.abilities.instabuild) {
                itemInHand = copy;
            }
            InteractionResult interactLivingEntity = itemInHand.interactLivingEntity(this, (LivingEntity) entity, interactionHand);
            if (interactLivingEntity.consumesAction()) {
                if (itemInHand.isEmpty() && !this.abilities.instabuild) {
                    setItemInHand(interactionHand, ItemStack.EMPTY);
                }
                return interactLivingEntity;
            }
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return -0.35d;
    }

    @Override // net.minecraft.world.entity.Entity
    public void removeVehicle() {
        super.removeVehicle();
        this.boardingCooldown = 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean isImmobile() {
        return super.isImmobile() || isSleeping();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isAffectedByFluids() {
        return !this.abilities.flying;
    }

    @Override // net.minecraft.world.entity.Entity
    protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType moverType) {
        if (!this.abilities.flying && ((moverType == MoverType.SELF || moverType == MoverType.PLAYER) && isStayingOnGroundSurface() && isAboveGround())) {
            double d = vec3.x;
            double d2 = vec3.z;
            while (d != 0.0d && this.level.noCollision(this, getBoundingBox().move(d, -this.maxUpStep, 0.0d))) {
                if (d < 0.05d && d >= -0.05d) {
                    d = 0.0d;
                } else if (d > 0.0d) {
                    d -= 0.05d;
                } else {
                    d += 0.05d;
                }
            }
            while (d2 != 0.0d && this.level.noCollision(this, getBoundingBox().move(0.0d, -this.maxUpStep, d2))) {
                if (d2 < 0.05d && d2 >= -0.05d) {
                    d2 = 0.0d;
                } else if (d2 > 0.0d) {
                    d2 -= 0.05d;
                } else {
                    d2 += 0.05d;
                }
            }
            while (d != 0.0d && d2 != 0.0d && this.level.noCollision(this, getBoundingBox().move(d, -this.maxUpStep, d2))) {
                if (d < 0.05d && d >= -0.05d) {
                    d = 0.0d;
                } else if (d > 0.0d) {
                    d -= 0.05d;
                } else {
                    d += 0.05d;
                }
                if (d2 < 0.05d && d2 >= -0.05d) {
                    d2 = 0.0d;
                } else if (d2 > 0.0d) {
                    d2 -= 0.05d;
                } else {
                    d2 += 0.05d;
                }
            }
            vec3 = new Vec3(d, vec3.y, d2);
        }
        return vec3;
    }

    private boolean isAboveGround() {
        return this.onGround || (this.fallDistance < this.maxUpStep && !this.level.noCollision(this, getBoundingBox().move(0.0d, (double) (this.fallDistance - this.maxUpStep), 0.0d)));
    }

    public void attack(Entity entity) {
        float damageBonus;
        if (!entity.isAttackable() || entity.skipAttackInteraction(this)) {
            return;
        }
        float attributeValue = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (entity instanceof LivingEntity) {
            damageBonus = EnchantmentHelper.getDamageBonus(getMainHandItem(), ((LivingEntity) entity).getMobType());
        } else {
            damageBonus = EnchantmentHelper.getDamageBonus(getMainHandItem(), MobType.UNDEFINED);
        }
        float attackStrengthScale = getAttackStrengthScale(0.5f);
        float f = attributeValue * (0.2f + (attackStrengthScale * attackStrengthScale * 0.8f));
        float f2 = damageBonus * attackStrengthScale;
        resetAttackStrengthTicker();
        if (f > 0.0f || f2 > 0.0f) {
            boolean z = attackStrengthScale > 0.9f;
            boolean z2 = false;
            int knockbackBonus = 0 + EnchantmentHelper.getKnockbackBonus(this);
            if (isSprinting() && z) {
                this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, getSoundSource(), 1.0f, 1.0f);
                knockbackBonus++;
                z2 = true;
            }
            boolean z3 = (z && (this.fallDistance > 0.0f ? 1 : (this.fallDistance == 0.0f ? 0 : -1)) > 0 && !this.onGround && !onClimbable() && !isInWater() && !hasEffect(MobEffects.BLINDNESS) && !isPassenger() && (entity instanceof LivingEntity)) && !isSprinting();
            if (z3) {
                f *= 1.5f;
            }
            float f3 = f + f2;
            boolean z4 = false;
            double d = this.walkDist - this.walkDistO;
            if (z && !z3 && !z2 && this.onGround && d < getSpeed() && (getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwordItem)) {
                z4 = true;
            }
            float f4 = 0.0f;
            boolean z5 = false;
            int fireAspect = EnchantmentHelper.getFireAspect(this);
            if (entity instanceof LivingEntity) {
                f4 = ((LivingEntity) entity).getHealth();
                if (fireAspect > 0 && !entity.isOnFire()) {
                    z5 = true;
                    entity.setSecondsOnFire(1);
                }
            }
            Vec3 deltaMovement = entity.getDeltaMovement();
            if (entity.hurt(DamageSource.playerAttack(this), f3)) {
                if (knockbackBonus > 0) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).knockback(knockbackBonus * 0.5f, Mth.sin(this.yRot * 0.017453292f), -Mth.cos(this.yRot * 0.017453292f));
                    } else {
                        entity.push((-Mth.sin(this.yRot * 0.017453292f)) * knockbackBonus * 0.5f, 0.1d, Mth.cos(this.yRot * 0.017453292f) * knockbackBonus * 0.5f);
                    }
                    setDeltaMovement(getDeltaMovement().multiply(0.6d, 1.0d, 0.6d));
                    setSprinting(false);
                }
                if (z4) {
                    float sweepingDamageRatio = 1.0f + (EnchantmentHelper.getSweepingDamageRatio(this) * f3);
                    for (LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0d, 0.25d, 1.0d))) {
                        if (livingEntity != this && livingEntity != entity && !isAlliedTo(livingEntity) && (!(livingEntity instanceof ArmorStand) || !((ArmorStand) livingEntity).isMarker())) {
                            if (distanceToSqr(livingEntity) < 9.0d) {
                                livingEntity.knockback(0.4f, Mth.sin(this.yRot * 0.017453292f), -Mth.cos(this.yRot * 0.017453292f));
                                livingEntity.hurt(DamageSource.playerAttack(this), sweepingDamageRatio);
                            }
                        }
                    }
                    this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, getSoundSource(), 1.0f, 1.0f);
                    sweepAttack();
                }
                if ((entity instanceof ServerPlayer) && entity.hurtMarked) {
                    ((ServerPlayer) entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
                    entity.hurtMarked = false;
                    entity.setDeltaMovement(deltaMovement);
                }
                if (z3) {
                    this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_CRIT, getSoundSource(), 1.0f, 1.0f);
                    crit(entity);
                }
                if (!z3 && !z4) {
                    if (z) {
                        this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_STRONG, getSoundSource(), 1.0f, 1.0f);
                    } else {
                        this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_WEAK, getSoundSource(), 1.0f, 1.0f);
                    }
                }
                if (f2 > 0.0f) {
                    magicCrit(entity);
                }
                setLastHurtMob(entity);
                if (entity instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects((LivingEntity) entity, this);
                }
                EnchantmentHelper.doPostDamageEffects(this, entity);
                ItemStack mainHandItem = getMainHandItem();
                Entity entity2 = entity;
                if (entity instanceof EnderDragonPart) {
                    entity2 = ((EnderDragonPart) entity).parentMob;
                }
                if (!this.level.isClientSide && !mainHandItem.isEmpty() && (entity2 instanceof LivingEntity)) {
                    mainHandItem.hurtEnemy((LivingEntity) entity2, this);
                    if (mainHandItem.isEmpty()) {
                        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
                if (entity instanceof LivingEntity) {
                    float health = f4 - ((LivingEntity) entity).getHealth();
                    awardStat(Stats.DAMAGE_DEALT, Math.round(health * 10.0f));
                    if (fireAspect > 0) {
                        entity.setSecondsOnFire(fireAspect * 4);
                    }
                    if ((this.level instanceof ServerLevel) && health > 2.0f) {
                        ((ServerLevel) this.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5d), entity.getZ(), (int) (health * 0.5d), 0.1d, 0.0d, 0.1d, 0.2d);
                    }
                }
                causeFoodExhaustion(0.1f);
                return;
            }
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, getSoundSource(), 1.0f, 1.0f);
            if (z5) {
                entity.clearFire();
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
        attack(livingEntity);
    }

    public void disableShield(boolean z) {
        float blockEfficiency = 0.25f + (EnchantmentHelper.getBlockEfficiency(this) * 0.05f);
        if (z) {
            blockEfficiency += 0.75f;
        }
        if (this.random.nextFloat() < blockEfficiency) {
            getCooldowns().addCooldown(Items.SHIELD, 100);
            stopUsingItem();
            this.level.broadcastEntityEvent(this, (byte) 30);
        }
    }

    public void crit(Entity entity) {
    }

    public void magicCrit(Entity entity) {
    }

    public void sweepAttack() {
        double d = -Mth.sin(this.yRot * 0.017453292f);
        double cos = Mth.cos(this.yRot * 0.017453292f);
        if (this.level instanceof ServerLevel) {
            ((ServerLevel) this.level).sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + d, getY(0.5d), getZ() + cos, 0, d, 0.0d, cos, 0.0d);
        }
    }

    public void respawn() {
    }

    @Override // net.minecraft.world.entity.Entity
    public void remove() {
        super.remove();
        this.inventoryMenu.removed(this);
        if (this.containerMenu != null) {
            this.containerMenu.removed(this);
        }
    }

    public boolean isLocalPlayer() {
        return false;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/Player$BedSleepingProblem.class */
    public enum BedSleepingProblem {
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW(new TranslatableComponent("block.minecraft.bed.no_sleep")),
        TOO_FAR_AWAY(new TranslatableComponent("block.minecraft.bed.too_far_away")),
        OBSTRUCTED(new TranslatableComponent("block.minecraft.bed.obstructed")),
        OTHER_PROBLEM,
        NOT_SAFE(new TranslatableComponent("block.minecraft.bed.not_safe"));


        @Nullable
        private final Component message;

        BedSleepingProblem() {
            this.message = null;
        }

        BedSleepingProblem(Component component) {
            this.message = component;
        }

        @Nullable
        public Component getMessage() {
            return this.message;
        }
    }

    public Either<BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
        startSleeping(blockPos);
        this.sleepCounter = 0;
        return Either.right(Unit.INSTANCE);
    }

    public void stopSleepInBed(boolean z, boolean z2) {
        super.stopSleeping();
        if ((this.level instanceof ServerLevel) && z2) {
            ((ServerLevel) this.level).updateSleepingPlayerList();
        }
        this.sleepCounter = z ? 0 : 100;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void stopSleeping() {
        stopSleepInBed(true, true);
    }

    public static Optional<Vec3> findRespawnPositionAndUseSpawnBlock(ServerLevel serverLevel, BlockPos blockPos, float f, boolean z, boolean z2) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if ((block instanceof RespawnAnchorBlock) && ((Integer) blockState.getValue(RespawnAnchorBlock.CHARGE)).intValue() > 0 && RespawnAnchorBlock.canSetSpawn(serverLevel)) {
            Optional<Vec3> findStandUpPosition = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos);
            if (!z2 && findStandUpPosition.isPresent()) {
                serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(RespawnAnchorBlock.CHARGE, Integer.valueOf(((Integer) blockState.getValue(RespawnAnchorBlock.CHARGE)).intValue() - 1)), 3);
            }
            return findStandUpPosition;
        }
        if ((block instanceof BedBlock) && BedBlock.canSetSpawn(serverLevel)) {
            return BedBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos, f);
        }
        if (!z) {
            return Optional.empty();
        }
        boolean isPossibleToRespawnInThis = block.isPossibleToRespawnInThis();
        boolean isPossibleToRespawnInThis2 = serverLevel.getBlockState(blockPos.above()).getBlock().isPossibleToRespawnInThis();
        if (isPossibleToRespawnInThis && isPossibleToRespawnInThis2) {
            return Optional.of(new Vec3(blockPos.getX() + 0.5d, blockPos.getY() + 0.1d, blockPos.getZ() + 0.5d));
        }
        return Optional.empty();
    }

    public boolean isSleepingLongEnough() {
        return isSleeping() && this.sleepCounter >= 100;
    }

    public int getSleepTimer() {
        return this.sleepCounter;
    }

    public void displayClientMessage(Component component, boolean z) {
    }

    public void awardStat(ResourceLocation resourceLocation) {
        awardStat(Stats.CUSTOM.get(resourceLocation));
    }

    public void awardStat(ResourceLocation resourceLocation, int i) {
        awardStat(Stats.CUSTOM.get(resourceLocation), i);
    }

    public void awardStat(Stat<?> stat) {
        awardStat(stat, 1);
    }

    public void awardStat(Stat<?> stat, int i) {
    }

    public void resetStat(Stat<?> stat) {
    }

    public int awardRecipes(Collection<Recipe<?>> collection) {
        return 0;
    }

    public void awardRecipesByKey(ResourceLocation[] resourceLocationArr) {
    }

    public int resetRecipes(Collection<Recipe<?>> collection) {
        return 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void jumpFromGround() {
        super.jumpFromGround();
        awardStat(Stats.JUMP);
        if (isSprinting()) {
            causeFoodExhaustion(0.2f);
        } else {
            causeFoodExhaustion(0.05f);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        double x = getX();
        double y = getY();
        double z = getZ();
        if (isSwimming() && !isPassenger()) {
            double d = getLookAngle().y;
            double d2 = d < -0.2d ? 0.085d : 0.06d;
            if (d <= 0.0d || this.jumping || !this.level.getBlockState(new BlockPos(getX(), (getY() + 1.0d) - 0.1d, getZ())).getFluidState().isEmpty()) {
                Vec3 deltaMovement = getDeltaMovement();
                setDeltaMovement(deltaMovement.add(0.0d, (d - deltaMovement.y) * d2, 0.0d));
            }
        }
        if (this.abilities.flying && !isPassenger()) {
            double d3 = getDeltaMovement().y;
            float f = this.flyingSpeed;
            this.flyingSpeed = this.abilities.getFlyingSpeed() * (isSprinting() ? 2 : 1);
            super.travel(vec3);
            Vec3 deltaMovement2 = getDeltaMovement();
            setDeltaMovement(deltaMovement2.x, d3 * 0.6d, deltaMovement2.z);
            this.flyingSpeed = f;
            this.fallDistance = 0.0f;
            setSharedFlag(7, false);
        } else {
            super.travel(vec3);
        }
        checkMovementStatistics(getX() - x, getY() - y, getZ() - z);
    }

    @Override // net.minecraft.world.entity.Entity
    public void updateSwimming() {
        if (this.abilities.flying) {
            setSwimming(false);
        } else {
            super.updateSwimming();
        }
    }

    protected boolean freeAt(BlockPos blockPos) {
        return !this.level.getBlockState(blockPos).isSuffocating(this.level, blockPos);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public float getSpeed() {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    public void checkMovementStatistics(double d, double d2, double d3) {
        if (isPassenger()) {
            return;
        }
        if (isSwimming()) {
            int round = Math.round(Mth.sqrt((d * d) + (d2 * d2) + (d3 * d3)) * 100.0f);
            if (round > 0) {
                awardStat(Stats.SWIM_ONE_CM, round);
                causeFoodExhaustion(0.01f * round * 0.01f);
                return;
            }
            return;
        }
        if (isEyeInFluid(FluidTags.WATER)) {
            int round2 = Math.round(Mth.sqrt((d * d) + (d2 * d2) + (d3 * d3)) * 100.0f);
            if (round2 > 0) {
                awardStat(Stats.WALK_UNDER_WATER_ONE_CM, round2);
                causeFoodExhaustion(0.01f * round2 * 0.01f);
                return;
            }
            return;
        }
        if (isInWater()) {
            int round3 = Math.round(Mth.sqrt((d * d) + (d3 * d3)) * 100.0f);
            if (round3 > 0) {
                awardStat(Stats.WALK_ON_WATER_ONE_CM, round3);
                causeFoodExhaustion(0.01f * round3 * 0.01f);
                return;
            }
            return;
        }
        if (onClimbable()) {
            if (d2 > 0.0d) {
                awardStat(Stats.CLIMB_ONE_CM, (int) Math.round(d2 * 100.0d));
                return;
            }
            return;
        }
        if (!this.onGround) {
            if (isFallFlying()) {
                awardStat(Stats.AVIATE_ONE_CM, Math.round(Mth.sqrt((d * d) + (d2 * d2) + (d3 * d3)) * 100.0f));
                return;
            } else {
                int round4 = Math.round(Mth.sqrt((d * d) + (d3 * d3)) * 100.0f);
                if (round4 > 25) {
                    awardStat(Stats.FLY_ONE_CM, round4);
                    return;
                }
                return;
            }
        }
        int round5 = Math.round(Mth.sqrt((d * d) + (d3 * d3)) * 100.0f);
        if (round5 > 0) {
            if (isSprinting()) {
                awardStat(Stats.SPRINT_ONE_CM, round5);
                causeFoodExhaustion(0.1f * round5 * 0.01f);
            } else if (isCrouching()) {
                awardStat(Stats.CROUCH_ONE_CM, round5);
                causeFoodExhaustion(0.0f * round5 * 0.01f);
            } else {
                awardStat(Stats.WALK_ONE_CM, round5);
                causeFoodExhaustion(0.0f * round5 * 0.01f);
            }
        }
    }

    private void checkRidingStatistics(double d, double d2, double d3) {
        int round;
        if (isPassenger() && (round = Math.round(Mth.sqrt((d * d) + (d2 * d2) + (d3 * d3)) * 100.0f)) > 0) {
            Entity vehicle = getVehicle();
            if (vehicle instanceof AbstractMinecart) {
                awardStat(Stats.MINECART_ONE_CM, round);
                return;
            }
            if (vehicle instanceof Boat) {
                awardStat(Stats.BOAT_ONE_CM, round);
                return;
            }
            if (vehicle instanceof Pig) {
                awardStat(Stats.PIG_ONE_CM, round);
            } else if (vehicle instanceof AbstractHorse) {
                awardStat(Stats.HORSE_ONE_CM, round);
            } else if (vehicle instanceof Strider) {
                awardStat(Stats.STRIDER_ONE_CM, round);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        if (this.abilities.mayfly) {
            return false;
        }
        if (f >= 2.0f) {
            awardStat(Stats.FALL_ONE_CM, (int) Math.round(f * 100.0d));
        }
        return super.causeFallDamage(f, f2);
    }

    public boolean tryToStartFallFlying() {
        if (!this.onGround && !isFallFlying() && !isInWater() && !hasEffect(MobEffects.LEVITATION)) {
            ItemStack itemBySlot = getItemBySlot(EquipmentSlot.CHEST);
            if (itemBySlot.getItem() == Items.ELYTRA && ElytraItem.isFlyEnabled(itemBySlot)) {
                startFallFlying();
                return true;
            }
            return false;
        }
        return false;
    }

    public void startFallFlying() {
        setSharedFlag(7, true);
    }

    public void stopFallFlying() {
        setSharedFlag(7, true);
        setSharedFlag(7, false);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void doWaterSplashEffect() {
        if (!isSpectator()) {
            super.doWaterSplashEffect();
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getFallDamageSound(int i) {
        if (i > 4) {
            return SoundEvents.PLAYER_BIG_FALL;
        }
        return SoundEvents.PLAYER_SMALL_FALL;
    }

    @Override // net.minecraft.world.entity.Entity
    public void killed(ServerLevel serverLevel, LivingEntity livingEntity) {
        awardStat(Stats.ENTITY_KILLED.get(livingEntity.getType()));
    }

    @Override // net.minecraft.world.entity.Entity
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
        if (!this.abilities.flying) {
            super.makeStuckInBlock(blockState, vec3);
        }
    }

    public void giveExperiencePoints(int i) {
        increaseScore(i);
        this.experienceProgress += i / getXpNeededForNextLevel();
        this.totalExperience = Mth.clamp(this.totalExperience + i, 0, Integer.MAX_VALUE);
        while (this.experienceProgress < 0.0f) {
            float xpNeededForNextLevel = this.experienceProgress * getXpNeededForNextLevel();
            if (this.experienceLevel > 0) {
                giveExperienceLevels(-1);
                this.experienceProgress = 1.0f + (xpNeededForNextLevel / getXpNeededForNextLevel());
            } else {
                giveExperienceLevels(-1);
                this.experienceProgress = 0.0f;
            }
        }
        while (this.experienceProgress >= 1.0f) {
            this.experienceProgress = (this.experienceProgress - 1.0f) * getXpNeededForNextLevel();
            giveExperienceLevels(1);
            this.experienceProgress /= getXpNeededForNextLevel();
        }
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed;
    }

    public void onEnchantmentPerformed(ItemStack itemStack, int i) {
        this.experienceLevel -= i;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        this.enchantmentSeed = this.random.nextInt();
    }

    public void giveExperienceLevels(int i) {
        this.experienceLevel += i;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        if (i > 0 && this.experienceLevel % 5 == 0 && this.lastLevelUpTime < this.tickCount - 100.0f) {
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_LEVELUP, getSoundSource(), (this.experienceLevel > 30 ? 1.0f : this.experienceLevel / 30.0f) * 0.75f, 1.0f);
            this.lastLevelUpTime = this.tickCount;
        }
    }

    public int getXpNeededForNextLevel() {
        if (this.experienceLevel >= 30) {
            return 112 + ((this.experienceLevel - 30) * 9);
        }
        if (this.experienceLevel >= 15) {
            return 37 + ((this.experienceLevel - 15) * 5);
        }
        return 7 + (this.experienceLevel * 2);
    }

    public void causeFoodExhaustion(float f) {
        if (!this.abilities.invulnerable && !this.level.isClientSide) {
            this.foodData.addExhaustion(f);
        }
    }

    public FoodData getFoodData() {
        return this.foodData;
    }

    public boolean canEat(boolean z) {
        return this.abilities.invulnerable || z || this.foodData.needsFood();
    }

    public boolean isHurt() {
        return getHealth() > 0.0f && getHealth() < getMaxHealth();
    }

    public boolean mayBuild() {
        return this.abilities.mayBuild;
    }

    public boolean mayUseItemAt(BlockPos blockPos, Direction direction, ItemStack itemStack) {
        if (this.abilities.mayBuild) {
            return true;
        } else {
            BlockPos var2 = blockPos.relative(direction.getOpposite());
            BlockInWorld var3x = new BlockInWorld(this.level, var2, false);
            return itemStack.hasAdventureModePlaceTagForBlock(this.level.getTagManager(), var3x);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected int getExperienceReward(Player player) {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || isSpectator()) {
            return 0;
        }
        int i = this.experienceLevel * 7;
        if (i > 100) {
            return 100;
        }
        return i;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean isAlwaysExperienceDropper() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean shouldShowName() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return (this.abilities.flying || (this.onGround && isDiscrete())) ? false : true;
    }

    public void onUpdateAbilities() {
    }

    public void setGameMode(GameType gameType) {
    }

    @Override // net.minecraft.world.entity.Entity, net.minecraft.world.Nameable
    public Component getName() {
        return new TextComponent(this.gameProfile.getName());
    }

    public PlayerEnderChestContainer getEnderChestInventory() {
        return this.enderChestInventory;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return this.inventory.getSelected();
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            return this.inventory.offhand.get(0);
        }
        if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
            return this.inventory.armor.get(equipmentSlot.getIndex());
        }
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            playEquipSound(itemStack);
            this.inventory.items.set(this.inventory.selected, itemStack);
        } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
            playEquipSound(itemStack);
            this.inventory.offhand.set(0, itemStack);
        } else if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
            playEquipSound(itemStack);
            this.inventory.armor.set(equipmentSlot.getIndex(), itemStack);
        }
    }

    public boolean addItem(ItemStack itemStack) {
        playEquipSound(itemStack);
        return this.inventory.add(itemStack);
    }

    @Override // net.minecraft.world.entity.Entity
    public Iterable<ItemStack> getHandSlots() {
        return Lists.newArrayList(new ItemStack[]{getMainHandItem(), getOffhandItem()});
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public Iterable<ItemStack> getArmorSlots() {
        return this.inventory.armor;
    }

    public boolean setEntityOnShoulder(CompoundTag compoundTag) {
        if (isPassenger() || !this.onGround || isInWater()) {
            return false;
        }
        if (getShoulderEntityLeft().isEmpty()) {
            setShoulderEntityLeft(compoundTag);
            this.timeEntitySatOnShoulder = this.level.getGameTime();
            return true;
        }
        if (getShoulderEntityRight().isEmpty()) {
            setShoulderEntityRight(compoundTag);
            this.timeEntitySatOnShoulder = this.level.getGameTime();
            return true;
        }
        return false;
    }

    protected void removeEntitiesOnShoulder() {
        if (this.timeEntitySatOnShoulder + 20 < this.level.getGameTime()) {
            respawnEntityOnShoulder(getShoulderEntityLeft());
            setShoulderEntityLeft(new CompoundTag());
            respawnEntityOnShoulder(getShoulderEntityRight());
            setShoulderEntityRight(new CompoundTag());
        }
    }

    private void respawnEntityOnShoulder(CompoundTag compoundTag) {
        if (!this.level.isClientSide && !compoundTag.isEmpty()) {
            EntityType.create(compoundTag, this.level).ifPresent(entity -> {
                if (entity instanceof TamableAnimal) {
                    ((TamableAnimal) entity).setOwnerUUID(this.uuid);
                }
                entity.setPos(getX(), getY() + 0.699999988079071d, getZ());
                ((ServerLevel) this.level).addWithUUID(entity);
            });
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isSwimming() {
        return (this.abilities.flying || isSpectator() || !super.isSwimming()) ? false : true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPushedByFluid() {
        return !this.abilities.flying;
    }

    public Scoreboard getScoreboard() {
        return this.level.getScoreboard();
    }

    @Override // net.minecraft.world.entity.Entity, net.minecraft.world.Nameable
    public Component getDisplayName() {
        return decorateDisplayNameComponent(PlayerTeam.formatNameForTeam(getTeam(), getName()));
    }

    private MutableComponent decorateDisplayNameComponent(MutableComponent mutableComponent) {
        String name = getGameProfile().getName();
        return mutableComponent.withStyle(style -> {
            return style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + name + " ")).withHoverEvent(createHoverEvent()).withInsertion(name);
        });
    }

    @Override // net.minecraft.world.entity.Entity
    public String getScoreboardName() {
        return getGameProfile().getName();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        switch (pose) {
            case SWIMMING:
            case FALL_FLYING:
            case SPIN_ATTACK:
                return 0.4f;
            case CROUCHING:
                return 1.27f;
            default:
                return 1.62f;
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void setAbsorptionAmount(float f) {
        if (f < 0.0f) {
            f = 0.0f;
        }
        getEntityData().set(DATA_PLAYER_ABSORPTION_ID, Float.valueOf(f));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public float getAbsorptionAmount() {
        return ((Float) getEntityData().get(DATA_PLAYER_ABSORPTION_ID)).floatValue();
    }

    public static UUID createPlayerUUID(GameProfile gameProfile) {
        UUID id = gameProfile.getId();
        if (id == null) {
            id = createPlayerUUID(gameProfile.getName());
        }
        return id;
    }

    public static UUID createPlayerUUID(String str) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + str).getBytes(StandardCharsets.UTF_8));
    }

    public boolean isModelPartShown(PlayerModelPart playerModelPart) {
        return (((Byte) getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION)).byteValue() & playerModelPart.getMask()) == playerModelPart.getMask();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        EquipmentSlot equipmentSlot;
        if (i >= 0 && i < this.inventory.items.size()) {
            this.inventory.setItem(i, itemStack);
            return true;
        }
        if (i == 100 + EquipmentSlot.HEAD.getIndex()) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (i == 100 + EquipmentSlot.CHEST.getIndex()) {
            equipmentSlot = EquipmentSlot.CHEST;
        } else if (i == 100 + EquipmentSlot.LEGS.getIndex()) {
            equipmentSlot = EquipmentSlot.LEGS;
        } else if (i == 100 + EquipmentSlot.FEET.getIndex()) {
            equipmentSlot = EquipmentSlot.FEET;
        } else {
            equipmentSlot = null;
        }
        if (i == 98) {
            setItemSlot(EquipmentSlot.MAINHAND, itemStack);
            return true;
        }
        if (i == 99) {
            setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            return true;
        }
        if (equipmentSlot != null) {
            if (!itemStack.isEmpty()) {
                if ((itemStack.getItem() instanceof ArmorItem) || (itemStack.getItem() instanceof ElytraItem)) {
                    if (Mob.getEquipmentSlotForItem(itemStack) != equipmentSlot) {
                        return false;
                    }
                } else if (equipmentSlot != EquipmentSlot.HEAD) {
                    return false;
                }
            }
            this.inventory.setItem(equipmentSlot.getIndex() + this.inventory.items.size(), itemStack);
            return true;
        }
        int i2 = i - 200;
        if (i2 >= 0 && i2 < this.enderChestInventory.getContainerSize()) {
            this.enderChestInventory.setItem(i2, itemStack);
            return true;
        }
        return false;
    }

    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void setReducedDebugInfo(boolean z) {
        this.reducedDebugInfo = z;
    }

    @Override // net.minecraft.world.entity.Entity
    public void setRemainingFireTicks(int i) {
        super.setRemainingFireTicks(this.abilities.invulnerable ? Math.min(i, 1) : i);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public HumanoidArm getMainArm() {
        return ((Byte) this.entityData.get(DATA_PLAYER_MAIN_HAND)).byteValue() == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public void setMainArm(HumanoidArm humanoidArm) {
        this.entityData.set(DATA_PLAYER_MAIN_HAND, Byte.valueOf((byte) (humanoidArm == HumanoidArm.LEFT ? 0 : 1)));
    }

    public CompoundTag getShoulderEntityLeft() {
        return (CompoundTag) this.entityData.get(DATA_SHOULDER_LEFT);
    }

    protected void setShoulderEntityLeft(CompoundTag compoundTag) {
        this.entityData.set(DATA_SHOULDER_LEFT, compoundTag);
    }

    public CompoundTag getShoulderEntityRight() {
        return (CompoundTag) this.entityData.get(DATA_SHOULDER_RIGHT);
    }

    protected void setShoulderEntityRight(CompoundTag compoundTag) {
        this.entityData.set(DATA_SHOULDER_RIGHT, compoundTag);
    }

    public float getCurrentItemAttackStrengthDelay() {
        return (float) ((1.0d / getAttributeValue(Attributes.ATTACK_SPEED)) * 20.0d);
    }

    public float getAttackStrengthScale(float f) {
        return Mth.clamp((this.attackStrengthTicker + f) / getCurrentItemAttackStrengthDelay(), 0.0f, 1.0f);
    }

    public void resetAttackStrengthTicker() {
        this.attackStrengthTicker = 0;
    }

    public ItemCooldowns getCooldowns() {
        return this.cooldowns;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected float getBlockSpeedFactor() {
        if (this.abilities.flying || isFallFlying()) {
            return 1.0f;
        }
        return super.getBlockSpeedFactor();
    }

    public float getLuck() {
        return (float) getAttributeValue(Attributes.LUCK);
    }

    public boolean canUseGameMasterBlocks() {
        return this.abilities.instabuild && getPermissionLevel() >= 2;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canTakeItem(ItemStack itemStack) {
        return getItemBySlot(Mob.getEquipmentSlotForItem(itemStack)).isEmpty();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        return POSES.getOrDefault(pose, STANDING_DIMENSIONS);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public ItemStack getProjectile(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ProjectileWeaponItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(this, ((ProjectileWeaponItem) itemStack.getItem()).getSupportedHeldProjectiles());
        if (!heldProjectile.isEmpty()) {
            return heldProjectile;
        }
        Predicate<ItemStack> allSupportedProjectiles = ((ProjectileWeaponItem) itemStack.getItem()).getAllSupportedProjectiles();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack item = this.inventory.getItem(i);
            if (allSupportedProjectiles.test(item)) {
                return item;
            }
        }
        return this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public ItemStack eat(Level level, ItemStack itemStack) {
        getFoodData().eat(itemStack.getItem(), itemStack);
        awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, (level.random.nextFloat() * 0.1f) + 0.9f);
        if (this instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) this, itemStack);
        }
        return super.eat(level, itemStack);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean shouldRemoveSoulSpeed(BlockState blockState) {
        return this.abilities.flying || super.shouldRemoveSoulSpeed(blockState);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getRopeHoldPosition(float f) {
        float f2;
        double d = 0.22d * (getMainArm() == HumanoidArm.RIGHT ? -1.0d : 1.0d);
        float lerp = Mth.lerp(f * 0.5f, this.xRot, this.xRotO) * 0.017453292f;
        float lerp2 = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * 0.017453292f;
        if (isFallFlying() || isAutoSpinAttack()) {
            Vec3 viewVector = getViewVector(f);
            Vec3 deltaMovement = getDeltaMovement();
            double horizontalDistanceSqr = Entity.getHorizontalDistanceSqr(deltaMovement);
            double horizontalDistanceSqr2 = Entity.getHorizontalDistanceSqr(viewVector);
            if (horizontalDistanceSqr > 0.0d && horizontalDistanceSqr2 > 0.0d) {
                f2 = (float) (Math.signum((deltaMovement.x * viewVector.z) - (deltaMovement.z * viewVector.x)) * Math.acos(((deltaMovement.x * viewVector.x) + (deltaMovement.z * viewVector.z)) / Math.sqrt(horizontalDistanceSqr * horizontalDistanceSqr2)));
            } else {
                f2 = 0.0f;
            }
            return getPosition(f).add(new Vec3(d, -0.11d, 0.85d).zRot(-f2).xRot(-lerp).yRot(-lerp2));
        }
        if (isVisuallySwimming()) {
            return getPosition(f).add(new Vec3(d, 0.2d, -0.15d).xRot(-lerp).yRot(-lerp2));
        }
        return getPosition(f).add(new Vec3(d, getBoundingBox().getYsize() - 1.0d, isCrouching() ? -0.2d : 0.07d).yRot(-lerp2));
    }
}
