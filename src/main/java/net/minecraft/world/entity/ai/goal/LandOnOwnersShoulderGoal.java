package net.minecraft.world.entity.ai.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/LandOnOwnersShoulderGoal.class */
public class LandOnOwnersShoulderGoal extends Goal {
    private final ShoulderRidingEntity entity;
    private ServerPlayer owner;
    private boolean isSittingOnShoulder;

    public LandOnOwnersShoulderGoal(ShoulderRidingEntity shoulderRidingEntity) {
        this.entity = shoulderRidingEntity;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        ServerPlayer serverPlayer = (ServerPlayer) this.entity.getOwner();
        return !this.entity.isOrderedToSit() && (serverPlayer != null && !serverPlayer.isSpectator() && !serverPlayer.abilities.flying && !serverPlayer.isInWater()) && this.entity.canSitOnShoulder();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean isInterruptable() {
        return !this.isSittingOnShoulder;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.owner = (ServerPlayer) this.entity.getOwner();
        this.isSittingOnShoulder = false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (!this.isSittingOnShoulder && !this.entity.isInSittingPose() && !this.entity.isLeashed() && this.entity.getBoundingBox().intersects(this.owner.getBoundingBox())) {
            this.isSittingOnShoulder = this.entity.setEntityOnShoulder(this.owner);
        }
    }
}
