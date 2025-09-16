package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/item/ItemEntity.class */
public class ItemEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private int age;
    private int pickupDelay;
    private int health;
    private UUID thrower;
    private UUID owner;
    public final float bobOffs;

    public ItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
        this.health = 5;
        this.bobOffs = (float) (Math.random() * 3.141592653589793d * 2.0d);
    }

    public ItemEntity(Level level, double d, double d2, double d3) {
        this(EntityType.ITEM, level);
        setPos(d, d2, d3);
        this.yRot = this.random.nextFloat() * 360.0f;
        setDeltaMovement((this.random.nextDouble() * 0.2d) - 0.1d, 0.2d, (this.random.nextDouble() * 0.2d) - 0.1d);
    }

    public ItemEntity(Level level, double d, double d2, double d3, ItemStack itemStack) {
        this(level, d, d2, d3);
        setItem(itemStack);
    }

    private ItemEntity(ItemEntity itemEntity) {
        super(itemEntity.getType(), itemEntity.level);
        this.health = 5;
        setItem(itemEntity.getItem().copy());
        copyPosition(itemEntity);
        this.age = itemEntity.age;
        this.bobOffs = itemEntity.bobOffs;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick(){
        if (getItem().isEmpty()) {
            remove();
            return;
        }
        super.tick();
        if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            this.pickupDelay--;
        }
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        Vec3 deltaMovement = getDeltaMovement();
        float eyeHeight = getEyeHeight() - 0.11111111f;
        if (isInWater() && getFluidHeight(FluidTags.WATER) > eyeHeight) {
            setUnderwaterMovement();
        } else if (isInLava() && getFluidHeight(FluidTags.LAVA) > eyeHeight) {
            setUnderLavaMovement();
        } else if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.04d, 0.0d));
        }
        if (this.level.isClientSide) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level.noCollision(this);
            if (this.noPhysics) {
                moveTowardsClosestSpace(getX(), (getBoundingBox().minY + getBoundingBox().maxY) / 2.0d, getZ());
            }
        }
        if (!this.onGround || getHorizontalDistanceSqr(getDeltaMovement()) > 9.999999747378752E-6d || (this.tickCount + getId()) % 4 == 0) {
            move(MoverType.SELF, getDeltaMovement());
            float f = 0.98f;
            if (this.onGround) {
                f = this.level.getBlockState(new BlockPos(getX(), getY() - 1.0d, getZ())).getBlock().getFriction() * 0.98f;
            }
            setDeltaMovement(getDeltaMovement().multiply(f, 0.98d, f));
            if (this.onGround) {
                Vec3 deltaMovement2 = getDeltaMovement();
                if (deltaMovement2.y < 0.0d) {
                    setDeltaMovement(deltaMovement2.multiply(1.0d, -0.5d, 1.0d));
                }
            }
        }
        if (this.tickCount % (Mth.floor(this.xo) != Mth.floor(getX()) || Mth.floor(this.yo) != Mth.floor(getY()) || Mth.floor(this.zo) != Mth.floor(getZ()) ? 2 : 40) == 0) {
            if (this.level.getFluidState(blockPosition()).is(FluidTags.LAVA) && !fireImmune()) {
                playSound(SoundEvents.GENERIC_BURN, 0.4f, 2.0f + (this.random.nextFloat() * 0.4f));
            }
            if (!this.level.isClientSide && isMergable()) {
                mergeWithNeighbours();
            }
        }
        if (this.age != -32768) {
            this.age++;
        }
        this.hasImpulse |= updateInWaterStateAndDoFluidPushing();
        if (!this.level.isClientSide && getDeltaMovement().subtract(deltaMovement).lengthSqr() > 0.01d) {
            this.hasImpulse = true;
        }
        if (!this.level.isClientSide && this.age >= 6000) {
            remove();
        }
    }

    private void setUnderwaterMovement() {
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x * 0.9900000095367432d, deltaMovement.y + (deltaMovement.y < 0.05999999865889549d ? 5.0E-4f : 0.0f), deltaMovement.z * 0.9900000095367432d);
    }

    private void setUnderLavaMovement() {
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x * 0.949999988079071d, deltaMovement.y + (deltaMovement.y < 0.05999999865889549d ? 5.0E-4f : 0.0f), deltaMovement.z * 0.949999988079071d);
    }

    private void mergeWithNeighbours() {
        if (!isMergable()) {
            return;
        }
        for (ItemEntity itemEntity : this.level.getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(0.5d, 0.0d, 0.5d), itemEntity2 -> {
            return itemEntity2 != this && itemEntity2.isMergable();
        })) {
            if (itemEntity.isMergable()) {
                tryToMerge(itemEntity);
                if (this.removed) {
                    return;
                }
            }
        }
    }

    private boolean isMergable() {
        ItemStack item = getItem();
        return isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && item.getCount() < item.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity itemEntity) {
        ItemStack item = getItem();
        ItemStack item2 = itemEntity.getItem();
        if (!Objects.equals(getOwner(), itemEntity.getOwner()) || !areMergable(item, item2)) {
            return;
        }
        if (item2.getCount() < item.getCount()) {
            merge(this, item, itemEntity, item2);
        } else {
            merge(itemEntity, item2, this, item);
        }
    }

    public static boolean areMergable(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.getItem() != itemStack.getItem() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize() || (itemStack2.hasTag() ^ itemStack.hasTag())) {
            return false;
        }
        if (itemStack2.hasTag() && !itemStack2.getTag().equals(itemStack.getTag())) {
            return false;
        }
        return true;
    }

    public static ItemStack merge(ItemStack itemStack, ItemStack itemStack2, int i) {
        int min = Math.min(Math.min(itemStack.getMaxStackSize(), i) - itemStack.getCount(), itemStack2.getCount());
        ItemStack copy = itemStack.copy();
        copy.grow(min);
        itemStack2.shrink(min);
        return copy;
    }

    private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemStack itemStack2) {
        itemEntity.setItem(merge(itemStack, itemStack2, 64));
    }

    private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2) {
        merge(itemEntity, itemStack, itemStack2);
        itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, itemEntity2.pickupDelay);
        itemEntity.age = Math.min(itemEntity.age, itemEntity2.age);
        if (itemStack2.isEmpty()) {
            itemEntity2.remove();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean fireImmune() {
        return getItem().getItem().isFireResistant() || super.fireImmune();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if ((!getItem().isEmpty() && getItem().getItem() == Items.NETHER_STAR && damageSource.isExplosion()) || !getItem().getItem().canBeHurtBy(damageSource)) {
            return false;
        }
        markHurt();
        this.health = (int) (this.health - f);
        if (this.health <= 0) {
            remove();
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putShort("Health", (short) this.health);
        compoundTag.putShort("Age", (short) this.age);
        compoundTag.putShort("PickupDelay", (short) this.pickupDelay);
        if (getThrower() != null) {
            compoundTag.putUUID("Thrower", getThrower());
        }
        if (getOwner() != null) {
            compoundTag.putUUID("Owner", getOwner());
        }
        if (!getItem().isEmpty()) {
            compoundTag.put("Item", getItem().save(new CompoundTag()));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.health = compoundTag.getShort("Health");
        this.age = compoundTag.getShort("Age");
        if (compoundTag.contains("PickupDelay")) {
            this.pickupDelay = compoundTag.getShort("PickupDelay");
        }
        if (compoundTag.hasUUID("Owner")) {
            this.owner = compoundTag.getUUID("Owner");
        }
        if (compoundTag.hasUUID("Thrower")) {
            this.thrower = compoundTag.getUUID("Thrower");
        }
        setItem(ItemStack.of(compoundTag.getCompound("Item")));
        if (getItem().isEmpty()) {
            remove();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void playerTouch(Player player) {
        if (this.level.isClientSide) {
            return;
        }
        ItemStack item = getItem();
        Item item2 = item.getItem();
        int count = item.getCount();
        if (this.pickupDelay == 0) {
            if ((this.owner == null || this.owner.equals(player.getUUID())) && player.inventory.add(item)) {
                player.take(this, count);
                if (item.isEmpty()) {
                    remove();
                    item.setCount(count);
                }
                player.awardStat(Stats.ITEM_PICKED_UP.get(item2), count);
                player.onItemPickup(this);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity, net.minecraft.world.Nameable
    public Component getName() {
        Component customName = getCustomName();
        if (customName != null) {
            return customName;
        }
        return new TranslatableComponent(getItem().getDescriptionId());
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAttackable() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        Entity changeDimension = super.changeDimension(serverLevel);
        if (!this.level.isClientSide && (changeDimension instanceof ItemEntity)) {
            ((ItemEntity) changeDimension).mergeWithNeighbours();
        }
        return changeDimension;
    }

    public ItemStack getItem() {
        return (ItemStack) getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack itemStack) {
        getEntityData().set(DATA_ITEM, itemStack);
    }

    @Override // net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_ITEM.equals(entityDataAccessor)) {
            getItem().setEntityRepresentation(this);
        }
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID uuid) {
        this.owner = uuid;
    }

    @Nullable
    public UUID getThrower() {
        return this.thrower;
    }

    public void setThrower(@Nullable UUID uuid) {
        this.thrower = uuid;
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = 32767;
    }

    public void setPickUpDelay(int i) {
        this.pickupDelay = i;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        setNeverPickUp();
        this.age = 5999;
    }

    public float getSpin(float f) {
        return ((getAge() + f) / 20.0f) + this.bobOffs;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public ItemEntity copy() {
        return new ItemEntity(this);
    }
}
