package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrownTrident.class */
public class ThrownTrident extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private ItemStack tridentItem;
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public ThrownTrident(EntityType<? extends ThrownTrident> entityType, Level level) {
        super(entityType, level);
        this.tridentItem = new ItemStack(Items.TRIDENT);
    }

    public ThrownTrident(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(EntityType.TRIDENT, livingEntity, level);
        this.tridentItem = new ItemStack(Items.TRIDENT);
        this.tridentItem = itemStack.copy();
        this.entityData.set(ID_LOYALTY, Byte.valueOf((byte) EnchantmentHelper.getLoyalty(itemStack)));
        this.entityData.set(ID_FOIL, Boolean.valueOf(itemStack.hasFoil()));
    }

    public ThrownTrident(Level level, double d, double d2, double d3) {
        super(EntityType.TRIDENT, d, d2, d3, level);
        this.tridentItem = new ItemStack(Items.TRIDENT);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte) 0);
        this.entityData.define(ID_FOIL, false);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity owner = getOwner();
        if ((this.dealtDamage || isNoPhysics()) && owner != null) {
            int byteValue = ((Byte) this.entityData.get(ID_LOYALTY)).byteValue();
            if (byteValue > 0 && !isAcceptibleReturnOwner()) {
                if (!this.level.isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    spawnAtLocation(getPickupItem(), 0.1f);
                }
                remove();
            } else if (byteValue > 0) {
                setNoPhysics(true);
                Vec3 vec3 = new Vec3(owner.getX() - getX(), owner.getEyeY() - getY(), owner.getZ() - getZ());
                setPosRaw(getX(), getY() + (vec3.y * 0.015d * byteValue), getZ());
                if (this.level.isClientSide) {
                    this.yOld = getY();
                }
                setDeltaMovement(getDeltaMovement().scale(0.95d).add(vec3.normalize().scale(0.05d * byteValue)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    playSound(SoundEvents.TRIDENT_RETURN, 10.0f, 1.0f);
                }
                this.clientSideReturnTridentTickCount++;
            }
        }
        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        if ((owner instanceof ServerPlayer) && owner.isSpectator()) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    public boolean isFoil() {
        return ((Boolean) this.entityData.get(ID_FOIL)).booleanValue();
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec32) {
        if (this.dealtDamage) {
            return null;
        }
        return super.findHitEntity(vec3, vec32);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float f = 8.0f;
        if (entity instanceof LivingEntity) {
            f = 8.0f + EnchantmentHelper.getDamageBonus(this.tridentItem, ((LivingEntity) entity).getMobType());
        }
        Entity owner = getOwner();
        DamageSource trident = DamageSource.trident(this, owner == null ? this : owner);
        this.dealtDamage = true;
        SoundEvent soundEvent = SoundEvents.TRIDENT_HIT;
        if (entity.hurt(trident, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingEntity);
                }
                doPostHurtEffects(livingEntity);
            }
        }
        setDeltaMovement(getDeltaMovement().multiply(-0.01d, -0.1d, -0.01d));
        float f2 = 1.0f;
        if ((this.level instanceof ServerLevel) && this.level.isThundering() && EnchantmentHelper.hasChanneling(this.tridentItem)) {
            BlockPos blockPosition = entity.blockPosition();
            if (this.level.canSeeSky(blockPosition)) {
                LightningBolt create = EntityType.LIGHTNING_BOLT.create(this.level);
                create.moveTo(Vec3.atBottomCenterOf(blockPosition));
                create.setCause(owner instanceof ServerPlayer ? (ServerPlayer) owner : null);
                this.level.addFreshEntity(create);
                soundEvent = SoundEvents.TRIDENT_THUNDER;
                f2 = 5.0f;
            }
        }
        playSound(soundEvent, f2, 1.0f);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.Entity
    public void playerTouch(Player player) {
        Entity owner = getOwner();
        if (owner != null && owner.getUUID() != player.getUUID()) {
            return;
        }
        super.playerTouch(player);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Trident", 10)) {
            this.tridentItem = ItemStack.of(compoundTag.getCompound("Trident"));
        }
        this.dealtDamage = compoundTag.getBoolean("DealtDamage");
        this.entityData.set(ID_LOYALTY, Byte.valueOf((byte) EnchantmentHelper.getLoyalty(this.tridentItem)));
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow, net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("Trident", this.tridentItem.save(new CompoundTag()));
        compoundTag.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    public void tickDespawn() {
        int byteValue = ((Byte) this.entityData.get(ID_LOYALTY)).byteValue();
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || byteValue <= 0) {
            super.tickDespawn();
        }
    }

    @Override // net.minecraft.world.entity.projectile.AbstractArrow
    protected float getWaterInertia() {
        return 0.99f;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRender(double d, double d2, double d3) {
        return true;
    }
}
