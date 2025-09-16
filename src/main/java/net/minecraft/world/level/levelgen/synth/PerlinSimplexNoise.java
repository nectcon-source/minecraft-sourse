package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/synth/PerlinSimplexNoise.class */
public class PerlinSimplexNoise implements SurfaceNoise {
    private final SimplexNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public PerlinSimplexNoise(WorldgenRandom worldgenRandom, IntStream intStream) {
        this(worldgenRandom, (List<Integer>) intStream.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinSimplexNoise(WorldgenRandom worldgenRandom, List<Integer> list) {
        this(worldgenRandom, (IntSortedSet) new IntRBTreeSet(list));
    }

    private PerlinSimplexNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
        if (intSortedSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -intSortedSet.firstInt();
        int lastInt = intSortedSet.lastInt();
        int i2 = i + lastInt + 1;
        if (i2 < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        SimplexNoise simplexNoise = new SimplexNoise(worldgenRandom);
        this.noiseLevels = new SimplexNoise[i2];
        if (lastInt >= 0 && lastInt < i2 && intSortedSet.contains(0)) {
            this.noiseLevels[lastInt] = simplexNoise;
        }
        for (int i3 = lastInt + 1; i3 < i2; i3++) {
            if (i3 >= 0 && intSortedSet.contains(lastInt - i3)) {
                this.noiseLevels[i3] = new SimplexNoise(worldgenRandom);
            } else {
                worldgenRandom.consumeCount(262);
            }
        }
        if (lastInt > 0) {
            WorldgenRandom worldgenRandom2 = new WorldgenRandom((long) (simplexNoise.getValue(simplexNoise.xo, simplexNoise.yo, simplexNoise.zo) * 9.223372036854776E18d));
            for (int i4 = lastInt - 1; i4 >= 0; i4--) {
                if (i4 < i2 && intSortedSet.contains(lastInt - i4)) {
                    this.noiseLevels[i4] = new SimplexNoise(worldgenRandom2);
                } else {
                    worldgenRandom2.consumeCount(262);
                }
            }
        }
        this.highestFreqInputFactor = Math.pow(2.0d, lastInt);
        this.highestFreqValueFactor = 1.0d / (Math.pow(2.0d, i2) - 1.0d);
    }

    public double getValue(double d, double d2, boolean z) {
        double d3 = 0.0d;
        double d4 = this.highestFreqInputFactor;
        double d5 = this.highestFreqValueFactor;
        for (SimplexNoise simplexNoise : this.noiseLevels) {
            if (simplexNoise != null) {
                d3 += simplexNoise.getValue((d * d4) + (z ? simplexNoise.xo : 0.0d), (d2 * d4) + (z ? simplexNoise.yo : 0.0d)) * d5;
            }
            d4 /= 2.0d;
            d5 *= 2.0d;
        }
        return d3;
    }

    @Override // net.minecraft.world.level.levelgen.synth.SurfaceNoise
    public double getSurfaceNoiseValue(double d, double d2, double d3, double d4) {
        return getValue(d, d2, true) * 0.55d;
    }
}
