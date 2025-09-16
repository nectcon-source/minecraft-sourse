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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/CanyonWorldCarver.class */
public class CanyonWorldCarver extends WorldCarver<ProbabilityFeatureConfiguration> {

    /* renamed from: rs */
    private final float[] rs;

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    public /* bridge */ /* synthetic */ boolean carve(ChunkAccess chunkAccess, Function function, Random random, int i, int i2, int i3, int i4, int i5, BitSet bitSet, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        return carve2(chunkAccess, (Function<BlockPos, Biome>) function, random, i, i2, i3, i4, i5, bitSet, probabilityFeatureConfiguration);
    }

    public CanyonWorldCarver(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec, 256);
        this.rs = new float[1024];
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    public boolean isStartChunk(Random random, int i, int i2, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        return random.nextFloat() <= probabilityFeatureConfiguration.probability;
    }

    /* renamed from: carve, reason: avoid collision after fix types in other method */
    public boolean carve2(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int i2, int i3, int i4, int i5, BitSet bitSet, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        int range = ((getRange() * 2) - 1) * 16;
        genCanyon(chunkAccess, function, random.nextLong(), i, i4, i5, (i2 * 16) + random.nextInt(16), random.nextInt(random.nextInt(40) + 8) + 20, (i3 * 16) + random.nextInt(16), ((random.nextFloat() * 2.0f) + random.nextFloat()) * 2.0f, random.nextFloat() * 6.2831855f, ((random.nextFloat() - 0.5f) * 2.0f) / 8.0f, 0, range - random.nextInt(range / 4), 3.0d, bitSet);
        return true;
    }

    private void genCanyon(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long j, int i, int i2, int i3, double d, double d2, double d3, float f, float f2, float f3, int i4, int i5, double d4, BitSet bitSet) {
        Random random = new Random(j);
        float f4 = 1.0f;
        for (int i6 = 0; i6 < 256; i6++) {
            if (i6 == 0 || random.nextInt(3) == 0) {
                f4 = 1.0f + (random.nextFloat() * random.nextFloat());
            }
            this.rs[i6] = f4 * f4;
        }
        float f5 = 0.0f;
        float f6 = 0.0f;
        for (int i7 = i4; i7 < i5; i7++) {
            double sin = 1.5d + (Mth.sin((i7 * 3.1415927f) / i5) * f);
            double d5 = sin * d4;
            double nextFloat = sin * ((random.nextFloat() * 0.25d) + 0.75d);
            double nextFloat2 = d5 * ((random.nextFloat() * 0.25d) + 0.75d);
            float cos = Mth.cos(f3);
            d += Mth.cos(f2) * cos;
            d2 += Mth.sin(f3);
            d3 += Mth.sin(f2) * cos;
            f3 = (f3 * 0.7f) + (f6 * 0.05f);
            f2 += f5 * 0.05f;
            f6 = (f6 * 0.8f) + ((random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f);
            f5 = (f5 * 0.5f) + ((random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f);
            if (random.nextInt(4) != 0) {
                if (!canReach(i2, i3, d, d3, i7, i5, f)) {
                    return;
                } else {
                    carveSphere(chunkAccess, function, j, i, i2, i3, d, d2, d3, nextFloat, nextFloat2, bitSet);
                }
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    protected boolean skip(double d, double d2, double d3, int i) {
        return (((d * d) + (d3 * d3)) * ((double) this.rs[i - 1])) + ((d2 * d2) / 6.0d) >= 1.0d;
    }
}
