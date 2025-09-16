package net.minecraft.world.level.border;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/border/BorderStatus.class */
public enum BorderStatus {
    GROWING(4259712),
    SHRINKING(16724016),
    STATIONARY(2138367);

    private final int color;

    BorderStatus(int i) {
        this.color = i;
    }

    public int getColor() {
        return this.color;
    }
}
