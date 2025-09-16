package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/LakeFeature.class */
public class LakeFeature extends Feature<BlockStateConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateConfiguration blockStateConfiguration) {
        while (blockPos.getY() > 5 && worldGenLevel.isEmptyBlock(blockPos)) {
            blockPos = blockPos.below();
        }
        if (blockPos.getY() <= 4) {
            return false;
        }
        BlockPos below = blockPos.below(4);
        if (worldGenLevel.startsForFeature(SectionPos.of(below), StructureFeature.VILLAGE).findAny().isPresent()) {
            return false;
        }
        boolean[] zArr = new boolean[2048];
        int nextInt = random.nextInt(4) + 4;
        for (int i = 0; i < nextInt; i++) {
            double nextDouble = (random.nextDouble() * 6.0d) + 3.0d;
            double nextDouble2 = (random.nextDouble() * 4.0d) + 2.0d;
            double nextDouble3 = (random.nextDouble() * 6.0d) + 3.0d;
            double nextDouble4 = (random.nextDouble() * ((16.0d - nextDouble) - 2.0d)) + 1.0d + (nextDouble / 2.0d);
            double nextDouble5 = (random.nextDouble() * ((8.0d - nextDouble2) - 4.0d)) + 2.0d + (nextDouble2 / 2.0d);
            double nextDouble6 = (random.nextDouble() * ((16.0d - nextDouble3) - 2.0d)) + 1.0d + (nextDouble3 / 2.0d);
            for (int i2 = 1; i2 < 15; i2++) {
                for (int i3 = 1; i3 < 15; i3++) {
                    for (int i4 = 1; i4 < 7; i4++) {
                        double d = (i2 - nextDouble4) / (nextDouble / 2.0d);
                        double d2 = (i4 - nextDouble5) / (nextDouble2 / 2.0d);
                        double d3 = (i3 - nextDouble6) / (nextDouble3 / 2.0d);
                        if ((d * d) + (d2 * d2) + (d3 * d3) < 1.0d) {
                            zArr[(((i2 * 16) + i3) * 8) + i4] = true;
                        }
                    }
                }
            }
        }
        int i5 = 0;
        while (i5 < 16) {
            int i6 = 0;
            while (i6 < 16) {
                int i7 = 0;
                while (i7 < 8) {
                    if (!zArr[(((i5 * 16) + i6) * 8) + i7] && ((i5 < 15 && zArr[((((i5 + 1) * 16) + i6) * 8) + i7]) || ((i5 > 0 && zArr[((((i5 - 1) * 16) + i6) * 8) + i7]) || ((i6 < 15 && zArr[((((i5 * 16) + i6) + 1) * 8) + i7]) || ((i6 > 0 && zArr[(((i5 * 16) + (i6 - 1)) * 8) + i7]) || ((i7 < 7 && zArr[((((i5 * 16) + i6) * 8) + i7) + 1]) || (i7 > 0 && zArr[(((i5 * 16) + i6) * 8) + (i7 - 1)]))))))) {
                        Material material = worldGenLevel.getBlockState(below.offset(i5, i7, i6)).getMaterial();
                        if (i7 >= 4 && material.isLiquid()) {
                            return false;
                        }
                        if (i7 < 4 && !material.isSolid() && worldGenLevel.getBlockState(below.offset(i5, i7, i6)) != blockStateConfiguration.state) {
                            return false;
                        }
                    }
                    i7++;
                }
                i6++;
            }
            i5++;
        }
        for (int i8 = 0; i8 < 16; i8++) {
            for (int i9 = 0; i9 < 16; i9++) {
                int i10 = 0;
                while (i10 < 8) {
                    if (zArr[(((i8 * 16) + i9) * 8) + i10]) {
                        worldGenLevel.setBlock(below.offset(i8, i10, i9), i10 >= 4 ? AIR : blockStateConfiguration.state, 2);
                    }
                    i10++;
                }
            }
        }
        for (int i11 = 0; i11 < 16; i11++) {
            for (int i12 = 0; i12 < 16; i12++) {
                for (int i13 = 4; i13 < 8; i13++) {
                    if (zArr[(((i11 * 16) + i12) * 8) + i13]) {
                        BlockPos offset = below.offset(i11, i13 - 1, i12);
                        if (isDirt(worldGenLevel.getBlockState(offset).getBlock()) && worldGenLevel.getBrightness(LightLayer.SKY, below.offset(i11, i13, i12)) > 0) {
                            if (worldGenLevel.getBiome(offset).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(Blocks.MYCELIUM)) {
                                worldGenLevel.setBlock(offset, Blocks.MYCELIUM.defaultBlockState(), 2);
                            } else {
                                worldGenLevel.setBlock(offset, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
        if (blockStateConfiguration.state.getMaterial() == Material.LAVA) {
            int i14 = 0;
            while (i14 < 16) {
                int i15 = 0;
                while (i15 < 16) {
                    int i16 = 0;
                    while (i16 < 8) {
                        if ((!zArr[(((i14 * 16) + i15) * 8) + i16] && ((i14 < 15 && zArr[((((i14 + 1) * 16) + i15) * 8) + i16]) || ((i14 > 0 && zArr[((((i14 - 1) * 16) + i15) * 8) + i16]) || ((i15 < 15 && zArr[((((i14 * 16) + i15) + 1) * 8) + i16]) || ((i15 > 0 && zArr[(((i14 * 16) + (i15 - 1)) * 8) + i16]) || ((i16 < 7 && zArr[((((i14 * 16) + i15) * 8) + i16) + 1]) || (i16 > 0 && zArr[(((i14 * 16) + i15) * 8) + (i16 - 1)]))))))) && ((i16 < 4 || random.nextInt(2) != 0) && worldGenLevel.getBlockState(below.offset(i14, i16, i15)).getMaterial().isSolid())) {
                            worldGenLevel.setBlock(below.offset(i14, i16, i15), Blocks.STONE.defaultBlockState(), 2);
                        }
                        i16++;
                    }
                    i15++;
                }
                i14++;
            }
        }
        if (blockStateConfiguration.state.getMaterial() == Material.WATER) {
            for (int i17 = 0; i17 < 16; i17++) {
                for (int i18 = 0; i18 < 16; i18++) {
                    BlockPos offset2 = below.offset(i17, 4, i18);
                    if (worldGenLevel.getBiome(offset2).shouldFreeze(worldGenLevel, offset2, false)) {
                        worldGenLevel.setBlock(offset2, Blocks.ICE.defaultBlockState(), 2);
                    }
                }
            }
            return true;
        }
        return true;
    }
}
