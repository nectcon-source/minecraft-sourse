package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/CaveWorldCarver.class */
public class CaveWorldCarver extends WorldCarver<ProbabilityFeatureConfiguration> {
    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    public /* bridge */ /* synthetic */ boolean carve(ChunkAccess chunkAccess, Function function, Random random, int i, int i2, int i3, int i4, int i5, BitSet bitSet, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        return carve2(chunkAccess, (Function<BlockPos, Biome>) function, random, i, i2, i3, i4, i5, bitSet, probabilityFeatureConfiguration);
    }

    public CaveWorldCarver(Codec<ProbabilityFeatureConfiguration> codec, int i) {
        super(codec, i);
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    public boolean isStartChunk(Random random, int i, int i2, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        return random.nextFloat() <= probabilityFeatureConfiguration.probability;
    }

    /* renamed from: carve, reason: avoid collision after fix types in other method */
    public boolean carve2(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int i2, int i3, int i4, int i5, BitSet bitSet, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        int range = ((getRange() * 2) - 1) * 16;
        int nextInt = random.nextInt(random.nextInt(random.nextInt(getCaveBound()) + 1) + 1);
        for (int i6 = 0; i6 < nextInt; i6++) {
            double nextInt2 = (i2 * 16) + random.nextInt(16);
            double caveY = getCaveY(random);
            double nextInt3 = (i3 * 16) + random.nextInt(16);
            int i7 = 1;
            if (random.nextInt(4) == 0) {
                genRoom(chunkAccess, function, random.nextLong(), i, i4, i5, nextInt2, caveY, nextInt3, 1.0f + (random.nextFloat() * 6.0f), 0.5d, bitSet);
                i7 = 1 + random.nextInt(4);
            }
            for (int i8 = 0; i8 < i7; i8++) {
                genTunnel(chunkAccess, function, random.nextLong(), i, i4, i5, nextInt2, caveY, nextInt3, getThickness(random), random.nextFloat() * 6.2831855f, (random.nextFloat() - 0.5f) / 4.0f, 0, range - random.nextInt(range / 4), getYScale(), bitSet);
            }
        }
        return true;
    }

    protected int getCaveBound() {
        return 15;
    }

    protected float getThickness(Random random) {
        float nextFloat = (random.nextFloat() * 2.0f) + random.nextFloat();
        if (random.nextInt(10) == 0) {
            nextFloat *= (random.nextFloat() * random.nextFloat() * 3.0f) + 1.0f;
        }
        return nextFloat;
    }

    protected double getYScale() {
        return 1.0d;
    }

    protected int getCaveY(Random random) {
        return random.nextInt(random.nextInt(120) + 8);
    }

    protected void genRoom(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long j, int i, int i2, int i3, double d, double d2, double d3, float f, double d4, BitSet bitSet) {
        double sin = 1.5d + (Mth.sin(1.5707964f) * f);
        carveSphere(chunkAccess, function, j, i, i2, i3, d + 1.0d, d2, d3, sin, sin * d4, bitSet);
    }

    protected void genTunnel(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long j, int i, int i2, int i3, double d, double d2, double d3, float f, float f2, float f3, int i4, int i5, double d4, BitSet bitSet) {
        Random random = new Random(j);
        int nextInt = random.nextInt(i5 / 2) + (i5 / 4);
        boolean z = random.nextInt(6) == 0;
        float f4 = 0.0f;
        float f5 = 0.0f;
        for (int i6 = i4; i6 < i5; i6++) {
            double sin = 1.5d + (Mth.sin((3.1415927f * i6) / i5) * f);
            double d5 = sin * d4;
            float cos = Mth.cos(f3);
            d += Mth.cos(f2) * cos;
            d2 += Mth.sin(f3);
            d3 += Mth.sin(f2) * cos;
            f3 = (f3 * (z ? 0.92f : 0.7f)) + (f5 * 0.1f);
            f2 += f4 * 0.1f;
            f5 = (f5 * 0.9f) + ((random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f);
            f4 = (f4 * 0.75f) + ((random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f);
            if (i6 == nextInt && f > 1.0f) {
                genTunnel(chunkAccess, function, random.nextLong(), i, i2, i3, d, d2, d3, (random.nextFloat() * 0.5f) + 0.5f, f2 - 1.5707964f, f3 / 3.0f, i6, i5, 1.0d, bitSet);
                genTunnel(chunkAccess, function, random.nextLong(), i, i2, i3, d, d2, d3, (random.nextFloat() * 0.5f) + 0.5f, f2 + 1.5707964f, f3 / 3.0f, i6, i5, 1.0d, bitSet);
                return;
            } else {
                if (random.nextInt(4) != 0) {
                    if (!canReach(i2, i3, d, d3, i6, i5, f)) {
                        return;
                    } else {
                        carveSphere(chunkAccess, function, j, i, i2, i3, d, d2, d3, sin, d5, bitSet);
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    protected boolean skip(double d, double d2, double d3, int i) {
        return d2 <= -0.7d || ((d * d) + (d2 * d2)) + (d3 * d3) >= 1.0d;
    }
}
