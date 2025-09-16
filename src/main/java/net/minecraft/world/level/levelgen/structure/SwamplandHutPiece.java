package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/SwamplandHutPiece.class */
public class SwamplandHutPiece extends ScatteredFeaturePiece {
    private boolean spawnedWitch;
    private boolean spawnedCat;

    public SwamplandHutPiece(Random random, int i, int i2) {
        super(StructurePieceType.SWAMPLAND_HUT, random, i, 64, i2, 7, 7, 9);
    }

    public SwamplandHutPiece(StructureManager structureManager, CompoundTag compoundTag) {
        super(StructurePieceType.SWAMPLAND_HUT, compoundTag);
        this.spawnedWitch = compoundTag.getBoolean("Witch");
        this.spawnedCat = compoundTag.getBoolean("Cat");
    }

    @Override // net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Witch", this.spawnedWitch);
        compoundTag.putBoolean("Cat", this.spawnedCat);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        if (!updateAverageGroundHeight(worldGenLevel, boundingBox, 0)) {
            return false;
        }
        generateBox(worldGenLevel, boundingBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, boundingBox);
        placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 1, 3, 4, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 3, 4, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 3, 5, boundingBox);
        placeBlock(worldGenLevel, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, boundingBox);
        placeBlock(worldGenLevel, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, boundingBox);
        placeBlock(worldGenLevel, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, boundingBox);
        placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, boundingBox);
        BlockState blockState = (BlockState) Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState blockState2 = (BlockState) Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState blockState3 = (BlockState) Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
        BlockState blockState4 = (BlockState) Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        generateBox(worldGenLevel, boundingBox, 0, 4, 1, 6, 4, 1, blockState, blockState, false);
        generateBox(worldGenLevel, boundingBox, 0, 4, 2, 0, 4, 7, blockState2, blockState2, false);
        generateBox(worldGenLevel, boundingBox, 6, 4, 2, 6, 4, 7, blockState3, blockState3, false);
        generateBox(worldGenLevel, boundingBox, 0, 4, 8, 6, 4, 8, blockState4, blockState4, false);
        placeBlock(worldGenLevel, (BlockState) blockState.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, boundingBox);
        placeBlock(worldGenLevel, (BlockState) blockState.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, boundingBox);
        placeBlock(worldGenLevel, (BlockState) blockState4.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, boundingBox);
        placeBlock(worldGenLevel, (BlockState) blockState4.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, boundingBox);
        for (int i = 2; i <= 7; i += 5) {
            for (int i2 = 1; i2 <= 5; i2 += 4) {
                fillColumnDown(worldGenLevel, Blocks.OAK_LOG.defaultBlockState(), i2, -1, i, boundingBox);
            }
        }
        if (!this.spawnedWitch) {
            int worldX = getWorldX(2, 5);
            int worldY = getWorldY(2);
            int worldZ = getWorldZ(2, 5);
            if (boundingBox.isInside(new BlockPos(worldX, worldY, worldZ))) {
                this.spawnedWitch = true;
                Witch create = EntityType.WITCH.create(worldGenLevel.getLevel());
                create.setPersistenceRequired();
                create.moveTo(worldX + 0.5d, worldY, worldZ + 0.5d, 0.0f, 0.0f);
                create.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(new BlockPos(worldX, worldY, worldZ)), MobSpawnType.STRUCTURE, null, null);
                worldGenLevel.addFreshEntityWithPassengers(create);
            }
        }
        spawnCat(worldGenLevel, boundingBox);
        return true;
    }

    private void spawnCat(ServerLevelAccessor serverLevelAccessor, BoundingBox boundingBox) {
        if (!this.spawnedCat) {
            int worldX = getWorldX(2, 5);
            int worldY = getWorldY(2);
            int worldZ = getWorldZ(2, 5);
            if (boundingBox.isInside(new BlockPos(worldX, worldY, worldZ))) {
                this.spawnedCat = true;
                Cat create = EntityType.CAT.create(serverLevelAccessor.getLevel());
                create.setPersistenceRequired();
                create.moveTo(worldX + 0.5d, worldY, worldZ + 0.5d, 0.0f, 0.0f);
                create.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(new BlockPos(worldX, worldY, worldZ)), MobSpawnType.STRUCTURE, null, null);
                serverLevelAccessor.addFreshEntityWithPassengers(create);
            }
        }
    }
}
