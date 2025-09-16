package net.minecraft.world.entity.monster;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Witch.class */
public class Witch extends Raider implements RangedAttackMob {
    private static final UUID SPEED_MODIFIER_DRINKING_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(SPEED_MODIFIER_DRINKING_UUID, "Drinking speed penalty", -0.25d, AttributeModifier.Operation.ADDITION);
    private static final EntityDataAccessor<Boolean> DATA_USING_ITEM = SynchedEntityData.defineId(Witch.class, EntityDataSerializers.BOOLEAN);
    private int usingTime;
    private NearestHealableRaiderTargetGoal<Raider> healRaidersGoal;
    private NearestAttackableWitchTargetGoal<Player> attackPlayersGoal;

    public Witch(EntityType<? extends Witch> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.healRaidersGoal = new NearestHealableRaiderTargetGoal<>(this, Raider.class, true, livingEntity -> {
            return (livingEntity == null || !hasActiveRaid() || livingEntity.getType() == EntityType.WITCH) ? false : true;
        });
        this.attackPlayersGoal = new NearestAttackableWitchTargetGoal<>(this, Player.class, 10, true, false, null);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0d, 60, 10.0f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class));
        this.targetSelector.addGoal(2, this.healRaidersGoal);
        this.targetSelector.addGoal(3, this.attackPlayersGoal);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(DATA_USING_ITEM, false);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITCH_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    public void setUsingItem(boolean z) {
        getEntityData().set(DATA_USING_ITEM, Boolean.valueOf(z));
    }

    public boolean isDrinkingPotion() {
        return ((Boolean) getEntityData().get(DATA_USING_ITEM)).booleanValue();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 26.0d).add(Attributes.MOVEMENT_SPEED, 0.25d);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        List<MobEffectInstance> mobEffects;
        if (!this.level.isClientSide && isAlive()) {
            this.healRaidersGoal.decrementCooldown();
            if (this.healRaidersGoal.getCooldown() <= 0) {
                this.attackPlayersGoal.setCanAttack(true);
            } else {
                this.attackPlayersGoal.setCanAttack(false);
            }
            if (isDrinkingPotion()) {
                int i = this.usingTime;
                this.usingTime = i - 1;
                if (i <= 0) {
                    setUsingItem(false);
                    ItemStack mainHandItem = getMainHandItem();
                    setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    if (mainHandItem.getItem() == Items.POTION && (mobEffects = PotionUtils.getMobEffects(mainHandItem)) != null) {
                        Iterator<MobEffectInstance> it = mobEffects.iterator();
                        while (it.hasNext()) {
                            addEffect(new MobEffectInstance(it.next()));
                        }
                    }
                    getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
                }
            } else {
                Potion potion = null;
                if (this.random.nextFloat() < 0.15f && isEyeInFluid(FluidTags.WATER) && !hasEffect(MobEffects.WATER_BREATHING)) {
                    potion = Potions.WATER_BREATHING;
                } else if (this.random.nextFloat() < 0.15f && ((isOnFire() || (getLastDamageSource() != null && getLastDamageSource().isFire())) && !hasEffect(MobEffects.FIRE_RESISTANCE))) {
                    potion = Potions.FIRE_RESISTANCE;
                } else if (this.random.nextFloat() < 0.05f && getHealth() < getMaxHealth()) {
                    potion = Potions.HEALING;
                } else if (this.random.nextFloat() < 0.5f && getTarget() != null && !hasEffect(MobEffects.MOVEMENT_SPEED) && getTarget().distanceToSqr(this) > 121.0d) {
                    potion = Potions.SWIFTNESS;
                }
                if (potion != null) {
                    setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
                    this.usingTime = getMainHandItem().getUseDuration();
                    setUsingItem(true);
                    if (!isSilent()) {
                        this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_DRINK, getSoundSource(), 1.0f, 0.8f + (this.random.nextFloat() * 0.4f));
                    }
                    AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
                    attribute.removeModifier(SPEED_MODIFIER_DRINKING);
                    attribute.addTransientModifier(SPEED_MODIFIER_DRINKING);
                }
            }
            if (this.random.nextFloat() < 7.5E-4f) {
                this.level.broadcastEntityEvent(this, (byte) 15);
            }
        }
        super.aiStep();
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public SoundEvent getCelebrateSound() {
        return SoundEvents.WITCH_CELEBRATE;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 15) {
            for (int i = 0; i < this.random.nextInt(35) + 10; i++) {
                this.level.addParticle(ParticleTypes.WITCH, getX() + (this.random.nextGaussian() * 0.12999999523162842d), getBoundingBox().maxY + 0.5d + (this.random.nextGaussian() * 0.12999999523162842d), getZ() + (this.random.nextGaussian() * 0.12999999523162842d), 0.0d, 0.0d, 0.0d);
            }
            return;
        }
        super.handleEntityEvent(b);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getDamageAfterMagicAbsorb(DamageSource damageSource, float f) {
        float damageAfterMagicAbsorb = super.getDamageAfterMagicAbsorb(damageSource, f);
        if (damageSource.getEntity() == this) {
            damageAfterMagicAbsorb = 0.0f;
        }
        if (damageSource.isMagic()) {
            damageAfterMagicAbsorb = (float) (damageAfterMagicAbsorb * 0.15d);
        }
        return damageAfterMagicAbsorb;
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        if (isDrinkingPotion()) {
            return;
        }
        Vec3 deltaMovement = livingEntity.getDeltaMovement();
        double x = (livingEntity.getX() + deltaMovement.x) - getX();
        double eyeY = (livingEntity.getEyeY() - 1.100000023841858d) - getY();
        double z = (livingEntity.getZ() + deltaMovement.z) - getZ();
        float sqrt = Mth.sqrt((x * x) + (z * z));
        Potion potion = Potions.HARMING;
        if (livingEntity instanceof Raider) {
            if (livingEntity.getHealth() <= 4.0f) {
                potion = Potions.HEALING;
            } else {
                potion = Potions.REGENERATION;
            }
            setTarget(null);
        } else if (sqrt >= 8.0f && !livingEntity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            potion = Potions.SLOWNESS;
        } else if (livingEntity.getHealth() >= 8.0f && !livingEntity.hasEffect(MobEffects.POISON)) {
            potion = Potions.POISON;
        } else if (sqrt <= 3.0f && !livingEntity.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25f) {
            potion = Potions.WEAKNESS;
        }
        ThrownPotion thrownPotion = new ThrownPotion(this.level, this);
        thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        thrownPotion.xRot -= -20.0f;
        thrownPotion.shoot(x, eyeY + (sqrt * 0.2f), z, 0.75f, 8.0f);
        if (!isSilent()) {
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_THROW, getSoundSource(), 1.0f, 0.8f + (this.random.nextFloat() * 0.4f));
        }
        this.level.addFreshEntity(thrownPotion);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 1.62f;
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public void applyRaidBuffs(int i, boolean z) {
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster
    public boolean canBeLeader() {
        return false;
    }
}
