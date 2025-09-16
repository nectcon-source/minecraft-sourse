package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/NeutralMob.class */
public interface NeutralMob {
    int getRemainingPersistentAngerTime();

    void setRemainingPersistentAngerTime(int i);

    @Nullable
    UUID getPersistentAngerTarget();

    void setPersistentAngerTarget(@Nullable UUID uuid);

    void startPersistentAngerTimer();

    void setLastHurtByMob(@Nullable LivingEntity livingEntity);

    void setLastHurtByPlayer(@Nullable Player player);

    void setTarget(@Nullable LivingEntity livingEntity);

    @Nullable
    LivingEntity getTarget();

    default void addPersistentAngerSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("AngerTime", getRemainingPersistentAngerTime());
        if (getPersistentAngerTarget() != null) {
            compoundTag.putUUID("AngryAt", getPersistentAngerTarget());
        }
    }

    default void readPersistentAngerSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
        setRemainingPersistentAngerTime(compoundTag.getInt("AngerTime"));
        if (!compoundTag.hasUUID("AngryAt")) {
            setPersistentAngerTarget(null);
            return;
        }
        UUID uuid = compoundTag.getUUID("AngryAt");
        setPersistentAngerTarget(uuid);
        Entity entity = serverLevel.getEntity(uuid);
        if (entity == null) {
            return;
        }
        if (entity instanceof Mob) {
            setLastHurtByMob((Mob) entity);
        }
        if (entity.getType() == EntityType.PLAYER) {
            setLastHurtByPlayer((Player) entity);
        }
    }

    default void updatePersistentAnger(ServerLevel serverLevel, boolean z) {
        LivingEntity target = getTarget();
        UUID persistentAngerTarget = getPersistentAngerTarget();
        if ((target == null || target.isDeadOrDying()) && persistentAngerTarget != null && (serverLevel.getEntity(persistentAngerTarget) instanceof Mob)) {
            stopBeingAngry();
            return;
        }
        if (target != null && !Objects.equals(persistentAngerTarget, target.getUUID())) {
            setPersistentAngerTarget(target.getUUID());
            startPersistentAngerTimer();
        }
        if (getRemainingPersistentAngerTime() > 0) {
            if (target == null || target.getType() != EntityType.PLAYER || !z) {
                setRemainingPersistentAngerTime(getRemainingPersistentAngerTime() - 1);
                if (getRemainingPersistentAngerTime() == 0) {
                    stopBeingAngry();
                }
            }
        }
    }

    default boolean isAngryAt(LivingEntity livingEntity) {
        if (!EntitySelector.ATTACK_ALLOWED.test(livingEntity)) {
            return false;
        }
        if (livingEntity.getType() == EntityType.PLAYER && isAngryAtAllPlayers(livingEntity.level)) {
            return true;
        }
        return livingEntity.getUUID().equals(getPersistentAngerTarget());
    }

    default boolean isAngryAtAllPlayers(Level level) {
        return level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && isAngry() && getPersistentAngerTarget() == null;
    }

    default boolean isAngry() {
        return getRemainingPersistentAngerTime() > 0;
    }

    default void playerDied(Player player) {
        if (!player.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS) || !player.getUUID().equals(getPersistentAngerTarget())) {
            return;
        }
        stopBeingAngry();
    }

    default void forgetCurrentTargetAndRefreshUniversalAnger() {
        stopBeingAngry();
        startPersistentAngerTimer();
    }

    default void stopBeingAngry() {
        setLastHurtByMob(null);
        setPersistentAngerTarget(null);
        setTarget(null);
        setRemainingPersistentAngerTime(0);
    }
}
