package net.minecraft.world.entity.animal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Ocelot.class */
public class Ocelot extends Animal {
    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
    private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(Ocelot.class, EntityDataSerializers.BOOLEAN);
    private OcelotAvoidEntityGoal<Player> ocelotAvoidPlayersGoal;
    private OcelotTemptGoal temptGoal;

    public Ocelot(EntityType<? extends Ocelot> entityType, Level level) {
        super(entityType, level);
        reassessTrustingGoals();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isTrusting() {
        return ((Boolean) this.entityData.get(DATA_TRUSTING)).booleanValue();
    }

    private void setTrusting(boolean z) {
        this.entityData.set(DATA_TRUSTING, Boolean.valueOf(z));
        reassessTrustingGoals();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Trusting", isTrusting());
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setTrusting(compoundTag.getBoolean("Trusting"));
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRUSTING, false);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.temptGoal = new OcelotTemptGoal(this, 0.6d, TEMPT_INGREDIENT, true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(7, new LeapAtTargetGoal(this, 0.3f));
        this.goalSelector.addGoal(8, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(9, new BreedGoal(this, 0.8d));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8d, 1.0000001E-5f));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Chicken.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));
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

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return !isTrusting() && this.tickCount > 2400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0d).add(Attributes.MOVEMENT_SPEED, 0.30000001192092896d).add(Attributes.ATTACK_DAMAGE, 3.0d);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.OCELOT_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public int getAmbientSoundInterval() {
        return 900;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.OCELOT_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.OCELOT_DEATH;
    }

    private float getAttackDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        return entity.hurt(DamageSource.mobAttack(this), getAttackDamage());
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if ((this.temptGoal == null || this.temptGoal.isRunning()) && !isTrusting() && isFood(itemInHand) && player.distanceToSqr(this) < 9.0d) {
            usePlayerItem(player, itemInHand);
            if (!this.level.isClientSide) {
                if (this.random.nextInt(3) == 0) {
                    setTrusting(true);
                    spawnTrustingParticles(true);
                    this.level.broadcastEntityEvent(this, (byte) 41);
                } else {
                    spawnTrustingParticles(false);
                    this.level.broadcastEntityEvent(this, (byte) 40);
                }
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 41) {
            spawnTrustingParticles(true);
        } else if (b == 40) {
            spawnTrustingParticles(false);
        } else {
            super.handleEntityEvent(b);
        }
    }

    private void spawnTrustingParticles(boolean z) {
        ParticleOptions particleOptions = ParticleTypes.HEART;
        if (!z) {
            particleOptions = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 7; i++) {
            this.level.addParticle(particleOptions, getRandomX(1.0d), getRandomY() + 0.5d, getRandomZ(1.0d), this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d);
        }
    }

    protected void reassessTrustingGoals() {
        if (this.ocelotAvoidPlayersGoal == null) {
            this.ocelotAvoidPlayersGoal = new OcelotAvoidEntityGoal<>(this, Player.class, 16.0f, 0.8d, 1.33d);
        }
        this.goalSelector.removeGoal(this.ocelotAvoidPlayersGoal);
        if (!isTrusting()) {
            this.goalSelector.addGoal(4, this.ocelotAvoidPlayersGoal);
        }
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Ocelot getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return EntityType.OCELOT.create(serverLevel);
    }

    @Override // net.minecraft.world.entity.animal.Animal
    public boolean isFood(ItemStack itemStack) {
        return TEMPT_INGREDIENT.test(itemStack);
    }

    public static boolean checkOcelotSpawnRules(EntityType<Ocelot> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return random.nextInt(3) != 0;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        if (levelReader.isUnobstructed(this) && !levelReader.containsAnyLiquid(getBoundingBox())) {
            BlockPos blockPosition = blockPosition();
            if (blockPosition.getY() < levelReader.getSeaLevel()) {
                return false;
            }
            BlockState blockState = levelReader.getBlockState(blockPosition.below());
            if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(BlockTags.LEAVES)) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData(1.0f);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.5f * getEyeHeight(), getBbWidth() * 0.4f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Ocelot$OcelotAvoidEntityGoal.class */
    static class OcelotAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Ocelot ocelot;


        public OcelotAvoidEntityGoal(Ocelot var1, Class<T> var2, float var3, double var4, double var6) {
            super(var1, var2, var3, var4, var6, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.ocelot = var1;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.ocelot.isTrusting() && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.AvoidEntityGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return !this.ocelot.isTrusting() && super.canContinueToUse();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Ocelot$OcelotTemptGoal.class */
    static class OcelotTemptGoal extends TemptGoal {
        private final Ocelot ocelot;

        public OcelotTemptGoal(Ocelot ocelot, double d, Ingredient ingredient, boolean z) {
            super(ocelot, d, ingredient, z);
            this.ocelot = ocelot;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.TemptGoal
        protected boolean canScare() {
            return super.canScare() && !this.ocelot.isTrusting();
        }
    }
}
