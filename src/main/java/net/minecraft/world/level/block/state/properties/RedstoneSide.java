package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/RedstoneSide.class */
public enum RedstoneSide implements StringRepresentable {
    UP("up"),
    SIDE("side"),
    NONE("none");

    private final String name;

    RedstoneSide(String str) {
        this.name = str;
    }

    @Override // java.lang.Enum
    public String toString() {
        return getSerializedName();
    }

    @Override // net.minecraft.util.StringRepresentable
    public String getSerializedName() {
        return this.name;
    }

    public boolean isConnected() {
        return this != NONE;
    }
}
