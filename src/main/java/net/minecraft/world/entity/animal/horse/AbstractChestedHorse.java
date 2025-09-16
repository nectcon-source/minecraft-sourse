package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/AbstractChestedHorse.class */
public abstract class AbstractChestedHorse extends AbstractHorse {
    private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);

    protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> entityType, Level level) {
        super(entityType, level);
        this.canGallop = false;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void randomizeAttributes() {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(generateRandomMaxHealth());
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_CHEST, false);
    }

    public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
        return createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.17499999701976776d).add(Attributes.JUMP_STRENGTH, 0.5d);
    }

    public boolean hasChest() {
        return ((Boolean) this.entityData.get(DATA_ID_CHEST)).booleanValue();
    }

    public void setChest(boolean z) {
        this.entityData.set(DATA_ID_CHEST, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected int getInventorySize() {
        if (hasChest()) {
            return 17;
        }
        return super.getInventorySize();
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.25d;
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.LivingEntity
    protected void dropEquipment() {
        super.dropEquipment();
        if (hasChest()) {
            if (!this.level.isClientSide) {
                spawnAtLocation(Blocks.CHEST);
            }
            setChest(false);
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("ChestedHorse", hasChest());
        if (hasChest()) {
            ListTag listTag = new ListTag();
            for (int i = 2; i < this.inventory.getContainerSize(); i++) {
                ItemStack item = this.inventory.getItem(i);
                if (!item.isEmpty()) {
                    CompoundTag compoundTag2 = new CompoundTag();
                    compoundTag2.putByte("Slot", (byte) i);
                    item.save(compoundTag2);
                    listTag.add(compoundTag2);
                }
            }
            compoundTag.put("Items", listTag);
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setChest(compoundTag.getBoolean("ChestedHorse"));
        if (hasChest()) {
            ListTag list = compoundTag.getList("Items", 10);
            createInventory();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                int i2 = compound.getByte("Slot") & 255;
                if (i2 >= 2 && i2 < this.inventory.getContainerSize()) {
                    this.inventory.setItem(i2, ItemStack.of(compound));
                }
            }
        }
        updateContainerEquipment();
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob, net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        if (i == 499) {
            if (hasChest() && itemStack.isEmpty()) {
                setChest(false);
                createInventory();
                return true;
            }
            if (!hasChest() && itemStack.getItem() == Blocks.CHEST.asItem()) {
                setChest(true);
                createInventory();
                return true;
            }
        }
        return super.setSlot(i, itemStack);
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
            if (!isTamed()) {
                makeMad();
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (!hasChest() && itemInHand.getItem() == Blocks.CHEST.asItem()) {
                setChest(true);
                playChestEquipsSound();
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
                createInventory();
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (!isBaby() && !isSaddled() && itemInHand.getItem() == Items.SADDLE) {
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

    protected void playChestEquipsSound() {
        playSound(SoundEvents.DONKEY_CHEST, 1.0f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f);
    }

    public int getInventoryColumns() {
        return 5;
    }
}
