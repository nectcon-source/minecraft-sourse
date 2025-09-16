package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/TamableAnimal.class */
public abstract class TamableAnimal extends Animal {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID);
    private boolean orderedToSit;

    protected TamableAnimal(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        reassessTameGoals();
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", getOwnerUUID());
        }
        compoundTag.putBoolean("Sitting", this.orderedToSit);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        UUID convertMobOwnerIfNecessary;
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.hasUUID("Owner")) {
            convertMobOwnerIfNecessary = compoundTag.getUUID("Owner");
        } else {
            convertMobOwnerIfNecessary = OldUsersConverter.convertMobOwnerIfNecessary(getServer(), compoundTag.getString("Owner"));
        }
        if (convertMobOwnerIfNecessary != null) {
            try {
                setOwnerUUID(convertMobOwnerIfNecessary);
                setTame(true);
            } catch (Throwable th) {
                setTame(false);
            }
        }
        this.orderedToSit = compoundTag.getBoolean("Sitting");
        setInSittingPose(this.orderedToSit);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return !isLeashed();
    }

    protected void spawnTamingParticles(boolean z) {
        ParticleOptions particleOptions = ParticleTypes.HEART;
        if (!z) {
            particleOptions = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 7; i++) {
            this.level.addParticle(particleOptions, getRandomX(1.0d), getRandomY() + 0.5d, getRandomZ(1.0d), this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 7) {
            spawnTamingParticles(true);
        } else if (b == 6) {
            spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(b);
        }
    }

    public boolean isTame() {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & 4) != 0;
    }

    public void setTame(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue();
        if (z) {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (byteValue | 4)));
        } else {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (byteValue & (-5))));
        }
        reassessTameGoals();
    }

    protected void reassessTameGoals() {
    }

    public boolean isInSittingPose() {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & 1) != 0;
    }

    public void setInSittingPose(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue();
        if (z) {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (byteValue | 1)));
        } else {
            this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (byteValue & (-2))));
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        return (UUID) ((Optional) this.entityData.get(DATA_OWNERUUID_ID)).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    public void tame(Player player) {
        setTame(true);
        setOwnerUUID(player.getUUID());
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer) player, this);
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID ownerUUID = getOwnerUUID();
            if (ownerUUID == null) {
                return null;
            }
            return this.level.getPlayerByUUID(ownerUUID);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public boolean canAttack(LivingEntity livingEntity) {
        if (isOwnedBy(livingEntity)) {
            return false;
        }
        return super.canAttack(livingEntity);
    }

    public boolean isOwnedBy(LivingEntity livingEntity) {
        return livingEntity == getOwner();
    }

    public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public Team getTeam() {
        LivingEntity owner;
        if (isTame() && (owner = getOwner()) != null) {
            return owner.getTeam();
        }
        return super.getTeam();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAlliedTo(Entity entity) {
        if (isTame()) {
            LivingEntity owner = getOwner();
            if (entity == owner) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(entity);
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        if (!this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && (getOwner() instanceof ServerPlayer)) {
            getOwner().sendMessage(getCombatTracker().getDeathMessage(), Util.NIL_UUID);
        }
        super.die(damageSource);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean z) {
        this.orderedToSit = z;
    }
}
