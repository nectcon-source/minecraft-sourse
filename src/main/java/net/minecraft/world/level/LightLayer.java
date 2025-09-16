package net.minecraft.world.level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/LightLayer.class */
public enum LightLayer {
    SKY(15),
    BLOCK(0);

    public final int surrounding;

    LightLayer(int i) {
        this.surrounding = i;
    }
}
