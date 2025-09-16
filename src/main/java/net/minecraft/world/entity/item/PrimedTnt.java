package net.minecraft.world.entity.item;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/item/PrimedTnt.class */
public class PrimedTnt extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);

    @Nullable
    private LivingEntity owner;
    private int life;

    public PrimedTnt(EntityType<? extends PrimedTnt> entityType, Level level) {
        super(entityType, level);
        this.life = 80;
        this.blocksBuilding = true;
    }

    public PrimedTnt(Level level, double d, double d2, double d3, @Nullable LivingEntity livingEntity) {
        this(EntityType.TNT, level);
        setPos(d, d2, d3);
        double nextDouble = level.random.nextDouble() * 6.2831854820251465d;
        setDeltaMovement((-Math.sin(nextDouble)) * 0.02d, 0.20000000298023224d, (-Math.cos(nextDouble)) * 0.02d);
        setFuse(80);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
        this.owner = livingEntity;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        this.entityData.define(DATA_FUSE_ID, 80);
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return !this.removed;
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.04d, 0.0d));
        }
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98d));
        if (this.onGround) {
            setDeltaMovement(getDeltaMovement().multiply(0.7d, -0.5d, 0.7d));
        }
        this.life--;
        if (this.life <= 0) {
            remove();
            if (!this.level.isClientSide) {
                explode();
                return;
            }
            return;
        }
        updateInWaterStateAndDoFluidPushing();
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5d, getZ(), 0.0d, 0.0d, 0.0d);
        }
    }

    private void explode() {
        this.level.explode(this, getX(), getY(0.0625d), getZ(), 4.0f, Explosion.BlockInteraction.BREAK);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putShort("Fuse", (short) getLife());
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setFuse(compoundTag.getShort("Fuse"));
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.owner;
    }

    @Override // net.minecraft.world.entity.Entity
    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.15f;
    }

    public void setFuse(int i) {
        this.entityData.set(DATA_FUSE_ID, Integer.valueOf(i));
        this.life = i;
    }

    @Override // net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_FUSE_ID.equals(entityDataAccessor)) {
            this.life = getFuse();
        }
    }

    public int getFuse() {
        return ((Integer) this.entityData.get(DATA_FUSE_ID)).intValue();
    }

    public int getLife() {
        return this.life;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
