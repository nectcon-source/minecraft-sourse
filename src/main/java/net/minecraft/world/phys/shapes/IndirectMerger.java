package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.phys.shapes.IndexMerger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/IndirectMerger.class */
public final class IndirectMerger implements IndexMerger {
    private final DoubleArrayList result;
    private final IntArrayList firstIndices;
    private final IntArrayList secondIndices;

    protected IndirectMerger(DoubleList doubleList, DoubleList doubleList2, boolean z, boolean z2) {
        double d;
        int i = 0;
        int i2 = 0;
        double d2 = Double.NaN;
        int size = doubleList.size();
        int size2 = doubleList2.size();
        int i3 = size + size2;
        this.result = new DoubleArrayList(i3);
        this.firstIndices = new IntArrayList(i3);
        this.secondIndices = new IntArrayList(i3);
        while (true) {
            boolean z3 = i < size;
            boolean z4 = i2 < size2;
            if (!z3 && !z4) {
                break;
            }
            boolean z5 = z3 && (!z4 || doubleList.getDouble(i) < doubleList2.getDouble(i2) + 1.0E-7d);
            if (z5) {
                int i4 = i;
                i++;
                d = doubleList.getDouble(i4);
            } else {
                int i5 = i2;
                i2++;
                d = doubleList2.getDouble(i5);
            }
            double d3 = d;
            if ((i != 0 && z3) || z5 || z2) {
                if ((i2 != 0 && z4) || !z5 || z) {
                    if (d2 < d3 - 1.0E-7d) {
                        this.firstIndices.add(i - 1);
                        this.secondIndices.add(i2 - 1);
                        this.result.add(d3);
                        d2 = d3;
                    } else if (!this.result.isEmpty()) {
                        this.firstIndices.set(this.firstIndices.size() - 1, i - 1);
                        this.secondIndices.set(this.secondIndices.size() - 1, i2 - 1);
                    }
                }
            }
        }
        if (this.result.isEmpty()) {
            this.result.add(Math.min(doubleList.getDouble(size - 1), doubleList2.getDouble(size2 - 1)));
        }
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        for (int i = 0; i < this.result.size() - 1; i++) {
            if (!indexConsumer.merge(this.firstIndices.getInt(i), this.secondIndices.getInt(i), i)) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public DoubleList getList() {
        return this.result;
    }
}
