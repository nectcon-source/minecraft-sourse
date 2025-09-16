package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/BuriedTreasurePieces.class */
public class BuriedTreasurePieces {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/BuriedTreasurePieces$BuriedTreasurePiece.class */
    public static class BuriedTreasurePiece extends StructurePiece {
        public BuriedTreasurePiece(BlockPos blockPos) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, 0);
            this.boundingBox = new BoundingBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        public BuriedTreasurePiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.boundingBox.x0, worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.x0, this.boundingBox.z0), this.boundingBox.z0);
            while (mutableBlockPos.getY() > 0) {
                BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
                BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos.below());
                if (blockState2 == Blocks.SANDSTONE.defaultBlockState() || blockState2 == Blocks.STONE.defaultBlockState() || blockState2 == Blocks.ANDESITE.defaultBlockState() || blockState2 == Blocks.GRANITE.defaultBlockState() || blockState2 == Blocks.DIORITE.defaultBlockState()) {
                    BlockState defaultBlockState = (blockState.isAir() || isLiquid(blockState)) ? Blocks.SAND.defaultBlockState() : blockState;
                    for (Direction direction : Direction.values()) {
                        BlockPos relative = mutableBlockPos.relative(direction);
                        BlockState blockState3 = worldGenLevel.getBlockState(relative);
                        if (blockState3.isAir() || isLiquid(blockState3)) {
                            BlockState blockState4 = worldGenLevel.getBlockState(relative.below());
                            if ((blockState4.isAir() || isLiquid(blockState4)) && direction != Direction.UP) {
                                worldGenLevel.setBlock(relative, blockState2, 3);
                            } else {
                                worldGenLevel.setBlock(relative, defaultBlockState, 3);
                            }
                        }
                    }
                    this.boundingBox = new BoundingBox(mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ(), mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ());
                    return createChest(worldGenLevel, boundingBox, random, mutableBlockPos, BuiltInLootTables.BURIED_TREASURE, null);
                }
                mutableBlockPos.move(0, -1, 0);
            }
            return false;
        }

        private boolean isLiquid(BlockState blockState) {
            return blockState == Blocks.WATER.defaultBlockState() || blockState == Blocks.LAVA.defaultBlockState();
        }
    }
}
