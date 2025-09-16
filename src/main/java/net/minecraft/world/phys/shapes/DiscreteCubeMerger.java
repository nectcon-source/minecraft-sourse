package net.minecraft.world.phys.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.IndexMerger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/DiscreteCubeMerger.class */
public final class DiscreteCubeMerger implements IndexMerger {
    private final CubePointRange result;
    private final int firstSize;
    private final int secondSize;
    private final int gcd;

    DiscreteCubeMerger(int i, int i2) {
        this.result = new CubePointRange((int) Shapes.lcm(i, i2));
        this.firstSize = i;
        this.secondSize = i2;
        this.gcd = IntMath.gcd(i, i2);
    }

    @Override // net.minecraft.world.phys.shapes.IndexMerger
    public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
        int i = this.firstSize / this.gcd;
        int i2 = this.secondSize / this.gcd;
        for (int i3 = 0; i3 <= this.result.size(); i3++) {
            if (!indexConsumer.merge(i3 / i2, i3 / i, i3)) {
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
