package net.minecraft.world.level.levelgen;

import java.util.Random;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/WorldgenRandom.class */
public class WorldgenRandom extends Random {
    private int count;

    public WorldgenRandom() {
    }

    public WorldgenRandom(long j) {
        super(j);
    }

    public void consumeCount(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            next(1);
        }
    }

    @Override // java.util.Random
    protected int next(int i) {
        this.count++;
        return super.next(i);
    }

    public long setBaseChunkSeed(int i, int i2) {
        long j = (i * 341873128712L) + (i2 * 132897987541L);
        setSeed(j);
        return j;
    }

    public long setDecorationSeed(long j, int i, int i2) {
        setSeed(j);
        long nextLong = ((i * (nextLong() | 1)) + (i2 * (nextLong() | 1))) ^ j;
        setSeed(nextLong);
        return nextLong;
    }

    public long setFeatureSeed(long j, int i, int i2) {
        long j2 = j + i + (10000 * i2);
        setSeed(j2);
        return j2;
    }

    public long setLargeFeatureSeed(long j, int i, int i2) {
        setSeed(j);
        long nextLong = ((i * nextLong()) ^ (i2 * nextLong())) ^ j;
        setSeed(nextLong);
        return nextLong;
    }

    public long setLargeFeatureWithSalt(long j, int i, int i2, int i3) {
        long j2 = (i * 341873128712L) + (i2 * 132897987541L) + j + i3;
        setSeed(j2);
        return j2;
    }

    public static Random seedSlimeChunk(int i, int i2, long j, long j2) {
        return new Random(((((j + ((i * i) * 4987142)) + (i * 5947611)) + ((i2 * i2) * 4392871)) + (i2 * 389711)) ^ j2);
    }
}
