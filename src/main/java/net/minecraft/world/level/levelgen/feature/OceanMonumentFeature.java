package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/OceanMonumentFeature.class */
public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4));

    public OceanMonumentFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    protected boolean linearSeparation() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        Iterator<Biome> it = biomeSource.getBiomesWithin((i * 16) + 9, chunkGenerator.getSeaLevel(), (i2 * 16) + 9, 16).iterator();
        while (it.hasNext()) {
            if (!it.next().getGenerationSettings().isValidStart(this)) {
                return false;
            }
        }
        for (Biome biome2 : biomeSource.getBiomesWithin((i * 16) + 9, chunkGenerator.getSeaLevel(), (i2 * 16) + 9, 29)) {
            if (biome2.getBiomeCategory() != Biome.BiomeCategory.OCEAN && biome2.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return OceanMonumentStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/OceanMonumentFeature$OceanMonumentStart.class */
    public static class OceanMonumentStart extends StructureStart<NoneFeatureConfiguration> {
        private boolean isCreated;

        public OceanMonumentStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            generatePieces(i, i2);
        }

        private void generatePieces(int i, int i2) {
            this.pieces.add(new OceanMonumentPieces.MonumentBuilding(this.random, (i * 16) - 29, (i2 * 16) - 29, Direction.Plane.HORIZONTAL.getRandomDirection(this.random)));
            calculateBoundingBox();
            this.isCreated = true;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void placeInChunk(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
            if (!this.isCreated) {
                this.pieces.clear();
                generatePieces(getChunkX(), getChunkZ());
            }
            super.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos);
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return MONUMENT_ENEMIES;
    }
}
