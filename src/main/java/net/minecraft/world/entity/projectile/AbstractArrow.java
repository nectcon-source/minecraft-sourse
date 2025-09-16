package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/AbstractArrow.class */
public abstract class AbstractArrow extends Projectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);

    @Nullable
    private BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime;
    public Pickup pickup;
    public int shakeTime;
    private int life;
    private double baseDamage;
    private int knockback;
    private SoundEvent soundEvent;
    private IntOpenHashSet piercingIgnoreEntityIds;
    private List<Entity> piercedAndKilledEntities;

    protected abstract ItemStack getPickupItem();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/AbstractArrow$Pickup.class */
    public enum Pickup {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static Pickup byOrdinal(int i) {
            if (i < 0 || i > values().length) {
                i = 0;
            }
            return values()[i];
        }
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.pickup = Pickup.DISALLOWED;
        this.baseDamage = 2.0d;
        this.soundEvent = getDefaultHitGroundSoundEvent();
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, double d, double d2, double d3, Level level) {
        this(entityType, level);
        setPos(d, d2, d3);
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, LivingEntity livingEntity, Level level) {
        this(entityType, livingEntity.getX(), livingEntity.getEyeY() - 0.10000000149011612d, livingEntity.getZ(), level);
        setOwner(livingEntity);
        if (livingEntity instanceof Player) {
            this.pickup = Pickup.ALLOWED;
        }
    }

    public void setSoundEvent(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        double size = getBoundingBox().getSize() * 10.0d;
        if (Double.isNaN(size)) {
            size = 1.0d;
        }
        double viewScale = size * 64.0d * getViewScale();
        return d < viewScale * viewScale;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        this.entityData.define(ID_FLAGS, (byte) 0);
        this.entityData.define(PIERCE_LEVEL, (byte) 0);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    public void shoot(double d, double d2, double d3, float f, float f2) {
        super.shoot(d, d2, d3, f, f2);
        this.life = 0;
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpTo(double d, double d2, double d3, float f, float f2, int i, boolean z) {
        setPos(d, d2, d3);
        setRot(f, f2);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void lerpMotion(double d, double d2, double d3) {
        super.lerpMotion(d, d2, d3);
        this.life = 0;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        boolean isNoPhysics = isNoPhysics();
        Vec3 deltaMovement = getDeltaMovement();
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float sqrt = Mth.sqrt(getHorizontalDistanceSqr(deltaMovement));
            this.yRot = (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 57.2957763671875d);
            this.xRot = (float) (Mth.atan2(deltaMovement.y, sqrt) * 57.2957763671875d);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }
        BlockPos blockPosition = blockPosition();
        BlockState blockState = this.level.getBlockState(blockPosition);
        if (!blockState.isAir() && !isNoPhysics) {
            VoxelShape collisionShape = blockState.getCollisionShape(this.level, blockPosition);
            if (!collisionShape.isEmpty()) {
                Vec3 position = position();
                Iterator<AABB> it = collisionShape.toAabbs().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next().move(blockPosition).contains(position)) {
                            this.inGround = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        if (this.shakeTime > 0) {
            this.shakeTime--;
        }
        if (isInWaterOrRain()) {
            clearFire();
        }
        if (this.inGround && !isNoPhysics) {
            if (this.lastState != blockState && shouldFall()) {
                startFalling();
            } else if (!this.level.isClientSide) {
                tickDespawn();
            }
            this.inGroundTime++;
            return;
        }
        this.inGroundTime = 0;
        Vec3 position2 = position();
        Vec3 add = position2.add(deltaMovement);
        HitResult clip = this.level.clip(new ClipContext(position2, add, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (clip.getType() != HitResult.Type.MISS) {
            add = clip.getLocation();
        }
        while (!this.removed) {
            EntityHitResult findHitEntity = findHitEntity(position2, add);
            if (findHitEntity != null) {
                clip = findHitEntity;
            }
            if (clip != null && clip.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) clip).getEntity();
                Entity owner = getOwner();
                if ((entity instanceof Player) && (owner instanceof Player) && !((Player) owner).canHarmPlayer((Player) entity)) {
                    clip = null;
                    findHitEntity = null;
                }
            }
            if (clip != null && !isNoPhysics) {
                onHit(clip);
                this.hasImpulse = true;
            }
            if (findHitEntity == null || getPierceLevel() <= 0) {
                break;
            } else {
                clip = null;
            }
        }
        Vec3 deltaMovement2 = getDeltaMovement();
        double d = deltaMovement2.x;
        double d2 = deltaMovement2.y;
        double d3 = deltaMovement2.z;
        if (isCritArrow()) {
            for (int i = 0; i < 4; i++) {
                this.level.addParticle(ParticleTypes.CRIT, getX() + ((d * i) / 4.0d), getY() + ((d2 * i) / 4.0d), getZ() + ((d3 * i) / 4.0d), -d, (-d2) + 0.2d, -d3);
            }
        }
        double x = getX() + d;
        double y = getY() + d2;
        double z = getZ() + d3;
        float sqrt2 = Mth.sqrt(getHorizontalDistanceSqr(deltaMovement2));
        if (isNoPhysics) {
            this.yRot = (float) (Mth.atan2(-d, -d3) * 57.2957763671875d);
        } else {
            this.yRot = (float) (Mth.atan2(d, d3) * 57.2957763671875d);
        }
        this.xRot = (float) (Mth.atan2(d2, sqrt2) * 57.2957763671875d);
        this.xRot = lerpRotation(this.xRotO, this.xRot);
        this.yRot = lerpRotation(this.yRotO, this.yRot);
        float f = 0.99f;
        if (isInWater()) {
            for (int i2 = 0; i2 < 4; i2++) {
                this.level.addParticle(ParticleTypes.BUBBLE, x - (d * 0.25d), y - (d2 * 0.25d), z - (d3 * 0.25d), d, d2, d3);
            }
            f = getWaterInertia();
        }
        setDeltaMovement(deltaMovement2.scale(f));
        if (!isNoGravity() && !isNoPhysics) {
            Vec3 deltaMovement3 = getDeltaMovement();
            setDeltaMovement(deltaMovement3.x, deltaMovement3.y - 0.05000000074505806d, deltaMovement3.z);
        }
        setPos(x, y, z);
        checkInsideBlocks();
    }

    private boolean shouldFall() {
        return this.inGround && this.level.noCollision(new AABB(position(), position()).inflate(0.06d));
    }

    private void startFalling() {
        this.inGround = false;
        setDeltaMovement(getDeltaMovement().multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    @Override // net.minecraft.world.entity.Entity
    public void move(MoverType moverType, Vec3 vec3) {
        super.move(moverType, vec3);
        if (moverType != MoverType.SELF && shouldFall()) {
            startFalling();
        }
    }

    protected void tickDespawn() {
        this.life++;
        if (this.life >= 1200) {
            remove();
        }
    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }
        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        DamageSource arrow;
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        int ceil = Mth.ceil(Mth.clamp(((float) getDeltaMovement().length()) * this.baseDamage, 0.0d, 2.147483647E9d));
        if (getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercingIgnoreEntityIds.size() < getPierceLevel() + 1) {
                this.piercingIgnoreEntityIds.add(entity.getId());
            } else {
                remove();
                return;
            }
        }
        if (isCritArrow()) {
            ceil = (int) Math.min(this.random.nextInt((ceil / 2) + 2) + ceil, 2147483647L);
        }
        Entity owner = getOwner();
        if (owner == null) {
            arrow = DamageSource.arrow(this, this);
        } else {
            arrow = DamageSource.arrow(this, owner);
            if (owner instanceof LivingEntity) {
                ((LivingEntity) owner).setLastHurtMob(entity);
            }
        }
        boolean z = entity.getType() == EntityType.ENDERMAN;
        int remainingFireTicks = entity.getRemainingFireTicks();
        if (isOnFire() && !z) {
            entity.setSecondsOnFire(5);
        }
        if (entity.hurt(arrow, ceil)) {
            if (z) {
                return;
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (!this.level.isClientSide && getPierceLevel() <= 0) {
                    livingEntity.setArrowCount(livingEntity.getArrowCount() + 1);
                }
                if (this.knockback > 0) {
                    Vec3 scale = getDeltaMovement().multiply(1.0d, 0.0d, 1.0d).normalize().scale(this.knockback * 0.6d);
                    if (scale.lengthSqr() > 0.0d) {
                        livingEntity.push(scale.x, 0.1d, scale.z);
                    }
                }
                if (!this.level.isClientSide && (owner instanceof LivingEntity)) {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingEntity);
                }
                doPostHurtEffects(livingEntity);
                if (owner != null && livingEntity != owner && (livingEntity instanceof Player) && (owner instanceof ServerPlayer) && !isSilent()) {
                    ((ServerPlayer) owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0f));
                }
                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingEntity);
                }
                if (!this.level.isClientSide && (owner instanceof ServerPlayer)) {
                    ServerPlayer serverPlayer = (ServerPlayer) owner;
                    if (this.piercedAndKilledEntities != null && shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, Arrays.asList(entity));
                    }
                }
            }
            playSound(this.soundEvent, 1.0f, 1.2f / ((this.random.nextFloat() * 0.2f) + 0.9f));
            if (getPierceLevel() <= 0) {
                remove();
                return;
            }
            return;
        }
        entity.setRemainingFireTicks(remainingFireTicks);
        setDeltaMovement(getDeltaMovement().scale(-0.1d));
        this.yRot += 180.0f;
        this.yRotO += 180.0f;
        if (!this.level.isClientSide && getDeltaMovement().lengthSqr() < 1.0E-7d) {
            if (this.pickup == Pickup.ALLOWED) {
                spawnAtLocation(getPickupItem(), 0.1f);
            }
            remove();
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.lastState = this.level.getBlockState(blockHitResult.getBlockPos());
        super.onHitBlock(blockHitResult);
        Vec3 subtract = blockHitResult.getLocation().subtract(getX(), getY(), getZ());
        setDeltaMovement(subtract);
        Vec3 scale = subtract.normalize().scale(0.05000000074505806d);
        setPosRaw(getX() - scale.x, getY() - scale.y, getZ() - scale.z);
        playSound(getHitGroundSoundEvent(), 1.0f, 1.2f / ((this.random.nextFloat() * 0.2f) + 0.9f));
        this.inGround = true;
        this.shakeTime = 7;
        setCritArrow(false);
        setPierceLevel((byte) 0);
        setSoundEvent(SoundEvents.ARROW_HIT);
        setShotFromCrossbow(false);
        resetPiercedEntities();
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    protected final SoundEvent getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected void doPostHurtEffects(LivingEntity livingEntity) {
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec32) {
        return ProjectileUtil.getEntityHitResult(this.level, this, vec3, vec32, getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0d), (Predicate<Entity>) this::canHitEntity);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putShort("life", (short) this.life);
        if (this.lastState != null) {
            compoundTag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }
        compoundTag.putByte("shake", (byte) this.shakeTime);
        compoundTag.putBoolean("inGround", this.inGround);
        compoundTag.putByte("pickup", (byte) this.pickup.ordinal());
        compoundTag.putDouble("damage", this.baseDamage);
        compoundTag.putBoolean("crit", isCritArrow());
        compoundTag.putByte("PierceLevel", getPierceLevel());
        compoundTag.putString("SoundEvent", Registry.SOUND_EVENT.getKey(this.soundEvent).toString());
        compoundTag.putBoolean("ShotFromCrossbow", shotFromCrossbow());
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.life = compoundTag.getShort("life");
        if (compoundTag.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(compoundTag.getCompound("inBlockState"));
        }
        this.shakeTime = compoundTag.getByte("shake") & 255;
        this.inGround = compoundTag.getBoolean("inGround");
        if (compoundTag.contains("damage", 99)) {
            this.baseDamage = compoundTag.getDouble("damage");
        }
        if (compoundTag.contains("pickup", 99)) {
            this.pickup = Pickup.byOrdinal(compoundTag.getByte("pickup"));
        } else if (compoundTag.contains("player", 99)) {
            this.pickup = compoundTag.getBoolean("player") ? Pickup.ALLOWED : Pickup.DISALLOWED;
        }
        setCritArrow(compoundTag.getBoolean("crit"));
        setPierceLevel(compoundTag.getByte("PierceLevel"));
        if (compoundTag.contains("SoundEvent", 8)) {
            this.soundEvent = Registry.SOUND_EVENT.getOptional(new ResourceLocation(compoundTag.getString("SoundEvent"))).orElse(getDefaultHitGroundSoundEvent());
        }
        setShotFromCrossbow(compoundTag.getBoolean("ShotFromCrossbow"));
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        if (entity instanceof Player) {
            this.pickup = ((Player) entity).abilities.instabuild ? Pickup.CREATIVE_ONLY : Pickup.ALLOWED;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void playerTouch(Player player) {
        if (this.level.isClientSide) {
            return;
        }
        if ((!this.inGround && !isNoPhysics()) || this.shakeTime > 0) {
            return;
        }
        boolean z = this.pickup == Pickup.ALLOWED || (this.pickup == Pickup.CREATIVE_ONLY && player.abilities.instabuild) || (isNoPhysics() && getOwner().getUUID() == player.getUUID());
        if (this.pickup == Pickup.ALLOWED && !player.inventory.add(getPickupItem())) {
            z = false;
        }
        if (z) {
            player.take(this, 1);
            remove();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    public void setBaseDamage(double d) {
        this.baseDamage = d;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    public void setKnockback(int i) {
        this.knockback = i;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAttackable() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.13f;
    }

    public void setCritArrow(boolean z) {
        setFlag(1, z);
    }

    public void setPierceLevel(byte b) {
        this.entityData.set(PIERCE_LEVEL, Byte.valueOf(b));
    }

    private void setFlag(int i, boolean z) {
        byte byteValue = ((Byte) this.entityData.get(ID_FLAGS)).byteValue();
        if (z) {
            this.entityData.set(ID_FLAGS, Byte.valueOf((byte) (byteValue | i)));
        } else {
            this.entityData.set(ID_FLAGS, Byte.valueOf((byte) (byteValue & (i ^ (-1)))));
        }
    }

    public boolean isCritArrow() {
        return (((Byte) this.entityData.get(ID_FLAGS)).byteValue() & 1) != 0;
    }

    public boolean shotFromCrossbow() {
        return (((Byte) this.entityData.get(ID_FLAGS)).byteValue() & 4) != 0;
    }

    public byte getPierceLevel() {
        return ((Byte) this.entityData.get(PIERCE_LEVEL)).byteValue();
    }

    public void setEnchantmentEffectsFromEntity(LivingEntity livingEntity, float f) {
        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, livingEntity);
        int enchantmentLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, livingEntity);
        setBaseDamage((f * 2.0f) + (this.random.nextGaussian() * 0.25d) + (this.level.getDifficulty().getId() * 0.11f));
        if (enchantmentLevel > 0) {
            setBaseDamage(getBaseDamage() + (enchantmentLevel * 0.5d) + 0.5d);
        }
        if (enchantmentLevel2 > 0) {
            setKnockback(enchantmentLevel2);
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, livingEntity) > 0) {
            setSecondsOnFire(100);
        }
    }

    protected float getWaterInertia() {
        return 0.6f;
    }

    public void setNoPhysics(boolean z) {
        this.noPhysics = z;
        setFlag(2, z);
    }

    public boolean isNoPhysics() {
        if (this.level.isClientSide) {
            return (((Byte) this.entityData.get(ID_FLAGS)).byteValue() & 2) != 0;
        }
        return this.noPhysics;
    }

    public void setShotFromCrossbow(boolean z) {
        setFlag(4, z);
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        Entity owner = getOwner();
        return new ClientboundAddEntityPacket(this, owner == null ? 0 : owner.getId());
    }
}
