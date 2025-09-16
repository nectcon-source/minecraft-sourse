package net.minecraft.world.level.levelgen.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherFossilPieces.class */
public class NetherFossilPieces {
    private static final ResourceLocation[] FOSSILS = {new ResourceLocation("nether_fossils/fossil_1"), new ResourceLocation("nether_fossils/fossil_2"), new ResourceLocation("nether_fossils/fossil_3"), new ResourceLocation("nether_fossils/fossil_4"), new ResourceLocation("nether_fossils/fossil_5"), new ResourceLocation("nether_fossils/fossil_6"), new ResourceLocation("nether_fossils/fossil_7"), new ResourceLocation("nether_fossils/fossil_8"), new ResourceLocation("nether_fossils/fossil_9"), new ResourceLocation("nether_fossils/fossil_10"), new ResourceLocation("nether_fossils/fossil_11"), new ResourceLocation("nether_fossils/fossil_12"), new ResourceLocation("nether_fossils/fossil_13"), new ResourceLocation("nether_fossils/fossil_14")};

    public static void addPieces(StructureManager structureManager, List<StructurePiece> list, Random random, BlockPos blockPos) {
        list.add(new NetherFossilPiece(structureManager, (ResourceLocation) Util.getRandom(FOSSILS, random), blockPos, Rotation.getRandom(random)));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherFossilPieces$NetherFossilPiece.class */
    public static class NetherFossilPiece extends TemplateStructurePiece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public NetherFossilPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation) {
            super(StructurePieceType.NETHER_FOSSIL, 0);
            this.templateLocation = resourceLocation;
            this.templatePosition = blockPos;
            this.rotation = rotation;
            loadTemplate(structureManager);
        }

        public NetherFossilPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FOSSIL, compoundTag);
            this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
            this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            loadTemplate(structureManager);
        }

        private void loadTemplate(StructureManager structureManager) {
            setup(structureManager.getOrCreate(this.templateLocation), this.templatePosition, new StructurePlaceSettings().setRotation(this.rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR));
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString("Template", this.templateLocation.toString());
            compoundTag.putString("Rot", this.rotation.name());
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
        protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            boundingBox.expand(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
            return super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
        }
    }
}
