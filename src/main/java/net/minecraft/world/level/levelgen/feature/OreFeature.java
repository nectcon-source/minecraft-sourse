package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/OreFeature.class */
public class OreFeature extends Feature<OreConfiguration> {
    public OreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, OreConfiguration oreConfiguration) {
        float nextFloat = random.nextFloat() * 3.1415927f;
        float f = oreConfiguration.size / 8.0f;
        int ceil = Mth.ceil((((oreConfiguration.size / 16.0f) * 2.0f) + 1.0f) / 2.0f);
        double x = blockPos.getX() + (Math.sin(nextFloat) * f);
        double x2 = blockPos.getX() - (Math.sin(nextFloat) * f);
        double z = blockPos.getZ() + (Math.cos(nextFloat) * f);
        double z2 = blockPos.getZ() - (Math.cos(nextFloat) * f);
        double y = (blockPos.getY() + random.nextInt(3)) - 2;
        double y2 = (blockPos.getY() + random.nextInt(3)) - 2;
        int x3 = (blockPos.getX() - Mth.ceil(f)) - ceil;
        int y3 = (blockPos.getY() - 2) - ceil;
        int z3 = (blockPos.getZ() - Mth.ceil(f)) - ceil;
        int ceil2 = 2 * (Mth.ceil(f) + ceil);
        int i = 2 * (2 + ceil);
        for (int i2 = x3; i2 <= x3 + ceil2; i2++) {
            for (int i3 = z3; i3 <= z3 + ceil2; i3++) {
                if (y3 <= worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, i2, i3)) {
                    return doPlace(worldGenLevel, random, oreConfiguration, x, x2, z, z2, y, y2, x3, y3, z3, ceil2, i);
                }
            }
        }
        return false;
    }

    protected boolean doPlace(LevelAccessor levelAccessor, Random random, OreConfiguration oreConfiguration, double d, double d2, double d3, double d4, double d5, double d6, int i, int i2, int i3, int i4, int i5) {
        int i6 = 0;
        BitSet bitSet = new BitSet(i4 * i5 * i4);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i7 = oreConfiguration.size;
        double[] dArr = new double[i7 * 4];
        for (int i8 = 0; i8 < i7; i8++) {
            float f = i8 / i7;
            double lerp = Mth.lerp(f, d, d2);
            double lerp2 = Mth.lerp(f, d5, d6);
            double lerp3 = Mth.lerp(f, d3, d4);
            double sin = (((Mth.sin(3.1415927f * f) + 1.0f) * ((random.nextDouble() * i7) / 16.0d)) + 1.0d) / 2.0d;
            dArr[(i8 * 4) + 0] = lerp;
            dArr[(i8 * 4) + 1] = lerp2;
            dArr[(i8 * 4) + 2] = lerp3;
            dArr[(i8 * 4) + 3] = sin;
        }
        for (int i9 = 0; i9 < i7 - 1; i9++) {
            if (dArr[(i9 * 4) + 3] > 0.0d) {
                for (int i10 = i9 + 1; i10 < i7; i10++) {
                    if (dArr[(i10 * 4) + 3] > 0.0d) {
                        double d7 = dArr[(i9 * 4) + 0] - dArr[(i10 * 4) + 0];
                        double d8 = dArr[(i9 * 4) + 1] - dArr[(i10 * 4) + 1];
                        double d9 = dArr[(i9 * 4) + 2] - dArr[(i10 * 4) + 2];
                        double d10 = dArr[(i9 * 4) + 3] - dArr[(i10 * 4) + 3];
                        if (d10 * d10 > (d7 * d7) + (d8 * d8) + (d9 * d9)) {
                            if (d10 > 0.0d) {
                                dArr[(i10 * 4) + 3] = -1.0d;
                            } else {
                                dArr[(i9 * 4) + 3] = -1.0d;
                            }
                        }
                    }
                }
            }
        }
        for (int i11 = 0; i11 < i7; i11++) {
            double d11 = dArr[(i11 * 4) + 3];
            if (d11 >= 0.0d) {
                double d12 = dArr[(i11 * 4) + 0];
                double d13 = dArr[(i11 * 4) + 1];
                double d14 = dArr[(i11 * 4) + 2];
                int max = Math.max(Mth.floor(d12 - d11), i);
                int max2 = Math.max(Mth.floor(d13 - d11), i2);
                int max3 = Math.max(Mth.floor(d14 - d11), i3);
                int max4 = Math.max(Mth.floor(d12 + d11), max);
                int max5 = Math.max(Mth.floor(d13 + d11), max2);
                int max6 = Math.max(Mth.floor(d14 + d11), max3);
                for (int i12 = max; i12 <= max4; i12++) {
                    double d15 = ((i12 + 0.5d) - d12) / d11;
                    if (d15 * d15 < 1.0d) {
                        for (int i13 = max2; i13 <= max5; i13++) {
                            double d16 = ((i13 + 0.5d) - d13) / d11;
                            if ((d15 * d15) + (d16 * d16) < 1.0d) {
                                for (int i14 = max3; i14 <= max6; i14++) {
                                    double d17 = ((i14 + 0.5d) - d14) / d11;
                                    if ((d15 * d15) + (d16 * d16) + (d17 * d17) < 1.0d) {
                                        int i15 = (i12 - i) + ((i13 - i2) * i4) + ((i14 - i3) * i4 * i5);
                                        if (!bitSet.get(i15)) {
                                            bitSet.set(i15);
                                            mutableBlockPos.set(i12, i13, i14);
                                            if (oreConfiguration.target.test(levelAccessor.getBlockState(mutableBlockPos), random)) {
                                                levelAccessor.setBlock(mutableBlockPos, oreConfiguration.state, 2);
                                                i6++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return i6 > 0;
    }
}
