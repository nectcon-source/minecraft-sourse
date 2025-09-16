package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/PistonType.class */
public enum PistonType implements StringRepresentable {
    DEFAULT("normal"),
    STICKY("sticky");

    private final String name;

    PistonType(String str) {
        this.name = str;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.name;
    }

    @Override // net.minecraft.util.StringRepresentable
    public String getSerializedName() {
        return this.name;
    }
}
