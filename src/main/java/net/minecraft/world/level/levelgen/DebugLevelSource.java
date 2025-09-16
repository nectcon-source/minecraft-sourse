package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/DebugLevelSource.class */
public class DebugLevelSource extends ChunkGenerator {
    public static final Codec<DebugLevelSource> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY).xmap(DebugLevelSource::new, (v0) -> v0.biomes()).stable().codec();
    private static final List<BlockState> ALL_BLOCKS =  StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap(block -> block.getStateDefinition().getPossibleStates().stream()).collect(Collectors.toList());
    private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt(ALL_BLOCKS.size()));
    private static final int GRID_HEIGHT = Mth.ceil(ALL_BLOCKS.size() / GRID_WIDTH);
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
    private final Registry<Biome> biomes;

    public DebugLevelSource(Registry<Biome> registry) {
        super(new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS)), new StructureSettings(false));
        this.biomes = registry;
    }

    public Registry<Biome> biomes() {
        return this.biomes;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public ChunkGenerator withSeed(long j) {
        return this;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void applyCarvers(long j, BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int centerX = worldGenRegion.getCenterX();
        int centerZ = worldGenRegion.getCenterZ();
        for (int i = 0; i < 16; i++) {
            for (int i2 = 0; i2 < 16; i2++) {
                int i3 = (centerX << 4) + i;
                int i4 = (centerZ << 4) + i2;
                worldGenRegion.setBlock(mutableBlockPos.set(i3, 60, i4), BARRIER, 2);
                BlockState blockStateFor = getBlockStateFor(i3, i4);
                if (blockStateFor != null) {
                    worldGenRegion.setBlock(mutableBlockPos.set(i3, 70, i4), blockStateFor, 2);
                }
            }
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public int getBaseHeight(int i, int i2, Heightmap.Types types) {
        return 0;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public BlockGetter getBaseColumn(int i, int i2) {
        return new NoiseColumn(new BlockState[0]);
    }

    public static BlockState getBlockStateFor(int i, int i2) {
        int abs;
        BlockState blockState = AIR;
        if (i > 0 && i2 > 0 && i % 2 != 0 && i2 % 2 != 0) {
            int i3 = i / 2;
            int i4 = i2 / 2;
            if (i3 <= GRID_WIDTH && i4 <= GRID_HEIGHT && (abs = Mth.abs((i3 * GRID_WIDTH) + i4)) < ALL_BLOCKS.size()) {
                blockState = ALL_BLOCKS.get(abs);
            }
        }
        return blockState;
    }
}
