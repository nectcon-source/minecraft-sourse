package net.minecraft.world.entity.animal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit.class */
public class Rabbit extends Animal {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final ResourceLocation KILLER_BUNNY = new ResourceLocation("killer_bunny");
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;
    private int moreCarrotTicks;

    public Rabbit(EntityType<? extends Rabbit> entityType, Level level) {
        super(entityType, level);
        this.jumpControl = new RabbitJumpControl(this);
        this.moveControl = new RabbitMoveControl(this);
        setSpeedModifier(0.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RabbitPanicGoal(this, 2.2d));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8d));
        this.goalSelector.addGoal(3, new TemptGoal((PathfinderMob) this, 1.0d, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION), false));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal(this, Player.class, 8.0f, 2.2d, 2.2d));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal(this, Wolf.class, 10.0f, 2.2d, 2.2d));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal(this, Monster.class, 4.0f, 2.2d, 2.2d));
        this.goalSelector.addGoal(5, new RaidGardenGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6d));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0f));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getJumpPower() {
        if (this.horizontalCollision) {
            return 0.5f;
        }
        if (this.moveControl.hasWanted() && this.moveControl.getWantedY() > getY() + 0.5d) {
            return 0.5f;
        }
        Path path = this.navigation.getPath();
        if (path != null && !path.isDone() && path.getNextEntityPos(this).y > getY() + 0.5d) {
            return 0.5f;
        }
        if (this.moveControl.getSpeedModifier() <= 0.6d) {
            return 0.2f;
        }
        return 0.3f;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (this.moveControl.getSpeedModifier() > 0.0d && getHorizontalDistanceSqr(getDeltaMovement()) < 0.01d) {
            moveRelative(0.1f, new Vec3(0.0d, 0.0d, 1.0d));
        }
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 1);
        }
    }

    public float getJumpCompletion(float f) {
        if (this.jumpDuration == 0) {
            return 0.0f;
        }
        return (this.jumpTicks + f) / this.jumpDuration;
    }

    public void setSpeedModifier(double d) {
        getNavigation().setSpeedModifier(d);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), d);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void setJumping(boolean z) {
        super.setJumping(z);
        if (z) {
            playSound(getJumpSound(), getSoundVolume(), (((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f) * 0.8f);
        }
    }

    public void startJumping() {
        setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, 0);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public void customServerAiStep() {
        LivingEntity target;
        if (this.jumpDelayTicks > 0) {
            this.jumpDelayTicks--;
        }
        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= this.random.nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }
        if (this.onGround) {
            if (!this.wasOnGround) {
                setJumping(false);
                checkLandingDelay();
            }
            if (getRabbitType() == 99 && this.jumpDelayTicks == 0 && (target = getTarget()) != null && distanceToSqr(target) < 16.0d) {
                facePoint(target.getX(), target.getZ());
                this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), this.moveControl.getSpeedModifier());
                startJumping();
                this.wasOnGround = true;
            }
            RabbitJumpControl rabbitJumpControl = (RabbitJumpControl) this.jumpControl;
            if (!rabbitJumpControl.wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path path = this.navigation.getPath();
                    Vec3 vec3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if (path != null && !path.isDone()) {
                        vec3 = path.getNextEntityPos(this);
                    }
                    facePoint(vec3.x, vec3.z);
                    startJumping();
                }
            } else if (!rabbitJumpControl.canJump()) {
                enableJumpControl();
            }
        }
        this.wasOnGround = this.onGround;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void facePoint(double d, double d2) {
        this.yRot = ((float) (Mth.atan2(d2 - getZ(), d - getX()) * 57.2957763671875d)) - 90.0f;
    }

    private void enableJumpControl() {
        ((RabbitJumpControl) this.jumpControl).setCanJump(true);
    }

    private void disableJumpControl() {
        ((RabbitJumpControl) this.jumpControl).setCanJump(false);
    }

    private void setLandingDelay() {
        if (this.moveControl.getSpeedModifier() < 2.2d) {
            this.jumpDelayTicks = 10;
        } else {
            this.jumpDelayTicks = 1;
        }
    }

    private void checkLandingDelay() {
        setLandingDelay();
        disableJumpControl();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (this.jumpTicks != this.jumpDuration) {
            this.jumpTicks++;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            setJumping(false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("RabbitType", getRabbitType());
        compoundTag.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setRabbitType(compoundTag.getInt("RabbitType"));
        this.moreCarrotTicks = compoundTag.getInt("MoreCarrotTicks");
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.RABBIT_JUMP;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        if (getRabbitType() == 99) {
            playSound(SoundEvents.RABBIT_ATTACK, 1.0f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f);
            return entity.hurt(DamageSource.mobAttack(this), 8.0f);
        }
        return entity.hurt(DamageSource.mobAttack(this), 3.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return getRabbitType() == 99 ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    private boolean isTemptingItem(Item item) {
        return item == Items.CARROT || item == Items.GOLDEN_CARROT || item == Blocks.DANDELION.asItem();
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Rabbit getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Rabbit create = EntityType.RABBIT.create(serverLevel);
        int randomRabbitType = getRandomRabbitType(serverLevel);
        if (this.random.nextInt(20) != 0) {
            if ((agableMob instanceof Rabbit) && this.random.nextBoolean()) {
                randomRabbitType = ((Rabbit) agableMob).getRabbitType();
            } else {
                randomRabbitType = getRabbitType();
            }
        }
        create.setRabbitType(randomRabbitType);
        return create;
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return isTemptingItem(itemStack.getItem());
    }

    public int getRabbitType() {
        return ((Integer) this.entityData.get(DATA_TYPE_ID)).intValue();
    }

    public void setRabbitType(int i) {
        if (i == 99) {
            getAttribute(Attributes.ARMOR).setBaseValue(8.0d);
            this.goalSelector.addGoal(4, new EvilRabbitAttackGoal(this));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Wolf.class, true));
            if (!hasCustomName()) {
                setCustomName(new TranslatableComponent(Util.makeDescriptionId("entity", KILLER_BUNNY)));
            }
        }
        this.entityData.set(DATA_TYPE_ID, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        int randomRabbitType = getRandomRabbitType(serverLevelAccessor);
        if (spawnGroupData instanceof RabbitGroupData) {
            randomRabbitType = ((RabbitGroupData) spawnGroupData).rabbitType;
        } else {
            spawnGroupData = new RabbitGroupData(randomRabbitType);
        }
        setRabbitType(randomRabbitType);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    private int getRandomRabbitType(LevelAccessor levelAccessor) {
        Biome biome = levelAccessor.getBiome(blockPosition());
        int nextInt = this.random.nextInt(100);
        if (biome.getPrecipitation() == Biome.Precipitation.SNOW) {
            return nextInt < 80 ? 1 : 3;
        }
        if (biome.getBiomeCategory() == Biome.BiomeCategory.DESERT) {
            return 4;
        }
        if (nextInt < 50) {
            return 0;
        }
        return nextInt < 90 ? 5 : 2;
    }

    public static boolean checkRabbitSpawnRules(EntityType<Rabbit> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        BlockState blockState = levelAccessor.getBlockState(blockPos.below());
        return (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.SNOW) || blockState.is(Blocks.SAND)) && levelAccessor.getRawBrightness(blockPos, 0) > 8;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$RabbitGroupData.class */
    public static class RabbitGroupData extends AgableMob.AgableMobGroupData {
        public final int rabbitType;

        public RabbitGroupData(int i) {
            super(1.0f);
            this.rabbitType = i;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean wantsMoreFood() {
        return this.moreCarrotTicks == 0;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 1) {
            spawnSprintParticle();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
            return;
        }
        super.handleEntityEvent(b);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.6f * getEyeHeight(), getBbWidth() * 0.4f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$RabbitJumpControl.class */
    public class RabbitJumpControl extends JumpControl {
        private final Rabbit rabbit;
        private boolean canJump;

        public RabbitJumpControl(Rabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        public boolean wantJump() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean z) {
            this.canJump = z;
        }

        @Override // net.minecraft.world.entity.p000ai.control.JumpControl
        public void tick() {
            if (this.jump) {
                this.rabbit.startJumping();
                this.jump = false;
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$RabbitMoveControl.class */
    static class RabbitMoveControl extends MoveControl {
        private final Rabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveControl(Rabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            if (this.rabbit.onGround && !this.rabbit.jumping && !((RabbitJumpControl) this.rabbit.jumpControl).wantJump()) {
                this.rabbit.setSpeedModifier(0.0d);
            } else if (hasWanted()) {
                this.rabbit.setSpeedModifier(this.nextJumpSpeed);
            }
            super.tick();
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void setWantedPosition(double d, double d2, double d3, double d4) {
            if (this.rabbit.isInWater()) {
                d4 = 1.5d;
            }
            super.setWantedPosition(d, d2, d3, d4);
            if (d4 > 0.0d) {
                this.nextJumpSpeed = d4;
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$RabbitAvoidEntityGoal.class */
    static class RabbitAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Rabbit rabbit;

        public RabbitAvoidEntityGoal(Rabbit rabbit, Class<T> cls, float f, double d, double d2) {
            super(rabbit, cls, f, d, d2);
            this.rabbit = rabbit;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.rabbit.getRabbitType() != 99 && super.canUse();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$RaidGardenGoal.class */
    static class RaidGardenGoal extends MoveToBlockGoal {
        private final Rabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public RaidGardenGoal(Rabbit rabbit) {
            super(rabbit, 0.699999988079071d, 16);
            this.rabbit = rabbit;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (this.nextStartTick <= 0) {
                if (!this.rabbit.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    return false;
                }
                this.canRaid = false;
                this.wantsToRaid = this.rabbit.wantsMoreFood();
                this.wantsToRaid = true;
            }
            return super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return this.canRaid && super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            super.tick();
            this.rabbit.getLookControl().setLookAt(this.blockPos.getX() + 0.5d, this.blockPos.getY() + 1, this.blockPos.getZ() + 0.5d, 10.0f, this.rabbit.getMaxHeadXRot());
            if (isReachedTarget()) {
                Level level = this.rabbit.level;
                BlockPos above = this.blockPos.above();
                BlockState blockState = level.getBlockState(above);
                Block block = blockState.getBlock();
                if (this.canRaid && (block instanceof CarrotBlock)) {
                    Integer num = (Integer) blockState.getValue(CarrotBlock.AGE);
                    if (num.intValue() == 0) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                        level.destroyBlock(above, true, this.rabbit);
                    } else {
                        level.setBlock(above, (BlockState) blockState.setValue(CarrotBlock.AGE, Integer.valueOf(num.intValue() - 1)), 2);
                        level.levelEvent(2001, above, Block.getId(blockState));
                    }
                    this.rabbit.moreCarrotTicks = 40;
                }
                this.canRaid = false;
                this.nextStartTick = 10;
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            if (levelReader.getBlockState(blockPos).getBlock() == Blocks.FARMLAND && this.wantsToRaid && !this.canRaid) {
                BlockState blockState = levelReader.getBlockState(blockPos.above());
                Block block = blockState.getBlock();
                if ((block instanceof CarrotBlock) && ((CarrotBlock) block).isMaxAge(blockState)) {
                    this.canRaid = true;
                    return true;
                }
                return false;
            }
            return false;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$RabbitPanicGoal.class */
    static class RabbitPanicGoal extends PanicGoal {
        private final Rabbit rabbit;

        public RabbitPanicGoal(Rabbit rabbit, double d) {
            super(rabbit, d);
            this.rabbit = rabbit;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            super.tick();
            this.rabbit.setSpeedModifier(this.speedModifier);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Rabbit$EvilRabbitAttackGoal.class */
    static class EvilRabbitAttackGoal extends MeleeAttackGoal {
        public EvilRabbitAttackGoal(Rabbit rabbit) {
            super(rabbit, 1.4d, true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            return 4.0f + livingEntity.getBbWidth();
        }
    }
}
