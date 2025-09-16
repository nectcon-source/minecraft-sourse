package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/NonOverlappingMerger.class */
public class NonOverlappingMerger extends AbstractDoubleList implements IndexMerger {
    private final DoubleList lower;
    private final DoubleList upper;
    private final boolean swap;

    public NonOverlappingMerger(DoubleList doubleList, DoubleList doubleList2, boolean z) {
        this.lower = doubleList;
        this.upper = doubleList2;
        this.swap = z;
    }

    public int size() {
        return this.lower.size() + this.upper.size();
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        if (this.swap) {
            return forNonSwappedIndexes((i, i2, i3) -> {
                return indexConsumer.merge(i2, i, i3);
            });
        }
        return forNonSwappedIndexes(indexConsumer);
    }

    private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int size = this.lower.size() - 1;
        for (int i = 0; i < size; i++) {
            if (!indexConsumer.merge(i, -1, i)) {
                return false;
            }
        }
        if (!indexConsumer.merge(size, -1, size)) {
            return false;
        }
        for (int i2 = 0; i2 < this.upper.size(); i2++) {
            if (!indexConsumer.merge(size, i2, size + 1 + i2)) {
                return false;
            }
        }
        return true;
    }

    public double getDouble(int i) {
        if (i < this.lower.size()) {
            return this.lower.getDouble(i);
        }
        return this.upper.getDouble(i - this.lower.size());
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public DoubleList getList() {
        return this;
    }
}
