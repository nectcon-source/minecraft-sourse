package net.minecraft.world.entity.animal;

import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Pufferfish.class */
public class Pufferfish extends AbstractFish {
    private int inflateCounter;
    private int deflateTimer;
    private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(Pufferfish.class, EntityDataSerializers.INT);
    private static final Predicate<LivingEntity> NO_SPECTATORS_AND_NO_WATER_MOB = livingEntity -> {
        if (livingEntity == null) {
            return false;
        }
        return (((livingEntity instanceof Player) && (livingEntity.isSpectator() || ((Player) livingEntity).isCreative())) || livingEntity.getMobType() == MobType.WATER) ? false : true;
    };

    public Pufferfish(EntityType<? extends Pufferfish> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PUFF_STATE, 0);
    }

    public int getPuffState() {
        return ((Integer) this.entityData.get(PUFF_STATE)).intValue();
    }

    public void setPuffState(int i) {
        this.entityData.set(PUFF_STATE, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (PUFF_STATE.equals(entityDataAccessor)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("PuffState", getPuffState());
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setPuffState(compoundTag.getInt("PuffState"));
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected ItemStack getBucketItemStack() {
        return new ItemStack(Items.PUFFERFISH_BUCKET);
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PufferfishPuffGoal(this));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        if (!this.level.isClientSide && isAlive() && isEffectiveAi()) {
            if (this.inflateCounter > 0) {
                if (getPuffState() == 0) {
                    playSound(SoundEvents.PUFFER_FISH_BLOW_UP, getSoundVolume(), getVoicePitch());
                    setPuffState(1);
                } else if (this.inflateCounter > 40 && getPuffState() == 1) {
                    playSound(SoundEvents.PUFFER_FISH_BLOW_UP, getSoundVolume(), getVoicePitch());
                    setPuffState(2);
                }
                this.inflateCounter++;
            } else if (getPuffState() != 0) {
                if (this.deflateTimer > 60 && getPuffState() == 2) {
                    playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, getSoundVolume(), getVoicePitch());
                    setPuffState(1);
                } else if (this.deflateTimer > 100 && getPuffState() == 1) {
                    playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, getSoundVolume(), getVoicePitch());
                    setPuffState(0);
                }
                this.deflateTimer++;
            }
        }
        super.tick();
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (isAlive() && getPuffState() > 0) {
            for (Mob mob : this.level.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(0.3d), NO_SPECTATORS_AND_NO_WATER_MOB)) {
                if (mob.isAlive()) {
                    touch(mob);
                }
            }
        }
    }

    private void touch(Mob mob) {
        int puffState = getPuffState();
        if (mob.hurt(DamageSource.mobAttack(this), 1 + puffState)) {
            mob.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * puffState, 0));
            playSound(SoundEvents.PUFFER_FISH_STING, 1.0f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void playerTouch(Player player) {
        int puffState = getPuffState();
        if ((player instanceof ServerPlayer) && puffState > 0 && player.hurt(DamageSource.mobAttack(this), 1 + puffState)) {
            if (!isSilent()) {
                ((ServerPlayer) player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0f));
            }
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * puffState, 0));
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PUFFER_FISH_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.PUFFER_FISH_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PUFFER_FISH_HURT;
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected SoundEvent getFlopSound() {
        return SoundEvents.PUFFER_FISH_FLOP;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getScale(getPuffState()));
    }

    private static float getScale(int i) {
        switch (i) {
            case 0:
                return 0.5f;
            case 1:
                return 0.7f;
            default:
                return 1.0f;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Pufferfish$PufferfishPuffGoal.class */
    static class PufferfishPuffGoal extends Goal {
        private final Pufferfish fish;

        public PufferfishPuffGoal(Pufferfish pufferfish) {
            this.fish = pufferfish;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !this.fish.level.getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0d), Pufferfish.NO_SPECTATORS_AND_NO_WATER_MOB).isEmpty();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.fish.inflateCounter = 1;
            this.fish.deflateTimer = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.fish.inflateCounter = 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return !this.fish.level.getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0d), Pufferfish.NO_SPECTATORS_AND_NO_WATER_MOB).isEmpty();
        }
    }
}
