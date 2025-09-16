package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/EndCrystal.class */
public class EndCrystal extends Entity {
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    public int time;

    public EndCrystal(EntityType<? extends EndCrystal> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public EndCrystal(Level level, double d, double d2, double d3) {
        this(EntityType.END_CRYSTAL, level);
        setPos(d, d2, d3);
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
        getEntityData().define(DATA_SHOW_BOTTOM, true);
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        this.time++;
        if (this.level instanceof ServerLevel) {
            BlockPos blockPosition = blockPosition();
            if (((ServerLevel) this.level).dragonFight() != null && this.level.getBlockState(blockPosition).isAir()) {
                this.level.setBlockAndUpdate(blockPosition, BaseFireBlock.getState(this.level, blockPosition));
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (getBeamTarget() != null) {
            compoundTag.put("BeamTarget", NbtUtils.writeBlockPos(getBeamTarget()));
        }
        compoundTag.putBoolean("ShowBottom", showsBottom());
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("BeamTarget", 10)) {
            setBeamTarget(NbtUtils.readBlockPos(compoundTag.getCompound("BeamTarget")));
        }
        if (compoundTag.contains("ShowBottom", 1)) {
            setShowBottom(compoundTag.getBoolean("ShowBottom"));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource) || (damageSource.getEntity() instanceof EnderDragon)) {
            return false;
        }
        if (!this.removed && !this.level.isClientSide) {
            remove();
            if (!damageSource.isExplosion()) {
                this.level.explode(null, getX(), getY(), getZ(), 6.0f, Explosion.BlockInteraction.DESTROY);
            }
            onDestroyedBy(damageSource);
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public void kill() {
        onDestroyedBy(DamageSource.GENERIC);
        super.kill();
    }

    private void onDestroyedBy(DamageSource damageSource) {
        EndDragonFight dragonFight;
        if ((this.level instanceof ServerLevel) && (dragonFight = ((ServerLevel) this.level).dragonFight()) != null) {
            dragonFight.onCrystalDestroyed(this, damageSource);
        }
    }

    public void setBeamTarget(@Nullable BlockPos blockPos) {
        getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(blockPos));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return (BlockPos) ((Optional) getEntityData().get(DATA_BEAM_TARGET)).orElse(null);
    }

    public void setShowBottom(boolean z) {
        getEntityData().set(DATA_SHOW_BOTTOM, Boolean.valueOf(z));
    }

    public boolean showsBottom() {
        return ((Boolean) getEntityData().get(DATA_SHOW_BOTTOM)).booleanValue();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        return super.shouldRenderAtSqrDistance(d) || getBeamTarget() != null;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
