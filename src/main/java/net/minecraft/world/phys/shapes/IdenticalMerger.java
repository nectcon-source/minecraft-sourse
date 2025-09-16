package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/IdenticalMerger.class */
public class IdenticalMerger implements IndexMerger {
    private final DoubleList coords;

    public IdenticalMerger(DoubleList doubleList) {
        this.coords = doubleList;
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        for (int i = 0; i <= this.coords.size(); i++) {
            if (!indexConsumer.merge(i, i, i)) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public DoubleList getList() {
        return this.coords;
    }
}
