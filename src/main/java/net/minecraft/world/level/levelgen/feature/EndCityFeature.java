package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/EndCityFeature.class */
public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
    public EndCityFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    protected boolean linearSeparation() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        return getYPositionForFeature(i, i2, chunkGenerator) >= 60;
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return EndCityStart::new;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getYPositionForFeature(int i, int i2, ChunkGenerator chunkGenerator) {
        Rotation random = Rotation.getRandom(new Random(i + (i2 * 10387313)));
        int i3 = 5;
        int i4 = 5;
        if (random == Rotation.CLOCKWISE_90) {
            i3 = -5;
        } else if (random == Rotation.CLOCKWISE_180) {
            i3 = -5;
            i4 = -5;
        } else if (random == Rotation.COUNTERCLOCKWISE_90) {
            i4 = -5;
        }
        int i5 = (i << 4) + 7;
        int i6 = (i2 << 4) + 7;
        return Math.min(Math.min(chunkGenerator.getFirstOccupiedHeight(i5, i6, Heightmap.Types.WORLD_SURFACE_WG), chunkGenerator.getFirstOccupiedHeight(i5, i6 + i4, Heightmap.Types.WORLD_SURFACE_WG)), Math.min(chunkGenerator.getFirstOccupiedHeight(i5 + i3, i6, Heightmap.Types.WORLD_SURFACE_WG), chunkGenerator.getFirstOccupiedHeight(i5 + i3, i6 + i4, Heightmap.Types.WORLD_SURFACE_WG)));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/EndCityFeature$EndCityStart.class */
    public static class EndCityStart extends StructureStart<NoneFeatureConfiguration> {
        public EndCityStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            Rotation random = Rotation.getRandom(this.random);
            int yPositionForFeature = EndCityFeature.getYPositionForFeature(i, i2, chunkGenerator);
            if (yPositionForFeature < 60) {
                return;
            }
            EndCityPieces.startHouseTower(structureManager, new BlockPos((i * 16) + 8, yPositionForFeature, (i2 * 16) + 8), random, this.pieces, this.random);
            calculateBoundingBox();
        }
    }
}
