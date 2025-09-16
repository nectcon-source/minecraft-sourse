package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/BadlandsSurfaceBuilder.class */
public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    protected BlockState[] clayBands;
    protected long seed;
    protected PerlinSimplexNoise pillarNoise;
    protected PerlinSimplexNoise pillarRoofNoise;
    protected PerlinSimplexNoise clayBandsOffsetNoise;

    public BadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        BlockState blockState3;
        int i5 = i & 15;
        int i6 = i2 & 15;
        BlockState blockState4 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration surfaceBuilderConfig = biome.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState underMaterial = surfaceBuilderConfig.getUnderMaterial();
        BlockState topMaterial = surfaceBuilderConfig.getTopMaterial();
        BlockState blockState5 = underMaterial;
        int nextDouble = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        boolean z = Math.cos((d / 3.0d) * 3.141592653589793d) > 0.0d;
        int i7 = -1;
        boolean z2 = false;
        int i8 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i9 = i3; i9 >= 0; i9--) {
            if (i8 < 15) {
                mutableBlockPos.set(i5, i9, i6);
                BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
                if (blockState6.isAir()) {
                    i7 = -1;
                } else if (blockState6.is(blockState.getBlock())) {
                    if (i7 == -1) {
                        z2 = false;
                        if (nextDouble <= 0) {
                            blockState4 = Blocks.AIR.defaultBlockState();
                            blockState5 = blockState;
                        } else if (i9 >= i4 - 4 && i9 <= i4 + 1) {
                            blockState4 = WHITE_TERRACOTTA;
                            blockState5 = underMaterial;
                        }
                        if (i9 < i4 && (blockState4 == null || blockState4.isAir())) {
                            blockState4 = blockState2;
                        }
                        i7 = nextDouble + Math.max(0, i9 - i4);
                        if (i9 >= i4 - 1) {
                            if (i9 > i4 + 3 + nextDouble) {
                                if (i9 < 64 || i9 > 127) {
                                    blockState3 = ORANGE_TERRACOTTA;
                                } else if (z) {
                                    blockState3 = TERRACOTTA;
                                } else {
                                    blockState3 = getBand(i, i9, i2);
                                }
                                chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                            } else {
                                chunkAccess.setBlockState(mutableBlockPos, topMaterial, false);
                                z2 = true;
                            }
                        } else {
                            chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
                            Block block = blockState5.getBlock();
                            if (block == Blocks.WHITE_TERRACOTTA || block == Blocks.ORANGE_TERRACOTTA || block == Blocks.MAGENTA_TERRACOTTA || block == Blocks.LIGHT_BLUE_TERRACOTTA || block == Blocks.YELLOW_TERRACOTTA || block == Blocks.LIME_TERRACOTTA || block == Blocks.PINK_TERRACOTTA || block == Blocks.GRAY_TERRACOTTA || block == Blocks.LIGHT_GRAY_TERRACOTTA || block == Blocks.CYAN_TERRACOTTA || block == Blocks.PURPLE_TERRACOTTA || block == Blocks.BLUE_TERRACOTTA || block == Blocks.BROWN_TERRACOTTA || block == Blocks.GREEN_TERRACOTTA || block == Blocks.RED_TERRACOTTA || block == Blocks.BLACK_TERRACOTTA) {
                                chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                            }
                        }
                    } else if (i7 > 0) {
                        i7--;
                        if (z2) {
                            chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                        } else {
                            chunkAccess.setBlockState(mutableBlockPos, getBand(i, i9, i2), false);
                        }
                    }
                    i8++;
                }
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void initNoise(long j) {
        if (this.seed != j || this.clayBands == null) {
            generateBands(j);
        }
        if (this.seed != j || this.pillarNoise == null || this.pillarRoofNoise == null) {
            WorldgenRandom worldgenRandom = new WorldgenRandom(j);
            this.pillarNoise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-3, 0));
            this.pillarRoofNoise = new PerlinSimplexNoise(worldgenRandom, (List<Integer>) ImmutableList.of(0));
        }
        this.seed = j;
    }

    protected void generateBands(long j) {
        this.clayBands = new BlockState[64];
        Arrays.fill(this.clayBands, TERRACOTTA);
        WorldgenRandom worldgenRandom = new WorldgenRandom(j);
        this.clayBandsOffsetNoise = new PerlinSimplexNoise(worldgenRandom, (List<Integer>) ImmutableList.of(0));
        int i = 0;
        while (i < 64) {
            int nextInt = i + worldgenRandom.nextInt(5) + 1;
            if (nextInt < 64) {
                this.clayBands[nextInt] = ORANGE_TERRACOTTA;
            }
            i = nextInt + 1;
        }
        int nextInt2 = worldgenRandom.nextInt(4) + 2;
        for (int i2 = 0; i2 < nextInt2; i2++) {
            int nextInt3 = worldgenRandom.nextInt(3) + 1;
            int nextInt4 = worldgenRandom.nextInt(64);
            for (int i3 = 0; nextInt4 + i3 < 64 && i3 < nextInt3; i3++) {
                this.clayBands[nextInt4 + i3] = YELLOW_TERRACOTTA;
            }
        }
        int nextInt5 = worldgenRandom.nextInt(4) + 2;
        for (int i4 = 0; i4 < nextInt5; i4++) {
            int nextInt6 = worldgenRandom.nextInt(3) + 2;
            int nextInt7 = worldgenRandom.nextInt(64);
            for (int i5 = 0; nextInt7 + i5 < 64 && i5 < nextInt6; i5++) {
                this.clayBands[nextInt7 + i5] = BROWN_TERRACOTTA;
            }
        }
        int nextInt8 = worldgenRandom.nextInt(4) + 2;
        for (int i6 = 0; i6 < nextInt8; i6++) {
            int nextInt9 = worldgenRandom.nextInt(3) + 1;
            int nextInt10 = worldgenRandom.nextInt(64);
            for (int i7 = 0; nextInt10 + i7 < 64 && i7 < nextInt9; i7++) {
                this.clayBands[nextInt10 + i7] = RED_TERRACOTTA;
            }
        }
        int nextInt11 = worldgenRandom.nextInt(3) + 3;
        int i8 = 0;
        for (int i9 = 0; i9 < nextInt11; i9++) {
            i8 += worldgenRandom.nextInt(16) + 4;
            for (int i10 = 0; i8 + i10 < 64 && i10 < 1; i10++) {
                this.clayBands[i8 + i10] = WHITE_TERRACOTTA;
                if (i8 + i10 > 1 && worldgenRandom.nextBoolean()) {
                    this.clayBands[(i8 + i10) - 1] = LIGHT_GRAY_TERRACOTTA;
                }
                if (i8 + i10 < 63 && worldgenRandom.nextBoolean()) {
                    this.clayBands[i8 + i10 + 1] = LIGHT_GRAY_TERRACOTTA;
                }
            }
        }
    }

    protected BlockState getBand(int i, int i2, int i3) {
        return this.clayBands[((i2 + ((int) Math.round(this.clayBandsOffsetNoise.getValue(i / 512.0d, i3 / 512.0d, false) * 2.0d))) + 64) % 64];
    }
}
