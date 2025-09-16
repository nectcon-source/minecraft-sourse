package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Horse.class */
public class Horse extends AbstractHorse {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);

    public Horse(EntityType<? extends Horse> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void randomizeAttributes() {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(generateRandomMaxHealth());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(generateRandomSpeed());
        getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateRandomJumpStrength());
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", getTypeVariant());
        if (!this.inventory.getItem(1).isEmpty()) {
            compoundTag.put("ArmorItem", this.inventory.getItem(1).save(new CompoundTag()));
        }
    }

    public ItemStack getArmor() {
        return getItemBySlot(EquipmentSlot.CHEST);
    }

    private void setArmor(ItemStack itemStack) {
        setItemSlot(EquipmentSlot.CHEST, itemStack);
        setDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setTypeVariant(compoundTag.getInt("Variant"));
        if (compoundTag.contains("ArmorItem", 10)) {
            ItemStack m66of = ItemStack.of(compoundTag.getCompound("ArmorItem"));
            if (!m66of.isEmpty() && isArmor(m66of)) {
                this.inventory.setItem(1, m66of);
            }
        }
        updateContainerEquipment();
    }

    private void setTypeVariant(int i) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, Integer.valueOf(i));
    }

    private int getTypeVariant() {
        return ((Integer) this.entityData.get(DATA_ID_TYPE_VARIANT)).intValue();
    }

    private void setVariantAndMarkings(Variant variant, Markings markings) {
        setTypeVariant((variant.getId() & 255) | ((markings.getId() << 8) & 65280));
    }

    public Variant getVariant() {
        return Variant.byId(getTypeVariant() & 255);
    }

    public Markings getMarkings() {
        return Markings.byId((getTypeVariant() & 65280) >> 8);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void updateContainerEquipment() {
        if (this.level.isClientSide) {
            return;
        }
        super.updateContainerEquipment();
        setArmorEquipment(this.inventory.getItem(1));
        setDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    private void setArmorEquipment(ItemStack itemStack) {
        int protection;
        setArmor(itemStack);
        if (!this.level.isClientSide) {
            getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
            if (isArmor(itemStack) && (protection = ((HorseArmorItem) itemStack.getItem()).getProtection()) != 0) {
                getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", protection, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.ContainerListener
    public void containerChanged(Container container) {
        ItemStack armor = getArmor();
        super.containerChanged(container);
        ItemStack armor2 = getArmor();
        if (this.tickCount > 20 && isArmor(armor2) && armor != armor2) {
            playSound(SoundEvents.HORSE_ARMOR, 0.5f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void playGallopSound(SoundType soundType) {
        super.playGallopSound(soundType);
        if (this.random.nextInt(10) == 0) {
            playSound(SoundEvents.HORSE_BREATHE, soundType.getVolume() * 0.6f, soundType.getPitch());
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.HORSE_DEATH;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        super.getHurtSound(damageSource);
        return SoundEvents.HORSE_HURT;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.HORSE_ANGRY;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!isBaby()) {
            if (isTamed() && player.isSecondaryUseActive()) {
                openInventory(player);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (isVehicle()) {
                return super.mobInteract(player, interactionHand);
            }
        }
        if (!itemInHand.isEmpty()) {
            if (isFood(itemInHand)) {
                return fedFood(player, itemInHand);
            }
            InteractionResult interactLivingEntity = itemInHand.interactLivingEntity(player, this, interactionHand);
            if (interactLivingEntity.consumesAction()) {
                return interactLivingEntity;
            }
            if (!isTamed()) {
                makeMad();
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            boolean z = (isBaby() || isSaddled() || itemInHand.getItem() != Items.SADDLE) ? false : true;
            if (isArmor(itemInHand) || z) {
                openInventory(player);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
        if (isBaby()) {
            return super.mobInteract(player, interactionHand);
        }
        doPlayerRide(player);
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        return ((animal instanceof Donkey) || (animal instanceof Horse)) && canParent() && ((AbstractHorse) animal).canParent();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        AbstractHorse create;
        Variant variant;
        Markings markings;
        if (agableMob instanceof Donkey) {
            create = EntityType.MULE.create(serverLevel);
        } else {
            Horse horse = (Horse) agableMob;
            create = EntityType.HORSE.create(serverLevel);
            int nextInt = this.random.nextInt(9);
            if (nextInt < 4) {
                variant = getVariant();
            } else if (nextInt < 8) {
                variant = horse.getVariant();
            } else {
                variant = (Variant) Util.getRandom(Variant.values(), this.random);
            }
            int nextInt2 = this.random.nextInt(5);
            if (nextInt2 < 2) {
                markings = getMarkings();
            } else if (nextInt2 < 4) {
                markings = horse.getMarkings();
            } else {
                markings = (Markings) Util.getRandom(Markings.values(), this.random);
            }
            ((Horse) create).setVariantAndMarkings(variant, markings);
        }
        setOffspringAttributes(agableMob, create);
        return create;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public boolean canWearArmor() {
        return true;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    public boolean isArmor(ItemStack itemStack) {
        return itemStack.getItem() instanceof HorseArmorItem;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        Variant variant;
        if (spawnGroupData instanceof HorseGroupData) {
            variant = ((HorseGroupData) spawnGroupData).variant;
        } else {
            variant = (Variant) Util.getRandom(Variant.values(), this.random);
            spawnGroupData = new HorseGroupData(variant);
        }
        setVariantAndMarkings(variant, (Markings) Util.getRandom(Markings.values(), this.random));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/Horse$HorseGroupData.class */
    public static class HorseGroupData extends AgableMob.AgableMobGroupData {
        public final Variant variant;

        public HorseGroupData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}
