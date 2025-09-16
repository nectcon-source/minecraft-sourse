package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/JigsawFeature.class */
public class JigsawFeature extends StructureFeature<JigsawConfiguration> {
    private final int startY;
    private final boolean doExpansionHack;
    private final boolean projectStartToHeightmap;

    public JigsawFeature(Codec<JigsawConfiguration> codec, int i, boolean z, boolean z2) {
        super(codec);
        this.startY = i;
        this.doExpansionHack = z;
        this.projectStartToHeightmap = z2;
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
        return (structureFeature, i, i2, boundingBox, i3, j) -> {
            return new FeatureStart(this, i, i2, boundingBox, i3, j);
        };
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/JigsawFeature$FeatureStart.class */
    public static class FeatureStart extends BeardedStructureStart<JigsawConfiguration> {
        private final JigsawFeature feature;

        public FeatureStart(JigsawFeature jigsawFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(jigsawFeature, i, i2, boundingBox, i3, j);
            this.feature = jigsawFeature;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, JigsawConfiguration jigsawConfiguration) {
            BlockPos blockPos = new BlockPos(i * 16, this.feature.startY, i2 * 16);
            Pools.bootstrap();
            JigsawPlacement.addPieces(registryAccess, jigsawConfiguration, PoolElementStructurePiece::new, chunkGenerator, structureManager, blockPos, this.pieces, this.random, this.feature.doExpansionHack, this.feature.projectStartToHeightmap);
            calculateBoundingBox();
        }
    }
}
