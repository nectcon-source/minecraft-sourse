package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/NoiseBasedChunkGenerator.class */
public final class NoiseBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> {
            return noiseBasedChunkGenerator.biomeSource;
        }), Codec.LONG.fieldOf("seed").stable().forGetter(noiseBasedChunkGenerator2 -> {
            return Long.valueOf(noiseBasedChunkGenerator2.seed);
        }), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator3 -> {
            return noiseBasedChunkGenerator3.settings;
        })).apply(instance, instance.stable((v1, v2, v3) -> {
            return new NoiseBasedChunkGenerator(v1, v2, v3);
        }));
    });
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], fArr -> {
        for (int i = 0; i < 24; i++) {
            for (int i2 = 0; i2 < 24; i2++) {
                for (int i3 = 0; i3 < 24; i3++) {
                    fArr[(i * 24 * 24) + (i2 * 24) + i3] = (float) computeContribution(i2 - 12, i3 - 12, i - 12);
                }
            }
        }
    });
    private static final float[] BIOME_WEIGHTS =  Util.make(new float[25], fArr -> {
        for (int i = -2; i <= 2; i++) {
            for (int i2 = -2; i2 <= 2; i2++) {
                fArr[i + 2 + ((i2 + 2) * 5)] = 10.0f / Mth.sqrt(((i * i) + (i2 * i2)) + 0.2f);
            }
        }
    });
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final int chunkHeight;
    private final int chunkWidth;
    private final int chunkCountX;
    private final int chunkCountY;
    private final int chunkCountZ;
    protected final WorldgenRandom random;
    private final PerlinNoise minLimitPerlinNoise;
    private final PerlinNoise maxLimitPerlinNoise;
    private final PerlinNoise mainPerlinNoise;
    private final SurfaceNoise surfaceNoise;
    private final PerlinNoise depthNoise;

    @Nullable
    private final SimplexNoise islandNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    private final int height;

    public NoiseBasedChunkGenerator(BiomeSource biomeSource, long j, Supplier<NoiseGeneratorSettings> supplier) {
        this(biomeSource, biomeSource, j, supplier);
    }

    private NoiseBasedChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, long j, Supplier<NoiseGeneratorSettings> supplier) {
        super(biomeSource, biomeSource2, supplier.get().structureSettings(), j);
        this.seed = j;
        NoiseGeneratorSettings noiseGeneratorSettings = supplier.get();
        this.settings = supplier;
        NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        this.height = noiseSettings.height();
        this.chunkHeight = noiseSettings.noiseSizeVertical() * 4;
        this.chunkWidth = noiseSettings.noiseSizeHorizontal() * 4;
        this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
        this.defaultFluid = noiseGeneratorSettings.getDefaultFluid();
        this.chunkCountX = 16 / this.chunkWidth;
        this.chunkCountY = noiseSettings.height() / this.chunkHeight;
        this.chunkCountZ = 16 / this.chunkWidth;
        this.random = new WorldgenRandom(j);
        this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
        this.surfaceNoise = noiseSettings.useSimplexSurfaceNoise() ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0)) : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0));
        this.random.consumeCount(2620);
        this.depthNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        if (noiseSettings.islandNoiseOverride()) {
            WorldgenRandom worldgenRandom = new WorldgenRandom(j);
            worldgenRandom.consumeCount(17292);
            this.islandNoise = new SimplexNoise(worldgenRandom);
//            return;
        }else {
            this.islandNoise = null;
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public ChunkGenerator withSeed(long j) {
        return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(j), j, this.settings);
    }

    public boolean stable(long j, ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return this.seed == j && this.settings.get().stable(resourceKey);
    }

    private double sampleAndClampNoise(int i, int i2, int i3, double d, double d2, double d3, double d4) {
        ImprovedNoise octaveNoise;
        double d5 = 0.0d;
        double d6 = 0.0d;
        double d7 = 0.0d;
        double d8 = 1.0d;
        for (int i4 = 0; i4 < 16; i4++) {
            double wrap = PerlinNoise.wrap(i * d * d8);
            double wrap2 = PerlinNoise.wrap(i2 * d2 * d8);
            double wrap3 = PerlinNoise.wrap(i3 * d * d8);
            double d9 = d2 * d8;
            ImprovedNoise octaveNoise2 = this.minLimitPerlinNoise.getOctaveNoise(i4);
            if (octaveNoise2 != null) {
                d5 += octaveNoise2.noise(wrap, wrap2, wrap3, d9, i2 * d9) / d8;
            }
            ImprovedNoise octaveNoise3 = this.maxLimitPerlinNoise.getOctaveNoise(i4);
            if (octaveNoise3 != null) {
                d6 += octaveNoise3.noise(wrap, wrap2, wrap3, d9, i2 * d9) / d8;
            }
            if (i4 < 8 && (octaveNoise = this.mainPerlinNoise.getOctaveNoise(i4)) != null) {
                d7 += octaveNoise.noise(PerlinNoise.wrap((i * d3) * d8), PerlinNoise.wrap((i2 * d4) * d8), PerlinNoise.wrap((i3 * d3) * d8), d4 * d8, (i2 * d4) * d8) / d8;
            }
            d8 /= 2.0d;
        }
        return Mth.clampedLerp(d5 / 512.0d, d6 / 512.0d, ((d7 / 10.0d) + 1.0d) / 2.0d);
    }

    private double[] makeAndFillNoiseColumn(int i, int i2) {
        double[] dArr = new double[this.chunkCountY + 1];
        fillNoiseColumn(dArr, i, i2);
        return dArr;
    }

    private void fillNoiseColumn(double[] dArr, int i, int i2) {
        double d;
        double d2;
        float f;
        float f2;
        double d3;
        NoiseSettings noiseSettings = this.settings.get().noiseSettings();
        if (this.islandNoise != null) {
            d = TheEndBiomeSource.getHeightValue(this.islandNoise, i, i2) - 8.0f;
            if (d > 0.0d) {
                d2 = 0.25d;
            } else {
                d2 = 1.0d;
            }
        } else {
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = 0.0f;
            int seaLevel = getSeaLevel();
            float depth = this.biomeSource.getNoiseBiome(i, seaLevel, i2).getDepth();
            for (int i3 = -2; i3 <= 2; i3++) {
                for (int i4 = -2; i4 <= 2; i4++) {
                    Biome noiseBiome = this.biomeSource.getNoiseBiome(i + i3, seaLevel, i2 + i4);
                    float depth2 = noiseBiome.getDepth();
                    float scale = noiseBiome.getScale();
                    if (noiseSettings.isAmplified() && depth2 > 0.0f) {
                        f = 1.0f + (depth2 * 2.0f);
                        f2 = 1.0f + (scale * 4.0f);
                    } else {
                        f = depth2;
                        f2 = scale;
                    }
                    float f6 = ((depth2 > depth ? 0.5f : 1.0f) * BIOME_WEIGHTS[(i3 + 2) + ((i4 + 2) * 5)]) / (f + 2.0f);
                    f3 += f2 * f6;
                    f4 += f * f6;
                    f5 += f6;
                }
            }
            d = (((f4 / f5) * 0.5f) - 0.125f) * 0.265625d;
            d2 = 96.0d / (((f3 / f5) * 0.9f) + 0.1f);
        }
        double xzScale = 684.412d * noiseSettings.noiseSamplingSettings().xzScale();
        double yScale = 684.412d * noiseSettings.noiseSamplingSettings().yScale();
        double xzFactor = xzScale / noiseSettings.noiseSamplingSettings().xzFactor();
        double yFactor = yScale / noiseSettings.noiseSamplingSettings().yFactor();
        double target = noiseSettings.topSlideSettings().target();
        double size = noiseSettings.topSlideSettings().size();
        double offset = noiseSettings.topSlideSettings().offset();
        double target2 = noiseSettings.bottomSlideSettings().target();
        double size2 = noiseSettings.bottomSlideSettings().size();
        double offset2 = noiseSettings.bottomSlideSettings().offset();
        double randomDensity = noiseSettings.randomDensityOffset() ? getRandomDensity(i, i2) : 0.0d;
        double densityFactor = noiseSettings.densityFactor();
        double densityOffset = noiseSettings.densityOffset();
        for (int i5 = 0; i5 <= this.chunkCountY; i5++) {
            double sampleAndClampNoise = sampleAndClampNoise(i, i5, i2, xzScale, yScale, xzFactor, yFactor);
            double d4 = ((((1.0d - ((i5 * 2.0d) / this.chunkCountY)) + randomDensity) * densityFactor) + densityOffset + d) * d2;
            if (d4 > 0.0d) {
                d3 = sampleAndClampNoise + (d4 * 4.0d);
            } else {
                d3 = sampleAndClampNoise + d4;
            }
            if (size > 0.0d) {
                d3 = Mth.clampedLerp(target, d3, ((this.chunkCountY - i5) - offset) / size);
            }
            if (size2 > 0.0d) {
                d3 = Mth.clampedLerp(target2, d3, (i5 - offset2) / size2);
            }
            dArr[i5] = d3;
        }
    }

    private double getRandomDensity(int i, int i2) {
        double d;
        double value = this.depthNoise.getValue(i * 200, 10.0d, i2 * 200, 1.0d, 0.0d, true);
        if (value < 0.0d) {
            d = (-value) * 0.3d;
        } else {
            d = value;
        }
        double d2 = (d * 24.575625d) - 2.0d;
        if (d2 < 0.0d) {
            return d2 * 0.009486607142857142d;
        }
        return Math.min(d2, 1.0d) * 0.006640625d;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public int getBaseHeight(int i, int i2, Heightmap.Types types) {
        return iterateNoiseColumn(i, i2, null, types.isOpaque());
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public BlockGetter getBaseColumn(int i, int i2) {
        BlockState[] blockStateArr = new BlockState[this.chunkCountY * this.chunkHeight];
        iterateNoiseColumn(i, i2, blockStateArr, null);
        return new NoiseColumn(blockStateArr);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private int iterateNoiseColumn(int i, int i2, @Nullable BlockState[] blockStateArr, @Nullable Predicate<BlockState> predicate) {
        int var5 = Math.floorDiv(i, this.chunkWidth);
        int var6 = Math.floorDiv(i2, this.chunkWidth);
        int var7 = Math.floorMod(i, this.chunkWidth);
        int var8 = Math.floorMod(i2, this.chunkWidth);
        double var9 = (double)var7 / (double)this.chunkWidth;
        double var11 = (double)var8 / (double)this.chunkWidth;
        double[][] var13 = new double[][]{this.makeAndFillNoiseColumn(var5, var6), this.makeAndFillNoiseColumn(var5, var6 + 1), this.makeAndFillNoiseColumn(var5 + 1, var6), this.makeAndFillNoiseColumn(var5 + 1, var6 + 1)};

        for(int var14 = this.chunkCountY - 1; var14 >= 0; --var14) {
            double var15 = var13[0][var14];
            double var17 = var13[1][var14];
            double var19 = var13[2][var14];
            double var21 = var13[3][var14];
            double var23 = var13[0][var14 + 1];
            double var25 = var13[1][var14 + 1];
            double var27 = var13[2][var14 + 1];
            double var29 = var13[3][var14 + 1];

            for(int var31 = this.chunkHeight - 1; var31 >= 0; --var31) {
                double var32 = (double)var31 / (double)this.chunkHeight;
                double var34 = Mth.lerp3(var32, var9, var11, var15, var23, var19, var27, var17, var25, var21, var29);
                int var36 = var14 * this.chunkHeight + var31;
                BlockState var37 = this.generateBaseState(var34, var36);
                if (blockStateArr != null) {
                    blockStateArr[var36] = var37;
                }

                if (predicate != null && predicate.test(var37)) {
                    return var36 + 1;
                }
            }
        }

        return 0;
    }

    protected BlockState generateBaseState(double d, int i) {
        BlockState blockState;
        if (d > 0.0d) {
            blockState = this.defaultBlock;
        } else if (i < getSeaLevel()) {
            blockState = this.defaultFluid;
        } else {
            blockState = AIR;
        }
        return blockState;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
        ChunkPos pos = chunkAccess.getPos();
        int i = pos.x;
        int i2 = pos.z;
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        worldgenRandom.setBaseChunkSeed(i, i2);
        ChunkPos pos2 = chunkAccess.getPos();
        int minBlockX = pos2.getMinBlockX();
        int minBlockZ = pos2.getMinBlockZ();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i3 = 0; i3 < 16; i3++) {
            for (int i4 = 0; i4 < 16; i4++) {
                int i5 = minBlockX + i3;
                int i6 = minBlockZ + i4;
                int height = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i3, i4) + 1;
                worldGenRegion.getBiome(mutableBlockPos.set(minBlockX + i3, height, minBlockZ + i4)).buildSurfaceAt(worldgenRandom, chunkAccess, i5, i6, height, this.surfaceNoise.getSurfaceNoiseValue(i5 * 0.0625d, i6 * 0.0625d, 0.0625d, i3 * 0.0625d) * 15.0d, this.defaultBlock, this.defaultFluid, getSeaLevel(), worldGenRegion.getSeed());
            }
        }
        setBedrock(chunkAccess, worldgenRandom);
    }

    private void setBedrock(ChunkAccess chunkAccess, Random random) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int minBlockX = chunkAccess.getPos().getMinBlockX();
        int minBlockZ = chunkAccess.getPos().getMinBlockZ();
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.get();
        int bedrockFloorPosition = noiseGeneratorSettings.getBedrockFloorPosition();
        int bedrockRoofPosition = (this.height - 1) - noiseGeneratorSettings.getBedrockRoofPosition();
        boolean z = bedrockRoofPosition + 4 >= 0 && bedrockRoofPosition < this.height;
        boolean z2 = bedrockFloorPosition + 4 >= 0 && bedrockFloorPosition < this.height;
        if (!z && !z2) {
            return;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed(minBlockX, 0, minBlockZ, minBlockX + 15, 0, minBlockZ + 15)) {
            if (z) {
                for (int i = 0; i < 5; i++) {
                    if (i <= random.nextInt(5)) {
                        chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), bedrockRoofPosition - i, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                    }
                }
            }
            if (z2) {
                for (int i2 = 4; i2 >= 0; i2--) {
                    if (i2 <= random.nextInt(5)) {
                        chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), bedrockFloorPosition + i2, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        double d;
        ObjectArrayList objectArrayList = new ObjectArrayList(10);
        ObjectArrayList objectArrayList2 = new ObjectArrayList(32);
        ChunkPos pos = chunkAccess.getPos();
        int i = pos.x;
        int i2 = pos.z;
        int i3 = i << 4;
        int i4 = i2 << 4;
        Iterator<StructureFeature<?>> it = StructureFeature.NOISE_AFFECTING_FEATURES.iterator();
        while (it.hasNext()) {
            structureFeatureManager.startsForFeature(SectionPos.of(pos, 0), it.next()).forEach(structureStart -> {
                for (StructurePiece structurePiece : structureStart.getPieces()) {
                    if (structurePiece.isCloseToChunk(pos, 12)) {
                        if (structurePiece instanceof PoolElementStructurePiece) {
                            PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece) structurePiece;
                            if (poolElementStructurePiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
                                objectArrayList.add(poolElementStructurePiece);
                            }
                            for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
                                int sourceX = jigsawJunction.getSourceX();
                                int sourceZ = jigsawJunction.getSourceZ();
                                if (sourceX > i3 - 12 && sourceZ > i4 - 12 && sourceX < i3 + 15 + 12 && sourceZ < i4 + 15 + 12) {
                                    objectArrayList2.add(jigsawJunction);
                                }
                            }
                        } else {
                            objectArrayList.add(structurePiece);
                        }
                    }
                }
            });
        }
        double[][][] dArr = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];
        for (int i5 = 0; i5 < this.chunkCountZ + 1; i5++) {
            dArr[0][i5] = new double[this.chunkCountY + 1];
            fillNoiseColumn(dArr[0][i5], i * this.chunkCountX, (i2 * this.chunkCountZ) + i5);
            dArr[1][i5] = new double[this.chunkCountY + 1];
        }
        ProtoChunk protoChunk = (ProtoChunk) chunkAccess;
        Heightmap orCreateHeightmapUnprimed = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap orCreateHeightmapUnprimed2 = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        ObjectListIterator<StructurePiece> it2 = objectArrayList.iterator();
        ObjectListIterator<JigsawJunction> it3 = objectArrayList2.iterator();
        for (int i6 = 0; i6 < this.chunkCountX; i6++) {
            for (int i7 = 0; i7 < this.chunkCountZ + 1; i7++) {
                fillNoiseColumn(dArr[1][i7], (i * this.chunkCountX) + i6 + 1, (i2 * this.chunkCountZ) + i7);
            }
            for (int i8 = 0; i8 < this.chunkCountZ; i8++) {
                LevelChunkSection orCreateSection = protoChunk.getOrCreateSection(15);
                orCreateSection.acquire();
                for (int i9 = this.chunkCountY - 1; i9 >= 0; i9--) {
                    double d2 = dArr[0][i8][i9];
                    double d3 = dArr[0][i8 + 1][i9];
                    double d4 = dArr[1][i8][i9];
                    double d5 = dArr[1][i8 + 1][i9];
                    double d6 = dArr[0][i8][i9 + 1];
                    double d7 = dArr[0][i8 + 1][i9 + 1];
                    double d8 = dArr[1][i8][i9 + 1];
                    double d9 = dArr[1][i8 + 1][i9 + 1];
                    for (int i10 = this.chunkHeight - 1; i10 >= 0; i10--) {
                        int i11 = (i9 * this.chunkHeight) + i10;
                        int i12 = i11 & 15;
                        int i13 = i11 >> 4;
                        if ((orCreateSection.bottomBlockY() >> 4) != i13) {
                            orCreateSection.release();
                            orCreateSection = protoChunk.getOrCreateSection(i13);
                            orCreateSection.acquire();
                        }
                        double d10 = i10 / this.chunkHeight;
                        double lerp = Mth.lerp(d10, d2, d6);
                        double lerp2 = Mth.lerp(d10, d4, d8);
                        double lerp3 = Mth.lerp(d10, d3, d7);
                        double lerp4 = Mth.lerp(d10, d5, d9);
                        for (int i14 = 0; i14 < this.chunkWidth; i14++) {
                            int i15 = i3 + (i6 * this.chunkWidth) + i14;
                            int i16 = i15 & 15;
                            double d11 = i14 / this.chunkWidth;
                            double lerp5 = Mth.lerp(d11, lerp, lerp2);
                            double lerp6 = Mth.lerp(d11, lerp3, lerp4);
                            for (int i17 = 0; i17 < this.chunkWidth; i17++) {
                                int i18 = i4 + (i8 * this.chunkWidth) + i17;
                                int i19 = i18 & 15;
                                double clamp = Mth.clamp(Mth.lerp(i17 / this.chunkWidth, lerp5, lerp6) / 200.0d, -1.0d, 1.0d);
                                double d12 = (clamp / 2.0d) - (((clamp * clamp) * clamp) / 24.0d);
                                while (true) {
                                    d = d12;
                                    if (!it2.hasNext()) {
                                        break;
                                    }
                                    StructurePiece structurePiece = (StructurePiece) it2.next();
                                    BoundingBox boundingBox = structurePiece.getBoundingBox();
                                    d12 = d + (getContribution(Math.max(0, Math.max(boundingBox.x0 - i15, i15 - boundingBox.x1)), i11 - (boundingBox.y0 + (structurePiece instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece) structurePiece).getGroundLevelDelta() : 0)), Math.max(0, Math.max(boundingBox.z0 - i18, i18 - boundingBox.z1))) * 0.8d);
                                }
                                it2.back(objectArrayList.size());
                                while (it3.hasNext()) {
                                    JigsawJunction jigsawJunction = (JigsawJunction) it3.next();
                                    d += getContribution(i15 - jigsawJunction.getSourceX(), i11 - jigsawJunction.getSourceGroundY(), i18 - jigsawJunction.getSourceZ()) * 0.4d;
                                }
                                it3.back(objectArrayList2.size());
                                BlockState generateBaseState = generateBaseState(d, i11);
                                if (generateBaseState != AIR) {
                                    if (generateBaseState.getLightEmission() != 0) {
                                        mutableBlockPos.set(i15, i11, i18);
                                        protoChunk.addLight(mutableBlockPos);
                                    }
                                    orCreateSection.setBlockState(i16, i12, i19, generateBaseState, false);
                                    orCreateHeightmapUnprimed.update(i16, i11, i19, generateBaseState);
                                    orCreateHeightmapUnprimed2.update(i16, i11, i19, generateBaseState);
                                }
                            }
                        }
                    }
                }
                orCreateSection.release();
            }
            double[][] dArr2 = dArr[0];
            dArr[0] = dArr[1];
            dArr[1] = dArr2;
        }
    }

    private static double getContribution(int i, int i2, int i3) {
        int i4 = i + 12;
        int i5 = i2 + 12;
        int i6 = i3 + 12;
        if (i4 < 0 || i4 >= 24 || i5 < 0 || i5 >= 24 || i6 < 0 || i6 >= 24) {
            return 0.0d;
        }
        return BEARD_KERNEL[(i6 * 24 * 24) + (i4 * 24) + i5];
    }

    private static double computeContribution(int i, int i2, int i3) {
        double d = (i * i) + (i3 * i3);
        double d2 = i2 + 0.5d;
        double d3 = d2 * d2;
        return (((-d2) * Mth.fastInvSqrt((d3 / 2.0d) + (d / 2.0d))) / 2.0d) * Math.pow(2.718281828459045d, -((d3 / 16.0d) + (d / 16.0d)));
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public int getGenDepth() {
        return this.height;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public int getSeaLevel() {
        return this.settings.get().seaLevel();
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.SWAMP_HUT).isValid()) {
            if (mobCategory == MobCategory.MONSTER) {
                return StructureFeature.SWAMP_HUT.getSpecialEnemies();
            }
            if (mobCategory == MobCategory.CREATURE) {
                return StructureFeature.SWAMP_HUT.getSpecialAnimals();
            }
        }
        if (mobCategory == MobCategory.MONSTER) {
            if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
                return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
            }
            if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
                return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
            }
            if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.NETHER_BRIDGE).isValid()) {
                return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
            }
        }
        return super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        if (this.settings.get().disableMobGeneration()) {
            return;
        }
        int centerX = worldGenRegion.getCenterX();
        int centerZ = worldGenRegion.getCenterZ();
        Biome biome = worldGenRegion.getBiome(new ChunkPos(centerX, centerZ).getWorldPosition());
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), centerX << 4, centerZ << 4);
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, centerX, centerZ, worldgenRandom);
    }
}
