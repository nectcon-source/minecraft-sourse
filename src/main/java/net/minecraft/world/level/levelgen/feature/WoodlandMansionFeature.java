package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/WoodlandMansionFeature.class */
public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    protected boolean linearSeparation() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        Iterator<Biome> it = biomeSource.getBiomesWithin((i * 16) + 9, chunkGenerator.getSeaLevel(), (i2 * 16) + 9, 32).iterator();
        while (it.hasNext()) {
            if (!it.next().getGenerationSettings().isValidStart(this)) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return WoodlandMansionStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/WoodlandMansionFeature$WoodlandMansionStart.class */
    public static class WoodlandMansionStart extends StructureStart<NoneFeatureConfiguration> {
        public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            Rotation random = Rotation.getRandom(this.random);
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
            int min = Math.min(Math.min(chunkGenerator.getFirstOccupiedHeight(i5, i6, Heightmap.Types.WORLD_SURFACE_WG), chunkGenerator.getFirstOccupiedHeight(i5, i6 + i4, Heightmap.Types.WORLD_SURFACE_WG)), Math.min(chunkGenerator.getFirstOccupiedHeight(i5 + i3, i6, Heightmap.Types.WORLD_SURFACE_WG), chunkGenerator.getFirstOccupiedHeight(i5 + i3, i6 + i4, Heightmap.Types.WORLD_SURFACE_WG)));
            if (min < 60) {
                return;
            }
            BlockPos blockPos = new BlockPos((i * 16) + 8, min + 1, (i2 * 16) + 8);
            List<WoodlandMansionPieces.WoodlandMansionPiece> newLinkedList = Lists.newLinkedList();
            WoodlandMansionPieces.generateMansion(structureManager, blockPos, random, newLinkedList, this.random);
            this.pieces.addAll(newLinkedList);
            calculateBoundingBox();
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void placeInChunk(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
            super.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos);
            int i = this.boundingBox.y0;
            for (int i2 = boundingBox.x0; i2 <= boundingBox.x1; i2++) {
                for (int i3 = boundingBox.z0; i3 <= boundingBox.z1; i3++) {
                    BlockPos blockPos = new BlockPos(i2, i, i3);
                    if (!worldGenLevel.isEmptyBlock(blockPos) && this.boundingBox.isInside(blockPos)) {
                        boolean z = false;
                        Iterator<StructurePiece> it = this.pieces.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (it.next().getBoundingBox().isInside(blockPos)) {
                                    z = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (z) {
                            for (int i4 = i - 1; i4 > 1; i4--) {
                                BlockPos blockPos2 = new BlockPos(i2, i4, i3);
                                if (worldGenLevel.isEmptyBlock(blockPos2) || worldGenLevel.getBlockState(blockPos2).getMaterial().isLiquid()) {
                                    worldGenLevel.setBlock(blockPos2, Blocks.COBBLESTONE.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
