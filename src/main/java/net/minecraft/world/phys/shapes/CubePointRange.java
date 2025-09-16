package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/CubePointRange.class */
public class CubePointRange extends AbstractDoubleList {
    private final int parts;

    CubePointRange(int i) {
        this.parts = i;
    }

    public double getDouble(int i) {
        return i / this.parts;
    }

    public int size() {
        return this.parts + 1;
    }
}
