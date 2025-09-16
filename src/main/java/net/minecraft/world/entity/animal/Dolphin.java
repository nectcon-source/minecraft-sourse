package net.minecraft.world.entity.animal;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.DolphinLookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import net.minecraft.world.entity.ai.goal.DolphinJumpGoal;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Dolphin.class */
public class Dolphin extends WaterAnimal {
    private static final EntityDataAccessor<BlockPos> TREASURE_POS = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> GOT_FISH = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MOISTNESS_LEVEL = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.INT);
    private static final TargetingConditions SWIM_WITH_PLAYER_TARGETING = new TargetingConditions().range(10.0d).allowSameTeam().allowInvulnerable().allowUnseeable();
    public static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> {
        return !itemEntity.hasPickUpDelay() && itemEntity.isAlive() && itemEntity.isInWater();
    };

    public Dolphin(EntityType<? extends Dolphin> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new DolphinMoveControl(this);
        this.lookControl = new DolphinLookControl(this, 10);
        setCanPickUpLoot(true);
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setAirSupply(getMaxAirSupply());
        this.xRot = 0.0f;
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.animal.WaterAnimal, net.minecraft.world.entity.LivingEntity
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override // net.minecraft.world.entity.animal.WaterAnimal
    protected void handleAirSupply(int i) {
    }

    public void setTreasurePos(BlockPos blockPos) {
        this.entityData.set(TREASURE_POS, blockPos);
    }

    public BlockPos getTreasurePos() {
        return (BlockPos) this.entityData.get(TREASURE_POS);
    }

    public boolean gotFish() {
        return ((Boolean) this.entityData.get(GOT_FISH)).booleanValue();
    }

    public void setGotFish(boolean z) {
        this.entityData.set(GOT_FISH, Boolean.valueOf(z));
    }

    public int getMoistnessLevel() {
        return ((Integer) this.entityData.get(MOISTNESS_LEVEL)).intValue();
    }

    public void setMoisntessLevel(int i) {
        this.entityData.set(MOISTNESS_LEVEL, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TREASURE_POS, BlockPos.ZERO);
        this.entityData.define(GOT_FISH, false);
        this.entityData.define(MOISTNESS_LEVEL, 2400);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("TreasurePosX", getTreasurePos().getX());
        compoundTag.putInt("TreasurePosY", getTreasurePos().getY());
        compoundTag.putInt("TreasurePosZ", getTreasurePos().getZ());
        compoundTag.putBoolean("GotFish", gotFish());
        compoundTag.putInt("Moistness", getMoistnessLevel());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        setTreasurePos(new BlockPos(compoundTag.getInt("TreasurePosX"), compoundTag.getInt("TreasurePosY"), compoundTag.getInt("TreasurePosZ")));
        super.readAdditionalSaveData(compoundTag);
        setGotFish(compoundTag.getBoolean("GotFish"));
        setMoisntessLevel(compoundTag.getInt("Moistness"));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BreathAirGoal(this));
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new DolphinSwimToTreasureGoal(this));
        this.goalSelector.addGoal(2, new DolphinSwimWithPlayerGoal(this, 4.0d));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0d, 10));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new DolphinJumpGoal(this, 10));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2000000476837158d, true));
        this.goalSelector.addGoal(8, new PlayWithItemsGoal());
        this.goalSelector.addGoal(8, new FollowBoatGoal(this));
        this.goalSelector.addGoal(9, new AvoidEntityGoal(this, Guardian.class, 8.0f, 1.0d, 1.0d));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Guardian.class).setAlertOthers(new Class[0]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0d).add(Attributes.MOVEMENT_SPEED, 1.2000000476837158d).add(Attributes.ATTACK_DAMAGE, 3.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = entity.hurt(DamageSource.mobAttack(this), (int) getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            doEnchantDamageEffects(this, entity);
            playSound(SoundEvents.DOLPHIN_ATTACK, 1.0f, 1.0f);
        }
        return hurt;
    }

    @Override // net.minecraft.world.entity.Entity
    public int getMaxAirSupply() {
        return 4800;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected int increaseAirSupply(int i) {
        return getMaxAirSupply();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.3f;
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadYRot() {
        return 1;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean canRide(Entity entity) {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlotForItem = Mob.getEquipmentSlotForItem(itemStack);
        return getItemBySlot(equipmentSlotForItem).isEmpty() && equipmentSlotForItem == EquipmentSlot.MAINHAND && super.canTakeItem(itemStack);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void pickUpItem(ItemEntity itemEntity) {
        if (getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            ItemStack item = itemEntity.getItem();
            if (canHoldItem(item)) {
                onItemPickup(itemEntity);
                setItemSlot(EquipmentSlot.MAINHAND, item);
                this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0f;
                take(itemEntity, item.getCount());
                itemEntity.remove();
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (isNoAi()) {
            setAirSupply(getMaxAirSupply());
            return;
        }
        if (isInWaterRainOrBubble()) {
            setMoisntessLevel(2400);
        } else {
            setMoisntessLevel(getMoistnessLevel() - 1);
            if (getMoistnessLevel() <= 0) {
                hurt(DamageSource.DRY_OUT, 1.0f);
            }
            if (this.onGround) {
                setDeltaMovement(getDeltaMovement().add(((this.random.nextFloat() * 2.0f) - 1.0f) * 0.2f, 0.5d, ((this.random.nextFloat() * 2.0f) - 1.0f) * 0.2f));
                this.yRot = this.random.nextFloat() * 360.0f;
                this.onGround = false;
                this.hasImpulse = true;
            }
        }
        if (this.level.isClientSide && isInWater() && getDeltaMovement().lengthSqr() > 0.03d) {
            Vec3 viewVector = getViewVector(0.0f);
            float cos = Mth.cos(this.yRot * 0.017453292f) * 0.3f;
            float sin = Mth.sin(this.yRot * 0.017453292f) * 0.3f;
            float nextFloat = 1.2f - (this.random.nextFloat() * 0.7f);
            for (int i = 0; i < 2; i++) {
                this.level.addParticle(ParticleTypes.DOLPHIN, (getX() - (viewVector.x * nextFloat)) + cos, getY() - viewVector.y, (getZ() - (viewVector.z * nextFloat)) + sin, 0.0d, 0.0d, 0.0d);
                this.level.addParticle(ParticleTypes.DOLPHIN, (getX() - (viewVector.x * nextFloat)) - cos, getY() - viewVector.y, (getZ() - (viewVector.z * nextFloat)) - sin, 0.0d, 0.0d, 0.0d);
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 38) {
            addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else {
            super.handleEntityEvent(b);
        }
    }

    private void addParticlesAroundSelf(ParticleOptions particleOptions) {
        for (int i = 0; i < 7; i++) {
            this.level.addParticle(particleOptions, getRandomX(1.0d), getRandomY() + 0.2d, getRandomZ(1.0d), this.random.nextGaussian() * 0.01d, this.random.nextGaussian() * 0.01d, this.random.nextGaussian() * 0.01d);
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!itemInHand.isEmpty() && itemInHand.getItem().is(ItemTags.FISHES)) {
            if (!this.level.isClientSide) {
                playSound(SoundEvents.DOLPHIN_EAT, 1.0f, 1.0f);
            }
            setGotFish(true);
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, interactionHand);
    }

    public static boolean checkDolphinSpawnRules(EntityType<Dolphin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        if (blockPos.getY() <= 45 || blockPos.getY() >= levelAccessor.getSeaLevel()) {
            return false;
        }
        Optional<ResourceKey<Biome>> biomeName = levelAccessor.getBiomeName(blockPos);
        return !(Objects.equals(biomeName, Optional.of(Biomes.OCEAN)) && Objects.equals(biomeName, Optional.of(Biomes.DEEP_OCEAN))) && levelAccessor.getFluidState(blockPos).is(FluidTags.WATER);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        return isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.DOLPHIN_SPLASH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSound() {
        return SoundEvents.DOLPHIN_SWIM;
    }

    protected boolean closeToNextPos() {
        BlockPos targetPos = getNavigation().getTargetPos();
        if (targetPos != null) {
            return targetPos.closerThan(position(), 12.0d);
        }
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(getSpeed(), vec3);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9d));
            if (getTarget() == null) {
                setDeltaMovement(getDeltaMovement().add(0.0d, -0.005d, 0.0d));
                return;
            }
            return;
        }
        super.travel(vec3);
    }

    @Override // net.minecraft.world.entity.animal.WaterAnimal, net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Dolphin$DolphinMoveControl.class */
    static class DolphinMoveControl extends MoveControl {
        private final Dolphin dolphin;

        public DolphinMoveControl(Dolphin dolphin) {
            super(dolphin);
            this.dolphin = dolphin;
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            if (this.dolphin.isInWater()) {
                this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add(0.0d, 0.005d, 0.0d));
            }
            if (this.operation != MoveControl.Operation.MOVE_TO || this.dolphin.getNavigation().isDone()) {
                this.dolphin.setSpeed(0.0f);
                this.dolphin.setXxa(0.0f);
                this.dolphin.setYya(0.0f);
                this.dolphin.setZza(0.0f);
                return;
            }
            double x = this.wantedX - this.dolphin.getX();
            double y = this.wantedY - this.dolphin.getY();
            double z = this.wantedZ - this.dolphin.getZ();
            if ((x * x) + (y * y) + (z * z) < 2.500000277905201E-7d) {
                this.mob.setZza(0.0f);
                return;
            }
            this.dolphin.yRot = rotlerp(this.dolphin.yRot, ((float) (Mth.atan2(z, x) * 57.2957763671875d)) - 90.0f, 10.0f);
            this.dolphin.yBodyRot = this.dolphin.yRot;
            this.dolphin.yHeadRot = this.dolphin.yRot;
            float attributeValue = (float) (this.speedModifier * this.dolphin.getAttributeValue(Attributes.MOVEMENT_SPEED));
            if (this.dolphin.isInWater()) {
                this.dolphin.setSpeed(attributeValue * 0.02f);
                this.dolphin.xRot = rotlerp(this.dolphin.xRot, Mth.clamp(Mth.wrapDegrees(-((float) (Mth.atan2(y, Mth.sqrt((x * x) + (z * z))) * 57.2957763671875d))), -85.0f, 85.0f), 5.0f);
                float cos = Mth.cos(this.dolphin.xRot * 0.017453292f);
                float sin = Mth.sin(this.dolphin.xRot * 0.017453292f);
                this.dolphin.zza = cos * attributeValue;
                this.dolphin.yya = (-sin) * attributeValue;
                return;
            }
            this.dolphin.setSpeed(attributeValue * 0.1f);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Dolphin$PlayWithItemsGoal.class */
    class PlayWithItemsGoal extends Goal {
        private int cooldown;

        private PlayWithItemsGoal() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (this.cooldown > Dolphin.this.tickCount) {
                return false;
            }
            return (Dolphin.this.level.getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), Dolphin.ALLOWED_ITEMS).isEmpty() && Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            List<ItemEntity> entitiesOfClass = Dolphin.this.level.getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), Dolphin.ALLOWED_ITEMS);
            if (!entitiesOfClass.isEmpty()) {
                Dolphin.this.getNavigation().moveTo(entitiesOfClass.get(0), 1.2000000476837158d);
                Dolphin.this.playSound(SoundEvents.DOLPHIN_PLAY, 1.0f, 1.0f);
            }
            this.cooldown = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            ItemStack itemBySlot = Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemBySlot.isEmpty()) {
                drop(itemBySlot);
                Dolphin.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.cooldown = Dolphin.this.tickCount + Dolphin.this.random.nextInt(100);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            List<ItemEntity> entitiesOfClass = Dolphin.this.level.getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), Dolphin.ALLOWED_ITEMS);
            ItemStack itemBySlot = Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemBySlot.isEmpty()) {
                drop(itemBySlot);
                Dolphin.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            } else if (!entitiesOfClass.isEmpty()) {
                Dolphin.this.getNavigation().moveTo(entitiesOfClass.get(0), 1.2000000476837158d);
            }
        }

        private void drop(ItemStack itemStack) {
            if (itemStack.isEmpty()) {
                return;
            }
            ItemEntity itemEntity = new ItemEntity(Dolphin.this.level, Dolphin.this.getX(), Dolphin.this.getEyeY() - 0.30000001192092896d, Dolphin.this.getZ(), itemStack);
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(Dolphin.this.getUUID());
            float nextFloat = Dolphin.this.random.nextFloat() * 6.2831855f;
            float nextFloat2 = 0.02f * Dolphin.this.random.nextFloat();
            itemEntity.setDeltaMovement((0.3f * (-Mth.sin(Dolphin.this.yRot * 0.017453292f)) * Mth.cos(Dolphin.this.xRot * 0.017453292f)) + (Mth.cos(nextFloat) * nextFloat2), 0.3f * Mth.sin(Dolphin.this.xRot * 0.017453292f) * 1.5f, (0.3f * Mth.cos(Dolphin.this.yRot * 0.017453292f) * Mth.cos(Dolphin.this.xRot * 0.017453292f)) + (Mth.sin(nextFloat) * nextFloat2));
            Dolphin.this.level.addFreshEntity(itemEntity);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Dolphin$DolphinSwimWithPlayerGoal.class */
    static class DolphinSwimWithPlayerGoal extends Goal {
        private final Dolphin dolphin;
        private final double speedModifier;
        private Player player;

        DolphinSwimWithPlayerGoal(Dolphin dolphin, double d) {
            this.dolphin = dolphin;
            this.speedModifier = d;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            this.player = this.dolphin.level.getNearestPlayer(Dolphin.SWIM_WITH_PLAYER_TARGETING, this.dolphin);
            return (this.player == null || !this.player.isSwimming() || this.dolphin.getTarget() == this.player) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return this.player != null && this.player.isSwimming() && this.dolphin.distanceToSqr(this.player) < 256.0d;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.player = null;
            this.dolphin.getNavigation().stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.dolphin.getLookControl().setLookAt(this.player, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
            if (this.dolphin.distanceToSqr(this.player) < 6.25d) {
                this.dolphin.getNavigation().stop();
            } else {
                this.dolphin.getNavigation().moveTo(this.player, this.speedModifier);
            }
            if (this.player.isSwimming() && this.player.level.random.nextInt(6) == 0) {
                this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100));
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Dolphin$DolphinSwimToTreasureGoal.class */
    static class DolphinSwimToTreasureGoal extends Goal {
        private final Dolphin dolphin;
        private boolean stuck;

        DolphinSwimToTreasureGoal(Dolphin dolphin) {
            this.dolphin = dolphin;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean isInterruptable() {
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.dolphin.gotFish() && this.dolphin.getAirSupply() >= 100;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            BlockPos treasurePos = this.dolphin.getTreasurePos();
            return (new BlockPos((double) treasurePos.getX(), this.dolphin.getY(), (double) treasurePos.getZ()).closerThan(this.dolphin.position(), 4.0d) || this.stuck || this.dolphin.getAirSupply() < 100) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            if (!(this.dolphin.level instanceof ServerLevel)) {
                return;
            }
            ServerLevel serverLevel = (ServerLevel) this.dolphin.level;
            this.stuck = false;
            this.dolphin.getNavigation().stop();
            BlockPos blockPosition = this.dolphin.blockPosition();
            StructureFeature<?> structureFeature = ((double) serverLevel.random.nextFloat()) >= 0.5d ? StructureFeature.OCEAN_RUIN : StructureFeature.SHIPWRECK;
            BlockPos findNearestMapFeature = serverLevel.findNearestMapFeature(structureFeature, blockPosition, 50, false);
            if (findNearestMapFeature == null) {
                BlockPos findNearestMapFeature2 = serverLevel.findNearestMapFeature(structureFeature.equals(StructureFeature.OCEAN_RUIN) ? StructureFeature.SHIPWRECK : StructureFeature.OCEAN_RUIN, blockPosition, 50, false);
                if (findNearestMapFeature2 != null) {
                    this.dolphin.setTreasurePos(findNearestMapFeature2);
                } else {
                    this.stuck = true;
                    return;
                }
            } else {
                this.dolphin.setTreasurePos(findNearestMapFeature);
            }
            serverLevel.broadcastEntityEvent(this.dolphin, (byte) 38);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            BlockPos treasurePos = this.dolphin.getTreasurePos();
            if (new BlockPos(treasurePos.getX(), this.dolphin.getY(), treasurePos.getZ()).closerThan(this.dolphin.position(), 4.0d) || this.stuck) {
                this.dolphin.setGotFish(false);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            Level level = this.dolphin.level;
            if (this.dolphin.closeToNextPos() || this.dolphin.getNavigation().isDone()) {
                Vec3 atCenterOf = Vec3.atCenterOf(this.dolphin.getTreasurePos());
                Vec3 posTowards = RandomPos.getPosTowards(this.dolphin, 16, 1, atCenterOf, 0.39269909262657166d);
                if (posTowards == null) {
                    posTowards = RandomPos.getPosTowards(this.dolphin, 8, 4, atCenterOf);
                }
                if (posTowards != null) {
                    BlockPos blockPos = new BlockPos(posTowards);
                    if (!level.getFluidState(blockPos).is(FluidTags.WATER) || !level.getBlockState(blockPos).isPathfindable(level, blockPos, PathComputationType.WATER)) {
                        posTowards = RandomPos.getPosTowards(this.dolphin, 8, 5, atCenterOf);
                    }
                }
                if (posTowards == null) {
                    this.stuck = true;
                    return;
                }
                this.dolphin.getLookControl().setLookAt(posTowards.x, posTowards.y, posTowards.z, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
                this.dolphin.getNavigation().moveTo(posTowards.x, posTowards.y, posTowards.z, 1.3d);
                if (level.random.nextInt(80) == 0) {
                    level.broadcastEntityEvent(this.dolphin, (byte) 38);
                }
            }
        }
    }
}
