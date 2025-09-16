package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/FrozenOceanSurfaceBuilder.class */
public class FrozenOceanSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    protected static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    protected static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState ICE = Blocks.ICE.defaultBlockState();
    private PerlinSimplexNoise icebergNoise;
    private PerlinSimplexNoise icebergRoofNoise;
    private long seed;

    public FrozenOceanSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        double d2 = 0.0d;
        double d3 = 0.0d;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        float temperature = biome.getTemperature(mutableBlockPos.set(i, 63, i2));
        double min = Math.min(Math.abs(d), this.icebergNoise.getValue(i * 0.1d, i2 * 0.1d, false) * 15.0d);
        if (min > 1.8d) {
            double d4 = min * min * 1.2d;
            double ceil = Math.ceil(Math.abs(this.icebergRoofNoise.getValue(i * 0.09765625d, i2 * 0.09765625d, false)) * 40.0d) + 14.0d;
            if (d4 > ceil) {
                d4 = ceil;
            }
            if (temperature > 0.1f) {
                d4 -= 2.0d;
            }
            if (d4 > 2.0d) {
                d3 = (i4 - d4) - 7.0d;
                d2 = d4 + i4;
            } else {
                d2 = 0.0d;
            }
        }
        int i5 = i & 15;
        int i6 = i2 & 15;
        SurfaceBuilderConfiguration surfaceBuilderConfig = biome.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState underMaterial = surfaceBuilderConfig.getUnderMaterial();
        BlockState topMaterial = surfaceBuilderConfig.getTopMaterial();
        BlockState blockState3 = underMaterial;
        BlockState blockState4 = topMaterial;
        int nextDouble = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        int i7 = -1;
        int i8 = 0;
        int nextInt = 2 + random.nextInt(4);
        int nextInt2 = i4 + 18 + random.nextInt(10);
        for (int max = Math.max(i3, ((int) d2) + 1); max >= 0; max--) {
            mutableBlockPos.set(i5, max, i6);
            if (chunkAccess.getBlockState(mutableBlockPos).isAir() && max < ((int) d2) && random.nextDouble() > 0.01d) {
                chunkAccess.setBlockState(mutableBlockPos, PACKED_ICE, false);
            } else if (chunkAccess.getBlockState(mutableBlockPos).getMaterial() == Material.WATER && max > ((int) d3) && max < i4 && d3 != 0.0d && random.nextDouble() > 0.15d) {
                chunkAccess.setBlockState(mutableBlockPos, PACKED_ICE, false);
            }
            BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState5.isAir()) {
                i7 = -1;
            } else if (blockState5.is(blockState.getBlock())) {
                if (i7 == -1) {
                    if (nextDouble <= 0) {
                        blockState4 = AIR;
                        blockState3 = blockState;
                    } else if (max >= i4 - 4 && max <= i4 + 1) {
                        blockState4 = topMaterial;
                        blockState3 = underMaterial;
                    }
                    if (max < i4 && (blockState4 == null || blockState4.isAir())) {
                        if (biome.getTemperature(mutableBlockPos.set(i, max, i2)) < 0.15f) {
                            blockState4 = ICE;
                        } else {
                            blockState4 = blockState2;
                        }
                    }
                    i7 = nextDouble;
                    if (max >= i4 - 1) {
                        chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
                    } else if (max < (i4 - 7) - nextDouble) {
                        blockState4 = AIR;
                        blockState3 = blockState;
                        chunkAccess.setBlockState(mutableBlockPos, GRAVEL, false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                    }
                } else if (i7 > 0) {
                    i7--;
                    chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                    if (i7 == 0 && blockState3.is(Blocks.SAND) && nextDouble > 1) {
                        i7 = random.nextInt(4) + Math.max(0, max - 63);
                        blockState3 = blockState3.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                    }
                }
            } else if (blockState5.is(Blocks.PACKED_ICE) && i8 <= nextInt && max > nextInt2) {
                chunkAccess.setBlockState(mutableBlockPos, SNOW_BLOCK, false);
                i8++;
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void initNoise(long j) {
        if (this.seed != j || this.icebergNoise == null || this.icebergRoofNoise == null) {
            WorldgenRandom worldgenRandom = new WorldgenRandom(j);
            this.icebergNoise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-3, 0));
            this.icebergRoofNoise = new PerlinSimplexNoise(worldgenRandom, (List<Integer>) ImmutableList.of(0));
        }
        this.seed = j;
    }
}
