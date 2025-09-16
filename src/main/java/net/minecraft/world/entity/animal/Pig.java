package net.minecraft.world.entity.animal;

import com.google.common.collect.UnmodifiableIterator;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Pig.class */
public class Pig extends Animal implements ItemSteerable, Saddleable {
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
    private final ItemBasedSteering steering;

    public Pig(EntityType<? extends Pig> entityType, Level level) {
        super(entityType, level);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25d));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0d));
        this.goalSelector.addGoal(4, new TemptGoal((PathfinderMob) this, 1.2d, Ingredient.of(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.addGoal(4, new TemptGoal((PathfinderMob) this, 1.2d, false, FOOD_ITEMS));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1d));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0d).add(Attributes.MOVEMENT_SPEED, 0.25d);
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity getControllingPassenger() {
        if (getPassengers().isEmpty()) {
            return null;
        }
        return getPassengers().get(0);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeControlledByRider() {
        Entity controllingPassenger = getControllingPassenger();
        if (!(controllingPassenger instanceof Player)) {
            return false;
        }
        Player player = (Player) controllingPassenger;
        if (player.getMainHandItem().getItem() == Items.CARROT_ON_A_STICK || player.getOffhandItem().getItem() == Items.CARROT_ON_A_STICK) {
            return true;
        }
        return false;
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
        this.entityData.define(DATA_SADDLE_ID, false);
        this.entityData.define(DATA_BOOST_TIME, 0);
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

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIG_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.PIG_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (!isFood(player.getItemInHand(interactionHand)) && isSaddled() && !isVehicle() && !player.isSecondaryUseActive()) {
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
        return mobInteract;
    }

    @Override // net.minecraft.world.entity.Saddleable
    public boolean isSaddleable() {
        return isAlive() && !isBaby();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropEquipment() {
        super.dropEquipment();
        if (isSaddled()) {
            spawnAtLocation(Items.SADDLE);
        }
    }

    @Override // net.minecraft.world.entity.Saddleable
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override // net.minecraft.world.entity.Saddleable
    public void equipSaddle(@Nullable SoundSource soundSource) {
        this.steering.setSaddle(true);
        if (soundSource != null) {
            this.level.playSound((Player) null, this, SoundEvents.PIG_SADDLE, soundSource, 0.5f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Direction motionDirection = getMotionDirection();
        if (motionDirection.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(livingEntity);
        }
        int[][] offsetsForDirection = DismountHelper.offsetsForDirection(motionDirection);
        BlockPos blockPosition = blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        UnmodifiableIterator it = livingEntity.getDismountPoses().iterator();
        while (it.hasNext()) {
            Pose pose = (Pose) it.next();
            AABB localBoundsForPose = livingEntity.getLocalBoundsForPose(pose);
            for (int[] iArr : offsetsForDirection) {
                mutableBlockPos.set(blockPosition.getX() + iArr[0], blockPosition.getY(), blockPosition.getZ() + iArr[1]);
                double blockFloorHeight = this.level.getBlockFloorHeight(mutableBlockPos);
                if (DismountHelper.isBlockFloorValid(blockFloorHeight)) {
                    Vec3 upFromBottomCenterOf = Vec3.upFromBottomCenterOf(mutableBlockPos, blockFloorHeight);
                    if (DismountHelper.canDismountTo(this.level, livingEntity, localBoundsForPose.move(upFromBottomCenterOf))) {
                        livingEntity.setPose(pose);
                        return upFromBottomCenterOf;
                    }
                }
            }
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    @Override // net.minecraft.world.entity.Entity
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        if (serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
            ZombifiedPiglin create = EntityType.ZOMBIFIED_PIGLIN.create(serverLevel);
            create.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            create.moveTo(getX(), getY(), getZ(), this.yRot, this.xRot);
            create.setNoAi(isNoAi());
            create.setBaby(isBaby());
            if (hasCustomName()) {
                create.setCustomName(getCustomName());
                create.setCustomNameVisible(isCustomNameVisible());
            }
            create.setPersistenceRequired();
            serverLevel.addFreshEntity(create);
            remove();
            return;
        }
        super.thunderHit(serverLevel, lightningBolt);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        travel(this, this.steering, vec3);
    }

    @Override // net.minecraft.world.entity.ItemSteerable
    public float getSteeringSpeed() {
        return ((float) getAttributeValue(Attributes.MOVEMENT_SPEED)) * 0.225f;
    }

    @Override // net.minecraft.world.entity.ItemSteerable
    public void travelWithInput(Vec3 vec3) {
        super.travel(vec3);
    }

    @Override // net.minecraft.world.entity.ItemSteerable
    public boolean boost() {
        return this.steering.boost(getRandom());
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Pig getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return EntityType.PIG.create(serverLevel);
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.6f * getEyeHeight(), getBbWidth() * 0.4f);
    }
}
