package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/IglooPieces.class */
public class IglooPieces {
    private static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
    private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
    private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
    private static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7));
    private static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2));

    public static void addPieces(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, Random random) {
        if (random.nextDouble() < 0.5d) {
            int nextInt = random.nextInt(8) + 4;
            list.add(new IglooPiece(structureManager, STRUCTURE_LOCATION_LABORATORY, blockPos, rotation, nextInt * 3));
            for (int i = 0; i < nextInt - 1; i++) {
                list.add(new IglooPiece(structureManager, STRUCTURE_LOCATION_LADDER, blockPos, rotation, i * 3));
            }
        }
        list.add(new IglooPiece(structureManager, STRUCTURE_LOCATION_IGLOO, blockPos, rotation, 0));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/IglooPieces$IglooPiece.class */
    public static class IglooPiece extends TemplateStructurePiece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public IglooPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, int i) {
            super(StructurePieceType.IGLOO, 0);
            this.templateLocation = resourceLocation;
            BlockPos blockPos2 = (BlockPos) IglooPieces.OFFSETS.get(resourceLocation);
            this.templatePosition = blockPos.offset(blockPos2.getX(), blockPos2.getY() - i, blockPos2.getZ());
            this.rotation = rotation;
            loadTemplate(structureManager);
        }

        public IglooPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.IGLOO, compoundTag);
            this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
            this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            loadTemplate(structureManager);
        }

        private void loadTemplate(StructureManager structureManager) {
            setup(structureManager.getOrCreate(this.templateLocation), this.templatePosition, new StructurePlaceSettings().setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot((BlockPos) IglooPieces.PIVOTS.get(this.templateLocation)).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK));
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString("Template", this.templateLocation.toString());
            compoundTag.putString("Rot", this.rotation.name());
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
        protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
            if (!"chest".equals(str)) {
                return;
            }
            serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos.below());
            if (blockEntity instanceof ChestBlockEntity) {
                ((ChestBlockEntity) blockEntity).setLootTable(BuiltInLootTables.IGLOO_CHEST, random.nextLong());
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            StructurePlaceSettings addProcessor = new StructurePlaceSettings().setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot((BlockPos) IglooPieces.PIVOTS.get(this.templateLocation)).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            BlockPos blockPos2 = (BlockPos) IglooPieces.OFFSETS.get(this.templateLocation);
            BlockPos offset = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(addProcessor, new BlockPos(3 - blockPos2.getX(), 0, 0 - blockPos2.getZ())));
            int height = worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, offset.getX(), offset.getZ());
            BlockPos blockPos3 = this.templatePosition;
            this.templatePosition = this.templatePosition.offset(0, (height - 90) - 1, 0);
            boolean postProcess = super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
            if (this.templateLocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
                BlockPos offset2 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(addProcessor, new BlockPos(3, 0, 5)));
                BlockState blockState = worldGenLevel.getBlockState(offset2.below());
                if (!blockState.isAir() && !blockState.is(Blocks.LADDER)) {
                    worldGenLevel.setBlock(offset2, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
                }
            }
            this.templatePosition = blockPos3;
            return postProcess;
        }
    }
}
