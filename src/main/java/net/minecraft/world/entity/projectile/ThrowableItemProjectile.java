package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrowableItemProjectile.class */
public abstract class ThrowableItemProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrowableItemProjectile.class, EntityDataSerializers.ITEM_STACK);

    protected abstract Item getDefaultItem();

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, double d, double d2, double d3, Level level) {
        super(entityType, d, d2, d3, level);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity livingEntity, Level level) {
        super(entityType, livingEntity, level);
    }

    public void setItem(ItemStack itemStack) {
        if (itemStack.getItem() != getDefaultItem() || itemStack.hasTag()) {
            getEntityData().set(DATA_ITEM_STACK, Util.make(itemStack.copy(), itemStack2 -> {
                itemStack2.setCount(1);
            }));
        }
    }

    protected ItemStack getItemRaw() {
        return (ItemStack) getEntityData().get(DATA_ITEM_STACK);
    }

    @Override // net.minecraft.world.entity.projectile.ItemSupplier
    public ItemStack getItem() {
        ItemStack itemRaw = getItemRaw();
        return itemRaw.isEmpty() ? new ItemStack(getDefaultItem()) : itemRaw;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ItemStack itemRaw = getItemRaw();
        if (!itemRaw.isEmpty()) {
            compoundTag.put("Item", itemRaw.save(new CompoundTag()));
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setItem(ItemStack.of(compoundTag.getCompound("Item")));
    }
}
