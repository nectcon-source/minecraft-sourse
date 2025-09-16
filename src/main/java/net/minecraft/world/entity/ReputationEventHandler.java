package net.minecraft.world.entity;

import net.minecraft.world.entity.ai.village.ReputationEventType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ReputationEventHandler.class */
public interface ReputationEventHandler {
    void onReputationEventFrom(ReputationEventType reputationEventType, Entity entity);
}
