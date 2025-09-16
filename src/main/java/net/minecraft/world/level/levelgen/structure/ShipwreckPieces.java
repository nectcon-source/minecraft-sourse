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
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/ShipwreckPieces.class */
public class ShipwreckPieces {
    private static final BlockPos PIVOT = new BlockPos(4, 0, 15);
    private static final ResourceLocation[] STRUCTURE_LOCATION_BEACHED = {new ResourceLocation("shipwreck/with_mast"), new ResourceLocation("shipwreck/sideways_full"), new ResourceLocation("shipwreck/sideways_fronthalf"), new ResourceLocation("shipwreck/sideways_backhalf"), new ResourceLocation("shipwreck/rightsideup_full"), new ResourceLocation("shipwreck/rightsideup_fronthalf"), new ResourceLocation("shipwreck/rightsideup_backhalf"), new ResourceLocation("shipwreck/with_mast_degraded"), new ResourceLocation("shipwreck/rightsideup_full_degraded"), new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"), new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")};
    private static final ResourceLocation[] STRUCTURE_LOCATION_OCEAN = {new ResourceLocation("shipwreck/with_mast"), new ResourceLocation("shipwreck/upsidedown_full"), new ResourceLocation("shipwreck/upsidedown_fronthalf"), new ResourceLocation("shipwreck/upsidedown_backhalf"), new ResourceLocation("shipwreck/sideways_full"), new ResourceLocation("shipwreck/sideways_fronthalf"), new ResourceLocation("shipwreck/sideways_backhalf"), new ResourceLocation("shipwreck/rightsideup_full"), new ResourceLocation("shipwreck/rightsideup_fronthalf"), new ResourceLocation("shipwreck/rightsideup_backhalf"), new ResourceLocation("shipwreck/with_mast_degraded"), new ResourceLocation("shipwreck/upsidedown_full_degraded"), new ResourceLocation("shipwreck/upsidedown_fronthalf_degraded"), new ResourceLocation("shipwreck/upsidedown_backhalf_degraded"), new ResourceLocation("shipwreck/sideways_full_degraded"), new ResourceLocation("shipwreck/sideways_fronthalf_degraded"), new ResourceLocation("shipwreck/sideways_backhalf_degraded"), new ResourceLocation("shipwreck/rightsideup_full_degraded"), new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"), new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")};

    public static void addPieces(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, Random random, ShipwreckConfiguration shipwreckConfiguration) {
        list.add(new ShipwreckPiece(structureManager, (ResourceLocation) Util.getRandom(shipwreckConfiguration.isBeached ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, random), blockPos, rotation, shipwreckConfiguration.isBeached));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/ShipwreckPieces$ShipwreckPiece.class */
    public static class ShipwreckPiece extends TemplateStructurePiece {
        private final Rotation rotation;
        private final ResourceLocation templateLocation;
        private final boolean isBeached;

        public ShipwreckPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, boolean z) {
            super(StructurePieceType.SHIPWRECK_PIECE, 0);
            this.templatePosition = blockPos;
            this.rotation = rotation;
            this.templateLocation = resourceLocation;
            this.isBeached = z;
            loadTemplate(structureManager);
        }

        public ShipwreckPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.SHIPWRECK_PIECE, compoundTag);
            this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
            this.isBeached = compoundTag.getBoolean("isBeached");
            this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            loadTemplate(structureManager);
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString("Template", this.templateLocation.toString());
            compoundTag.putBoolean("isBeached", this.isBeached);
            compoundTag.putString("Rot", this.rotation.name());
        }

        private void loadTemplate(StructureManager structureManager) {
            setup(structureManager.getOrCreate(this.templateLocation), this.templatePosition, new StructurePlaceSettings().setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot(ShipwreckPieces.PIVOT).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR));
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
        protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
            if ("map_chest".equals(str)) {
                RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, blockPos.below(), BuiltInLootTables.SHIPWRECK_MAP);
            } else if ("treasure_chest".equals(str)) {
                RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, blockPos.below(), BuiltInLootTables.SHIPWRECK_TREASURE);
            } else if ("supply_chest".equals(str)) {
                RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, blockPos.below(), BuiltInLootTables.SHIPWRECK_SUPPLY);
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int i;
            int i2 = 256;
            int i3 = 0;
            BlockPos size = this.template.getSize();
            Heightmap.Types types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
            int x = size.getX() * size.getZ();
            if (x == 0) {
                i = worldGenLevel.getHeight(types, this.templatePosition.getX(), this.templatePosition.getZ());
            } else {
                for (BlockPos blockPos2 : BlockPos.betweenClosed(this.templatePosition, this.templatePosition.offset(size.getX() - 1, 0, size.getZ() - 1))) {
                    int height = worldGenLevel.getHeight(types, blockPos2.getX(), blockPos2.getZ());
                    i3 += height;
                    i2 = Math.min(i2, height);
                }
                i = i3 / x;
            }
            this.templatePosition = new BlockPos(this.templatePosition.getX(), this.isBeached ? (i2 - (size.getY() / 2)) - random.nextInt(3) : i, this.templatePosition.getZ());
            return super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
        }
    }
}
