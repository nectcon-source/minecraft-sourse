package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/BellAttachType.class */
public enum BellAttachType implements StringRepresentable {
    FLOOR("floor"),
    CEILING("ceiling"),
    SINGLE_WALL("single_wall"),
    DOUBLE_WALL("double_wall");

    private final String name;

    BellAttachType(String str) {
        this.name = str;
    }

    @Override // net.minecraft.util.StringRepresentable
    public String getSerializedName() {
        return this.name;
    }
}
