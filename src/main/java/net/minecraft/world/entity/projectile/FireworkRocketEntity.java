package net.minecraft.world.entity.projectile;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/FireworkRocketEntity.class */
public class FireworkRocketEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int lifetime;
    private LivingEntity attachedToEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FireworkRocketEntity(Level level, double d, double d2, double d3, ItemStack itemStack) {
        super(EntityType.FIREWORK_ROCKET, level);
        this.life = 0;
        setPos(d, d2, d3);
        int i = 1;
        if (!itemStack.isEmpty() && itemStack.hasTag()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemStack.copy());
            i = 1 + itemStack.getOrCreateTagElement("Fireworks").getByte("Flight");
        }
        setDeltaMovement(this.random.nextGaussian() * 0.001d, 0.05d, this.random.nextGaussian() * 0.001d);
        this.lifetime = (10 * i) + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(Level level, @Nullable Entity entity, double d, double d2, double d3, ItemStack itemStack) {
        this(level, d, d2, d3, itemStack);
        setOwner(entity);
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        this(level, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), itemStack);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(livingEntity.getId()));
        this.attachedToEntity = livingEntity;
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, double d, double d2, double d3, boolean z) {
        this(level, d, d2, d3, itemStack);
        this.entityData.set(DATA_SHOT_AT_ANGLE, Boolean.valueOf(z));
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, Entity entity, double d, double d2, double d3, boolean z) {
        this(level, itemStack, d, d2, d3, z);
        setOwner(entity);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_FIREWORKS_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
        this.entityData.define(DATA_SHOT_AT_ANGLE, false);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 4096.0d && !isAttachedToEntity();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRender(double d, double d2, double d3) {
        return super.shouldRender(d, d2, d3) && !isAttachedToEntity();
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                ((OptionalInt) this.entityData.get(DATA_ATTACHED_TO_TARGET)).ifPresent(i -> {
                    Entity entity = this.level.getEntity(i);
                    if (entity instanceof LivingEntity) {
                        this.attachedToEntity = (LivingEntity) entity;
                    }
                });
            }
            if (this.attachedToEntity != null) {
                if (this.attachedToEntity.isFallFlying()) {
                    Vec3 lookAngle = this.attachedToEntity.getLookAngle();
                    Vec3 deltaMovement = this.attachedToEntity.getDeltaMovement();
                    this.attachedToEntity.setDeltaMovement(deltaMovement.add((lookAngle.x * 0.1d) + (((lookAngle.x * 1.5d) - deltaMovement.x) * 0.5d), (lookAngle.y * 0.1d) + (((lookAngle.y * 1.5d) - deltaMovement.y) * 0.5d), (lookAngle.z * 0.1d) + (((lookAngle.z * 1.5d) - deltaMovement.z) * 0.5d)));
                }
                setPos(this.attachedToEntity.getX(), this.attachedToEntity.getY(), this.attachedToEntity.getZ());
                setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }
        } else {
            if (!isShotAtAngle()) {
                double d = this.horizontalCollision ? 1.0d : 1.15d;
                setDeltaMovement(getDeltaMovement().multiply(d, 1.0d, d).add(0.0d, 0.04d, 0.0d));
            }
            Vec3 deltaMovement2 = getDeltaMovement();
            move(MoverType.SELF, deltaMovement2);
            setDeltaMovement(deltaMovement2);
        }
        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (!this.noPhysics) {
            onHit(hitResult);
            this.hasImpulse = true;
        }
        updateRotation();
        if (this.life == 0 && !isSilent()) {
            this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
        }
        this.life++;
        if (this.level.isClientSide && this.life % 2 < 2) {
            this.level.addParticle(ParticleTypes.FIREWORK, getX(), getY() - 0.3d, getZ(), this.random.nextGaussian() * 0.05d, (-getDeltaMovement().y) * 0.5d, this.random.nextGaussian() * 0.05d);
        }
        if (!this.level.isClientSide && this.life > this.lifetime) {
            explode();
        }
    }

    private void explode() {
        this.level.broadcastEntityEvent(this, (byte) 17);
        dealExplosionDamage();
        remove();
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level.isClientSide) {
            return;
        }
        explode();
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
        this.level.getBlockState(blockPos).entityInside(this.level, blockPos, this);
        if (!this.level.isClientSide() && hasExplosion()) {
            explode();
        }
        super.onHitBlock(blockHitResult);
    }

    private boolean hasExplosion() {
        ItemStack itemStack = (ItemStack) this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        CompoundTag tagElement = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
        ListTag list = tagElement != null ? tagElement.getList("Explosions", 10) : null;
        return (list == null || list.isEmpty()) ? false : true;
    }

    private void dealExplosionDamage() {
        float f = 0.0f;
        ItemStack itemStack = (ItemStack) this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        CompoundTag tagElement = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
        ListTag list = tagElement != null ? tagElement.getList("Explosions", 10) : null;
        if (list != null && !list.isEmpty()) {
            f = 5.0f + (list.size() * 2);
        }
        if (f > 0.0f) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurt(DamageSource.fireworks(this, getOwner()), 5.0f + (list.size() * 2));
            }
            Vec3 position = position();
            for (LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5.0d))) {
                if (livingEntity != this.attachedToEntity && distanceToSqr(livingEntity) <= 25.0d) {
                    boolean z = false;
                    int i = 0;
                    while (true) {
                        if (i >= 2) {
                            break;
                        }
                        if (this.level.clip(new ClipContext(position, new Vec3(livingEntity.getX(), livingEntity.getY(0.5d * i), livingEntity.getZ()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) {
                            i++;
                        } else {
                            z = true;
                            break;
                        }
                    }
                    if (z) {
                        livingEntity.hurt(DamageSource.fireworks(this, getOwner()), f * ((float) Math.sqrt((5.0d - distanceTo(livingEntity)) / 5.0d)));
                    }
                }
            }
        }
    }

    private boolean isAttachedToEntity() {
        return ((OptionalInt) this.entityData.get(DATA_ATTACHED_TO_TARGET)).isPresent();
    }

    public boolean isShotAtAngle() {
        return ((Boolean) this.entityData.get(DATA_SHOT_AT_ANGLE)).booleanValue();
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 17 && this.level.isClientSide) {
            if (!hasExplosion()) {
                for (int i = 0; i < this.random.nextInt(3) + 2; i++) {
                    this.level.addParticle(ParticleTypes.POOF, getX(), getY(), getZ(), this.random.nextGaussian() * 0.05d, 0.005d, this.random.nextGaussian() * 0.05d);
                }
            } else {
                ItemStack itemStack = (ItemStack) this.entityData.get(DATA_ID_FIREWORKS_ITEM);
                CompoundTag tagElement = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
                Vec3 deltaMovement = getDeltaMovement();
                this.level.createFireworks(getX(), getY(), getZ(), deltaMovement.x, deltaMovement.y, deltaMovement.z, tagElement);
            }
        }
        super.handleEntityEvent(b);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Life", this.life);
        compoundTag.putInt("LifeTime", this.lifetime);
        ItemStack itemStack = (ItemStack) this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        if (!itemStack.isEmpty()) {
            compoundTag.put("FireworksItem", itemStack.save(new CompoundTag()));
        }
        compoundTag.putBoolean("ShotAtAngle", ((Boolean) this.entityData.get(DATA_SHOT_AT_ANGLE)).booleanValue());
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.life = compoundTag.getInt("Life");
        this.lifetime = compoundTag.getInt("LifeTime");
        ItemStack m66of = ItemStack.of(compoundTag.getCompound("FireworksItem"));
        if (!m66of.isEmpty()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, m66of);
        }
        if (compoundTag.contains("ShotAtAngle")) {
            this.entityData.set(DATA_SHOT_AT_ANGLE, Boolean.valueOf(compoundTag.getBoolean("ShotAtAngle")));
        }
    }

    @Override // net.minecraft.world.entity.projectile.ItemSupplier
    public ItemStack getItem() {
        ItemStack itemStack = (ItemStack) this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        return itemStack.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : itemStack;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAttackable() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
