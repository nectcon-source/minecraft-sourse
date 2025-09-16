package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/ChestType.class */
public enum ChestType implements StringRepresentable {
    SINGLE("single", 0),
    LEFT("left", 2),
    RIGHT("right", 1);

    public static final ChestType[] BY_ID = values();
    private final String name;
    private final int opposite;

    ChestType(String str, int i) {
        this.name = str;
        this.opposite = i;
    }

    @Override // net.minecraft.util.StringRepresentable
    public String getSerializedName() {
        return this.name;
    }

    public ChestType getOpposite() {
        return BY_ID[this.opposite];
    }
}
