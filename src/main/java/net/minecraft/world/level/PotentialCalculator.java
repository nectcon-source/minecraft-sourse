package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/PotentialCalculator.class */
public class PotentialCalculator {
    private final List<PointCharge> charges = Lists.newArrayList();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/PotentialCalculator$PointCharge.class */
    static class PointCharge {
        private final BlockPos pos;
        private final double charge;

        public PointCharge(BlockPos blockPos, double d) {
            this.pos = blockPos;
            this.charge = d;
        }

        public double getPotentialChange(BlockPos blockPos) {
            double distSqr = this.pos.distSqr(blockPos);
            if (distSqr == 0.0d) {
                return Double.POSITIVE_INFINITY;
            }
            return this.charge / Math.sqrt(distSqr);
        }
    }

    public void addCharge(BlockPos blockPos, double d) {
        if (d != 0.0d) {
            this.charges.add(new PointCharge(blockPos, d));
        }
    }

    public double getPotentialEnergyChange(BlockPos blockPos, double d) {
        if (d == 0.0d) {
            return 0.0d;
        }
        double d2 = 0.0d;
        Iterator<PointCharge> it = this.charges.iterator();
        while (it.hasNext()) {
            d2 += it.next().getPotentialChange(blockPos);
        }
        return d2 * d;
    }
}
