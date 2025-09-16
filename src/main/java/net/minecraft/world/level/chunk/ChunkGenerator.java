package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/ChunkGenerator.class */
public abstract class ChunkGenerator {
    public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.dispatchStable((v0) -> v0.codec(), Function.identity());
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    private final StructureSettings settings;
    private final long strongholdSeed;
    private final List<ChunkPos> strongholdPositions;

    protected abstract Codec<? extends ChunkGenerator> codec();

    public abstract ChunkGenerator withSeed(long j);

    public abstract void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess);

    public abstract void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess);

    public abstract int getBaseHeight(int i, int i2, Heightmap.Types types);

    public abstract BlockGetter getBaseColumn(int i, int i2);

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
    }

    public ChunkGenerator(BiomeSource biomeSource, StructureSettings structureSettings) {
        this(biomeSource, biomeSource, structureSettings, 0L);
    }

    public ChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, StructureSettings structureSettings, long j) {
        this.strongholdPositions = Lists.newArrayList();
        this.biomeSource = biomeSource;
        this.runtimeBiomeSource = biomeSource2;
        this.settings = structureSettings;
        this.strongholdSeed = j;
    }

    private void generateStrongholds() {
        if (this.strongholdPositions.isEmpty()) {
            StrongholdConfiguration var1 = this.settings.stronghold();
            if (var1 != null && var1.count() != 0) {
                List<Biome> var2 = Lists.newArrayList();

                for(Biome var4 : this.biomeSource.possibleBiomes()) {
                    if (var4.getGenerationSettings().isValidStart(StructureFeature.STRONGHOLD)) {
                        var2.add(var4);
                    }
                }

                int var17 = var1.distance();
                int var18 = var1.count();
                int var5 = var1.spread();
                Random var6 = new Random();
                var6.setSeed(this.strongholdSeed);
                double var7 = var6.nextDouble() * Math.PI * (double)2.0F;
                int var9 = 0;
                int var10 = 0;

                for(int var11 = 0; var11 < var18; ++var11) {
                    double var12 = (double)(4 * var17 + var17 * var10 * 6) + (var6.nextDouble() - (double)0.5F) * (double)var17 * (double)2.5F;
                    int var14 = (int)Math.round(Math.cos(var7) * var12);
                    int var15 = (int)Math.round(Math.sin(var7) * var12);
                    BlockPos var16 = this.biomeSource.findBiomeHorizontal((var14 << 4) + 8, 0, (var15 << 4) + 8, 112, var2::contains, var6);
                    if (var16 != null) {
                        var14 = var16.getX() >> 4;
                        var15 = var16.getZ() >> 4;
                    }

                    this.strongholdPositions.add(new ChunkPos(var14, var15));
                    var7 += (Math.PI * 2D) / (double)var5;
                    ++var9;
                    if (var9 == var5) {
                        ++var10;
                        var9 = 0;
                        var5 += 2 * var5 / (var10 + 1);
                        var5 = Math.min(var5, var18 - var11);
                        var7 += var6.nextDouble() * Math.PI * (double)2.0F;
                    }
                }

            }
        }
    }

    public void createBiomes(Registry<Biome> registry, ChunkAccess chunkAccess) {
        ((ProtoChunk) chunkAccess).setBiomes(new ChunkBiomeContainer(registry, chunkAccess.getPos(), this.runtimeBiomeSource));
    }

    public void applyCarvers(long j, BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        BiomeManager withDifferentSource = biomeManager.withDifferentSource(this.biomeSource);
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        ChunkPos pos = chunkAccess.getPos();
        int i = pos.x;
        int i2 = pos.z;
        BiomeGenerationSettings generationSettings = this.biomeSource.getNoiseBiome(pos.x << 2, 0, pos.z << 2).getGenerationSettings();
        BitSet orCreateCarvingMask = ((ProtoChunk) chunkAccess).getOrCreateCarvingMask(carving);
        for (int i3 = i - 8; i3 <= i + 8; i3++) {
            for (int i4 = i2 - 8; i4 <= i2 + 8; i4++) {
                ListIterator<Supplier<ConfiguredWorldCarver<?>>> listIterator = generationSettings.getCarvers(carving).listIterator();
                while (listIterator.hasNext()) {
                    int nextIndex = listIterator.nextIndex();
                    ConfiguredWorldCarver<?> configuredWorldCarver = listIterator.next().get();
                    worldgenRandom.setLargeFeatureSeed(j + nextIndex, i3, i4);
                    if (configuredWorldCarver.isStartChunk(worldgenRandom, i3, i4)) {
                        withDifferentSource.getClass();
                        configuredWorldCarver.carve(chunkAccess, withDifferentSource::getBiome, worldgenRandom, getSeaLevel(), i3, i4, i, i2, orCreateCarvingMask);
                    }
                }
            }
        }
    }

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel serverLevel, StructureFeature<?> structureFeature, BlockPos blockPos, int i, boolean z) {
        if (!this.biomeSource.canGenerateStructure(structureFeature)) {
            return null;
        }
        if (structureFeature == StructureFeature.STRONGHOLD) {
            generateStrongholds();
            BlockPos blockPos2 = null;
            double d = Double.MAX_VALUE;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (ChunkPos chunkPos : this.strongholdPositions) {
                mutableBlockPos.set((chunkPos.x << 4) + 8, 32, (chunkPos.z << 4) + 8);
                double distSqr = mutableBlockPos.distSqr(blockPos);
                if (blockPos2 == null) {
                    blockPos2 = new BlockPos(mutableBlockPos);
                    d = distSqr;
                } else if (distSqr < d) {
                    blockPos2 = new BlockPos(mutableBlockPos);
                    d = distSqr;
                }
            }
            return blockPos2;
        }
        StructureFeatureConfiguration config = this.settings.getConfig(structureFeature);
        if (config == null) {
            return null;
        }
        return structureFeature.getNearestGeneratedFeature(serverLevel, serverLevel.structureFeatureManager(), blockPos, i, z, serverLevel.getSeed(), config);
    }

    public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager) {
        int centerX = worldGenRegion.getCenterX();
        int centerZ = worldGenRegion.getCenterZ();
        int i = centerX * 16;
        int i2 = centerZ * 16;
        BlockPos blockPos = new BlockPos(i, 0, i2);
        Biome noiseBiome = this.biomeSource.getNoiseBiome((centerX << 2) + 2, 2, (centerZ << 2) + 2);
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        long decorationSeed = worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), i, i2);
        try {
            noiseBiome.generate(structureFeatureManager, this, worldGenRegion, decorationSeed, worldgenRandom, blockPos);
        } catch (Exception e) {
            CrashReport forThrowable = CrashReport.forThrowable(e, "Biome decoration");
            forThrowable.addCategory("Generation").setDetail("CenterX", Integer.valueOf(centerX)).setDetail("CenterZ", Integer.valueOf(centerZ)).setDetail("Seed", Long.valueOf(decorationSeed)).setDetail("Biome", noiseBiome);
            throw new ReportedException(forThrowable);
        }
    }

    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
    }

    public StructureSettings getSettings() {
        return this.settings;
    }

    public int getSpawnHeight() {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public int getGenDepth() {
        return 256;
    }

    public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        return biome.getMobSettings().getMobs(mobCategory);
    }

    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long j) {
        ChunkPos pos = chunkAccess.getPos();
        Biome noiseBiome = this.biomeSource.getNoiseBiome((pos.x << 2) + 2, 0, (pos.z << 2) + 2);
        createStructure(StructureFeatures.STRONGHOLD, registryAccess, structureFeatureManager, chunkAccess, structureManager, j, pos, noiseBiome);
        Iterator<Supplier<ConfiguredStructureFeature<?, ?>>> it = noiseBiome.getGenerationSettings().structures().iterator();
        while (it.hasNext()) {
            createStructure(it.next().get(), registryAccess, structureFeatureManager, chunkAccess, structureManager, j, pos, noiseBiome);
        }
    }

    private void createStructure(ConfiguredStructureFeature<?, ?> configuredStructureFeature, RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long j, ChunkPos chunkPos, Biome biome) {
        StructureStart<?> startForFeature = structureFeatureManager.getStartForFeature(SectionPos.of(chunkAccess.getPos(), 0), configuredStructureFeature.feature, chunkAccess);
        int references = startForFeature != null ? startForFeature.getReferences() : 0;
        StructureFeatureConfiguration config = this.settings.getConfig(configuredStructureFeature.feature);
        if (config != null) {
            structureFeatureManager.setStartForFeature(SectionPos.of(chunkAccess.getPos(), 0), configuredStructureFeature.feature, configuredStructureFeature.generate(registryAccess, this, this.biomeSource, structureManager, j, chunkPos, biome, references, config), chunkAccess);
        }
    }

    public void createReferences(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        int i = chunkAccess.getPos().x;
        int i2 = chunkAccess.getPos().z;
        int i3 = i << 4;
        int i4 = i2 << 4;
        SectionPos m37of = SectionPos.of(chunkAccess.getPos(), 0);
        for (int i5 = i - 8; i5 <= i + 8; i5++) {
            for (int i6 = i2 - 8; i6 <= i2 + 8; i6++) {
                long asLong = ChunkPos.asLong(i5, i6);
                for (StructureStart<?> structureStart : worldGenLevel.getChunk(i5, i6).getAllStarts().values()) {
                    try {
                        if (structureStart != StructureStart.INVALID_START && structureStart.getBoundingBox().intersects(i3, i4, i3 + 15, i4 + 15)) {
                            structureFeatureManager.addReferenceForFeature(m37of, structureStart.getFeature(), asLong, chunkAccess);
                            DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
                        }
                    } catch (Exception e) {
                        CrashReport forThrowable = CrashReport.forThrowable(e, "Generating structure reference");
                        CrashReportCategory addCategory = forThrowable.addCategory("Structure");
                        addCategory.setDetail("Id", () -> Registry.STRUCTURE_FEATURE.getKey(structureStart.getFeature()).toString());
                        addCategory.setDetail("Name", () -> structureStart.getFeature().getFeatureName());
                        addCategory.setDetail("Class", () -> structureStart.getFeature().getClass().getCanonicalName());
                        throw new ReportedException(forThrowable);
                    }
                }
            }
        }
    }

    public int getSeaLevel() {
        return 63;
    }

    public int getFirstFreeHeight(int i, int i2, Heightmap.Types types) {
        return getBaseHeight(i, i2, types);
    }

    public int getFirstOccupiedHeight(int i, int i2, Heightmap.Types types) {
        return getBaseHeight(i, i2, types) - 1;
    }

    public boolean hasStronghold(ChunkPos chunkPos) {
        generateStrongholds();
        return this.strongholdPositions.contains(chunkPos);
    }
}
