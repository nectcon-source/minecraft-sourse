package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/synth/NormalNoise.class */
public class NormalNoise {
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;

    public static NormalNoise create(WorldgenRandom worldgenRandom, int i, DoubleList doubleList) {
        return new NormalNoise(worldgenRandom, i, doubleList);
    }

    private NormalNoise(WorldgenRandom worldgenRandom, int i, DoubleList doubleList) {
        this.first = PerlinNoise.create(worldgenRandom, i, doubleList);
        this.second = PerlinNoise.create(worldgenRandom, i, doubleList);
        int i2 = Integer.MAX_VALUE;
        int i3 = Integer.MIN_VALUE;
        DoubleListIterator it = doubleList.iterator();
        while (it.hasNext()) {
            int nextIndex = it.nextIndex();
            if (it.nextDouble() != 0.0d) {
                i2 = Math.min(i2, nextIndex);
                i3 = Math.max(i3, nextIndex);
            }
        }
        this.valueFactor = 0.16666666666666666d / expectedDeviation(i3 - i2);
    }

    private static double expectedDeviation(int i) {
        return 0.1d * (1.0d + (1.0d / (i + 1)));
    }

    public double getValue(double d, double d2, double d3) {
        return (this.first.getValue(d, d2, d3) + this.second.getValue(d * 1.0181268882175227d, d2 * 1.0181268882175227d, d3 * 1.0181268882175227d)) * this.valueFactor;
    }
}
