package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/synth/PerlinNoise.class */
public class PerlinNoise implements SurfaceNoise {
    private final ImprovedNoise[] noiseLevels;
    private final DoubleList amplitudes;
    private final double lowestFreqValueFactor;
    private final double lowestFreqInputFactor;

    public PerlinNoise(WorldgenRandom worldgenRandom, IntStream intStream) {
        this(worldgenRandom, (List<Integer>) intStream.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinNoise(WorldgenRandom worldgenRandom, List<Integer> list) {
        this(worldgenRandom, (IntSortedSet) new IntRBTreeSet(list));
    }

    public static PerlinNoise create(WorldgenRandom worldgenRandom, int i, DoubleList doubleList) {
        return new PerlinNoise(worldgenRandom, (Pair<Integer, DoubleList>) Pair.of(Integer.valueOf(i), doubleList));
    }

    private static Pair<Integer, DoubleList> makeAmplitudes(IntSortedSet intSortedSet) {
        if (intSortedSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -intSortedSet.firstInt();
        int lastInt = i + intSortedSet.lastInt() + 1;
        if (lastInt < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        DoubleArrayList doubleArrayList = new DoubleArrayList(new double[lastInt]);
        IntBidirectionalIterator it = intSortedSet.iterator();
        while (it.hasNext()) {
            doubleArrayList.set(it.nextInt() + i, 1.0d);
        }
        return Pair.of(Integer.valueOf(-i), doubleArrayList);
    }

    private PerlinNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
        this(worldgenRandom, makeAmplitudes(intSortedSet));
    }

    private PerlinNoise(WorldgenRandom worldgenRandom, Pair<Integer, DoubleList> pair) {
        int intValue = ((Integer) pair.getFirst()).intValue();
        this.amplitudes = (DoubleList) pair.getSecond();
        ImprovedNoise improvedNoise = new ImprovedNoise(worldgenRandom);
        int size = this.amplitudes.size();
        int i = -intValue;
        this.noiseLevels = new ImprovedNoise[size];
        if (i >= 0 && i < size && this.amplitudes.getDouble(i) != 0.0d) {
            this.noiseLevels[i] = improvedNoise;
        }
        for (int i2 = i - 1; i2 >= 0; i2--) {
            if (i2 >= size) {
                worldgenRandom.consumeCount(262);
            } else if (this.amplitudes.getDouble(i2) != 0.0d) {
                this.noiseLevels[i2] = new ImprovedNoise(worldgenRandom);
            } else {
                worldgenRandom.consumeCount(262);
            }
        }
        if (i < size - 1) {
            WorldgenRandom worldgenRandom2 = new WorldgenRandom((long) (improvedNoise.noise(0.0d, 0.0d, 0.0d, 0.0d, 0.0d) * 9.223372036854776E18d));
            for (int i3 = i + 1; i3 < size; i3++) {
                if (i3 < 0) {
                    worldgenRandom2.consumeCount(262);
                } else if (this.amplitudes.getDouble(i3) != 0.0d) {
                    this.noiseLevels[i3] = new ImprovedNoise(worldgenRandom2);
                } else {
                    worldgenRandom2.consumeCount(262);
                }
            }
        }
        this.lowestFreqInputFactor = Math.pow(2.0d, -i);
        this.lowestFreqValueFactor = Math.pow(2.0d, size - 1) / (Math.pow(2.0d, size) - 1.0d);
    }

    public double getValue(double d, double d2, double d3) {
        return getValue(d, d2, d3, 0.0d, 0.0d, false);
    }

    public double getValue(double d, double d2, double d3, double d4, double d5, boolean z) {
        double d6 = 0.0d;
        double d7 = this.lowestFreqInputFactor;
        double d8 = this.lowestFreqValueFactor;
        for (int i = 0; i < this.noiseLevels.length; i++) {
            ImprovedNoise improvedNoise = this.noiseLevels[i];
            if (improvedNoise != null) {
                d6 += this.amplitudes.getDouble(i) * improvedNoise.noise(wrap(d * d7), z ? -improvedNoise.yo : wrap(d2 * d7), wrap(d3 * d7), d4 * d7, d5 * d7) * d8;
            }
            d7 *= 2.0d;
            d8 /= 2.0d;
        }
        return d6;
    }

    @Nullable
    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[(this.noiseLevels.length - 1) - i];
    }

    public static double wrap(double d) {
        return d - (Mth.lfloor((d / 3.3554432E7d) + 0.5d) * 3.3554432E7d);
    }

    @Override // net.minecraft.world.level.levelgen.synth.SurfaceNoise
    public double getSurfaceNoiseValue(double d, double d2, double d3, double d4) {
        return getValue(d, d2, 0.0d, d3, d4, false);
    }
}
