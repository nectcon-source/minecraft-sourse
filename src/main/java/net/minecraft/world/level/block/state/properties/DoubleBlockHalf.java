package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/DoubleBlockHalf.class */
public enum DoubleBlockHalf implements StringRepresentable {
    UPPER,
    LOWER;

    @Override // java.lang.Enum
    public String toString() {
        return getSerializedName();
    }

    @Override // net.minecraft.util.StringRepresentable
    public String getSerializedName() {
        return this == UPPER ? "upper" : "lower";
    }
}
