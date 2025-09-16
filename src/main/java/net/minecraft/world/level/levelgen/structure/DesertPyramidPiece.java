package net.minecraft.world.level.levelgen.structure;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/DesertPyramidPiece.class */
public class DesertPyramidPiece extends ScatteredFeaturePiece {
    private final boolean[] hasPlacedChest;

    public DesertPyramidPiece(Random random, int i, int i2) {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, random, i, 64, i2, 21, 15, 21);
        this.hasPlacedChest = new boolean[4];
    }

    public DesertPyramidPiece(StructureManager structureManager, CompoundTag compoundTag) {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, compoundTag);
        this.hasPlacedChest = new boolean[4];
        this.hasPlacedChest[0] = compoundTag.getBoolean("hasPlacedChest0");
        this.hasPlacedChest[1] = compoundTag.getBoolean("hasPlacedChest1");
        this.hasPlacedChest[2] = compoundTag.getBoolean("hasPlacedChest2");
        this.hasPlacedChest[3] = compoundTag.getBoolean("hasPlacedChest3");
    }

    @Override // net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
        compoundTag.putBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
        compoundTag.putBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
        compoundTag.putBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        generateBox(worldGenLevel, boundingBox, 0, -4, 0, this.width - 1, 0, this.depth - 1, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        for (int i = 1; i <= 9; i++) {
            generateBox(worldGenLevel, boundingBox, i, i, i, (this.width - 1) - i, i, (this.depth - 1) - i, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, i + 1, i, i + 1, (this.width - 2) - i, i, (this.depth - 2) - i, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        }
        for (int i2 = 0; i2 < this.width; i2++) {
            for (int i3 = 0; i3 < this.depth; i3++) {
                fillColumnDown(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), i2, -5, i3, boundingBox);
            }
        }
        BlockState blockState = (BlockState) Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState blockState2 = (BlockState) Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        BlockState blockState3 = (BlockState) Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState blockState4 = (BlockState) Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
        generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        placeBlock(worldGenLevel, blockState, 2, 10, 0, boundingBox);
        placeBlock(worldGenLevel, blockState2, 2, 10, 4, boundingBox);
        placeBlock(worldGenLevel, blockState3, 0, 10, 2, boundingBox);
        placeBlock(worldGenLevel, blockState4, 4, 10, 2, boundingBox);
        generateBox(worldGenLevel, boundingBox, this.width - 5, 0, 0, this.width - 1, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 4, 10, 1, this.width - 2, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        placeBlock(worldGenLevel, blockState, this.width - 3, 10, 0, boundingBox);
        placeBlock(worldGenLevel, blockState2, this.width - 3, 10, 4, boundingBox);
        placeBlock(worldGenLevel, blockState3, this.width - 5, 10, 2, boundingBox);
        placeBlock(worldGenLevel, blockState4, this.width - 1, 10, 2, boundingBox);
        generateBox(worldGenLevel, boundingBox, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 9, 1, 0, 11, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 1, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 2, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 3, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, 3, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 3, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 2, 1, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 1, 1, boundingBox);
        generateBox(worldGenLevel, boundingBox, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 4, 1, 2, 8, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 12, 1, 2, 16, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 5, 4, 5, this.width - 6, 4, this.depth - 6, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 9, 4, 9, 11, 4, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 8, 1, 8, 8, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 12, 1, 8, 12, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 8, 1, 12, 8, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 12, 1, 12, 12, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 5, 1, 5, this.width - 2, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 7, 7, 9, this.width - 7, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 5, 5, 9, 5, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 6, 5, 9, this.width - 6, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 5, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 6, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 6, 6, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 6, 5, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 6, 6, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 7, 6, 10, boundingBox);
        generateBox(worldGenLevel, boundingBox, 2, 4, 4, 2, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 3, 4, 4, this.width - 3, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        placeBlock(worldGenLevel, blockState, 2, 4, 5, boundingBox);
        placeBlock(worldGenLevel, blockState, 2, 3, 4, boundingBox);
        placeBlock(worldGenLevel, blockState, this.width - 3, 4, 5, boundingBox);
        placeBlock(worldGenLevel, blockState, this.width - 3, 3, 4, boundingBox);
        generateBox(worldGenLevel, boundingBox, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 3, 1, 3, this.width - 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), 1, 1, 2, boundingBox);
        placeBlock(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), this.width - 2, 1, 2, boundingBox);
        placeBlock(worldGenLevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), 1, 2, 2, boundingBox);
        placeBlock(worldGenLevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), this.width - 2, 2, 2, boundingBox);
        placeBlock(worldGenLevel, blockState4, 2, 1, 2, boundingBox);
        placeBlock(worldGenLevel, blockState3, this.width - 3, 1, 2, boundingBox);
        generateBox(worldGenLevel, boundingBox, 4, 3, 5, 4, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 5, 3, 5, this.width - 5, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 3, 1, 5, 4, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, this.width - 6, 1, 5, this.width - 5, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        for (int i4 = 5; i4 <= 17; i4 += 2) {
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 4, 1, i4, boundingBox);
            placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 4, 2, i4, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), this.width - 5, 1, i4, boundingBox);
            placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), this.width - 5, 2, i4, boundingBox);
        }
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 7, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 8, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 9, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 9, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 8, 0, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12, 0, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 7, 0, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 13, 0, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 11, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 11, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 12, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 13, boundingBox);
        placeBlock(worldGenLevel, Blocks.BLUE_TERRACOTTA.defaultBlockState(), 10, 0, 10, boundingBox);
        int i5 = 0;
        while (true) {
            int i6 = i5;
            if (i6 > this.width - 1) {
                break;
            }
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 2, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 2, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 2, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 3, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 3, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 3, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 4, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), i6, 4, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 4, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 5, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 5, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 5, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 6, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), i6, 6, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 6, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 7, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 7, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i6, 7, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 8, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 8, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i6, 8, 3, boundingBox);
            i5 = i6 + (this.width - 1);
        }
        int i7 = 2;
        while (true) {
            int i8 = i7;
            if (i8 > this.width - 3) {
                break;
            }
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 - 1, 2, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8, 2, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 + 1, 2, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 - 1, 3, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8, 3, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 + 1, 3, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8 - 1, 4, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), i8, 4, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8 + 1, 4, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 - 1, 5, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8, 5, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 + 1, 5, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8 - 1, 6, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), i8, 6, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8 + 1, 6, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8 - 1, 7, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8, 7, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), i8 + 1, 7, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 - 1, 8, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8, 8, 0, boundingBox);
            placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), i8 + 1, 8, 0, boundingBox);
            i7 = i8 + ((this.width - 3) - 2);
        }
        generateBox(worldGenLevel, boundingBox, 8, 4, 0, 12, 6, 0, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, 6, 0, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, 6, 0, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 5, 0, boundingBox);
        placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, 5, 0, boundingBox);
        placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 5, 0, boundingBox);
        generateBox(worldGenLevel, boundingBox, 8, -14, 8, 12, -11, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 8, -10, 8, 12, -10, 12, Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 8, -9, 8, 12, -9, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        generateBox(worldGenLevel, boundingBox, 9, -11, 9, 11, -1, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 10, -11, 10, boundingBox);
        generateBox(worldGenLevel, boundingBox, 9, -13, 9, 11, -13, 11, Blocks.TNT.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, -11, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, -10, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 7, -10, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 7, -11, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, -11, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, -10, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 13, -10, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 13, -11, 10, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -11, 8, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -10, 8, boundingBox);
        placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 7, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 7, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -11, 12, boundingBox);
        placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -10, 12, boundingBox);
        placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 13, boundingBox);
        placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 13, boundingBox);
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (!this.hasPlacedChest[next.get2DDataValue()]) {
                this.hasPlacedChest[next.get2DDataValue()] = createChest(worldGenLevel, boundingBox, random, 10 + (next.getStepX() * 2), -11, 10 + (next.getStepZ() * 2), BuiltInLootTables.DESERT_PYRAMID);
            }
        }
        return true;
    }
}
