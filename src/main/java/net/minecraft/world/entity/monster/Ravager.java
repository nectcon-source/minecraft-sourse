package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ravager.class */
public class Ravager extends Raider {
    private static final Predicate<Entity> NO_RAVAGER_AND_ALIVE = entity -> {
        return entity.isAlive() && !(entity instanceof Ravager);
    };
    private int attackTick;
    private int stunnedTick;
    private int roarTick;

    public Ravager(EntityType<? extends Ravager> entityType, Level level) {
        super(entityType, level);
        this.maxUpStep = 1.0f;
        this.xpReward = 20;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new RavagerMeleeAttackGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4d));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, IronGolem.class, true));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void updateControlFlags() {
        boolean z = !(getControllingPassenger() instanceof Mob) || getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
        boolean z2 = !(getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, z);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, z && z2);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, z);
        this.goalSelector.setControlFlag(Goal.Flag.TARGET, z);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0d).add(Attributes.MOVEMENT_SPEED, 0.3d).add(Attributes.KNOCKBACK_RESISTANCE, 0.75d).add(Attributes.ATTACK_DAMAGE, 12.0d).add(Attributes.ATTACK_KNOCKBACK, 1.5d).add(Attributes.FOLLOW_RANGE, 32.0d);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("AttackTick", this.attackTick);
        compoundTag.putInt("StunTick", this.stunnedTick);
        compoundTag.putInt("RoarTick", this.roarTick);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.attackTick = compoundTag.getInt("AttackTick");
        this.stunnedTick = compoundTag.getInt("StunTick");
        this.roarTick = compoundTag.getInt("RoarTick");
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public SoundEvent getCelebrateSound() {
        return SoundEvents.RAVAGER_CELEBRATE;
    }

    @Override // net.minecraft.world.entity.Mob
    protected PathNavigation createNavigation(Level level) {
        return new RavagerNavigation(this, level);
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadYRot() {
        return 45;
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return 2.1d;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeControlledByRider() {
        return !isNoAi() && (getControllingPassenger() instanceof LivingEntity);
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity getControllingPassenger() {
        if (getPassengers().isEmpty()) {
            return null;
        }
        return getPassengers().get(0);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (!isAlive()) {
            return;
        }
        if (isImmobile()) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0d);
        } else {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1d, getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue(), getTarget() != null ? 0.35d : 0.3d));
        }
        if (this.horizontalCollision && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            boolean z = false;
            AABB inflate = getBoundingBox().inflate(0.2d);
            for (BlockPos blockPos : BlockPos.betweenClosed(Mth.floor(inflate.minX), Mth.floor(inflate.minY), Mth.floor(inflate.minZ), Mth.floor(inflate.maxX), Mth.floor(inflate.maxY), Mth.floor(inflate.maxZ))) {
                if (this.level.getBlockState(blockPos).getBlock() instanceof LeavesBlock) {
                    z = this.level.destroyBlock(blockPos, true, this) || z;
                }
            }
            if (!z && this.onGround) {
                jumpFromGround();
            }
        }
        if (this.roarTick > 0) {
            this.roarTick--;
            if (this.roarTick == 10) {
                roar();
            }
        }
        if (this.attackTick > 0) {
            this.attackTick--;
        }
        if (this.stunnedTick > 0) {
            this.stunnedTick--;
            stunEffect();
            if (this.stunnedTick == 0) {
                playSound(SoundEvents.RAVAGER_ROAR, 1.0f, 1.0f);
                this.roarTick = 20;
            }
        }
    }

    private void stunEffect() {
        if (this.random.nextInt(6) == 0) {
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, (getX() - (getBbWidth() * Math.sin(this.yBodyRot * 0.017453292f))) + ((this.random.nextDouble() * 0.6d) - 0.3d), (getY() + getBbHeight()) - 0.3d, getZ() + (getBbWidth() * Math.cos(this.yBodyRot * 0.017453292f)) + ((this.random.nextDouble() * 0.6d) - 0.3d), 0.4980392156862745d, 0.5137254901960784d, 0.5725490196078431d);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean isImmobile() {
        return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canSee(Entity entity) {
        if (this.stunnedTick > 0 || this.roarTick > 0) {
            return false;
        }
        return super.canSee(entity);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void blockedByShield(LivingEntity livingEntity) {
        if (this.roarTick == 0) {
            if (this.random.nextDouble() < 0.5d) {
                this.stunnedTick = 40;
                playSound(SoundEvents.RAVAGER_STUNNED, 1.0f, 1.0f);
                this.level.broadcastEntityEvent(this, (byte) 39);
                livingEntity.push(this);
            } else {
                strongKnockback(livingEntity);
            }
            livingEntity.hurtMarked = true;
        }
    }

    private void roar() {
        if (isAlive()) {
            for (Entity entity : this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0d), NO_RAVAGER_AND_ALIVE)) {
                if (!(entity instanceof AbstractIllager)) {
                    entity.hurt(DamageSource.mobAttack(this), 6.0f);
                }
                strongKnockback(entity);
            }
            Vec3 center = getBoundingBox().getCenter();
            for (int i = 0; i < 40; i++) {
                this.level.addParticle(ParticleTypes.POOF, center.x, center.y, center.z, this.random.nextGaussian() * 0.2d, this.random.nextGaussian() * 0.2d, this.random.nextGaussian() * 0.2d);
            }
        }
    }

    private void strongKnockback(Entity entity) {
        double x = entity.getX() - getX();
        double z = entity.getZ() - getZ();
        double max = Math.max((x * x) + (z * z), 0.001d);
        entity.push((x / max) * 4.0d, 0.2d, (z / max) * 4.0d);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 4) {
            this.attackTick = 10;
            playSound(SoundEvents.RAVAGER_ATTACK, 1.0f, 1.0f);
        } else if (b == 39) {
            this.stunnedTick = 40;
        }
        super.handleEntityEvent(b);
    }

    public int getAttackTick() {
        return this.attackTick;
    }

    public int getStunnedTick() {
        return this.stunnedTick;
    }

    public int getRoarTick() {
        return this.roarTick;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        this.attackTick = 10;
        this.level.broadcastEntityEvent(this, (byte) 4);
        playSound(SoundEvents.RAVAGER_ATTACK, 1.0f, 1.0f);
        return super.doHurtTarget(entity);
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.RAVAGER_STEP, 0.15f, 1.0f);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return !levelReader.containsAnyLiquid(getBoundingBox());
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public void applyRaidBuffs(int i, boolean z) {
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster
    public boolean canBeLeader() {
        return false;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ravager$RavagerMeleeAttackGoal.class */
    class RavagerMeleeAttackGoal extends MeleeAttackGoal {
        public RavagerMeleeAttackGoal() {
            super(Ravager.this, 1.0d, true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            float bbWidth = Ravager.this.getBbWidth() - 0.1f;
            return (bbWidth * 2.0f * bbWidth * 2.0f) + livingEntity.getBbWidth();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ravager$RavagerNavigation.class */
    static class RavagerNavigation extends GroundPathNavigation {
        public RavagerNavigation(Mob mob, Level level) {
            super(mob, level);
        }

        @Override // net.minecraft.world.entity.p000ai.navigation.GroundPathNavigation, net.minecraft.world.entity.p000ai.navigation.PathNavigation
        protected PathFinder createPathFinder(int i) {
            this.nodeEvaluator = new RavagerNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, i);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Ravager$RavagerNodeEvaluator.class */
    static class RavagerNodeEvaluator extends WalkNodeEvaluator {
        private RavagerNodeEvaluator() {
        }

        @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator
        protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, boolean z, boolean z2, BlockPos blockPos, BlockPathTypes blockPathTypes) {
            if (blockPathTypes == BlockPathTypes.LEAVES) {
                return BlockPathTypes.OPEN;
            }
            return super.evaluateBlockPathType(blockGetter, z, z2, blockPos, blockPathTypes);
        }
    }
}
