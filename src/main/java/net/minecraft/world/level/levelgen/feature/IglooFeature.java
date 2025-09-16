package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/IglooFeature.class */
public class IglooFeature extends StructureFeature<NoneFeatureConfiguration> {
    public IglooFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return FeatureStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/IglooFeature$FeatureStart.class */
    public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            IglooPieces.addPieces(structureManager, new BlockPos(i * 16, 90, i2 * 16), Rotation.getRandom(this.random), this.pieces, this.random);
            calculateBoundingBox();
        }
    }
}
