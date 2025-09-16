package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned.class */
public class Drowned extends Zombie implements RangedAttackMob {
    private boolean searchingForLand;
    protected final WaterBoundPathNavigation waterNavigation;
    protected final GroundPathNavigation groundNavigation;

    public Drowned(EntityType<? extends Drowned> entityType, Level level) {
        super(entityType, level);
        this.maxUpStep = 1.0f;
        this.moveControl = new DrownedMoveControl(this);
        setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
        this.waterNavigation = new WaterBoundPathNavigation(this, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new DrownedGoToWaterGoal(this, 1.0d));
        this.goalSelector.addGoal(2, new DrownedTridentAttackGoal(this, 1.0d, 40, 10.0f));
        this.goalSelector.addGoal(2, new DrownedAttackGoal(this, 1.0d, false));
        this.goalSelector.addGoal(5, new DrownedGoToBeachGoal(this, 1.0d));
        this.goalSelector.addGoal(6, new DrownedSwimUpGoal(this, 1.0d, this.level.getSeaLevel()));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0d));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Drowned.class).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::okTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData finalizeSpawn = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        if (getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && this.random.nextFloat() < 0.03f) {
            setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
            this.handDropChances[EquipmentSlot.OFFHAND.getIndex()] = 2.0f;
        }
        return finalizeSpawn;
    }

    public static boolean checkDrownedSpawnRules(EntityType<Drowned> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        Optional<ResourceKey<Biome>> biomeName = serverLevelAccessor.getBiomeName(blockPos);
        boolean z = serverLevelAccessor.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(serverLevelAccessor, blockPos, random) && (mobSpawnType == MobSpawnType.SPAWNER || serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER));
        return (Objects.equals(biomeName, Optional.of(Biomes.RIVER)) || Objects.equals(biomeName, Optional.of(Biomes.FROZEN_RIVER))) ? random.nextInt(15) == 0 && z : random.nextInt(40) == 0 && isDeepEnoughToSpawn(serverLevelAccessor, blockPos) && z;
    }

    private static boolean isDeepEnoughToSpawn(LevelAccessor levelAccessor, BlockPos blockPos) {
        return blockPos.getY() < levelAccessor.getSeaLevel() - 5;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected boolean supportsBreakDoorGoal() {
        return false;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (isInWater()) {
            return SoundEvents.DROWNED_AMBIENT_WATER;
        }
        return SoundEvents.DROWNED_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (isInWater()) {
            return SoundEvents.DROWNED_HURT_WATER;
        }
        return SoundEvents.DROWNED_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        if (isInWater()) {
            return SoundEvents.DROWNED_DEATH_WATER;
        }
        return SoundEvents.DROWNED_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected SoundEvent getStepSound() {
        return SoundEvents.DROWNED_STEP;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSound() {
        return SoundEvents.DROWNED_SWIM;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        if (this.random.nextFloat() > 0.9d) {
            if (this.random.nextInt(16) < 10) {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            } else {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.getItem() == Items.NAUTILUS_SHELL) {
            return false;
        }
        if (itemStack2.getItem() == Items.TRIDENT) {
            return itemStack.getItem() == Items.TRIDENT && itemStack.getDamageValue() < itemStack2.getDamageValue();
        }
        if (itemStack.getItem() == Items.TRIDENT) {
            return true;
        }
        return super.canReplaceCurrentItem(itemStack, itemStack2);
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected boolean convertsInWater() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    public boolean okTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null) {
            if (this.level.isDay() && !livingEntity.isInWater()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPushedByFluid() {
        return !isSwimming();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean wantsToSwim() {
        if (this.searchingForLand) {
            return true;
        }
        LivingEntity target = getTarget();
        if (target != null && target.isInWater()) {
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        if (isEffectiveAi() && isInWater() && wantsToSwim()) {
            moveRelative(0.01f, vec3);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9d));
            return;
        }
        super.travel(vec3);
    }

    @Override // net.minecraft.world.entity.Entity
    public void updateSwimming() {
        if (!this.level.isClientSide) {
            if (isEffectiveAi() && isInWater() && wantsToSwim()) {
                this.navigation = this.waterNavigation;
                setSwimming(true);
            } else {
                this.navigation = this.groundNavigation;
                setSwimming(false);
            }
        }
    }

    protected boolean closeToNextPos() {
        BlockPos target;
        Path path = getNavigation().getPath();
        if (path != null && (target = path.getTarget()) != null && distanceToSqr(target.getX(), target.getY(), target.getZ()) < 4.0d) {
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        ThrownTrident var3 = new ThrownTrident(this.level, this, new ItemStack(Items.TRIDENT));
        double var4x = livingEntity.getX() - this.getX();
        double var6xx = livingEntity.getY(0.3333333333333333) - var3.getY();
        double var8xxx = livingEntity.getZ() - this.getZ();
        double var10xxxx = (double)Mth.sqrt(var4x * var4x + var8xxx * var8xxx);
      var3.shoot(var4x, var6xx + var10xxxx * 0.2F, var8xxx, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var3);
    }

    public void setSearchingForLand(boolean z) {
        this.searchingForLand = z;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned$DrownedTridentAttackGoal.class */
    static class DrownedTridentAttackGoal extends RangedAttackGoal {
        private final Drowned drowned;

        public DrownedTridentAttackGoal(RangedAttackMob rangedAttackMob, double d, int i, float f) {
            super(rangedAttackMob, d, i, f);
            this.drowned = (Drowned) rangedAttackMob;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RangedAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && this.drowned.getMainHandItem().getItem() == Items.TRIDENT;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            this.drowned.setAggressive(true);
            this.drowned.startUsingItem(InteractionHand.MAIN_HAND);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.RangedAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            super.stop();
            this.drowned.stopUsingItem();
            this.drowned.setAggressive(false);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned$DrownedSwimUpGoal.class */
    static class DrownedSwimUpGoal extends Goal {
        private final Drowned drowned;
        private final double speedModifier;
        private final int seaLevel;
        private boolean stuck;

        public DrownedSwimUpGoal(Drowned drowned, double d, int i) {
            this.drowned = drowned;
            this.speedModifier = d;
            this.seaLevel = i;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.drowned.level.isDay() && this.drowned.isInWater() && this.drowned.getY() < ((double) (this.seaLevel - 2));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return canUse() && !this.stuck;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.drowned.getY() < this.seaLevel - 1) {
                if (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos()) {
                    Vec3 posTowards = RandomPos.getPosTowards(this.drowned, 4, 8, new Vec3(this.drowned.getX(), this.seaLevel - 1, this.drowned.getZ()));
                    if (posTowards == null) {
                        this.stuck = true;
                    } else {
                        this.drowned.getNavigation().moveTo(posTowards.x, posTowards.y, posTowards.z, this.speedModifier);
                    }
                }
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.drowned.setSearchingForLand(true);
            this.stuck = false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.drowned.setSearchingForLand(false);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned$DrownedGoToBeachGoal.class */
    static class DrownedGoToBeachGoal extends MoveToBlockGoal {
        private final Drowned drowned;

        public DrownedGoToBeachGoal(Drowned drowned, double d) {
            super(drowned, d, 8, 2);
            this.drowned = drowned;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && !this.drowned.level.isDay() && this.drowned.isInWater() && this.drowned.getY() >= ((double) (this.drowned.level.getSeaLevel() - 3));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            BlockPos above = blockPos.above();
            if (!levelReader.isEmptyBlock(above) || !levelReader.isEmptyBlock(above.above())) {
                return false;
            }
            return levelReader.getBlockState(blockPos).entityCanStandOn(levelReader, blockPos, this.drowned);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.drowned.setSearchingForLand(false);
            this.drowned.navigation = this.drowned.groundNavigation;
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            super.stop();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned$DrownedGoToWaterGoal.class */
    static class DrownedGoToWaterGoal extends Goal {
        private final PathfinderMob mob;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private final double speedModifier;
        private final Level level;

        public DrownedGoToWaterGoal(PathfinderMob pathfinderMob, double d) {
            this.mob = pathfinderMob;
            this.speedModifier = d;
            this.level = pathfinderMob.level;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            Vec3 waterPos;
            if (!this.level.isDay() || this.mob.isInWater() || (waterPos = getWaterPos()) == null) {
                return false;
            }
            this.wantedX = waterPos.x;
            this.wantedY = waterPos.y;
            this.wantedZ = waterPos.z;
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }

        @Nullable
        private Vec3 getWaterPos() {
            Random random = this.mob.getRandom();
            BlockPos blockPosition = this.mob.blockPosition();
            for (int i = 0; i < 10; i++) {
                BlockPos offset = blockPosition.offset(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);
                if (this.level.getBlockState(offset).is(Blocks.WATER)) {
                    return Vec3.atBottomCenterOf(offset);
                }
            }
            return null;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned$DrownedAttackGoal.class */
    static class DrownedAttackGoal extends ZombieAttackGoal {
        private final Drowned drowned;

        public DrownedAttackGoal(Drowned drowned, double d, boolean z) {
            super(drowned, d, z);
            this.drowned = drowned;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && this.drowned.okTarget(this.drowned.getTarget());
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.drowned.okTarget(this.drowned.getTarget());
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Drowned$DrownedMoveControl.class */
    static class DrownedMoveControl extends MoveControl {
        private final Drowned drowned;

        public DrownedMoveControl(Drowned drowned) {
            super(drowned);
            this.drowned = drowned;
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            LivingEntity target = this.drowned.getTarget();
            if (!this.drowned.wantsToSwim() || !this.drowned.isInWater()) {
                if (!this.drowned.onGround) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0d, -0.008d, 0.0d));
                }
                super.tick();
                return;
            }
            if ((target != null && target.getY() > this.drowned.getY()) || this.drowned.searchingForLand) {
                this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0d, 0.002d, 0.0d));
            }
            if (this.operation != MoveControl.Operation.MOVE_TO || this.drowned.getNavigation().isDone()) {
                this.drowned.setSpeed(0.0f);
                return;
            }
            double x = this.wantedX - this.drowned.getX();
            double y = this.wantedY - this.drowned.getY();
            double z = this.wantedZ - this.drowned.getZ();
            double sqrt = y / Mth.sqrt(((x * x) + (y * y)) + (z * z));
            this.drowned.yRot = rotlerp(this.drowned.yRot, ((float) (Mth.atan2(z, x) * 57.2957763671875d)) - 90.0f, 90.0f);
            this.drowned.yBodyRot = this.drowned.yRot;
            float lerp = Mth.lerp(0.125f, this.drowned.getSpeed(), (float) (this.speedModifier * this.drowned.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            this.drowned.setSpeed(lerp);
            this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(lerp * x * 0.005d, lerp * sqrt * 0.1d, lerp * z * 0.005d));
        }
    }
}
