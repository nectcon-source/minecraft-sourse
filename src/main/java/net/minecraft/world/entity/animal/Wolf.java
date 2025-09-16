package net.minecraft.world.entity.animal;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Wolf.class */
public class Wolf extends TamableAnimal implements NeutralMob {
    private float interestedAngle;
    private float interestedAngleO;
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;
    private UUID persistentAngerTarget;
    private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    public static final Predicate<LivingEntity> PREY_SELECTOR = livingEntity -> {
        EntityType<?> type = livingEntity.getType();
        return type == EntityType.SHEEP || type == EntityType.RABBIT || type == EntityType.FOX;
    };
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    public Wolf(EntityType<? extends Wolf> entityType, Level level) {
        super(entityType, level);
        setTame(false);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new WolfAvoidEntityGoal(this, Llama.class, 24.0f, 1.5d, 1.5d));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0d, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0d, 10.0f, 2.0f, false));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0d));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.MAX_HEALTH, 8.0d).add(Attributes.ATTACK_DAMAGE, 2.0d);
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_INTERESTED_ID, false);
        this.entityData.define(DATA_COLLAR_COLOR, Integer.valueOf(DyeColor.RED.getId()));
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.WOLF_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putByte("CollarColor", (byte) getCollarColor().getId());
        addPersistentAngerSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("CollarColor", 99)) {
            setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
        }
        readPersistentAngerSaveData((ServerLevel) this.level, compoundTag);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (isAngry()) {
            return SoundEvents.WOLF_GROWL;
        }
        if (this.random.nextInt(3) == 0) {
            if (isTame() && getHealth() < 10.0f) {
                return SoundEvents.WOLF_WHINE;
            }
            return SoundEvents.WOLF_PANT;
        }
        return SoundEvents.WOLF_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WOLF_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide && this.isWet && !this.isShaking && !isPathFinding() && this.onGround) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
            this.level.broadcastEntityEvent(this, (byte) 8);
        }
        if (!this.level.isClientSide) {
            updatePersistentAnger((ServerLevel) this.level, true);
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        this.interestedAngleO = this.interestedAngle;
        if (isInterested()) {
            this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f;
        } else {
            this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f;
        }
        if (isInWaterRainOrBubble()) {
            this.isWet = true;
            if (this.isShaking && !this.level.isClientSide) {
                this.level.broadcastEntityEvent(this, (byte) 56);
                cancelShake();
                return;
            }
            return;
        }
        if ((this.isWet || this.isShaking) && this.isShaking) {
            if (this.shakeAnim == 0.0f) {
                playSound(SoundEvents.WOLF_SHAKE, getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f);
            }
            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05f;
            if (this.shakeAnimO >= 2.0f) {
                this.isWet = false;
                this.isShaking = false;
                this.shakeAnimO = 0.0f;
                this.shakeAnim = 0.0f;
            }
            if (this.shakeAnim > 0.4f) {
                float y = (float) getY();
                int sin = (int) (Mth.sin((this.shakeAnim - 0.4f) * 3.1415927f) * 7.0f);
                Vec3 deltaMovement = getDeltaMovement();
                for (int i = 0; i < sin; i++) {
                    this.level.addParticle(ParticleTypes.SPLASH, getX() + (((this.random.nextFloat() * 2.0f) - 1.0f) * getBbWidth() * 0.5f), y + 0.8f, getZ() + (((this.random.nextFloat() * 2.0f) - 1.0f) * getBbWidth() * 0.5f), deltaMovement.x, deltaMovement.y, deltaMovement.z);
                }
            }
        }
    }

    private void cancelShake() {
        this.isShaking = false;
        this.shakeAnim = 0.0f;
        this.shakeAnimO = 0.0f;
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        this.isWet = false;
        this.isShaking = false;
        this.shakeAnimO = 0.0f;
        this.shakeAnim = 0.0f;
        super.die(damageSource);
    }

    public boolean isWet() {
        return this.isWet;
    }

    public float getWetShade(float f) {
        return Math.min(0.5f + ((Mth.lerp(f, this.shakeAnimO, this.shakeAnim) / 2.0f) * 0.5f), 1.0f);
    }

    public float getBodyRollAngle(float f, float f2) {
        float lerp = (Mth.lerp(f, this.shakeAnimO, this.shakeAnim) + f2) / 1.8f;
        if (lerp < 0.0f) {
            lerp = 0.0f;
        } else if (lerp > 1.0f) {
            lerp = 1.0f;
        }
        return Mth.sin(lerp * 3.1415927f) * Mth.sin(lerp * 3.1415927f * 11.0f) * 0.15f * 3.1415927f;
    }

    public float getHeadRollAngle(float f) {
        return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.15f * 3.1415927f;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.8f;
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadXRot() {
        if (isInSittingPose()) {
            return 20;
        }
        return super.getMaxHeadXRot();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        Entity entity = damageSource.getEntity();
        setOrderedToSit(false);
        if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
            f = (f + 1.0f) / 2.0f;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = entity.hurt(DamageSource.mobAttack(this), (int) getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            doEnchantDamageEffects(this, entity);
        }
        return hurt;
    }

    @Override // net.minecraft.world.entity.TamableAnimal
    public void setTame(boolean z) {
        super.setTame(z);
        if (z) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0d);
            setHealth(20.0f);
        } else {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0d);
        }
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0d);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        Item item = itemInHand.getItem();
        if (this.level.isClientSide) {
            return isOwnedBy(player) || isTame() || (item == Items.BONE && !isTame() && !isAngry()) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (isTame()) {
            if (isFood(itemInHand) && getHealth() < getMaxHealth()) {
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
                heal(item.getFoodProperties().getNutrition());
                return InteractionResult.SUCCESS;
            }
            if (item instanceof DyeItem) {
                DyeColor dyeColor = ((DyeItem) item).getDyeColor();
                if (dyeColor != getCollarColor()) {
                    setCollarColor(dyeColor);
                    if (!player.abilities.instabuild) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            } else {
                InteractionResult mobInteract = super.mobInteract(player, interactionHand);
                if ((!mobInteract.consumesAction() || isBaby()) && isOwnedBy(player)) {
                    setOrderedToSit(!isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    setTarget(null);
                    return InteractionResult.SUCCESS;
                }
                return mobInteract;
            }
        } else if (item == Items.BONE && !isAngry()) {
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                tame(player);
                this.navigation.stop();
                setTarget(null);
                setOrderedToSit(true);
                this.level.broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level.broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
        } else if (b == 56) {
            cancelShake();
        } else {
            super.handleEntityEvent(b);
        }
    }

    public float getTailAngle() {
        if (isAngry()) {
            return 1.5393804f;
        }
        if (isTame()) {
            return (0.55f - ((getMaxHealth() - getHealth()) * 0.02f)) * 3.1415927f;
        }
        return 0.62831855f;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item.isEdible() && item.getFoodProperties().isMeat();
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public int getRemainingPersistentAngerTime() {
        return ((Integer) this.entityData.get(DATA_REMAINING_ANGER_TIME)).intValue();
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setRemainingPersistentAngerTime(int i) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    @Override // net.minecraft.world.entity.NeutralMob
    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(((Integer) this.entityData.get(DATA_COLLAR_COLOR)).intValue());
    }

    public void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, Integer.valueOf(dyeColor.getId()));
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Wolf getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Wolf create = EntityType.WOLF.create(serverLevel);
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null) {
            create.setOwnerUUID(ownerUUID);
            create.setTame(true);
        }
        return create;
    }

    public void setIsInterested(boolean z) {
        this.entityData.set(DATA_INTERESTED_ID, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        if (animal == this || !isTame() || !(animal instanceof Wolf)) {
            return false;
        }
        Wolf wolf = (Wolf) animal;
        return wolf.isTame() && !wolf.isInSittingPose() && isInLove() && wolf.isInLove();
    }

    public boolean isInterested() {
        return ((Boolean) this.entityData.get(DATA_INTERESTED_ID)).booleanValue();
    }

    @Override // net.minecraft.world.entity.TamableAnimal
    public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
        if ((livingEntity instanceof Creeper) || (livingEntity instanceof Ghast)) {
            return false;
        }
        if (livingEntity instanceof Wolf) {
            Wolf wolf = (Wolf) livingEntity;
            return (wolf.isTame() && wolf.getOwner() == livingEntity2) ? false : true;
        }
        if ((livingEntity instanceof Player) && (livingEntity2 instanceof Player) && !((Player) livingEntity2).canHarmPlayer((Player) livingEntity)) {
            return false;
        }
        if ((livingEntity instanceof AbstractHorse) && ((AbstractHorse) livingEntity).isTamed()) {
            return false;
        }
        return ((livingEntity instanceof TamableAnimal) && ((TamableAnimal) livingEntity).isTame()) ? false : true;
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return !isAngry() && super.canBeLeashed(player);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.6f * getEyeHeight(), getBbWidth() * 0.4f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Wolf$WolfAvoidEntityGoal.class */
    class WolfAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Wolf wolf;

        public WolfAvoidEntityGoal(Wolf wolf, Class<T> cls, float f, double d, double d2) {
            super(wolf, cls, f, d, d2);
            this.wolf = wolf;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && (this.toAvoid instanceof Llama) && !this.wolf.isTame() && avoidLlama((Llama) this.toAvoid);
        }

        private boolean avoidLlama(Llama llama) {
            return llama.getStrength() >= Wolf.this.random.nextInt(5);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Wolf.this.setTarget(null);
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            Wolf.this.setTarget(null);
            super.tick();
        }
    }
}
