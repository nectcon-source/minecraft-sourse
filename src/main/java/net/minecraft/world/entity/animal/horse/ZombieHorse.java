package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/ZombieHorse.class */
public class ZombieHorse extends AbstractHorse {
    public ZombieHorse(EntityType<? extends ZombieHorse> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0d).add(Attributes.MOVEMENT_SPEED, 0.20000000298023224d);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void randomizeAttributes() {
        getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateRandomJumpStrength());
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        super.getHurtSound(damageSource);
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob
    @Nullable
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return EntityType.ZOMBIE_HORSE.create(serverLevel);
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

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void addBehaviourGoals() {
    }
}
