package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/wither/WitherBoss.class */
public class WitherBoss extends Monster implements PowerableMob, RangedAttackMob {
    private final float[] xRotHeads;
    private final float[] yRotHeads;
    private final float[] xRotOHeads;
    private final float[] yRotOHeads;
    private final int[] nextHeadUpdate;
    private final int[] idleHeadUpdates;
    private int destroyBlocksTick;
    private final ServerBossEvent bossEvent;
    private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = livingEntity -> {
        return livingEntity.getMobType() != MobType.UNDEAD && livingEntity.attackable();
    };
    private static final TargetingConditions TARGETING_CONDITIONS = new TargetingConditions().range(20.0d).selector(LIVING_ENTITY_SELECTOR);

    public WitherBoss(EntityType<? extends WitherBoss> entityType, Level level) {
        super(entityType, level);
        this.xRotHeads = new float[2];
        this.yRotHeads = new float[2];
        this.xRotOHeads = new float[2];
        this.yRotOHeads = new float[2];
        this.nextHeadUpdate = new int[2];
        this.idleHeadUpdates = new int[2];
        this.bossEvent = (ServerBossEvent) new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
        setHealth(getMaxHealth());
        getNavigation().setCanFloat(true);
        this.xpReward = 50;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WitherDoNothingGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0d, 40, 20.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Mob.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_A, 0);
        this.entityData.define(DATA_TARGET_B, 0);
        this.entityData.define(DATA_TARGET_C, 0);
        this.entityData.define(DATA_ID_INV, 0);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Invul", getInvulnerableTicks());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setInvulnerableTicks(compoundTag.getInt("Invul"));
        if (hasCustomName()) {
            this.bossEvent.setName(getDisplayName());
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        this.bossEvent.setName(getDisplayName());
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        Entity entity;
        Vec3 multiply = getDeltaMovement().multiply(1.0d, 0.6d, 1.0d);
        if (!this.level.isClientSide && getAlternativeTarget(0) > 0 && (entity = this.level.getEntity(getAlternativeTarget(0))) != null) {
            double d = multiply.y;
            if (getY() < entity.getY() || (!isPowered() && getY() < entity.getY() + 5.0d)) {
                double max = Math.max(0.0d, d);
                d = max + (0.3d - (max * 0.6000000238418579d));
            }
            multiply = new Vec3(multiply.x, d, multiply.z);
            Vec3 vec3 = new Vec3(entity.getX() - getX(), 0.0d, entity.getZ() - getZ());
            if (getHorizontalDistanceSqr(vec3) > 9.0d) {
                Vec3 normalize = vec3.normalize();
                multiply = multiply.add((normalize.x * 0.3d) - (multiply.x * 0.6d), 0.0d, (normalize.z * 0.3d) - (multiply.z * 0.6d));
            }
        }
        setDeltaMovement(multiply);
        if (getHorizontalDistanceSqr(multiply) > 0.05d) {
            this.yRot = (((float) Mth.atan2(multiply.z, multiply.x)) * 57.295776f) - 90.0f;
        }
        super.aiStep();
        for (int i = 0; i < 2; i++) {
            this.yRotOHeads[i] = this.yRotHeads[i];
            this.xRotOHeads[i] = this.xRotHeads[i];
        }
        for (int i2 = 0; i2 < 2; i2++) {
            int alternativeTarget = getAlternativeTarget(i2 + 1);
            Entity entity2 = null;
            if (alternativeTarget > 0) {
                entity2 = this.level.getEntity(alternativeTarget);
            }
            if (entity2 != null) {
                double headX = getHeadX(i2 + 1);
                double headY = getHeadY(i2 + 1);
                double headZ = getHeadZ(i2 + 1);
                double x = entity2.getX() - headX;
                double eyeY = entity2.getEyeY() - headY;
                double z = entity2.getZ() - headZ;
                double sqrt = Mth.sqrt((x * x) + (z * z));
                float atan2 = ((float) (Mth.atan2(z, x) * 57.2957763671875d)) - 90.0f;
                this.xRotHeads[i2] = rotlerp(this.xRotHeads[i2], (float) (-(Mth.atan2(eyeY, sqrt) * 57.2957763671875d)), 40.0f);
                this.yRotHeads[i2] = rotlerp(this.yRotHeads[i2], atan2, 10.0f);
            } else {
                this.yRotHeads[i2] = rotlerp(this.yRotHeads[i2], this.yBodyRot, 10.0f);
            }
        }
        boolean isPowered = isPowered();
        for (int i3 = 0; i3 < 3; i3++) {
            double headX2 = getHeadX(i3);
            double headY2 = getHeadY(i3);
            double headZ2 = getHeadZ(i3);
            this.level.addParticle(ParticleTypes.SMOKE, headX2 + (this.random.nextGaussian() * 0.30000001192092896d), headY2 + (this.random.nextGaussian() * 0.30000001192092896d), headZ2 + (this.random.nextGaussian() * 0.30000001192092896d), 0.0d, 0.0d, 0.0d);
            if (isPowered && this.level.random.nextInt(4) == 0) {
                this.level.addParticle(ParticleTypes.ENTITY_EFFECT, headX2 + (this.random.nextGaussian() * 0.30000001192092896d), headY2 + (this.random.nextGaussian() * 0.30000001192092896d), headZ2 + (this.random.nextGaussian() * 0.30000001192092896d), 0.699999988079071d, 0.699999988079071d, 0.5d);
            }
        }
        if (getInvulnerableTicks() > 0) {
            for (int i4 = 0; i4 < 3; i4++) {
                this.level.addParticle(ParticleTypes.ENTITY_EFFECT, getX() + this.random.nextGaussian(), getY() + (this.random.nextFloat() * 3.3f), getZ() + this.random.nextGaussian(), 0.699999988079071d, 0.699999988079071d, 0.8999999761581421d);
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        if (getInvulnerableTicks() > 0) {
            int invulnerableTicks = getInvulnerableTicks() - 1;
            if (invulnerableTicks <= 0) {
                this.level.explode(this, getX(), getEyeY(), getZ(), 7.0f, false, this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
                if (!isSilent()) {
                    this.level.globalLevelEvent(1023, blockPosition(), 0);
                }
            }
            setInvulnerableTicks(invulnerableTicks);
            if (this.tickCount % 10 == 0) {
                heal(10.0f);
                return;
            }
            return;
        }
        super.customServerAiStep();
        for (int i = 1; i < 3; i++) {
            if (this.tickCount >= this.nextHeadUpdate[i - 1]) {
                this.nextHeadUpdate[i - 1] = this.tickCount + 10 + this.random.nextInt(10);
                if (this.level.getDifficulty() == Difficulty.NORMAL || this.level.getDifficulty() == Difficulty.HARD) {
                    int[] iArr = this.idleHeadUpdates;
                    int i2 = i - 1;
                    int i3 = iArr[i2];
                    iArr[i2] = i3 + 1;
                    if (i3 > 15) {
                        performRangedAttack(i + 1, Mth.nextDouble(this.random, getX() - 10.0d, getX() + 10.0d), Mth.nextDouble(this.random, getY() - 5.0d, getY() + 5.0d), Mth.nextDouble(this.random, getZ() - 10.0d, getZ() + 10.0d), true);
                        this.idleHeadUpdates[i - 1] = 0;
                    }
                }
                int alternativeTarget = getAlternativeTarget(i);
                if (alternativeTarget > 0) {
                    Entity entity = this.level.getEntity(alternativeTarget);
                    if (entity == null || !entity.isAlive() || distanceToSqr(entity) > 900.0d || !canSee(entity)) {
                        setAlternativeTarget(i, 0);
                    } else if ((entity instanceof Player) && ((Player) entity).abilities.invulnerable) {
                        setAlternativeTarget(i, 0);
                    } else {
                        performRangedAttack(i + 1, (LivingEntity) entity);
                        this.nextHeadUpdate[i - 1] = this.tickCount + 40 + this.random.nextInt(20);
                        this.idleHeadUpdates[i - 1] = 0;
                    }
                } else {
                    List<LivingEntity> nearbyEntities = this.level.getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, getBoundingBox().inflate(20.0d, 8.0d, 20.0d));
                    int i4 = 0;
                    while (true) {
                        if (i4 < 10 && !nearbyEntities.isEmpty()) {
                            LivingEntity livingEntity = nearbyEntities.get(this.random.nextInt(nearbyEntities.size()));
                            if (livingEntity != this && livingEntity.isAlive() && canSee(livingEntity)) {
                                if (livingEntity instanceof Player) {
                                    if (!((Player) livingEntity).abilities.invulnerable) {
                                        setAlternativeTarget(i, livingEntity.getId());
                                    }
                                } else {
                                    setAlternativeTarget(i, livingEntity.getId());
                                }
                            } else {
                                nearbyEntities.remove(livingEntity);
                                i4++;
                            }
                        }
                    }
                }
            }
        }
        if (getTarget() != null) {
            setAlternativeTarget(0, getTarget().getId());
        } else {
            setAlternativeTarget(0, 0);
        }
        if (this.destroyBlocksTick > 0) {
            this.destroyBlocksTick--;
            if (this.destroyBlocksTick == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                int floor = Mth.floor(getY());
                int floor2 = Mth.floor(getX());
                int floor3 = Mth.floor(getZ());
                boolean z = false;
                for (int i5 = -1; i5 <= 1; i5++) {
                    for (int i6 = -1; i6 <= 1; i6++) {
                        for (int i7 = 0; i7 <= 3; i7++) {
                            BlockPos blockPos = new BlockPos(floor2 + i5, floor + i7, floor3 + i6);
                            if (canDestroy(this.level.getBlockState(blockPos))) {
                                z = this.level.destroyBlock(blockPos, true, this) || z;
                            }
                        }
                    }
                }
                if (z) {
                    this.level.levelEvent(null, 1022, blockPosition(), 0);
                }
            }
        }
        if (this.tickCount % 20 == 0) {
            heal(1.0f);
        }
        this.bossEvent.setPercent(getHealth() / getMaxHealth());
    }

    public static boolean canDestroy(BlockState blockState) {
        return (blockState.isAir() || BlockTags.WITHER_IMMUNE.contains(blockState.getBlock())) ? false : true;
    }

    public void makeInvulnerable() {
        setInvulnerableTicks(220);
        setHealth(getMaxHealth() / 3.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
    }

    @Override // net.minecraft.world.entity.Entity
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
    }

    @Override // net.minecraft.world.entity.Entity
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    private double getHeadX(int i) {
        if (i <= 0) {
            return getX();
        }
        return getX() + (Mth.cos((this.yBodyRot + (180 * (i - 1))) * 0.017453292f) * 1.3d);
    }

    private double getHeadY(int i) {
        if (i <= 0) {
            return getY() + 3.0d;
        }
        return getY() + 2.2d;
    }

    private double getHeadZ(int i) {
        if (i <= 0) {
            return getZ();
        }
        return getZ() + (Mth.sin((this.yBodyRot + (180 * (i - 1))) * 0.017453292f) * 1.3d);
    }

    private float rotlerp(float f, float f2, float f3) {
        float wrapDegrees = Mth.wrapDegrees(f2 - f);
        if (wrapDegrees > f3) {
            wrapDegrees = f3;
        }
        if (wrapDegrees < (-f3)) {
            wrapDegrees = -f3;
        }
        return f + wrapDegrees;
    }

    private void performRangedAttack(int i, LivingEntity livingEntity) {
        performRangedAttack(i, livingEntity.getX(), livingEntity.getY() + (livingEntity.getEyeHeight() * 0.5d), livingEntity.getZ(), i == 0 && this.random.nextFloat() < 0.001f);
    }

    private void performRangedAttack(int i, double d, double d2, double d3, boolean z) {
        if (!isSilent()) {
            this.level.levelEvent(null, 1024, blockPosition(), 0);
        }
        double headX = getHeadX(i);
        double headY = getHeadY(i);
        double headZ = getHeadZ(i);
        WitherSkull witherSkull = new WitherSkull(this.level, this, d - headX, d2 - headY, d3 - headZ);
        witherSkull.setOwner(this);
        if (z) {
            witherSkull.setDangerous(true);
        }
        witherSkull.setPosRaw(headX, headY, headZ);
        this.level.addFreshEntity(witherSkull);
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        performRangedAttack(0, livingEntity);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource) || damageSource == DamageSource.DROWN || (damageSource.getEntity() instanceof WitherBoss)) {
            return false;
        }
        if (getInvulnerableTicks() > 0 && damageSource != DamageSource.OUT_OF_WORLD) {
            return false;
        }
        if (isPowered() && (damageSource.getDirectEntity() instanceof AbstractArrow)) {
            return false;
        }
        Entity entity = damageSource.getEntity();
        if (entity != null && !(entity instanceof Player) && (entity instanceof LivingEntity) && ((LivingEntity) entity).getMobType() == getMobType()) {
            return false;
        }
        if (this.destroyBlocksTick <= 0) {
            this.destroyBlocksTick = 20;
        }
        for (int i = 0; i < this.idleHeadUpdates.length; i++) {
            int[] iArr = this.idleHeadUpdates;
            int i2 = i;
            iArr[i2] = iArr[i2] + 3;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        ItemEntity spawnAtLocation = spawnAtLocation(Items.NETHER_STAR);
        if (spawnAtLocation != null) {
            spawnAtLocation.setExtendedLifetime();
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.Entity
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && shouldDespawnInPeaceful()) {
            remove();
        } else {
            this.noActionTime = 0;
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean addEffect(MobEffectInstance mobEffectInstance) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0d).add(Attributes.MOVEMENT_SPEED, 0.6000000238418579d).add(Attributes.FOLLOW_RANGE, 40.0d).add(Attributes.ARMOR, 4.0d);
    }

    public float getHeadYRot(int i) {
        return this.yRotHeads[i];
    }

    public float getHeadXRot(int i) {
        return this.xRotHeads[i];
    }

    public int getInvulnerableTicks() {
        return ((Integer) this.entityData.get(DATA_ID_INV)).intValue();
    }

    public void setInvulnerableTicks(int i) {
        this.entityData.set(DATA_ID_INV, Integer.valueOf(i));
    }

    public int getAlternativeTarget(int i) {
        return ((Integer) this.entityData.get(DATA_TARGETS.get(i))).intValue();
    }

    public void setAlternativeTarget(int i, int i2) {
        this.entityData.set(DATA_TARGETS.get(i), Integer.valueOf(i2));
    }

    @Override // net.minecraft.world.entity.PowerableMob
    public boolean isPowered() {
        return getHealth() <= getMaxHealth() / 2.0f;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canChangeDimensions() {
        return false;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/wither/WitherBoss$WitherDoNothingGoal.class */
    class WitherDoNothingGoal extends Goal {
        public WitherDoNothingGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return WitherBoss.this.getInvulnerableTicks() > 0;
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }
}
