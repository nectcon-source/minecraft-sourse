package net.minecraft.world.entity.animal.horse;

import java.util.Iterator;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Llama.class */
public class Llama extends AbstractChestedHorse implements RangedAttackMob {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Blocks.HAY_BLOCK.asItem());
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SWAG_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private boolean didSpit;

    @Nullable
    private Llama caravanHead;

    @Nullable
    private Llama caravanTail;

    public Llama(EntityType<? extends Llama> entityType, Level level) {
        super(entityType, level);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int i) {
        this.entityData.set(DATA_STRENGTH_ID, Integer.valueOf(Math.max(1, Math.min(5, i))));
    }

    private void setRandomStrength() {
        setStrength(1 + this.random.nextInt(this.random.nextFloat() < 0.04f ? 5 : 3));
    }

    public int getStrength() {
        return ((Integer) this.entityData.get(DATA_STRENGTH_ID)).intValue();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", getVariant());
        compoundTag.putInt("Strength", getStrength());
        if (!this.inventory.getItem(1).isEmpty()) {
            compoundTag.put("DecorItem", this.inventory.getItem(1).save(new CompoundTag()));
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        setStrength(compoundTag.getInt("Strength"));
        super.readAdditionalSaveData(compoundTag);
        setVariant(compoundTag.getInt("Variant"));
        if (compoundTag.contains("DecorItem", 10)) {
            this.inventory.setItem(1, ItemStack.of(compoundTag.getCompound("DecorItem")));
        }
        updateContainerEquipment();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2d));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.0999999046325684d));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25d, 40, 20.0f));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2d));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0d));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0d));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7d));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0d);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STRENGTH_ID, 0);
        this.entityData.define(DATA_SWAG_ID, -1);
        this.entityData.define(DATA_VARIANT_ID, 0);
    }

    public int getVariant() {
        return Mth.clamp(((Integer) this.entityData.get(DATA_VARIANT_ID)).intValue(), 0, 3);
    }

    public void setVariant(int i) {
        this.entityData.set(DATA_VARIANT_ID, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.animal.horse.AbstractHorse
    protected int getInventorySize() {
        if (hasChest()) {
            return 2 + (3 * getInventoryColumns());
        }
        return super.getInventorySize();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Entity
    public void positionRider(Entity entity) {
        if (!hasPassenger(entity)) {
            return;
        }
        float cos = Mth.cos(this.yBodyRot * 0.017453292f);
        entity.setPos(getX() + (0.3f * Mth.sin(this.yBodyRot * 0.017453292f)), getY() + getPassengersRidingOffset() + entity.getMyRidingOffset(), getZ() - (0.3f * cos));
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return getBbHeight() * 0.67d;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    public boolean canBeControlledByRider() {
        return false;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected boolean handleEating(Player player, ItemStack itemStack) {
        int i = 0;
        int i2 = 0;
        float f = 0.0f;
        boolean z = false;
        Item item = itemStack.getItem();
        if (item == Items.WHEAT) {
            i = 10;
            i2 = 3;
            f = 2.0f;
        } else if (item == Blocks.HAY_BLOCK.asItem()) {
            i = 90;
            i2 = 6;
            f = 10.0f;
            if (isTamed() && getAge() == 0 && canFallInLove()) {
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
        if (z && !isSilent() && getEatingSound() != null) {
            this.level.playSound(null, getX(), getY(), getZ(), getEatingSound(), getSoundSource(), 1.0f, 1.0f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f));
        }
        return z;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected boolean isImmobile() {
        return isDeadOrDying() || isEating();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        int nextInt;
        setRandomStrength();
        if (spawnGroupData instanceof LlamaGroupData) {
            nextInt = ((LlamaGroupData) spawnGroupData).variant;
        } else {
            nextInt = this.random.nextInt(4);
            spawnGroupData = new LlamaGroupData(nextInt);
        }
        setVariant(nextInt);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Llama$LlamaGroupData.class */
    static class LlamaGroupData extends AgableMob.AgableMobGroupData {
        public final int variant;

        private LlamaGroupData(int i) {
            super(true);
            this.variant = i;
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse
    protected void playChestEquipsSound() {
        playSound(SoundEvents.LLAMA_CHEST, 1.0f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public void makeMad() {
        SoundEvent angrySound = getAngrySound();
        if (angrySound != null) {
            playSound(angrySound, getSoundVolume(), getVoicePitch());
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractChestedHorse
    public int getInventoryColumns() {
        return getStrength();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public boolean canWearArmor() {
        return true;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public boolean isWearingArmor() {
        return !this.inventory.getItem(1).isEmpty();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public boolean isArmor(ItemStack itemStack) {
        return ItemTags.CARPETS.contains(itemStack.getItem());
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Saddleable
    public boolean isSaddleable() {
        return false;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.ContainerListener
    public void containerChanged(Container container) {
        DyeColor swag = getSwag();
        super.containerChanged(container);
        DyeColor swag2 = getSwag();
        if (this.tickCount > 20 && swag2 != null && swag2 != swag) {
            playSound(SoundEvents.LLAMA_SWAG, 0.5f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void updateContainerEquipment() {
        if (this.level.isClientSide) {
            return;
        }
        super.updateContainerEquipment();
        setSwag(getDyeColor(this.inventory.getItem(1)));
    }

    private void setSwag(@Nullable DyeColor dyeColor) {
        this.entityData.set(DATA_SWAG_ID, Integer.valueOf(dyeColor == null ? -1 : dyeColor.getId()));
    }

    @Nullable
    private static DyeColor getDyeColor(ItemStack itemStack) {
        Block byItem = Block.byItem(itemStack.getItem());
        if (byItem instanceof WoolCarpetBlock) {
            return ((WoolCarpetBlock) byItem).getColor();
        }
        return null;
    }

    @Nullable
    public DyeColor getSwag() {
        int intValue = ((Integer) this.entityData.get(DATA_SWAG_ID)).intValue();
        if (intValue == -1) {
            return null;
        }
        return DyeColor.byId(intValue);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public int getMaxTemper() {
        return 30;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        return animal != this && (animal instanceof Llama) && canParent() && ((Llama) animal).canParent();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob
    public Llama getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Llama makeBabyLlama = makeBabyLlama();
        setOffspringAttributes(agableMob, makeBabyLlama);
        Llama llama = (Llama) agableMob;
        int nextInt = this.random.nextInt(Math.max(getStrength(), llama.getStrength())) + 1;
        if (this.random.nextFloat() < 0.03f) {
            nextInt++;
        }
        makeBabyLlama.setStrength(nextInt);
        makeBabyLlama.setVariant(this.random.nextBoolean() ? getVariant() : llama.getVariant());
        return makeBabyLlama;
    }

    protected Llama makeBabyLlama() {
        return EntityType.LLAMA.create(this.level);
    }

    private void spit(LivingEntity var1) {
        LlamaSpit var2 = new LlamaSpit(this.level, this);
        double var3 = var1.getX() - this.getX();
        double var5 = var1.getY(0.3333333333333333) - var2.getY();
        double var7 = var1.getZ() - this.getZ();
        float var9 = Mth.sqrt(var3 * var3 + var7 * var7) * 0.2F;
        var2.shoot(var3, var5 + (double)var9, var7, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        }

        this.level.addFreshEntity(var2);
        this.didSpit = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDidSpit(boolean z) {
        this.didSpit = z;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        int calculateFallDamage = calculateFallDamage(f, f2);
        if (calculateFallDamage <= 0) {
            return false;
        }
        if (f >= 6.0f) {
            hurt(DamageSource.FALL, calculateFallDamage);
            if (isVehicle()) {
                Iterator<Entity> it = getIndirectPassengers().iterator();
                while (it.hasNext()) {
                    it.next().hurt(DamageSource.FALL, calculateFallDamage);
                }
            }
        }
        playBlockFallSound();
        return true;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(Llama llama) {
        this.caravanHead = llama;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override // net.minecraft.world.entity.PathfinderMob
    protected double followLeashSpeed() {
        return 2.0d;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void followMommy() {
        if (!inCaravan() && isBaby()) {
            super.followMommy();
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public boolean canEatGrass() {
        return false;
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        spit(livingEntity);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.75d * getEyeHeight(), getBbWidth() * 0.5d);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Llama$LlamaHurtByTargetGoal.class */
    static class LlamaHurtByTargetGoal extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama llama) {
            super(llama, new Class[0]);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            if (this.mob instanceof Llama) {
                Llama llama = (Llama) this.mob;
                if (llama.didSpit) {
                    llama.setDidSpit(false);
                    return false;
                }
            }
            return super.canContinueToUse();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Llama$LlamaAttackWolfGoal.class */
    static class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama llama) {
            super(llama, Wolf.class, 16, false, true, livingEntity -> {
                return !((Wolf) livingEntity).isTame();
            });
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25d;
        }
    }
}
