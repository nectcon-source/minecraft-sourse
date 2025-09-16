package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/Fireball.class */
public abstract class Fireball extends AbstractHurtingProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Fireball.class, EntityDataSerializers.ITEM_STACK);

    public Fireball(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    public Fireball(EntityType<? extends Fireball> entityType, double d, double d2, double d3, double d4, double d5, double d6, Level level) {
        super(entityType, d, d2, d3, d4, d5, d6, level);
    }

    public Fireball(EntityType<? extends Fireball> entityType, LivingEntity livingEntity, double d, double d2, double d3, Level level) {
        super(entityType, livingEntity, d, d2, d3, level);
    }

    public void setItem(ItemStack itemStack) {
        if (itemStack.getItem() != Items.FIRE_CHARGE || itemStack.hasTag()) {
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
        return itemRaw.isEmpty() ? new ItemStack(Items.FIRE_CHARGE) : itemRaw;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ItemStack itemRaw = getItemRaw();
        if (!itemRaw.isEmpty()) {
            compoundTag.put("Item", itemRaw.save(new CompoundTag()));
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractHurtingProjectile, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setItem(ItemStack.of(compoundTag.getCompound("Item")));
    }
}
