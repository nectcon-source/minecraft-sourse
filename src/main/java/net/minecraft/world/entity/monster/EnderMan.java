package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.IntRange;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/EnderMan.class */
public class EnderMan extends Monster implements NeutralMob {
    private int lastStareSound;
    private int targetChangeTime;
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.15000000596046448d, AttributeModifier.Operation.ADDITION);
    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STARED_AT = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<LivingEntity> ENDERMITE_SELECTOR = livingEntity -> {
        return (livingEntity instanceof Endermite) && ((Endermite) livingEntity).isPlayerSpawned();
    };
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    public EnderMan(EntityType<? extends EnderMan> entityType, Level level) {
        super(entityType, level);
        this.lastStareSound = Integer.MIN_VALUE;
        this.maxUpStep = 1.0f;
        setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EndermanFreezeWhenLookedAt(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0d, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0d, 0.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new EndermanLeaveBlockGoal(this));
        this.goalSelector.addGoal(11, new EndermanTakeBlockGoal(this));
        this.targetSelector.addGoal(1, new EndermanLookForPlayerGoal(this, this::isAngryAt));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Endermite.class, 10, true, false, ENDERMITE_SELECTOR));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.ATTACK_DAMAGE, 7.0d).add(Attributes.FOLLOW_RANGE, 64.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    public void setTarget(@Nullable LivingEntity livingEntity) {
        super.setTarget(livingEntity);
        AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
        if (livingEntity == null) {
            this.targetChangeTime = 0;
            this.entityData.set(DATA_CREEPY, false);
            this.entityData.set(DATA_STARED_AT, false);
            attribute.removeModifier(SPEED_MODIFIER_ATTACKING);
            return;
        }
        this.targetChangeTime = this.tickCount;
        this.entityData.set(DATA_CREEPY, true);
        if (!attribute.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            attribute.addTransientModifier(SPEED_MODIFIER_ATTACKING);
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CARRY_STATE, Optional.empty());
        this.entityData.define(DATA_CREEPY, false);
        this.entityData.define(DATA_STARED_AT, false);
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setRemainingPersistentAngerTime(int i) {
        this.remainingPersistentAngerTime = i;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void playStareSound() {
        if (this.tickCount >= this.lastStareSound + 400) {
            this.lastStareSound = this.tickCount;
            if (!isSilent()) {
                this.level.playLocalSound(getX(), getEyeY(), getZ(), SoundEvents.ENDERMAN_STARE, getSoundSource(), 2.5f, 1.0f, false);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_CREEPY.equals(entityDataAccessor) && hasBeenStaredAt() && this.level.isClientSide) {
            playStareSound();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        BlockState carriedBlock = getCarriedBlock();
        if (carriedBlock != null) {
            compoundTag.put("carriedBlockState", NbtUtils.writeBlockState(carriedBlock));
        }
        addPersistentAngerSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        BlockState blockState = null;
        if (compoundTag.contains("carriedBlockState", 10)) {
            blockState = NbtUtils.readBlockState(compoundTag.getCompound("carriedBlockState"));
            if (blockState.isAir()) {
                blockState = null;
            }
        }
        setCarriedBlock(blockState);
        readPersistentAngerSaveData((ServerLevel) this.level, compoundTag);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isLookingAtMe(Player player) {
        if (player.inventory.armor.get(3).getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
            return false;
        }
        Vec3 normalize = player.getViewVector(1.0f).normalize();
        Vec3 vec3 = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        if (normalize.dot(vec3.normalize()) > 1.0d - (0.025d / vec3.length())) {
            return player.canSee(this);
        }
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 2.55f;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.level.isClientSide) {
            for (int i = 0; i < 2; i++) {
                this.level.addParticle(ParticleTypes.PORTAL, getRandomX(0.5d), getRandomY() - 0.25d, getRandomZ(0.5d), (this.random.nextDouble() - 0.5d) * 2.0d, -this.random.nextDouble(), (this.random.nextDouble() - 0.5d) * 2.0d);
            }
        }
        this.jumping = false;
        if (!this.level.isClientSide) {
            updatePersistentAnger((ServerLevel) this.level, true);
        }
        super.aiStep();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        if (this.level.isDay() && this.tickCount >= this.targetChangeTime + 600) {
            float brightness = getBrightness();
            if (brightness > 0.5f && this.level.canSeeSky(blockPosition()) && this.random.nextFloat() * 30.0f < (brightness - 0.4f) * 2.0f) {
                setTarget(null);
                teleport();
            }
        }
        super.customServerAiStep();
    }

    protected boolean teleport() {
        if (this.level.isClientSide() || !isAlive()) {
            return false;
        }
        return teleport(getX() + ((this.random.nextDouble() - 0.5d) * 64.0d), getY() + (this.random.nextInt(64) - 32), getZ() + ((this.random.nextDouble() - 0.5d) * 64.0d));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean teleportTowards(Entity entity) {
        Vec3 normalize = new Vec3(getX() - entity.getX(), getY(0.5d) - entity.getEyeY(), getZ() - entity.getZ()).normalize();
        return teleport((getX() + ((this.random.nextDouble() - 0.5d) * 8.0d)) - (normalize.x * 16.0d), (getY() + (this.random.nextInt(16) - 8)) - (normalize.y * 16.0d), (getZ() + ((this.random.nextDouble() - 0.5d) * 8.0d)) - (normalize.z * 16.0d));
    }

    private boolean teleport(double d, double d2, double d3) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(d, d2, d3);
        while (mutableBlockPos.getY() > 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().blocksMotion()) {
            mutableBlockPos.move(Direction.DOWN);
        }
        BlockState blockState = this.level.getBlockState(mutableBlockPos);
        boolean blocksMotion = blockState.getMaterial().blocksMotion();
        boolean m85is = blockState.getFluidState().is(FluidTags.WATER);
        if (!blocksMotion || m85is) {
            return false;
        }
        boolean randomTeleport = randomTeleport(d, d2, d3, true);
        if (randomTeleport && !isSilent()) {
            this.level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0f, 1.0f);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
        return randomTeleport;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        BlockState carriedBlock = getCarriedBlock();
        if (carriedBlock != null) {
            spawnAtLocation(carriedBlock.getBlock());
        }
    }

    public void setCarriedBlock(@Nullable BlockState blockState) {
        this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(blockState));
    }

    @Nullable
    public BlockState getCarriedBlock() {
        return (BlockState) ((Optional) this.entityData.get(DATA_CARRY_STATE)).orElse(null);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (damageSource instanceof IndirectEntityDamageSource) {
            for (int i = 0; i < 64; i++) {
                if (teleport()) {
                    return true;
                }
            }
            return false;
        }
        boolean hurt = super.hurt(damageSource, f);
        if (!this.level.isClientSide() && !(damageSource.getEntity() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
            teleport();
        }
        return hurt;
    }

    public boolean isCreepy() {
        return ((Boolean) this.entityData.get(DATA_CREEPY)).booleanValue();
    }

    public boolean hasBeenStaredAt() {
        return ((Boolean) this.entityData.get(DATA_STARED_AT)).booleanValue();
    }

    public void setBeingStaredAt() {
        this.entityData.set(DATA_STARED_AT, true);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || getCarriedBlock() != null;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/EnderMan$EndermanLookForPlayerGoal.class */
    static class EndermanLookForPlayerGoal extends NearestAttackableTargetGoal<Player> {
        private final EnderMan enderman;
        private Player pendingTarget;
        private int aggroTime;
        private int teleportTime;
        private final TargetingConditions startAggroTargetConditions;
        private final TargetingConditions continueAggroTargetConditions;

        public EndermanLookForPlayerGoal(EnderMan enderMan, @Nullable Predicate<LivingEntity> predicate) {
            super(enderMan, Player.class, 10, false, false, predicate);
            this.continueAggroTargetConditions = new TargetingConditions().allowUnseeable();
            this.enderman = enderMan;
            this.startAggroTargetConditions = new TargetingConditions().range(getFollowDistance()).selector(livingEntity -> {
                return enderMan.isLookingAtMe((Player) livingEntity);
            });
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            this.pendingTarget = this.enderman.level.getNearestPlayer(this.startAggroTargetConditions, this.enderman);
            return this.pendingTarget != null;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.aggroTime = 5;
            this.teleportTime = 0;
            this.enderman.setBeingStaredAt();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.pendingTarget = null;
            super.stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            if (this.pendingTarget != null) {
                if (!this.enderman.isLookingAtMe(this.pendingTarget)) {
                    return false;
                }
                this.enderman.lookAt(this.pendingTarget, 10.0f, 10.0f);
                return true;
            }
            if (this.target != null && this.continueAggroTargetConditions.test(this.enderman, this.target)) {
                return true;
            }
            return super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.enderman.getTarget() == null) {
                super.setTarget(null);
            }
            if (this.pendingTarget != null) {
                int i = this.aggroTime - 1;
                this.aggroTime = i;
                if (i <= 0) {
                    this.target = this.pendingTarget;
                    this.pendingTarget = null;
                    super.start();
                    return;
                }
                return;
            }
            if (this.target != null && !this.enderman.isPassenger()) {
                if (this.enderman.isLookingAtMe((Player) this.target)) {
                    if (this.target.distanceToSqr(this.enderman) < 16.0d) {
                        this.enderman.teleport();
                    }
                    this.teleportTime = 0;
                } else if (this.target.distanceToSqr(this.enderman) > 256.0d) {
                    int i2 = this.teleportTime;
                    this.teleportTime = i2 + 1;
                    if (i2 >= 30 && this.enderman.teleportTowards(this.target)) {
                        this.teleportTime = 0;
                    }
                }
            }
            super.tick();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/EnderMan$EndermanFreezeWhenLookedAt.class */
    static class EndermanFreezeWhenLookedAt extends Goal {
        private final EnderMan enderman;
        private LivingEntity target;

        public EndermanFreezeWhenLookedAt(EnderMan enderMan) {
            this.enderman = enderMan;
            setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            this.target = this.enderman.getTarget();
            if ((this.target instanceof Player) && this.target.distanceToSqr(this.enderman) <= 256.0d) {
                return this.enderman.isLookingAtMe((Player) this.target);
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.enderman.getNavigation().stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/EnderMan$EndermanLeaveBlockGoal.class */
    static class EndermanLeaveBlockGoal extends Goal {
        private final EnderMan enderman;

        public EndermanLeaveBlockGoal(EnderMan enderMan) {
            this.enderman = enderMan;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.enderman.getCarriedBlock() != null && this.enderman.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.enderman.getRandom().nextInt(2000) == 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            Random random = this.enderman.getRandom();
            Level level = this.enderman.level;
            BlockPos blockPos = new BlockPos(Mth.floor((this.enderman.getX() - 1.0d) + (random.nextDouble() * 2.0d)), Mth.floor(this.enderman.getY() + (random.nextDouble() * 2.0d)), Mth.floor((this.enderman.getZ() - 1.0d) + (random.nextDouble() * 2.0d)));
            BlockState blockState = level.getBlockState(blockPos);
            BlockPos below = blockPos.below();
            BlockState blockState2 = level.getBlockState(below);
            BlockState carriedBlock = this.enderman.getCarriedBlock();
            if (carriedBlock == null) {
                return;
            }
            BlockState updateFromNeighbourShapes = Block.updateFromNeighbourShapes(carriedBlock, this.enderman.level, blockPos);
            if (canPlaceBlock(level, blockPos, updateFromNeighbourShapes, blockState, blockState2, below)) {
                level.setBlock(blockPos, updateFromNeighbourShapes, 3);
                this.enderman.setCarriedBlock(null);
            }
        }

        private boolean canPlaceBlock(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockPos blockPos2) {
            return blockState2.isAir() && !blockState3.isAir() && !blockState3.is(Blocks.BEDROCK) && blockState3.isCollisionShapeFullBlock(level, blockPos2) && blockState.canSurvive(level, blockPos) && level.getEntities(this.enderman, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos))).isEmpty();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/EnderMan$EndermanTakeBlockGoal.class */
    static class EndermanTakeBlockGoal extends Goal {
        private final EnderMan enderman;

        public EndermanTakeBlockGoal(EnderMan enderMan) {
            this.enderman = enderMan;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.enderman.getCarriedBlock() == null && this.enderman.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.enderman.getRandom().nextInt(20) == 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            Random random = this.enderman.getRandom();
            Level level = this.enderman.level;
            int floor = Mth.floor((this.enderman.getX() - 2.0d) + (random.nextDouble() * 4.0d));
            int floor2 = Mth.floor(this.enderman.getY() + (random.nextDouble() * 3.0d));
            int floor3 = Mth.floor((this.enderman.getZ() - 2.0d) + (random.nextDouble() * 4.0d));
            BlockPos blockPos = new BlockPos(floor, floor2, floor3);
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            boolean equals = level.clip(new ClipContext(new Vec3(Mth.floor(this.enderman.getX()) + 0.5d, floor2 + 0.5d, Mth.floor(this.enderman.getZ()) + 0.5d), new Vec3(floor + 0.5d, floor2 + 0.5d, floor3 + 0.5d), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.enderman)).getBlockPos().equals(blockPos);
            if (block.is(BlockTags.ENDERMAN_HOLDABLE) && equals) {
                level.removeBlock(blockPos, false);
                this.enderman.setCarriedBlock(blockState.getBlock().defaultBlockState());
            }
        }
    }
}
