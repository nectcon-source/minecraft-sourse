package net.minecraft.world.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ServerLevelAccessor.class */
public interface ServerLevelAccessor extends LevelAccessor {
    ServerLevel getLevel();

    default void addFreshEntityWithPassengers(Entity entity) {
        entity.getSelfAndPassengers().forEach(this::addFreshEntity);
    }
}
