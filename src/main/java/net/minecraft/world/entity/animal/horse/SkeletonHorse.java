package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/SkeletonHorse.class */
public class SkeletonHorse extends AbstractHorse {
    private final SkeletonTrapGoal skeletonTrapGoal;
    private boolean isTrap;
    private int trapTime;

    public SkeletonHorse(EntityType<? extends SkeletonHorse> entityType, Level level) {
        super(entityType, level);
        this.skeletonTrapGoal = new SkeletonTrapGoal(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0d).add(Attributes.MOVEMENT_SPEED, 0.20000000298023224d);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void randomizeAttributes() {
        getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateRandomJumpStrength());
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void addBehaviourGoals() {
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        if (isEyeInFluid(FluidTags.WATER)) {
            return SoundEvents.SKELETON_HORSE_AMBIENT_WATER;
        }
        return SoundEvents.SKELETON_HORSE_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.SKELETON_HORSE_DEATH;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        super.getHurtSound(damageSource);
        return SoundEvents.SKELETON_HORSE_HURT;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSound() {
        if (this.onGround) {
            if (isVehicle()) {
                this.gallopSoundCounter++;
                if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                    return SoundEvents.SKELETON_HORSE_GALLOP_WATER;
                }
                if (this.gallopSoundCounter <= 5) {
                    return SoundEvents.SKELETON_HORSE_STEP_WATER;
                }
            } else {
                return SoundEvents.SKELETON_HORSE_STEP_WATER;
            }
        }
        return SoundEvents.SKELETON_HORSE_SWIM;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playSwimSound(float f) {
        if (this.onGround) {
            super.playSwimSound(0.3f);
        } else {
            super.playSwimSound(Math.min(0.1f, f * 25.0f));
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void playJumpSound() {
        if (isInWater()) {
            playSound(SoundEvents.SKELETON_HORSE_JUMP_WATER, 0.4f, 1.0f);
        } else {
            super.playJumpSound();
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.1875d;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (isTrap()) {
            int i = this.trapTime;
            this.trapTime = i + 1;
            if (i >= 18000) {
                remove();
            }
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("SkeletonTrap", isTrap());
        compoundTag.putInt("SkeletonTrapTime", this.trapTime);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setTrap(compoundTag.getBoolean("SkeletonTrap"));
        this.trapTime = compoundTag.getInt("SkeletonTrapTime");
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean rideableUnderWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getWaterSlowDown() {
        return 0.96f;
    }

    public boolean isTrap() {
        return this.isTrap;
    }

    public void setTrap(boolean z) {
        if (z == this.isTrap) {
            return;
        }
        this.isTrap = z;
        if (z) {
            this.goalSelector.addGoal(1, this.skeletonTrapGoal);
        } else {
            this.goalSelector.removeGoal(this.skeletonTrapGoal);
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob
    @Nullable
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return EntityType.SKELETON_HORSE.create(serverLevel);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!isTamed()) {
            return InteractionResult.PASS;
        }
        if (isBaby()) {
            return super.mobInteract(player, interactionHand);
        }
        if (player.isSecondaryUseActive()) {
            openInventory(player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (isVehicle()) {
            return super.mobInteract(player, interactionHand);
        }
        if (!itemInHand.isEmpty()) {
            if (itemInHand.getItem() == Items.SADDLE && !isSaddled()) {
                openInventory(player);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            InteractionResult interactLivingEntity = itemInHand.interactLivingEntity(player, this, interactionHand);
            if (interactLivingEntity.consumesAction()) {
                return interactLivingEntity;
            }
        }
        doPlayerRide(player);
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }
}
