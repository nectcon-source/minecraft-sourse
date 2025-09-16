package net.minecraft.world.entity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/PlayerRideableJumping.class */
public interface PlayerRideableJumping {
    void onPlayerJump(int i);

    boolean canJump();

    void handleStartJump(int i);

    void handleStopJump();
}
