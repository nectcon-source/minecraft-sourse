package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherFossilFeature.class */
public class NetherFossilFeature extends StructureFeature<NoneFeatureConfiguration> {
    public NetherFossilFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return FeatureStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherFossilFeature$FeatureStart.class */
    public static class FeatureStart extends BeardedStructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            ChunkPos chunkPos = new ChunkPos(i, i2);
            int minBlockX = chunkPos.getMinBlockX() + this.random.nextInt(16);
            int minBlockZ = chunkPos.getMinBlockZ() + this.random.nextInt(16);
            int seaLevel = chunkGenerator.getSeaLevel();
            int nextInt = seaLevel + this.random.nextInt((chunkGenerator.getGenDepth() - 2) - seaLevel);
            BlockGetter baseColumn = chunkGenerator.getBaseColumn(minBlockX, minBlockZ);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(minBlockX, nextInt, minBlockZ);
            while (nextInt > seaLevel) {
                BlockState blockState = baseColumn.getBlockState(mutableBlockPos);
                mutableBlockPos.move(Direction.DOWN);
                BlockState blockState2 = baseColumn.getBlockState(mutableBlockPos);
                if (blockState.isAir() && (blockState2.is(Blocks.SOUL_SAND) || blockState2.isFaceSturdy(baseColumn, mutableBlockPos, Direction.UP))) {
                    break;
                } else {
                    nextInt--;
                }
            }
            if (nextInt <= seaLevel) {
                return;
            }
            NetherFossilPieces.addPieces(structureManager, this.pieces, this.random, new BlockPos(minBlockX, nextInt, minBlockZ));
            calculateBoundingBox();
        }
    }
}
