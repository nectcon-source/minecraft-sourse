package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Slime.class */
public class Slime extends Mob implements Enemy {
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
    public float targetSquish;
    public float squish;
    public float oSquish;
    private boolean wasOnGround;

    public Slime(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SlimeMoveControl(this);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new Slime.SlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new Slime.SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new Slime.SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new Slime.SlimeKeepOnJumpingGoal(this));
        this.targetSelector
                .addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, var1 -> Math.abs(var1.getY() - this.getY()) <= 4.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_SIZE, 1);
    }

    protected void setSize(int i, boolean z) {
        this.entityData.set(ID_SIZE, Integer.valueOf(i));
        reapplyPosition();
        refreshDimensions();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(i * i);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2f + (0.1f * i));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(i);
        if (z) {
            setHealth(getMaxHealth());
        }
        this.xpReward = i;
    }

    public int getSize() {
        return ((Integer) this.entityData.get(ID_SIZE)).intValue();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Size", getSize() - 1);
        compoundTag.putBoolean("wasOnGround", this.wasOnGround);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        int i = compoundTag.getInt("Size");
        if (i < 0) {
            i = 0;
        }
        setSize(i + 1, false);
        super.readAdditionalSaveData(compoundTag);
        this.wasOnGround = compoundTag.getBoolean("wasOnGround");
    }

    public boolean isTiny() {
        return getSize() <= 1;
    }

    protected ParticleOptions getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override // net.minecraft.world.entity.Mob
    protected boolean shouldDespawnInPeaceful() {
        return getSize() > 0;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        this.squish += (this.targetSquish - this.squish) * 0.5f;
        this.oSquish = this.squish;
        super.tick();
        if (this.onGround && !this.wasOnGround) {
            int size = getSize();
            for (int i = 0; i < size * 8; i++) {
                float nextFloat = this.random.nextFloat() * 6.2831855f;
                float nextFloat2 = (this.random.nextFloat() * 0.5f) + 0.5f;
                this.level.addParticle(getParticleType(), getX() + (Mth.sin(nextFloat) * size * 0.5f * nextFloat2), getY(), getZ() + (Mth.cos(nextFloat) * size * 0.5f * nextFloat2), 0.0d, 0.0d, 0.0d);
            }
            playSound(getSquishSound(), getSoundVolume(), (((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f) / 0.8f);
            this.targetSquish = -0.5f;
        } else if (!this.onGround && this.wasOnGround) {
            this.targetSquish = 1.0f;
        }
        this.wasOnGround = this.onGround;
        decreaseSquish();
    }

    protected void decreaseSquish() {
        this.targetSquish *= 0.6f;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override // net.minecraft.world.entity.Entity
    public void refreshDimensions() {
        double x = getX();
        double y = getY();
        double z = getZ();
        super.refreshDimensions();
        setPos(x, y, z);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ID_SIZE.equals(entityDataAccessor)) {
            refreshDimensions();
            this.yRot = this.yHeadRot;
            this.yBodyRot = this.yHeadRot;
            if (isInWater() && this.random.nextInt(20) == 0) {
                doWaterSplashEffect();
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.Entity
    public EntityType<? extends Slime> getType() {
        return (EntityType<? extends Slime>) super.getType();
    }

    @Override // net.minecraft.world.entity.Entity
    public void remove() {
        int size = getSize();
        if (!this.level.isClientSide && size > 1 && isDeadOrDying()) {
            Component customName = getCustomName();
            boolean isNoAi = isNoAi();
            float f = size / 4.0f;
            int i = size / 2;
            int nextInt = 2 + this.random.nextInt(3);
            for (int i2 = 0; i2 < nextInt; i2++) {
                float f2 = ((i2 % 2) - 0.5f) * f;
                float f3 = ((i2 / 2) - 0.5f) * f;
                Slime create = getType().create(this.level);
                if (isPersistenceRequired()) {
                    create.setPersistenceRequired();
                }
                create.setCustomName(customName);
                create.setNoAi(isNoAi);
                create.setInvulnerable(isInvulnerable());
                create.setSize(i, true);
                create.moveTo(getX() + f2, getY() + 0.5d, getZ() + f3, this.random.nextFloat() * 360.0f, 0.0f);
                this.level.addFreshEntity(create);
            }
        }
        super.remove();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void push(Entity entity) {
        super.push(entity);
        if ((entity instanceof IronGolem) && isDealsDamage()) {
            dealDamage((LivingEntity) entity);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void playerTouch(Player player) {
        if (isDealsDamage()) {
            dealDamage(player);
        }
    }

    protected void dealDamage(LivingEntity livingEntity) {
        if (isAlive()) {
            int size = getSize();
            if (distanceToSqr(livingEntity) < 0.6d * size * 0.6d * size && canSee(livingEntity) && livingEntity.hurt(DamageSource.mobAttack(this), getAttackDamage())) {
                playSound(SoundEvents.SLIME_ATTACK, 1.0f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f);
                doEnchantDamageEffects(this, livingEntity);
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.625f * entityDimensions.height;
    }

    protected boolean isDealsDamage() {
        return !isTiny() && isEffectiveAi();
    }

    protected float getAttackDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (isTiny()) {
            return SoundEvents.SLIME_HURT_SMALL;
        }
        return SoundEvents.SLIME_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        if (isTiny()) {
            return SoundEvents.SLIME_DEATH_SMALL;
        }
        return SoundEvents.SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        if (isTiny()) {
            return SoundEvents.SLIME_SQUISH_SMALL;
        }
        return SoundEvents.SLIME_SQUISH;
    }

    @Override // net.minecraft.world.entity.Mob
    protected ResourceLocation getDefaultLootTable() {
        return getSize() == 1 ? getType().getDefaultLootTable() : BuiltInLootTables.EMPTY;
    }

    public static boolean checkSlimeSpawnRules(EntityType<Slime> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        if (levelAccessor.getDifficulty() != Difficulty.PEACEFUL) {
            if (Objects.equals(levelAccessor.getBiomeName(blockPos), Optional.of(Biomes.SWAMP)) && blockPos.getY() > 50 && blockPos.getY() < 70 && random.nextFloat() < 0.5f && random.nextFloat() < levelAccessor.getMoonBrightness() && levelAccessor.getMaxLocalRawBrightness(blockPos) <= random.nextInt(8)) {
                return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
            }
            if (!(levelAccessor instanceof WorldGenLevel)) {
                return false;
            }
            ChunkPos chunkPos = new ChunkPos(blockPos);
            boolean z = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, ((WorldGenLevel) levelAccessor).getSeed(), 987234911L).nextInt(10) == 0;
            if (random.nextInt(10) == 0 && z && blockPos.getY() < 40) {
                return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.LivingEntity
    public float getSoundVolume() {
        return 0.4f * getSize();
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadXRot() {
        return 0;
    }

    protected boolean doPlayJumpSound() {
        return getSize() > 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void jumpFromGround() {
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x, getJumpPower(), deltaMovement.z);
        this.hasImpulse = true;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        int nextInt = this.random.nextInt(3);
        if (nextInt < 2 && this.random.nextFloat() < 0.5f * difficultyInstance.getSpecialMultiplier()) {
            nextInt++;
        }
        setSize(1 << nextInt, true);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Slime$SlimeMoveControl.class */
    static class SlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final Slime slime;
        private boolean isAggressive;

        public SlimeMoveControl(Slime slime) {
            super(slime);
            this.slime = slime;
            this.yRot = (180.0f * slime.yRot) / 3.1415927f;
        }

        public void setDirection(float f, boolean z) {
            this.yRot = f;
            this.isAggressive = z;
        }

        public void setWantedMovement(double d) {
            this.speedModifier = d;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            this.mob.yRot = rotlerp(this.mob.yRot, this.yRot, 90.0f);
            this.mob.yHeadRot = this.mob.yRot;
            this.mob.yBodyRot = this.mob.yRot;
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0f);
                return;
            }
            this.operation = MoveControl.Operation.WAIT;
            if (this.mob.isOnGround()) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                int i = this.jumpDelay;
                this.jumpDelay = i - 1;
                if (i <= 0) {
                    this.jumpDelay = this.slime.getJumpDelay();
                    if (this.isAggressive) {
                        this.jumpDelay /= 3;
                    }
                    this.slime.getJumpControl().jump();
                    if (this.slime.doPlayJumpSound()) {
                        this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
                        return;
                    }
                    return;
                }
                this.slime.xxa = 0.0f;
                this.slime.zza = 0.0f;
                this.mob.setSpeed(0.0f);
                return;
            }
            this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getSoundPitch() {
        return (((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f) * (isTiny() ? 1.4f : 0.8f);
    }

    protected SoundEvent getJumpSound() {
        return isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(0.255f * getSize());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Slime$SlimeAttackGoal.class */
    static class SlimeAttackGoal extends Goal {
        private final Slime slime;
        private int growTiredTimer;

        public SlimeAttackGoal(Slime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if ((target instanceof Player) && ((Player) target).abilities.invulnerable) {
                return false;
            }
            return this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.growTiredTimer = 300;
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if ((target instanceof Player) && ((Player) target).abilities.invulnerable) {
                return false;
            }
            int i = this.growTiredTimer - 1;
            this.growTiredTimer = i;
            if (i <= 0) {
                return false;
            }
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.slime.lookAt(this.slime.getTarget(), 10.0f, 10.0f);
            ((SlimeMoveControl) this.slime.getMoveControl()).setDirection(this.slime.yRot, this.slime.isDealsDamage());
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Slime$SlimeRandomDirectionGoal.class */
    static class SlimeRandomDirectionGoal extends Goal {
        private final Slime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(Slime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.slime.getTarget() == null && (this.slime.onGround || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && (this.slime.getMoveControl() instanceof SlimeMoveControl);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            int i = this.nextRandomizeTime - 1;
            this.nextRandomizeTime = i;
            if (i <= 0) {
                this.nextRandomizeTime = 40 + this.slime.getRandom().nextInt(60);
                this.chosenDegrees = this.slime.getRandom().nextInt(360);
            }
            ((SlimeMoveControl) this.slime.getMoveControl()).setDirection(this.chosenDegrees, false);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Slime$SlimeFloatGoal.class */
    static class SlimeFloatGoal extends Goal {
        private final Slime slime;

        public SlimeFloatGoal(Slime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            slime.getNavigation().setCanFloat(true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && (this.slime.getMoveControl() instanceof SlimeMoveControl);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8f) {
                this.slime.getJumpControl().jump();
            }
            ((SlimeMoveControl) this.slime.getMoveControl()).setWantedMovement(1.2d);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Slime$SlimeKeepOnJumpingGoal.class */
    static class SlimeKeepOnJumpingGoal extends Goal {
        private final Slime slime;

        public SlimeKeepOnJumpingGoal(Slime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            ((SlimeMoveControl) this.slime.getMoveControl()).setWantedMovement(1.0d);
        }
    }
}
