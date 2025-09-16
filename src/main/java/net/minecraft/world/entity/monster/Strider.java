package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Strider.class */
public class Strider extends Animal implements ItemSteerable, Saddleable {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WARPED_FUNGUS);
    private static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering;
    private TemptGoal temptGoal;
    private PanicGoal panicGoal;

    public Strider(EntityType<? extends Strider> entityType, Level level) {
        super(entityType, level);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
        this.blocksBuilding = true;
        setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
        setPathfindingMalus(BlockPathTypes.LAVA, 0.0f);
        setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0f);
        setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0f);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        do {
            mutable.move(Direction.UP);
        } while (levelAccessor.getFluidState(mutable).is(FluidTags.LAVA));
        return levelAccessor.getBlockState(mutable).isAir();
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BOOST_TIME.equals(entityDataAccessor) && this.level.isClientSide) {
            this.steering.onSynced();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BOOST_TIME, 0);
        this.entityData.define(DATA_SUFFOCATING, false);
        this.entityData.define(DATA_SADDLE_ID, false);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.steering.addAdditionalSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.steering.readAdditionalSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.Saddleable
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override // net.minecraft.world.entity.Saddleable
    public boolean isSaddleable() {
        return isAlive() && !isBaby();
    }

    @Override // net.minecraft.world.entity.Saddleable
    public void equipSaddle(@Nullable SoundSource soundSource) {
        this.steering.setSaddle(true);
        if (soundSource != null) {
            this.level.playSound((Player) null, this, SoundEvents.STRIDER_SADDLE, soundSource, 0.5f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.panicGoal = new PanicGoal(this, 1.65d);
        this.goalSelector.addGoal(1, this.panicGoal);
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0d));
        this.temptGoal = new TemptGoal((PathfinderMob) this, 1.4d, false, TEMPT_ITEMS);
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(4, new StriderGoToLavaGoal(this,1.5d));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1d));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0d, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0f));
    }

    public void setSuffocating(boolean z) {
        this.entityData.set(DATA_SUFFOCATING, Boolean.valueOf(z));
    }

    public boolean isSuffocating() {
        if (getVehicle() instanceof Strider) {
            return ((Strider) getVehicle()).isSuffocating();
        }
        return ((Boolean) this.entityData.get(DATA_SUFFOCATING)).booleanValue();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canStandOnFluid(Fluid fluid) {
        return fluid.is(FluidTags.LAVA);
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return (getBbHeight() - 0.19d) + (0.12f * Mth.cos(this.animationPosition * 1.5f) * 2.0f * Math.min(0.25f, this.animationSpeed));
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeControlledByRider() {
        Entity controllingPassenger = getControllingPassenger();
        if (!(controllingPassenger instanceof Player)) {
            return false;
        }
        Player player = (Player) controllingPassenger;
        return player.getMainHandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK || player.getOffhandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity getControllingPassenger() {
        if (getPassengers().isEmpty()) {
            return null;
        }
        return getPassengers().get(0);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3[] vec3Arr = {getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), livingEntity.yRot), getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), livingEntity.yRot - 22.5f), getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), livingEntity.yRot + 22.5f), getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), livingEntity.yRot - 45.0f), getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), livingEntity.yRot + 45.0f)};
        Set<BlockPos> newLinkedHashSet = Sets.newLinkedHashSet();
        double d = getBoundingBox().maxY;
        double d2 = getBoundingBox().minY - 0.5d;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Vec3 vec3 : vec3Arr) {
            mutableBlockPos.set(getX() + vec3.x, d, getZ() + vec3.z);
            double d3 = d;
            while (true) {
                double d4 = d3;
                if (d4 > d2) {
                    newLinkedHashSet.add(mutableBlockPos.immutable());
                    mutableBlockPos.move(Direction.DOWN);
                    d3 = d4 - 1.0d;
                }
            }
        }
        for (BlockPos blockPos : newLinkedHashSet) {
            if (!this.level.getFluidState(blockPos).is(FluidTags.LAVA)) {
                double blockFloorHeight = this.level.getBlockFloorHeight(blockPos);
                if (DismountHelper.isBlockFloorValid(blockFloorHeight)) {
                    Vec3 upFromBottomCenterOf = Vec3.upFromBottomCenterOf(blockPos, blockFloorHeight);
                    UnmodifiableIterator it = livingEntity.getDismountPoses().iterator();
                    while (it.hasNext()) {
                        Pose pose = (Pose) it.next();
                        if (DismountHelper.canDismountTo(this.level, livingEntity, livingEntity.getLocalBoundsForPose(pose).move(upFromBottomCenterOf))) {
                            livingEntity.setPose(pose);
                            return upFromBottomCenterOf;
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        return new Vec3(getX(), getBoundingBox().maxY, getZ());
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        setSpeed(getMoveSpeed());
        travel(this, this.steering, vec3);
    }

    public float getMoveSpeed() {
        return ((float) getAttributeValue(Attributes.MOVEMENT_SPEED)) * (isSuffocating() ? 0.66f : 1.0f);
    }

    @Override // net.minecraft.world.entity.ItemSteerable
    public float getSteeringSpeed() {
        return ((float) getAttributeValue(Attributes.MOVEMENT_SPEED)) * (isSuffocating() ? 0.23f : 0.55f);
    }

    @Override // net.minecraft.world.entity.ItemSteerable
    public void travelWithInput(Vec3 vec3) {
        super.travel(vec3);
    }

    @Override // net.minecraft.world.entity.Entity
    protected float nextStep() {
        return this.moveDist + 0.6f;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.entity.ItemSteerable
    public boolean boost() {
        return this.steering.boost(getRandom());
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void checkFallDamage(double d, boolean z, BlockState blockState, BlockPos blockPos) {
        checkInsideBlocks();
        if (isInLava()) {
            this.fallDistance = 0.0f;
        } else {
            super.checkFallDamage(d, z, blockState, blockPos);
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        if (isBeingTempted() && this.random.nextInt(140) == 0) {
            playSound(SoundEvents.STRIDER_HAPPY, 1.0f, getVoicePitch());
        } else if (isPanicking() && this.random.nextInt(60) == 0) {
            playSound(SoundEvents.STRIDER_RETREAT, 1.0f, getVoicePitch());
        }
        setSuffocating(!(this.level.getBlockState(blockPosition()).is(BlockTags.STRIDER_WARM_BLOCKS) || getBlockStateOn().is(BlockTags.STRIDER_WARM_BLOCKS) || (getFluidHeight(FluidTags.LAVA) > 0.0d ? 1 : (getFluidHeight(FluidTags.LAVA) == 0.0d ? 0 : -1)) > 0));
        super.tick();
        floatStrider();
        checkInsideBlocks();
    }

    private boolean isPanicking() {
        return this.panicGoal != null && this.panicGoal.isRunning();
    }

    private boolean isBeingTempted() {
        return this.temptGoal != null && this.temptGoal.isRunning();
    }

    @Override // net.minecraft.world.entity.Mob
    protected boolean shouldPassengersInheritMalus() {
        return true;
    }

    private void floatStrider() {
        if (isInLava()) {
            if (!CollisionContext.of(this).isAbove(LiquidBlock.STABLE_SHAPE, blockPosition(), true) || this.level.getFluidState(blockPosition().above()).is(FluidTags.LAVA)) {
                setDeltaMovement(getDeltaMovement().scale(0.5d).add(0.0d, 0.05d, 0.0d));
            } else {
                this.onGround = true;
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.17499999701976776d).add(Attributes.FOLLOW_RANGE, 16.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (isPanicking() || isBeingTempted()) {
            return null;
        }
        return SoundEvents.STRIDER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean canAddPassenger(Entity entity) {
        return getPassengers().isEmpty() && !isEyeInFluid(FluidTags.LAVA);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isOnFire() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    protected PathNavigation createNavigation(Level level) {
        return new StriderPathNavigation(this, level);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos).getFluidState().is(FluidTags.LAVA)) {
            return 10.0f;
        }
        return isInLava() ? Float.NEGATIVE_INFINITY : 0.0f;
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Strider getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return EntityType.STRIDER.create(serverLevel);
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropEquipment() {
        super.dropEquipment();
        if (isSaddled()) {
            spawnAtLocation(Items.SADDLE);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        boolean isFood = isFood(player.getItemInHand(interactionHand));
        if (!isFood && isSaddled() && !isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level.isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        InteractionResult mobInteract = super.mobInteract(player, interactionHand);
        if (!mobInteract.consumesAction()) {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            if (itemInHand.getItem() == Items.SADDLE) {
                return itemInHand.interactLivingEntity(player, this, interactionHand);
            }
            return InteractionResult.PASS;
        }
        if (isFood && !isSilent()) {
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.STRIDER_EAT, getSoundSource(), 1.0f, 1.0f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f));
        }
        return mobInteract;
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.6f * getEyeHeight(), getBbWidth() * 0.4f);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData agableMobGroupData;
        if (isBaby()) {
            return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        }
        if (this.random.nextInt(30) == 0) {
            Mob create = EntityType.ZOMBIFIED_PIGLIN.create(serverLevelAccessor.getLevel());
            agableMobGroupData = spawnJockey(serverLevelAccessor, difficultyInstance, create, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(this.random), false));
            create.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
            equipSaddle(null);
        } else if (this.random.nextInt(10) == 0) {
            AgableMob create2 = EntityType.STRIDER.create(serverLevelAccessor.getLevel());
            create2.setAge(-24000);
            agableMobGroupData = spawnJockey(serverLevelAccessor, difficultyInstance, create2, null);
        } else {
            agableMobGroupData = new AgableMob.AgableMobGroupData(0.5f);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, agableMobGroupData, compoundTag);
    }

    private SpawnGroupData spawnJockey(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, Mob mob, @Nullable SpawnGroupData spawnGroupData) {
        mob.moveTo(getX(), getY(), getZ(), this.yRot, 0.0f);
        mob.finalizeSpawn(serverLevelAccessor, difficultyInstance, MobSpawnType.JOCKEY, spawnGroupData, null);
        mob.startRiding(this, true);
        return new AgableMob.AgableMobGroupData(0.0f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Strider$StriderPathNavigation.class */
    static class StriderPathNavigation extends GroundPathNavigation {
        StriderPathNavigation(Strider strider, Level level) {
            super(strider, level);
        }

        @Override // net.minecraft.world.entity.p000ai.navigation.GroundPathNavigation, net.minecraft.world.entity.p000ai.navigation.PathNavigation
        protected PathFinder createPathFinder(int i) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, i);
        }

        @Override // net.minecraft.world.entity.p000ai.navigation.GroundPathNavigation
        protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
            if (blockPathTypes == BlockPathTypes.LAVA || blockPathTypes == BlockPathTypes.DAMAGE_FIRE || blockPathTypes == BlockPathTypes.DANGER_FIRE) {
                return true;
            }
            return super.hasValidPathType(blockPathTypes);
        }

        @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
        public boolean isStableDestination(BlockPos blockPos) {
            return this.level.getBlockState(blockPos).is(Blocks.LAVA) || super.isStableDestination(blockPos);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Strider$StriderGoToLavaGoal.class */
    static class StriderGoToLavaGoal extends MoveToBlockGoal {
        private final Strider strider;

        private StriderGoToLavaGoal(Strider strider, double d) {
            super(strider, d, 8, 2);
            this.strider = strider;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        public BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return !this.strider.isInLava() && isValidTarget(this.strider.level, this.blockPos);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.strider.isInLava() && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            return levelReader.getBlockState(blockPos).is(Blocks.LAVA) && levelReader.getBlockState(blockPos.above()).isPathfindable(levelReader, blockPos, PathComputationType.LAND);
        }
    }
}
