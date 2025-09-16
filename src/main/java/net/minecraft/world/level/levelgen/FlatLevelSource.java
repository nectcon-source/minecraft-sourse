package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/FlatLevelSource.class */
public class FlatLevelSource extends ChunkGenerator {
    public static final Codec<FlatLevelSource> CODEC = FlatLevelGeneratorSettings.CODEC.fieldOf("settings").xmap(FlatLevelSource::new, FlatLevelSource::settings).codec();
    private final FlatLevelGeneratorSettings settings;

    public FlatLevelSource(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        super(new FixedBiomeSource(flatLevelGeneratorSettings.getBiomeFromSettings()), new FixedBiomeSource(flatLevelGeneratorSettings.getBiome()), flatLevelGeneratorSettings.structureSettings(), 0L);
        this.settings = flatLevelGeneratorSettings;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public ChunkGenerator withSeed(long j) {
        return this;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.settings;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
        public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public int getSpawnHeight() {
        BlockState[] layers = this.settings.getLayers();
        for (int i = 0; i < layers.length; i++) {
            if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(layers[i] == null ? Blocks.AIR.defaultBlockState() : layers[i])) {
                return i - 1;
            }
        }
        return layers.length;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
        public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        BlockState[] layers = this.settings.getLayers();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Heightmap orCreateHeightmapUnprimed = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap orCreateHeightmapUnprimed2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int i = 0; i < layers.length; i++) {
            BlockState blockState = layers[i];
            if (blockState != null) {
                for (int i2 = 0; i2 < 16; i2++) {
                    for (int i3 = 0; i3 < 16; i3++) {
                        chunkAccess.setBlockState(mutableBlockPos.set(i2, i, i3), blockState, false);
                        orCreateHeightmapUnprimed.update(i2, i, i3, blockState);
                        orCreateHeightmapUnprimed2.update(i2, i, i3, blockState);
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public int getBaseHeight(int i, int i2, Heightmap.Types types) {
        BlockState[] layers = this.settings.getLayers();
        for (int length = layers.length - 1; length >= 0; length--) {
            BlockState blockState = layers[length];
            if (blockState != null && types.isOpaque().test(blockState)) {
                return length + 1;
            }
        }
        return 0;
    }

    @Override // net.minecraft.world.level.chunk.ChunkGenerator
    public BlockGetter getBaseColumn(int i, int i2) {
        return new NoiseColumn( Arrays.stream(this.settings.getLayers()).map(blockState -> blockState == null ? Blocks.AIR.defaultBlockState() : blockState).toArray(i3 -> new BlockState[i3]));
    }
}
