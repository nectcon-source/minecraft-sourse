package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/EvokerFangs.class */
public class EvokerFangs extends Entity {
    private int warmupDelayTicks;
    private boolean sentSpikeEvent;
    private int lifeTicks;
    private boolean clientSideAttackStarted;
    private LivingEntity owner;
    private UUID ownerUUID;

    public EvokerFangs(EntityType<? extends EvokerFangs> entityType, Level level) {
        super(entityType, level);
        this.lifeTicks = 22;
    }

    public EvokerFangs(Level level, double d, double d2, double d3, float f, int i, LivingEntity livingEntity) {
        this(EntityType.EVOKER_FANGS, level);
        this.warmupDelayTicks = i;
        setOwner(livingEntity);
        this.yRot = f * 57.295776f;
        setPos(d, d2, d3);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity;
        this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && (this.level instanceof ServerLevel)) {
            Entity entity = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }
        return this.owner;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.warmupDelayTicks = compoundTag.getInt("Warmup");
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Warmup", this.warmupDelayTicks);
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.clientSideAttackStarted) {
                this.lifeTicks--;
                if (this.lifeTicks == 14) {
                    for (int i = 0; i < 12; i++) {
                        this.level.addParticle(ParticleTypes.CRIT, getX() + (((this.random.nextDouble() * 2.0d) - 1.0d) * getBbWidth() * 0.5d), getY() + 0.05d + this.random.nextDouble() + 1.0d, getZ() + (((this.random.nextDouble() * 2.0d) - 1.0d) * getBbWidth() * 0.5d), ((this.random.nextDouble() * 2.0d) - 1.0d) * 0.3d, 0.3d + (this.random.nextDouble() * 0.3d), ((this.random.nextDouble() * 2.0d) - 1.0d) * 0.3d);
                    }
                    return;
                }
                return;
            }
            return;
        }
        int i2 = this.warmupDelayTicks - 1;
        this.warmupDelayTicks = i2;
        if (i2 < 0) {
            if (this.warmupDelayTicks == -8) {
                Iterator<LivingEntity> it = this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.2d, 0.0d, 0.2d)).iterator();
                while (it.hasNext()) {
                    dealDamageTo(it.next());
                }
            }
            if (!this.sentSpikeEvent) {
                this.level.broadcastEntityEvent(this, (byte) 4);
                this.sentSpikeEvent = true;
            }
            int i3 = this.lifeTicks - 1;
            this.lifeTicks = i3;
            if (i3 < 0) {
                remove();
            }
        }
    }

    private void dealDamageTo(LivingEntity livingEntity) {
        LivingEntity owner = getOwner();
        if (!livingEntity.isAlive() || livingEntity.isInvulnerable() || livingEntity == owner) {
            return;
        }
        if (owner == null) {
            livingEntity.hurt(DamageSource.MAGIC, 6.0f);
        } else {
            if (owner.isAlliedTo(livingEntity)) {
                return;
            }
            livingEntity.hurt(DamageSource.indirectMagic(this, owner), 6.0f);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        super.handleEntityEvent(b);
        if (b == 4) {
            this.clientSideAttackStarted = true;
            if (!isSilent()) {
                this.level.playLocalSound(getX(), getY(), getZ(), SoundEvents.EVOKER_FANGS_ATTACK, getSoundSource(), 1.0f, (this.random.nextFloat() * 0.2f) + 0.85f, false);
            }
        }
    }

    public float getAnimationProgress(float f) {
        if (!this.clientSideAttackStarted) {
            return 0.0f;
        }
        int i = this.lifeTicks - 2;
        if (i <= 0) {
            return 1.0f;
        }
        return 1.0f - ((i - f) / 20.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
