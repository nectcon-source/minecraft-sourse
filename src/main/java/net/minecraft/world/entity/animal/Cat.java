package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Cat.class */
public class Cat extends TamableAnimal {
    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
    public static final Map<Integer, ResourceLocation> TEXTURE_BY_TYPE =  Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(0, new ResourceLocation("textures/entity/cat/tabby.png"));
        hashMap.put(1, new ResourceLocation("textures/entity/cat/black.png"));
        hashMap.put(2, new ResourceLocation("textures/entity/cat/red.png"));
        hashMap.put(3, new ResourceLocation("textures/entity/cat/siamese.png"));
        hashMap.put(4, new ResourceLocation("textures/entity/cat/british_shorthair.png"));
        hashMap.put(5, new ResourceLocation("textures/entity/cat/calico.png"));
        hashMap.put(6, new ResourceLocation("textures/entity/cat/persian.png"));
        hashMap.put(7, new ResourceLocation("textures/entity/cat/ragdoll.png"));
        hashMap.put(8, new ResourceLocation("textures/entity/cat/white.png"));
        hashMap.put(9, new ResourceLocation("textures/entity/cat/jellie.png"));
        hashMap.put(10, new ResourceLocation("textures/entity/cat/all_black.png"));
    });
    private CatAvoidEntityGoal<Player> avoidPlayersGoal;
    private TemptGoal temptGoal;
    private float lieDownAmount;
    private float lieDownAmountO;
    private float lieDownAmountTail;
    private float lieDownAmountOTail;
    private float relaxStateOneAmount;
    private float relaxStateOneAmountO;

    public Cat(EntityType<? extends Cat> entityType, Level level) {
        super(entityType, level);
    }

    public ResourceLocation getResourceLocation() {
        return TEXTURE_BY_TYPE.getOrDefault(Integer.valueOf(getCatType()), TEXTURE_BY_TYPE.get(0));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.temptGoal = new CatTemptGoal(this, 0.6d, TEMPT_INGREDIENT, true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new CatRelaxOnOwnerGoal(this));
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1d, 8));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0d, 10.0f, 5.0f, false));
        this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8d));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3f));
        this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(10, new BreedGoal(this, 0.8d));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8d, 1.0000001E-5f));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal(this, Rabbit.class, false, null));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public int getCatType() {
        return ((Integer) this.entityData.get(DATA_TYPE_ID)).intValue();
    }

    public void setCatType(int i) {
        if (i < 0 || i >= 11) {
            i = this.random.nextInt(10);
        }
        this.entityData.set(DATA_TYPE_ID, Integer.valueOf(i));
    }

    public void setLying(boolean z) {
        this.entityData.set(IS_LYING, Boolean.valueOf(z));
    }

    public boolean isLying() {
        return ( this.entityData.get(IS_LYING)).booleanValue();
    }

    public void setRelaxStateOne(boolean z) {
        this.entityData.set(RELAX_STATE_ONE, Boolean.valueOf(z));
    }

    public boolean isRelaxStateOne() {
        return ( this.entityData.get(RELAX_STATE_ONE)).booleanValue();
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(( this.entityData.get(DATA_COLLAR_COLOR)).intValue());
    }

    public void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, Integer.valueOf(dyeColor.getId()));
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, 1);
        this.entityData.define(IS_LYING, false);
        this.entityData.define(RELAX_STATE_ONE, false);
        this.entityData.define(DATA_COLLAR_COLOR, Integer.valueOf(DyeColor.RED.getId()));
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("CatType", getCatType());
        compoundTag.putByte("CollarColor", (byte) getCollarColor().getId());
    }

    @Override // net.minecraft.world.entity.TamableAnimal, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setCatType(compoundTag.getInt("CatType"));
        if (compoundTag.contains("CollarColor", 99)) {
            setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public void customServerAiStep() {
        if (getMoveControl().hasWanted()) {
            double speedModifier = getMoveControl().getSpeedModifier();
            if (speedModifier == 0.6d) {
                setPose(Pose.CROUCHING);
                setSprinting(false);
                return;
            } else if (speedModifier == 1.33d) {
                setPose(Pose.STANDING);
                setSprinting(true);
                return;
            } else {
                setPose(Pose.STANDING);
                setSprinting(false);
                return;
            }
        }
        setPose(Pose.STANDING);
        setSprinting(false);
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (isTame()) {
            if (isInLove()) {
                return SoundEvents.CAT_PURR;
            }
            if (this.random.nextInt(4) == 0) {
                return SoundEvents.CAT_PURREOW;
            }
            return SoundEvents.CAT_AMBIENT;
        }
        return SoundEvents.CAT_STRAY_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public int getAmbientSoundInterval() {
        return 120;
    }

    public void hiss() {
        playSound(SoundEvents.CAT_HISS, getSoundVolume(), getVoicePitch());
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAT_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.ATTACK_DAMAGE, 3.0d);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    protected void usePlayerItem(Player player, ItemStack itemStack) {
        if (isFood(itemStack)) {
            playSound(SoundEvents.CAT_EAT, 1.0f, 1.0f);
        }
        super.usePlayerItem(player, itemStack);
    }

    private float getAttackDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        return entity.hurt(DamageSource.mobAttack(this), getAttackDamage());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isRunning() && !isTame() && this.tickCount % 100 == 0) {
            playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0f, 1.0f);
        }
        handleLieDown();
    }

    private void handleLieDown() {
        if ((isLying() || isRelaxStateOne()) && this.tickCount % 5 == 0) {
            playSound(SoundEvents.CAT_PURR, 0.6f + (0.4f * (this.random.nextFloat() - this.random.nextFloat())), 1.0f);
        }
        updateLieDownAmount();
        updateRelaxStateOneAmount();
    }

    private void updateLieDownAmount() {
        this.lieDownAmountO = this.lieDownAmount;
        this.lieDownAmountOTail = this.lieDownAmountTail;
        if (isLying()) {
            this.lieDownAmount = Math.min(1.0f, this.lieDownAmount + 0.15f);
            this.lieDownAmountTail = Math.min(1.0f, this.lieDownAmountTail + 0.08f);
        } else {
            this.lieDownAmount = Math.max(0.0f, this.lieDownAmount - 0.22f);
            this.lieDownAmountTail = Math.max(0.0f, this.lieDownAmountTail - 0.13f);
        }
    }

    private void updateRelaxStateOneAmount() {
        this.relaxStateOneAmountO = this.relaxStateOneAmount;
        if (isRelaxStateOne()) {
            this.relaxStateOneAmount = Math.min(1.0f, this.relaxStateOneAmount + 0.1f);
        } else {
            this.relaxStateOneAmount = Math.max(0.0f, this.relaxStateOneAmount - 0.13f);
        }
    }

    public float getLieDownAmount(float f) {
        return Mth.lerp(f, this.lieDownAmountO, this.lieDownAmount);
    }

    public float getLieDownAmountTail(float f) {
        return Mth.lerp(f, this.lieDownAmountOTail, this.lieDownAmountTail);
    }

    public float getRelaxStateOneAmount(float f) {
        return Mth.lerp(f, this.relaxStateOneAmountO, this.relaxStateOneAmount);
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Cat getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Cat create = EntityType.CAT.create(serverLevel);
        if (agableMob instanceof Cat) {
            if (this.random.nextBoolean()) {
                create.setCatType(getCatType());
            } else {
                create.setCatType(((Cat) agableMob).getCatType());
            }
            if (isTame()) {
                create.setOwnerUUID(getOwnerUUID());
                create.setTame(true);
                if (this.random.nextBoolean()) {
                    create.setCollarColor(getCollarColor());
                } else {
                    create.setCollarColor(((Cat) agableMob).getCollarColor());
                }
            }
        }
        return create;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        return isTame() && (animal instanceof Cat) && ((Cat) animal).isTame() && super.canMate(animal);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData finalizeSpawn = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        if (serverLevelAccessor.getMoonBrightness() > 0.9f) {
            setCatType(this.random.nextInt(11));
        } else {
            setCatType(this.random.nextInt(10));
        }
        Level level = serverLevelAccessor.getLevel();
        if ((level instanceof ServerLevel) && ((ServerLevel) level).structureFeatureManager().getStructureAt(blockPosition(), true, StructureFeature.SWAMP_HUT).isValid()) {
            setCatType(10);
            setPersistenceRequired();
        }
        return finalizeSpawn;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        Item item = itemInHand.getItem();
        if (this.level.isClientSide) {
            if (isTame() && isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            }
            if (isFood(itemInHand) && (getHealth() < getMaxHealth() || !isTame())) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (isTame()) {
            if (isOwnedBy(player)) {
                if (!(item instanceof DyeItem)) {
                    if (item.isEdible() && isFood(itemInHand) && getHealth() < getMaxHealth()) {
                        usePlayerItem(player, itemInHand);
                        heal(item.getFoodProperties().getNutrition());
                        return InteractionResult.CONSUME;
                    }
                    InteractionResult mobInteract = super.mobInteract(player, interactionHand);
                    if (!mobInteract.consumesAction() || isBaby()) {
                        setOrderedToSit(!isOrderedToSit());
                    }
                    return mobInteract;
                }
                DyeColor dyeColor = ((DyeItem) item).getDyeColor();
                if (dyeColor != getCollarColor()) {
                    setCollarColor(dyeColor);
                    if (!player.abilities.instabuild) {
                        itemInHand.shrink(1);
                    }
                    setPersistenceRequired();
                    return InteractionResult.CONSUME;
                }
            }
        } else if (isFood(itemInHand)) {
            usePlayerItem(player, itemInHand);
            if (this.random.nextInt(3) == 0) {
                tame(player);
                setOrderedToSit(true);
                this.level.broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level.broadcastEntityEvent(this, (byte) 6);
            }
            setPersistenceRequired();
            return InteractionResult.CONSUME;
        }
        InteractionResult mobInteract2 = super.mobInteract(player, interactionHand);
        if (mobInteract2.consumesAction()) {
            setPersistenceRequired();
        }
        return mobInteract2;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return TEMPT_INGREDIENT.test(itemStack);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.5f;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return !isTame() && this.tickCount > 2400;
    }

    @Override // net.minecraft.world.entity.TamableAnimal
    protected void reassessTameGoals() {
        if (this.avoidPlayersGoal == null) {
            this.avoidPlayersGoal = new CatAvoidEntityGoal<>(this, Player.class, 16.0f, 0.8d, 1.33d);
        }
        this.goalSelector.removeGoal(this.avoidPlayersGoal);
        if (!isTame()) {
            this.goalSelector.addGoal(4, this.avoidPlayersGoal);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Cat$CatAvoidEntityGoal.class */
    static class CatAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Cat cat;

        public CatAvoidEntityGoal(Cat var1, Class<T> var2, float var3, double var4, double var6) {
            super(var1, var2, var3, var4, var6, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.cat = var1;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.cat.isTame() && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return !this.cat.isTame() && super.canContinueToUse();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Cat$CatTemptGoal.class */
    static class CatTemptGoal extends TemptGoal {

        @Nullable
        private Player selectedPlayer;
        private final Cat cat;

        public CatTemptGoal(Cat cat, double d, Ingredient ingredient, boolean z) {
            super(cat, d, ingredient, z);
            this.cat = cat;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.TemptGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            super.tick();
            if (this.selectedPlayer == null && this.mob.getRandom().nextInt(600) == 0) {
                this.selectedPlayer = this.player;
            } else if (this.mob.getRandom().nextInt(500) == 0) {
                this.selectedPlayer = null;
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.TemptGoal
        protected boolean canScare() {
            if (this.selectedPlayer != null && this.selectedPlayer.equals(this.player)) {
                return false;
            }
            return super.canScare();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.TemptGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && !this.cat.isTame();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Cat$CatRelaxOnOwnerGoal.class */
    static class CatRelaxOnOwnerGoal extends Goal {
        private final Cat cat;
        private Player ownerPlayer;
        private BlockPos goalPos;
        private int onBedTicks;

        public CatRelaxOnOwnerGoal(Cat cat) {
            this.cat = cat;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (!this.cat.isTame() || this.cat.isOrderedToSit()) {
                return false;
            }
            LivingEntity owner = this.cat.getOwner();
            if (owner instanceof Player) {
                this.ownerPlayer = (Player) owner;
                if (!owner.isSleeping() || this.cat.distanceToSqr(this.ownerPlayer) > 100.0d) {
                    return false;
                }
                BlockPos blockPosition = this.ownerPlayer.blockPosition();
                BlockState blockState = this.cat.level.getBlockState(blockPosition);
                if (blockState.getBlock().is(BlockTags.BEDS)) {
                    this.goalPos = (BlockPos) blockState.getOptionalValue(BedBlock.FACING).map(direction -> {
                        return blockPosition.relative(direction.getOpposite());
                    }).orElseGet(() -> {
                        return new BlockPos(blockPosition);
                    });
                    return !spaceIsOccupied();
                }
                return false;
            }
            return false;
        }

        private boolean spaceIsOccupied() {
            for (Cat cat : this.cat.level.getEntitiesOfClass(Cat.class, new AABB(this.goalPos).inflate(2.0d))) {
                if (cat != this.cat && (cat.isLying() || cat.isRelaxStateOne())) {
                    return true;
                }
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return (!this.cat.isTame() || this.cat.isOrderedToSit() || this.ownerPlayer == null || !this.ownerPlayer.isSleeping() || this.goalPos == null || spaceIsOccupied()) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            if (this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.100000023841858d);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.cat.setLying(false);
            float timeOfDay = this.cat.level.getTimeOfDay(1.0f);
            if (this.ownerPlayer.getSleepTimer() >= 100 && timeOfDay > 0.77d && timeOfDay < 0.8d && this.cat.level.getRandom().nextFloat() < 0.7d) {
                giveMorningGift();
            }
            this.onBedTicks = 0;
            this.cat.setRelaxStateOne(false);
            this.cat.getNavigation().stop();
        }

        private void giveMorningGift() {
            Random random = this.cat.getRandom();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            mutableBlockPos.set(this.cat.blockPosition());
            this.cat.randomTeleport((mutableBlockPos.getX() + random.nextInt(11)) - 5, (mutableBlockPos.getY() + random.nextInt(5)) - 2, (mutableBlockPos.getZ() + random.nextInt(11)) - 5, false);
            mutableBlockPos.set(this.cat.blockPosition());
            Iterator<ItemStack> it = this.cat.level.getServer().getLootTables().get(BuiltInLootTables.CAT_MORNING_GIFT).getRandomItems(new LootContext.Builder((ServerLevel) this.cat.level).withParameter(LootContextParams.ORIGIN, this.cat.position()).withParameter(LootContextParams.THIS_ENTITY, this.cat).withRandom(random).create(LootContextParamSets.GIFT)).iterator();
            while (it.hasNext()) {
                this.cat.level.addFreshEntity(new ItemEntity(this.cat.level, mutableBlockPos.getX() - Mth.sin(this.cat.yBodyRot * 0.017453292f), mutableBlockPos.getY(), mutableBlockPos.getZ() + Mth.cos(this.cat.yBodyRot * 0.017453292f), it.next()));
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.ownerPlayer != null && this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.100000023841858d);
                if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5d) {
                    this.onBedTicks++;
                    if (this.onBedTicks > 16) {
                        this.cat.setLying(true);
                        this.cat.setRelaxStateOne(false);
                        return;
                    } else {
                        this.cat.lookAt(this.ownerPlayer, 45.0f, 45.0f);
                        this.cat.setRelaxStateOne(true);
                        return;
                    }
                }
                this.cat.setLying(false);
            }
        }
    }
}
