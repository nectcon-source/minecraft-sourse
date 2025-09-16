package net.minecraft.world.entity.monster;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.IntRange;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/ZombifiedPiglin.class */
public class ZombifiedPiglin extends Zombie implements NeutralMob {
    private int playFirstAngerSoundIn;
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    private int ticksUntilNextAlert;
    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05d, AttributeModifier.Operation.ADDITION);
    private static final IntRange FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final IntRange ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);

    public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> entityType, Level level) {
        super(entityType, level);
        setPathfindingMalus(BlockPathTypes.LAVA, 8.0f);
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return isBaby() ? -0.05d : -0.45d;
    }

//    @Override // net.minecraft.world.entity.monster.Zombie
//    protected void addBehaviourGoals() {
//        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0d, false));
//        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0d));
//        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
//        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, this::isAngryAt));
//        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal(this, true));
//    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0d, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (LivingEntity target) -> this.isAngryAt(target)));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal(this, true));
    }

//

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0d).add(Attributes.MOVEMENT_SPEED, 0.23000000417232513d).add(Attributes.ATTACK_DAMAGE, 5.0d);
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected boolean convertsInWater() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
        if (isAngry()) {
            if (!isBaby() && !attribute.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                attribute.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
            maybePlayFirstAngerSound();
        } else if (attribute.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            attribute.removeModifier(SPEED_MODIFIER_ATTACKING);
        }
        updatePersistentAnger((ServerLevel) this.level, true);
        if (getTarget() != null) {
            maybeAlertOthers();
        }
        if (isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }
        super.customServerAiStep();
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            this.playFirstAngerSoundIn--;
            if (this.playFirstAngerSoundIn == 0) {
                playAngerSound();
            }
        }
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            this.ticksUntilNextAlert--;
            return;
        }
        if (getSensing().canSee(getTarget())) {
            alertOthers();
        }
        this.ticksUntilNextAlert = ALERT_INTERVAL.randomValue(this.random);
    }

    private void alertOthers() {
        double attributeValue = getAttributeValue(Attributes.FOLLOW_RANGE);
        this.level.getLoadedEntitiesOfClass(ZombifiedPiglin.class, AABB.unitCubeFromLowerCorner(position()).inflate(attributeValue, 10.0d, attributeValue)).stream().filter(zombifiedPiglin -> {
            return zombifiedPiglin != this;
        }).filter(zombifiedPiglin2 -> {
            return zombifiedPiglin2.getTarget() == null;
        }).filter(zombifiedPiglin3 -> {
            return !zombifiedPiglin3.isAlliedTo(getTarget());
        }).forEach(zombifiedPiglin4 -> {
            zombifiedPiglin4.setTarget(getTarget());
        });
    }

    private void playAngerSound() {
        playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, getSoundVolume() * 2.0f, getVoicePitch() * 1.8f);
    }

    @Override // net.minecraft.world.entity.Mob
    public void setTarget(@Nullable LivingEntity livingEntity) {
        if (getTarget() == null && livingEntity != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.randomValue(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.randomValue(this.random);
        }
        if (livingEntity instanceof Player) {
            setLastHurtByPlayer((Player) livingEntity);
        }
        super.setTarget(livingEntity);
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    public static boolean checkZombifiedPiglinSpawnRules(EntityType<ZombifiedPiglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return (levelAccessor.getDifficulty() == Difficulty.PEACEFUL || levelAccessor.getBlockState(blockPos.below()).getBlock() == Blocks.NETHER_WART_BLOCK) ? false : true;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this) && !levelReader.containsAnyLiquid(getBoundingBox());
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        addPersistentAngerSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        readPersistentAngerSaveData((ServerLevel) this.level, compoundTag);
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public void setRemainingPersistentAngerTime(int i) {
        this.remainingPersistentAngerTime = i;
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected void randomizeReinforcementsChance() {
        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0d);
    }

    @Override // net.minecraft.world.entity.NeutralMob
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override // net.minecraft.world.entity.monster.Monster
    public boolean isPreventingPlayerRest(Player player) {
        return isAngryAt(player);
    }
}
