package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vindicator.class */
public class Vindicator extends AbstractIllager {
    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = difficulty -> {
        return difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
    };
    private boolean isJohnny;

    public Vindicator(EntityType<? extends Vindicator> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new VindicatorBreakDoorGoal(this));
        this.goalSelector.addGoal(2, new AbstractIllager.RaiderOpenDoorGoal(this));
        this.goalSelector.addGoal(3, new Raider.HoldGroundAttackGoal(this, 10.0f));
        this.goalSelector.addGoal(4, new VindicatorMeleeAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new VindicatorJohnnyAttackGoal(this));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6d));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vindicator$VindicatorMeleeAttackGoal.class */
    class VindicatorMeleeAttackGoal extends MeleeAttackGoal {
        public VindicatorMeleeAttackGoal(Vindicator vindicator) {
            super(vindicator, 1.0d, false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            if (this.mob.getVehicle() instanceof Ravager) {
                float bbWidth = this.mob.getVehicle().getBbWidth() - 0.1f;
                return (bbWidth * 2.0f * bbWidth * 2.0f) + livingEntity.getBbWidth();
            }
            return super.getAttackReachSqr(livingEntity);
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        if (!isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
            ((GroundPathNavigation) getNavigation()).setCanOpenDoors(((ServerLevel) this.level).isRaided(blockPosition()));
        }
        super.customServerAiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.3499999940395355d).add(Attributes.FOLLOW_RANGE, 12.0d).add(Attributes.MAX_HEALTH, 24.0d).add(Attributes.ATTACK_DAMAGE, 5.0d);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.isJohnny) {
            compoundTag.putBoolean("Johnny", true);
        }
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (isAggressive()) {
            return AbstractIllager.IllagerArmPose.ATTACKING;
        }
        if (isCelebrating()) {
            return AbstractIllager.IllagerArmPose.CELEBRATING;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Johnny", 99)) {
            this.isJohnny = compoundTag.getBoolean("Johnny");
        }
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData finalizeSpawn = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        populateDefaultEquipmentSlots(difficultyInstance);
        populateDefaultEquipmentEnchantments(difficultyInstance);
        return finalizeSpawn;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        if (getCurrentRaid() == null) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }
        return (entity instanceof LivingEntity) && ((LivingEntity) entity).getMobType() == MobType.ILLAGER && getTeam() == null && entity.getTeam() == null;
    }

    @Override // net.minecraft.world.entity.Entity
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        if (!this.isJohnny && component != null && component.getString().equals("Johnny")) {
            this.isJohnny = true;
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VINDICATOR_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.VINDICATOR_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VINDICATOR_HURT;
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public void applyRaidBuffs(int i, boolean z) {
        ItemStack itemStack = new ItemStack(Items.IRON_AXE);
        Raid currentRaid = getCurrentRaid();
        int i2 = 1;
        if (i > currentRaid.getNumGroups(Difficulty.NORMAL)) {
            i2 = 2;
        }
        if (this.random.nextFloat() <= currentRaid.getEnchantOdds()) {
            Map<Enchantment, Integer> newHashMap = Maps.newHashMap();
            newHashMap.put(Enchantments.SHARPNESS, Integer.valueOf(i2));
            EnchantmentHelper.setEnchantments(newHashMap, itemStack);
        }
        setItemSlot(EquipmentSlot.MAINHAND, itemStack);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vindicator$VindicatorBreakDoorGoal.class */
    static class VindicatorBreakDoorGoal extends BreakDoorGoal {
        public VindicatorBreakDoorGoal(Mob mob) {
            super(mob, 6, Vindicator.DOOR_BREAKING_PREDICATE);
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.BreakDoorGoal, net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return ((Vindicator) this.mob).hasActiveRaid() && super.canContinueToUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.BreakDoorGoal, net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            Vindicator vindicator = (Vindicator) this.mob;
            return vindicator.hasActiveRaid() && vindicator.random.nextInt(10) == 0 && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.BreakDoorGoal, net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vindicator$VindicatorJohnnyAttackGoal.class */
    static class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
        public VindicatorJohnnyAttackGoal(Vindicator vindicator) {
            super(vindicator, LivingEntity.class, 0, true, true, (v0) -> {
                return v0.attackable();
            });
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return ((Vindicator) this.mob).isJohnny && super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }
}
