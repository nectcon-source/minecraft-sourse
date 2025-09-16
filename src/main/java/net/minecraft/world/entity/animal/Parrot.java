package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Parrot.class */
public class Parrot extends ShoulderRidingEntity implements FlyingAnimal {
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
    private static final Predicate<Mob> NOT_PARROT_PREDICATE = new Predicate<Mob>() { // from class: net.minecraft.world.entity.animal.Parrot.1
        @Override // java.util.function.Predicate
        public boolean test(@Nullable Mob mob) {
            return mob != null && Parrot.MOB_SOUND_MAP.containsKey(mob.getType());
        }
    };
    private static final Item POISONOUS_FOOD = Items.COOKIE;
    private static final Set<Item> TAME_FOOD = Sets.newHashSet(new Item[]{Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS});
    private static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP =  Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
        hashMap.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        hashMap.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
        hashMap.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
        hashMap.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
        hashMap.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
        hashMap.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
        hashMap.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
        hashMap.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
        hashMap.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
        hashMap.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
        hashMap.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
        hashMap.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
        hashMap.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
        hashMap.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
        hashMap.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
        hashMap.put(EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
        hashMap.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
        hashMap.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
        hashMap.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
        hashMap.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
        hashMap.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
        hashMap.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
        hashMap.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        hashMap.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
        hashMap.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
        hashMap.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
        hashMap.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
        hashMap.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
        hashMap.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
        hashMap.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
        hashMap.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
        hashMap.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping;
    private boolean partyParrot;
    private BlockPos jukebox;

    public Parrot(EntityType<? extends Parrot> entityType, Level level) {
        super(entityType, level);
        this.flapping = 1.0f;
        this.moveControl = new FlyingMoveControl(this, 10, false);
        setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0f);
        setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0f);
        setPathfindingMalus(BlockPathTypes.COCOA, -1.0f);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setVariant(this.random.nextInt(5));
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData(false);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.LivingEntity
    public boolean isBaby() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25d));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0d, 5.0f, 1.0f, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
        this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0d, 3.0f, 7.0f));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0d).add(Attributes.FLYING_SPEED, 0.4000000059604645d).add(Attributes.MOVEMENT_SPEED, 0.20000000298023224d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setCanPassDoors(true);
        return flyingPathNavigation;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.6f;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.jukebox == null || !this.jukebox.closerThan(position(), 3.46d) || !this.level.getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
            this.partyParrot = false;
            this.jukebox = null;
        }
        if (this.level.random.nextInt(400) == 0) {
            imitateNearbyMobs(this.level, this);
        }
        super.aiStep();
        calculateFlapping();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void setRecordPlayingNearby(BlockPos blockPos, boolean z) {
        this.jukebox = blockPos;
        this.partyParrot = z;
    }

    public boolean isPartyParrot() {
        return this.partyParrot;
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed = (float) (this.flapSpeed + (((this.onGround || isPassenger()) ? -1 : 4) * 0.3d));
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0f, 1.0f);
        if (!this.onGround && this.flapping < 1.0f) {
            this.flapping = 1.0f;
        }
        this.flapping = (float) (this.flapping * 0.9d);
        Vec3 deltaMovement = getDeltaMovement();
        if (!this.onGround && deltaMovement.y < 0.0d) {
            setDeltaMovement(deltaMovement.multiply(1.0d, 0.6d, 1.0d));
        }
        this.flap += this.flapping * 2.0f;
    }

    public static boolean imitateNearbyMobs(Level level, Entity entity) {
        if (!entity.isAlive() || entity.isSilent() || level.random.nextInt(2) != 0) {
            return false;
        }
        List<Mob> entitiesOfClass = level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(20.0d), NOT_PARROT_PREDICATE);
        if (!entitiesOfClass.isEmpty()) {
            Mob mob = entitiesOfClass.get(level.random.nextInt(entitiesOfClass.size()));
            if (!mob.isSilent()) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), getImitatedSound(mob.getType()), entity.getSoundSource(), 0.7f, getPitch(level.random));
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!isTame() && TAME_FOOD.contains(itemInHand.getItem())) {
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            if (!isSilent()) {
                this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.PARROT_EAT, getSoundSource(), 1.0f, 1.0f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f));
            }
            if (!this.level.isClientSide) {
                if (this.random.nextInt(10) == 0) {
                    tame(player);
                    this.level.broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level.broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (itemInHand.getItem() == POISONOUS_FOOD) {
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            addEffect(new MobEffectInstance(MobEffects.POISON, 900));
            if (player.isCreative() || !isInvulnerable()) {
                hurt(DamageSource.playerAttack(player), Float.MAX_VALUE);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (!isFlying() && isTame() && isOwnedBy(player)) {
            if (!this.level.isClientSide) {
                setOrderedToSit(!isOrderedToSit());
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    public static boolean checkParrotSpawnRules(EntityType<Parrot> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        BlockState blockState = levelAccessor.getBlockState(blockPos.below());
        return (blockState.is(BlockTags.LEAVES) || blockState.is(Blocks.GRASS_BLOCK) || blockState.is(BlockTags.LOGS) || blockState.is(Blocks.AIR)) && levelAccessor.getRawBrightness(blockPos, 0) > 8;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void checkFallDamage(double d, boolean z, BlockState blockState, BlockPos blockPos) {
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        return false;
    }

    @Override // net.minecraft.world.entity.AgableMob
    @Nullable
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return null;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        return entity.hurt(DamageSource.mobAttack(this), 3.0f);
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SoundEvent getAmbientSound() {
        return getAmbient(this.level, this.level.random);
    }

    public static SoundEvent getAmbient(Level level, Random random) {
        if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(1000) == 0) {
            List<EntityType<?>> newArrayList = Lists.newArrayList(MOB_SOUND_MAP.keySet());
            return getImitatedSound(newArrayList.get(random.nextInt(newArrayList.size())));
        }
        return SoundEvents.PARROT_AMBIENT;
    }

    private static SoundEvent getImitatedSound(EntityType<?> entityType) {
        return MOB_SOUND_MAP.getOrDefault(entityType, SoundEvents.PARROT_AMBIENT);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.PARROT_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    protected float playFlySound(float f) {
        playSound(SoundEvents.PARROT_FLY, 0.15f, 1.0f);
        return f + (this.flapSpeed / 2.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean makeFlySound() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getVoicePitch() {
        return getPitch(this.random);
    }

    public static float getPitch(Random random) {
        return ((random.nextFloat() - random.nextFloat()) * 0.2f) + 1.0f;
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean isPushable() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void doPush(Entity entity) {
        if (entity instanceof Player) {
            return;
        }
        super.doPush(entity);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        setOrderedToSit(false);
        return super.hurt(damageSource, f);
    }

    public int getVariant() {
        return Mth.clamp(((Integer) this.entityData.get(DATA_VARIANT_ID)).intValue(), 0, 4);
    }

    public void setVariant(int i) {
        this.entityData.set(DATA_VARIANT_ID, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_ID, 0);
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", getVariant());
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setVariant(compoundTag.getInt("Variant"));
    }

    public boolean isFlying() {
        return !this.onGround;
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.5f * getEyeHeight(), getBbWidth() * 0.4f);
    }
}
