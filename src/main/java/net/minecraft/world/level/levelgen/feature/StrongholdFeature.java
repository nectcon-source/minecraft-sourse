package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/StrongholdFeature.class */
public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
    public StrongholdFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return StrongholdStart::new;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        return chunkGenerator.hasStronghold(new ChunkPos(i, i2));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/StrongholdFeature$StrongholdStart.class */
    public static class StrongholdStart extends StructureStart<NoneFeatureConfiguration> {
        private final long seed;

        public StrongholdStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
            this.seed = j;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            int i3 = 0;
            while (true) {
                this.pieces.clear();
                this.boundingBox = BoundingBox.getUnknownBox();
                int i4 = i3;
                i3++;
                this.random.setLargeFeatureSeed(this.seed + i4, i, i2);
                StrongholdPieces.resetPieces();
                StrongholdPieces.StartPiece startPiece = new StrongholdPieces.StartPiece(this.random, (i << 4) + 2, (i2 << 4) + 2);
                this.pieces.add(startPiece);
                startPiece.addChildren(startPiece, this.pieces, this.random);
                List<StructurePiece> list = startPiece.pendingChildren;
                while (!list.isEmpty()) {
                    list.remove(this.random.nextInt(list.size())).addChildren(startPiece, this.pieces, this.random);
                }
                calculateBoundingBox();
                moveBelowSeaLevel(chunkGenerator.getSeaLevel(), this.random, 10);
                if (!this.pieces.isEmpty() && startPiece.portalRoomPiece != null) {
                    return;
                }
            }
        }
    }
}
