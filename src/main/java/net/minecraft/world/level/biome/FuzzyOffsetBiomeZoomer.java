package net.minecraft.world.level.biome;

import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.biome.BiomeManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/FuzzyOffsetBiomeZoomer.class */
public enum FuzzyOffsetBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override // net.minecraft.world.level.biome.BiomeZoomer
    public Biome getBiome(long j, int i, int i2, int i3, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        int i4 = (i - 2) >> 2;
        int i5 = (i2 - 2) >> 2;
        int i6 = (i3 - 2) >> 2;
        double d = (i4 & 3) / 4.0d;
        double d2 = (i5 & 3) / 4.0d;
        double d3 = (i6 & 3) / 4.0d;
        double[] dArr = new double[8];
        for (int i7 = 0; i7 < 8; i7++) {
            boolean z = (i7 & 4) == 0;
            boolean z2 = (i7 & 2) == 0;
            boolean z3 = (i7 & 1) == 0;
            dArr[i7] = getFiddledDistance(j, z ? i4 : i4 + 1, z2 ? i5 : i5 + 1, z3 ? i6 : i6 + 1, z ? d : d - 1.0d, z2 ? d2 : d2 - 1.0d, z3 ? d3 : d3 - 1.0d);
        }
        int i8 = 0;
        double d4 = dArr[0];
        for (int i9 = 1; i9 < 8; i9++) {
            if (d4 > dArr[i9]) {
                i8 = i9;
                d4 = dArr[i9];
            }
        }
        return noiseBiomeSource.getNoiseBiome((i8 & 4) == 0 ? i4 : i4 + 1, (i8 & 2) == 0 ? i5 : i5 + 1, (i8 & 1) == 0 ? i6 : i6 + 1);
    }

    private static double getFiddledDistance(long j, int i, int i2, int i3, double d, double d2, double d3) {
        long next = LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(j, i), i2), i3), i), i2), i3);
        double fiddle = getFiddle(next);
        long next2 = LinearCongruentialGenerator.next(next, j);
        return sqr(d3 + getFiddle(LinearCongruentialGenerator.next(next2, j))) + sqr(d2 + getFiddle(next2)) + sqr(d + fiddle);
    }

    private static double getFiddle(long j) {
        return ((((int) Math.floorMod(j >> 24, 1024L)) / 1024.0d) - 0.5d) * 0.9d;
    }

    private static double sqr(double d) {
        return d * d;
    }
}
