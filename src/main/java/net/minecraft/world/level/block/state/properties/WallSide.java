package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/WallSide.class */
public enum WallSide implements StringRepresentable {
    NONE("none"),
    LOW("low"),
    TALL("tall");

    private final String name;

    WallSide(String str) {
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
}
