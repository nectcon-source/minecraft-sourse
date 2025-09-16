package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces.class */
public class OceanMonumentPieces {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$MonumentRoomFitter.class */
    interface MonumentRoomFitter {
        boolean fits(RoomDefinition roomDefinition);

        OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentPiece.class */
    public static abstract class OceanMonumentPiece extends StructurePiece {
        protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
        protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
        protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
        protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
        protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
        protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
        protected static final Set<Block> FILL_KEEP = ImmutableSet.<Block>builder().add(Blocks.ICE).add(Blocks.PACKED_ICE).add(Blocks.BLUE_ICE).add(FILL_BLOCK.getBlock()).build();
        protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
        protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
        protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
        protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
        protected RoomDefinition roomDefinition;

        protected static final int getRoomIndex(int i, int i2, int i3) {
            return (i2 * 25) + (i3 * 5) + i;
        }

        public OceanMonumentPiece(StructurePieceType structurePieceType, int i) {
            super(structurePieceType, i);
        }

        public OceanMonumentPiece(StructurePieceType structurePieceType, Direction direction, BoundingBox boundingBox) {
            super(structurePieceType, 1);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        protected OceanMonumentPiece(StructurePieceType structurePieceType, int i, Direction direction, RoomDefinition roomDefinition, int i2, int i3, int i4) {
            super(structurePieceType, i);
            setOrientation(direction);
            this.roomDefinition = roomDefinition;
            int i5 = roomDefinition.index;
            int i6 = i5 % 5;
            int i7 = (i5 / 5) % 5;
            int i8 = i5 / 25;
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                this.boundingBox = new BoundingBox(0, 0, 0, (i2 * 8) - 1, (i3 * 4) - 1, (i4 * 8) - 1);
            } else {
                this.boundingBox = new BoundingBox(0, 0, 0, (i4 * 8) - 1, (i3 * 4) - 1, (i2 * 8) - 1);
            }
            switch (direction) {
                case NORTH:
                    this.boundingBox.move(i6 * 8, i8 * 4, ((-(i7 + i4)) * 8) + 1);
                    break;
                case SOUTH:
                    this.boundingBox.move(i6 * 8, i8 * 4, i7 * 8);
                    break;
                case WEST:
                    this.boundingBox.move(((-(i7 + i4)) * 8) + 1, i8 * 4, i6 * 8);
                    break;
                default:
                    this.boundingBox.move(i7 * 8, i8 * 4, i6 * 8);
                    break;
            }
        }

        public OceanMonumentPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
        }

        protected void generateWaterBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, int i6) {
            for (int i7 = i2; i7 <= i5; i7++) {
                for (int i8 = i; i8 <= i4; i8++) {
                    for (int i9 = i3; i9 <= i6; i9++) {
                        BlockState block = getBlock(worldGenLevel, i8, i7, i9, boundingBox);
                        if (!FILL_KEEP.contains(block.getBlock())) {
                            if (getWorldY(i7) >= worldGenLevel.getSeaLevel() && block != FILL_BLOCK) {
                                placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), i8, i7, i9, boundingBox);
                            } else {
                                placeBlock(worldGenLevel, FILL_BLOCK, i8, i7, i9, boundingBox);
                            }
                        }
                    }
                }
            }
        }

        protected void generateDefaultFloor(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, boolean z) {
            if (z) {
                generateBox(worldGenLevel, boundingBox, i + 0, 0, i2 + 0, i + 2, 0, (i2 + 8) - 1, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 5, 0, i2 + 0, (i + 8) - 1, 0, (i2 + 8) - 1, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 3, 0, i2 + 0, i + 4, 0, i2 + 2, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 3, 0, i2 + 5, i + 4, 0, (i2 + 8) - 1, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 3, 0, i2 + 2, i + 4, 0, i2 + 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, i + 3, 0, i2 + 5, i + 4, 0, i2 + 5, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, i + 2, 0, i2 + 3, i + 2, 0, i2 + 4, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, i + 5, 0, i2 + 3, i + 5, 0, i2 + 4, BASE_LIGHT, BASE_LIGHT, false);
                return;
            }
            generateBox(worldGenLevel, boundingBox, i + 0, 0, i2 + 0, (i + 8) - 1, 0, (i2 + 8) - 1, BASE_GRAY, BASE_GRAY, false);
        }

        protected void generateBoxOnFillOnly(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, int i6, BlockState blockState) {
            for (int i7 = i2; i7 <= i5; i7++) {
                for (int i8 = i; i8 <= i4; i8++) {
                    for (int i9 = i3; i9 <= i6; i9++) {
                        if (getBlock(worldGenLevel, i8, i7, i9, boundingBox) == FILL_BLOCK) {
                            placeBlock(worldGenLevel, blockState, i8, i7, i9, boundingBox);
                        }
                    }
                }
            }
        }

        protected boolean chunkIntersects(BoundingBox boundingBox, int i, int i2, int i3, int i4) {
            int worldX = getWorldX(i, i2);
            int worldZ = getWorldZ(i, i2);
            int worldX2 = getWorldX(i3, i4);
            int worldZ2 = getWorldZ(i3, i4);
            return boundingBox.intersects(Math.min(worldX, worldX2), Math.min(worldZ, worldZ2), Math.max(worldX, worldX2), Math.max(worldZ, worldZ2));
        }

        protected boolean spawnElder(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3) {
            int worldX = getWorldX(i, i3);
            int worldY = getWorldY(i2);
            int worldZ = getWorldZ(i, i3);
            if (boundingBox.isInside(new BlockPos(worldX, worldY, worldZ))) {
                ElderGuardian create = EntityType.ELDER_GUARDIAN.create(worldGenLevel.getLevel());
                create.heal(create.getMaxHealth());
                create.moveTo(worldX + 0.5d, worldY, worldZ + 0.5d, 0.0f, 0.0f);
                create.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(create.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                worldGenLevel.addFreshEntityWithPassengers(create);
                return true;
            }
            return false;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$MonumentBuilding.class */
    public static class MonumentBuilding extends OceanMonumentPiece {
        private RoomDefinition sourceRoom;
        private RoomDefinition coreRoom;
        private final List<OceanMonumentPiece> childPieces;

        public MonumentBuilding(Random random, int i, int i2, Direction direction) {
            super(StructurePieceType.OCEAN_MONUMENT_BUILDING, 0);
            this.childPieces = Lists.newArrayList();
            setOrientation(direction);
            Direction orientation = getOrientation();
            if (orientation.getAxis() == Direction.Axis.Z) {
                this.boundingBox = new BoundingBox(i, 39, i2, (i + 58) - 1, 61, (i2 + 58) - 1);
            } else {
                this.boundingBox = new BoundingBox(i, 39, i2, (i + 58) - 1, 61, (i2 + 58) - 1);
            }
            List<RoomDefinition> generateRoomGraph = generateRoomGraph(random);
            this.sourceRoom.claimed = true;
            this.childPieces.add(new OceanMonumentEntryRoom(orientation, this.sourceRoom));
            this.childPieces.add(new OceanMonumentCoreRoom(orientation, this.coreRoom));
            List<MonumentRoomFitter> newArrayList = Lists.newArrayList();
            newArrayList.add(new FitDoubleXYRoom());
            newArrayList.add(new FitDoubleYZRoom());
            newArrayList.add(new FitDoubleZRoom());
            newArrayList.add(new FitDoubleXRoom());
            newArrayList.add(new FitDoubleYRoom());
            newArrayList.add(new FitSimpleTopRoom());
            newArrayList.add(new FitSimpleRoom());
            for (RoomDefinition roomDefinition : generateRoomGraph) {
                if (!roomDefinition.claimed && !roomDefinition.isSpecial()) {
                    Iterator<MonumentRoomFitter> it = newArrayList.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            MonumentRoomFitter next = it.next();
                            if (next.fits(roomDefinition)) {
                                this.childPieces.add(next.create(orientation, roomDefinition, random));
                                break;
                            }
                        }
                    }
                }
            }
            int i3 = this.boundingBox.y0;
            int worldX = getWorldX(9, 22);
            int worldZ = getWorldZ(9, 22);
            Iterator<OceanMonumentPiece> it2 = this.childPieces.iterator();
            while (it2.hasNext()) {
                it2.next().getBoundingBox().move(worldX, i3, worldZ);
            }
            BoundingBox createProper = BoundingBox.createProper(getWorldX(1, 1), getWorldY(1), getWorldZ(1, 1), getWorldX(23, 21), getWorldY(8), getWorldZ(23, 21));
            BoundingBox createProper2 = BoundingBox.createProper(getWorldX(34, 1), getWorldY(1), getWorldZ(34, 1), getWorldX(56, 21), getWorldY(8), getWorldZ(56, 21));
            BoundingBox createProper3 = BoundingBox.createProper(getWorldX(22, 22), getWorldY(13), getWorldZ(22, 22), getWorldX(35, 35), getWorldY(17), getWorldZ(35, 35));
            int nextInt = random.nextInt();
            int i4 = nextInt + 1;
            this.childPieces.add(new OceanMonumentWingRoom(orientation, createProper, nextInt));
            int i5 = i4 + 1;
            this.childPieces.add(new OceanMonumentWingRoom(orientation, createProper2, i4));
            this.childPieces.add(new OceanMonumentPenthouse(orientation, createProper3));
        }

        public MonumentBuilding(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_BUILDING, compoundTag);
            this.childPieces = Lists.newArrayList();
        }

        private List<RoomDefinition> generateRoomGraph(Random random) {
            RoomDefinition[] roomDefinitionArr = new RoomDefinition[75];
            for (int i = 0; i < 5; i++) {
                for (int i2 = 0; i2 < 4; i2++) {
                    int roomIndex = getRoomIndex(i, 0, i2);
                    roomDefinitionArr[roomIndex] = new RoomDefinition(roomIndex);
                }
            }
            for (int i3 = 0; i3 < 5; i3++) {
                for (int i4 = 0; i4 < 4; i4++) {
                    int roomIndex2 = getRoomIndex(i3, 1, i4);
                    roomDefinitionArr[roomIndex2] = new RoomDefinition(roomIndex2);
                }
            }
            for (int i5 = 1; i5 < 4; i5++) {
                for (int i6 = 0; i6 < 2; i6++) {
                    int roomIndex3 = getRoomIndex(i5, 2, i6);
                    roomDefinitionArr[roomIndex3] = new RoomDefinition(roomIndex3);
                }
            }
            this.sourceRoom = roomDefinitionArr[GRIDROOM_SOURCE_INDEX];
            for (int i7 = 0; i7 < 5; i7++) {
                for (int i8 = 0; i8 < 5; i8++) {
                    for (int i9 = 0; i9 < 3; i9++) {
                        int roomIndex4 = getRoomIndex(i7, i9, i8);
                        if (roomDefinitionArr[roomIndex4] != null) {
                            for (Direction direction : Direction.values()) {
                                int stepX = i7 + direction.getStepX();
                                int stepY = i9 + direction.getStepY();
                                int stepZ = i8 + direction.getStepZ();
                                if (stepX >= 0 && stepX < 5 && stepZ >= 0 && stepZ < 5 && stepY >= 0 && stepY < 3) {
                                    int roomIndex5 = getRoomIndex(stepX, stepY, stepZ);
                                    if (roomDefinitionArr[roomIndex5] != null) {
                                        if (stepZ == i8) {
                                            roomDefinitionArr[roomIndex4].setConnection(direction, roomDefinitionArr[roomIndex5]);
                                        } else {
                                            roomDefinitionArr[roomIndex4].setConnection(direction.getOpposite(), roomDefinitionArr[roomIndex5]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            RoomDefinition roomDefinition = new RoomDefinition(1003);
            RoomDefinition roomDefinition2 = new RoomDefinition(1001);
            RoomDefinition roomDefinition3 = new RoomDefinition(1002);
            roomDefinitionArr[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, roomDefinition);
            roomDefinitionArr[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, roomDefinition2);
            roomDefinitionArr[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, roomDefinition3);
            roomDefinition.claimed = true;
            roomDefinition2.claimed = true;
            roomDefinition3.claimed = true;
            this.sourceRoom.isSource = true;
            this.coreRoom = roomDefinitionArr[getRoomIndex(random.nextInt(4), 0, 2)];
            this.coreRoom.claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            List<RoomDefinition> newArrayList = Lists.newArrayList();
            for (RoomDefinition roomDefinition4 : roomDefinitionArr) {
                if (roomDefinition4 != null) {
                    roomDefinition4.updateOpenings();
                    newArrayList.add(roomDefinition4);
                }
            }
            roomDefinition.updateOpenings();
            Collections.shuffle(newArrayList, random);
            int i10 = 1;
            for (RoomDefinition roomDefinition5 : newArrayList) {
                int i11 = 0;
                int i12 = 0;
                while (i11 < 2 && i12 < 5) {
                    i12++;
                    int nextInt = random.nextInt(6);
                    if (roomDefinition5.hasOpening[nextInt]) {
                        int i13 = Direction.from3DDataValue(nextInt).getOpposite().get3DDataValue();
                        roomDefinition5.hasOpening[nextInt] = false;
                        roomDefinition5.connections[nextInt].hasOpening[i13] = false;
                        int i14 = i10;
                        i10++;
                        if (roomDefinition5.findSource(i14)) {
                            i10++;
                            if (roomDefinition5.connections[nextInt].findSource(i10)) {
                                i11++;
                            }
                        }
                        roomDefinition5.hasOpening[nextInt] = true;
                        roomDefinition5.connections[nextInt].hasOpening[i13] = true;
                    }
                }
            }
            newArrayList.add(roomDefinition);
            newArrayList.add(roomDefinition2);
            newArrayList.add(roomDefinition3);
            return newArrayList;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateWaterBox(worldGenLevel, boundingBox, 0, 0, 0, 58, Math.max(worldGenLevel.getSeaLevel(), 64) - this.boundingBox.y0, 58);
            generateWing(false, 0, worldGenLevel, random, boundingBox);
            generateWing(true, 33, worldGenLevel, random, boundingBox);
            generateEntranceArchs(worldGenLevel, random, boundingBox);
            generateEntranceWall(worldGenLevel, random, boundingBox);
            generateRoofPiece(worldGenLevel, random, boundingBox);
            generateLowerWall(worldGenLevel, random, boundingBox);
            generateMiddleWall(worldGenLevel, random, boundingBox);
            generateUpperWall(worldGenLevel, random, boundingBox);
            for (int i = 0; i < 7; i++) {
                int i2 = 0;
                while (i2 < 7) {
                    if (i2 == 0 && i == 3) {
                        i2 = 6;
                    }
                    int i3 = i * 9;
                    int i4 = i2 * 9;
                    for (int i5 = 0; i5 < 4; i5++) {
                        for (int i6 = 0; i6 < 4; i6++) {
                            placeBlock(worldGenLevel, BASE_LIGHT, i3 + i5, 0, i4 + i6, boundingBox);
                            fillColumnDown(worldGenLevel, BASE_LIGHT, i3 + i5, -1, i4 + i6, boundingBox);
                        }
                    }
                    if (i == 0 || i == 6) {
                        i2++;
                    } else {
                        i2 += 6;
                    }
                }
            }
            for (int i7 = 0; i7 < 5; i7++) {
                generateWaterBox(worldGenLevel, boundingBox, (-1) - i7, 0 + (i7 * 2), (-1) - i7, (-1) - i7, 23, 58 + i7);
                generateWaterBox(worldGenLevel, boundingBox, 58 + i7, 0 + (i7 * 2), (-1) - i7, 58 + i7, 23, 58 + i7);
                generateWaterBox(worldGenLevel, boundingBox, 0 - i7, 0 + (i7 * 2), (-1) - i7, 57 + i7, 23, (-1) - i7);
                generateWaterBox(worldGenLevel, boundingBox, 0 - i7, 0 + (i7 * 2), 58 + i7, 57 + i7, 23, 58 + i7);
            }
            for (OceanMonumentPiece oceanMonumentPiece : this.childPieces) {
                if (oceanMonumentPiece.getBoundingBox().intersects(boundingBox)) {
                    oceanMonumentPiece.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
                }
            }
            return true;
        }

        private void generateWing(boolean z, int i, WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            int i2;
            int i3;
            if (chunkIntersects(boundingBox, i, 0, i + 23, 20)) {
                generateBox(worldGenLevel, boundingBox, i + 0, 0, 0, i + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, i + 0, 1, 0, i + 24, 10, 20);
                for (int i4 = 0; i4 < 4; i4++) {
                    generateBox(worldGenLevel, boundingBox, i + i4, i4 + 1, i4, i + i4, i4 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, i + i4 + 7, i4 + 5, i4 + 7, i + i4 + 7, i4 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, (i + 17) - i4, i4 + 5, i4 + 7, (i + 17) - i4, i4 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, (i + 24) - i4, i4 + 1, i4, (i + 24) - i4, i4 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, i + i4 + 1, i4 + 1, i4, (i + 23) - i4, i4 + 1, i4, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, i + i4 + 8, i4 + 5, i4 + 7, (i + 16) - i4, i4 + 5, i4 + 7, BASE_LIGHT, BASE_LIGHT, false);
                }
                generateBox(worldGenLevel, boundingBox, i + 4, 4, 4, i + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 7, 4, 4, i + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 18, 4, 4, i + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 11, 8, 11, i + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
                placeBlock(worldGenLevel, DOT_DECO_DATA, i + 12, 9, 12, boundingBox);
                placeBlock(worldGenLevel, DOT_DECO_DATA, i + 12, 9, 15, boundingBox);
                placeBlock(worldGenLevel, DOT_DECO_DATA, i + 12, 9, 18, boundingBox);
                int i5 = i + (z ? 19 : 5);
                int i6 = i + (z ? 5 : 19);
                for (int i7 = 20; i7 >= 5; i7 -= 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 5, i7, boundingBox);
                }
                for (int i8 = 19; i8 >= 7; i8 -= 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, i6, 5, i8, boundingBox);
                }
                for (int i9 = 0; i9 < 4; i9++) {
                    if (z) {
                        i2 = i + 24;
                        i3 = 17 - (i9 * 3);
                    } else {
                        i2 = i + 17;
                        i3 = i9 * 3;
                    }
                    placeBlock(worldGenLevel, DOT_DECO_DATA, i2 - i3, 5, 5, boundingBox);
                }
                placeBlock(worldGenLevel, DOT_DECO_DATA, i6, 5, 5, boundingBox);
                generateBox(worldGenLevel, boundingBox, i + 11, 1, 12, i + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, i + 12, 1, 11, i + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
            }
        }

        private void generateEntranceArchs(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            if (chunkIntersects(boundingBox, 22, 5, 35, 17)) {
                generateWaterBox(worldGenLevel, boundingBox, 25, 0, 0, 32, 8, 20);
                for (int i = 0; i < 4; i++) {
                    generateBox(worldGenLevel, boundingBox, 24, 2, 5 + (i * 4), 24, 4, 5 + (i * 4), BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 22, 4, 5 + (i * 4), 23, 4, 5 + (i * 4), BASE_LIGHT, BASE_LIGHT, false);
                    placeBlock(worldGenLevel, BASE_LIGHT, 25, 5, 5 + (i * 4), boundingBox);
                    placeBlock(worldGenLevel, BASE_LIGHT, 26, 6, 5 + (i * 4), boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, 26, 5, 5 + (i * 4), boundingBox);
                    generateBox(worldGenLevel, boundingBox, 33, 2, 5 + (i * 4), 33, 4, 5 + (i * 4), BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 34, 4, 5 + (i * 4), 35, 4, 5 + (i * 4), BASE_LIGHT, BASE_LIGHT, false);
                    placeBlock(worldGenLevel, BASE_LIGHT, 32, 5, 5 + (i * 4), boundingBox);
                    placeBlock(worldGenLevel, BASE_LIGHT, 31, 6, 5 + (i * 4), boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, 31, 5, 5 + (i * 4), boundingBox);
                    generateBox(worldGenLevel, boundingBox, 27, 6, 5 + (i * 4), 30, 6, 5 + (i * 4), BASE_GRAY, BASE_GRAY, false);
                }
            }
        }

        private void generateEntranceWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            if (chunkIntersects(boundingBox, 15, 20, 42, 21)) {
                generateBox(worldGenLevel, boundingBox, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 26, 1, 21, 31, 3, 21);
                generateBox(worldGenLevel, boundingBox, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                placeBlock(worldGenLevel, BASE_LIGHT, 27, 3, 21, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 30, 3, 21, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 26, 2, 21, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 31, 2, 21, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 25, 1, 21, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 32, 1, 21, boundingBox);
                for (int i = 0; i < 7; i++) {
                    placeBlock(worldGenLevel, BASE_BLACK, 28 - i, 6 + i, 21, boundingBox);
                    placeBlock(worldGenLevel, BASE_BLACK, 29 + i, 6 + i, 21, boundingBox);
                }
                for (int i2 = 0; i2 < 4; i2++) {
                    placeBlock(worldGenLevel, BASE_BLACK, 28 - i2, 9 + i2, 21, boundingBox);
                    placeBlock(worldGenLevel, BASE_BLACK, 29 + i2, 9 + i2, 21, boundingBox);
                }
                placeBlock(worldGenLevel, BASE_BLACK, 28, 12, 21, boundingBox);
                placeBlock(worldGenLevel, BASE_BLACK, 29, 12, 21, boundingBox);
                for (int i3 = 0; i3 < 3; i3++) {
                    placeBlock(worldGenLevel, BASE_BLACK, 22 - (i3 * 2), 8, 21, boundingBox);
                    placeBlock(worldGenLevel, BASE_BLACK, 22 - (i3 * 2), 9, 21, boundingBox);
                    placeBlock(worldGenLevel, BASE_BLACK, 35 + (i3 * 2), 8, 21, boundingBox);
                    placeBlock(worldGenLevel, BASE_BLACK, 35 + (i3 * 2), 9, 21, boundingBox);
                }
                generateWaterBox(worldGenLevel, boundingBox, 15, 13, 21, 42, 15, 21);
                generateWaterBox(worldGenLevel, boundingBox, 15, 1, 21, 15, 6, 21);
                generateWaterBox(worldGenLevel, boundingBox, 16, 1, 21, 16, 5, 21);
                generateWaterBox(worldGenLevel, boundingBox, 17, 1, 21, 20, 4, 21);
                generateWaterBox(worldGenLevel, boundingBox, 21, 1, 21, 21, 3, 21);
                generateWaterBox(worldGenLevel, boundingBox, 22, 1, 21, 22, 2, 21);
                generateWaterBox(worldGenLevel, boundingBox, 23, 1, 21, 24, 1, 21);
                generateWaterBox(worldGenLevel, boundingBox, 42, 1, 21, 42, 6, 21);
                generateWaterBox(worldGenLevel, boundingBox, 41, 1, 21, 41, 5, 21);
                generateWaterBox(worldGenLevel, boundingBox, 37, 1, 21, 40, 4, 21);
                generateWaterBox(worldGenLevel, boundingBox, 36, 1, 21, 36, 3, 21);
                generateWaterBox(worldGenLevel, boundingBox, 33, 1, 21, 34, 1, 21);
                generateWaterBox(worldGenLevel, boundingBox, 35, 1, 21, 35, 2, 21);
            }
        }

        private void generateRoofPiece(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            if (chunkIntersects(boundingBox, 21, 21, 36, 36)) {
                generateBox(worldGenLevel, boundingBox, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 21, 1, 22, 36, 23, 36);
                for (int i = 0; i < 4; i++) {
                    generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
                }
                generateBox(worldGenLevel, boundingBox, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
                placeBlock(worldGenLevel, BASE_LIGHT, 26, 20, 26, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 27, 21, 27, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 27, 20, 27, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 26, 20, 31, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 27, 21, 30, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 27, 20, 30, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 31, 20, 31, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 30, 21, 30, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 30, 20, 30, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 31, 20, 26, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 30, 21, 27, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 30, 20, 27, boundingBox);
                generateBox(worldGenLevel, boundingBox, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
            }
        }

        private void generateLowerWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            if (chunkIntersects(boundingBox, 0, 21, 6, 58)) {
                generateBox(worldGenLevel, boundingBox, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 21, 6, 7, 57);
                generateBox(worldGenLevel, boundingBox, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);
                for (int i = 0; i < 4; i++) {
                    generateBox(worldGenLevel, boundingBox, i, i + 1, 21, i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i2 = 23; i2 < 53; i2 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, 5, 5, i2, boundingBox);
                }
                placeBlock(worldGenLevel, DOT_DECO_DATA, 5, 5, 52, boundingBox);
                for (int i3 = 0; i3 < 4; i3++) {
                    generateBox(worldGenLevel, boundingBox, i3, i3 + 1, 21, i3, i3 + 1, 57 - i3, BASE_LIGHT, BASE_LIGHT, false);
                }
                generateBox(worldGenLevel, boundingBox, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
            }
            if (chunkIntersects(boundingBox, 51, 21, 58, 58)) {
                generateBox(worldGenLevel, boundingBox, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 51, 1, 21, 57, 7, 57);
                generateBox(worldGenLevel, boundingBox, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);
                for (int i4 = 0; i4 < 4; i4++) {
                    generateBox(worldGenLevel, boundingBox, 57 - i4, i4 + 1, 21, 57 - i4, i4 + 1, 57 - i4, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i5 = 23; i5 < 53; i5 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, 52, 5, i5, boundingBox);
                }
                placeBlock(worldGenLevel, DOT_DECO_DATA, 52, 5, 52, boundingBox);
                generateBox(worldGenLevel, boundingBox, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
            }
            if (chunkIntersects(boundingBox, 0, 51, 57, 57)) {
                generateBox(worldGenLevel, boundingBox, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 7, 1, 51, 50, 10, 57);
                for (int i6 = 0; i6 < 4; i6++) {
                    generateBox(worldGenLevel, boundingBox, i6 + 1, i6 + 1, 57 - i6, 56 - i6, i6 + 1, 57 - i6, BASE_LIGHT, BASE_LIGHT, false);
                }
            }
        }

        private void generateMiddleWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            if (chunkIntersects(boundingBox, 7, 21, 13, 50)) {
                generateBox(worldGenLevel, boundingBox, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 7, 1, 21, 13, 10, 50);
                generateBox(worldGenLevel, boundingBox, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);
                for (int i = 0; i < 4; i++) {
                    generateBox(worldGenLevel, boundingBox, i + 7, i + 5, 21, i + 7, i + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i2 = 21; i2 <= 45; i2 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, 12, 9, i2, boundingBox);
                }
            }
            if (chunkIntersects(boundingBox, 44, 21, 50, 54)) {
                generateBox(worldGenLevel, boundingBox, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 44, 1, 21, 50, 10, 50);
                generateBox(worldGenLevel, boundingBox, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);
                for (int i3 = 0; i3 < 4; i3++) {
                    generateBox(worldGenLevel, boundingBox, 50 - i3, i3 + 5, 21, 50 - i3, i3 + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i4 = 21; i4 <= 45; i4 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, 45, 9, i4, boundingBox);
                }
            }
            if (chunkIntersects(boundingBox, 8, 44, 49, 54)) {
                generateBox(worldGenLevel, boundingBox, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 14, 1, 44, 43, 10, 50);
                for (int i5 = 12; i5 <= 45; i5 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 9, 45, boundingBox);
                    placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 9, 52, boundingBox);
                    if (i5 == 12 || i5 == 18 || i5 == 24 || i5 == 33 || i5 == 39 || i5 == 45) {
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 9, 47, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 9, 50, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 10, 45, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 10, 46, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 10, 51, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 10, 52, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 11, 47, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 11, 50, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 12, 48, boundingBox);
                        placeBlock(worldGenLevel, DOT_DECO_DATA, i5, 12, 49, boundingBox);
                    }
                }
                for (int i6 = 0; i6 < 3; i6++) {
                    generateBox(worldGenLevel, boundingBox, 8 + i6, 5 + i6, 54, 49 - i6, 5 + i6, 54, BASE_GRAY, BASE_GRAY, false);
                }
                generateBox(worldGenLevel, boundingBox, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
            }
        }

        private void generateUpperWall(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox) {
            if (chunkIntersects(boundingBox, 14, 21, 20, 43)) {
                generateBox(worldGenLevel, boundingBox, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 14, 1, 22, 20, 14, 43);
                generateBox(worldGenLevel, boundingBox, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);
                for (int i = 0; i < 4; i++) {
                    generateBox(worldGenLevel, boundingBox, i + 14, i + 9, 21, i + 14, i + 9, 43 - i, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i2 = 23; i2 <= 39; i2 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, 19, 13, i2, boundingBox);
                }
            }
            if (chunkIntersects(boundingBox, 37, 21, 43, 43)) {
                generateBox(worldGenLevel, boundingBox, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 37, 1, 22, 43, 14, 43);
                generateBox(worldGenLevel, boundingBox, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);
                for (int i3 = 0; i3 < 4; i3++) {
                    generateBox(worldGenLevel, boundingBox, 43 - i3, i3 + 9, 21, 43 - i3, i3 + 9, 43 - i3, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i4 = 23; i4 <= 39; i4 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, 38, 13, i4, boundingBox);
                }
            }
            if (chunkIntersects(boundingBox, 15, 37, 42, 43)) {
                generateBox(worldGenLevel, boundingBox, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
                generateWaterBox(worldGenLevel, boundingBox, 21, 1, 37, 36, 14, 43);
                generateBox(worldGenLevel, boundingBox, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);
                for (int i5 = 0; i5 < 4; i5++) {
                    generateBox(worldGenLevel, boundingBox, 15 + i5, i5 + 9, 43 - i5, 42 - i5, i5 + 9, 43 - i5, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (int i6 = 21; i6 <= 36; i6 += 3) {
                    placeBlock(worldGenLevel, DOT_DECO_DATA, i6, 13, 38, boundingBox);
                }
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentEntryRoom.class */
    public static class OceanMonumentEntryRoom extends OceanMonumentPiece {
        public OceanMonumentEntryRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, direction, roomDefinition, 1, 1, 1);
        }

        public OceanMonumentEntryRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
            }
            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 1, 2, 4);
            }
            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 6, 1, 3, 7, 2, 4);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentSimpleRoom.class */
    public static class OceanMonumentSimpleRoom extends OceanMonumentPiece {
        private int mainDesign;

        public OceanMonumentSimpleRoom(Direction direction, RoomDefinition roomDefinition, Random random) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, direction, roomDefinition, 1, 1, 1);
            this.mainDesign = random.nextInt(3);
        }

        public OceanMonumentSimpleRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
            }
            boolean z = (this.mainDesign == 0 || !random.nextBoolean() || this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()] || this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()] || this.roomDefinition.countOpenings() <= 1) ? false : true;
            if (this.mainDesign == 0) {
                generateBox(worldGenLevel, boundingBox, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
                placeBlock(worldGenLevel, LAMP_BLOCK, 1, 2, 1, boundingBox);
                generateBox(worldGenLevel, boundingBox, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
                placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 1, boundingBox);
                generateBox(worldGenLevel, boundingBox, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
                placeBlock(worldGenLevel, LAMP_BLOCK, 1, 2, 6, boundingBox);
                generateBox(worldGenLevel, boundingBox, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
                placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 6, boundingBox);
                if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                }
            } else if (this.mainDesign == 1) {
                generateBox(worldGenLevel, boundingBox, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 2, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 5, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 5, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 2, boundingBox);
                generateBox(worldGenLevel, boundingBox, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                placeBlock(worldGenLevel, BASE_GRAY, 1, 2, 0, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 0, 2, 1, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 1, 2, 7, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 0, 2, 6, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 6, 2, 7, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 7, 2, 6, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 6, 2, 0, boundingBox);
                placeBlock(worldGenLevel, BASE_GRAY, 7, 2, 1, boundingBox);
                if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
                    generateBox(worldGenLevel, boundingBox, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
                }
            } else if (this.mainDesign == 2) {
                generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
                if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
                }
                if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
                }
                if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
                }
                if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
                }
            }
            if (z) {
                generateBox(worldGenLevel, boundingBox, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
                generateBox(worldGenLevel, boundingBox, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentSimpleTopRoom.class */
    public static class OceanMonumentSimpleTopRoom extends OceanMonumentPiece {
        public OceanMonumentSimpleTopRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, direction, roomDefinition, 1, 1, 1);
        }

        public OceanMonumentSimpleTopRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
            }
            for (int i = 1; i <= 6; i++) {
                for (int i2 = 1; i2 <= 6; i2++) {
                    if (random.nextInt(3) != 0) {
                        int i3 = random.nextInt(4) == 0 ? 0 : 1;
                        BlockState defaultBlockState = Blocks.WET_SPONGE.defaultBlockState();
                        generateBox(worldGenLevel, boundingBox, i, 2 + i3, i2, i, 3, i2, defaultBlockState, defaultBlockState, false);
                    }
                }
            }
            generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentDoubleYRoom.class */
    public static class OceanMonumentDoubleYRoom extends OceanMonumentPiece {
        public OceanMonumentDoubleYRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, direction, roomDefinition, 1, 2, 1);
        }

        public OceanMonumentDoubleYRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
            if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 6, 8, 6, BASE_GRAY);
            }
            generateBox(worldGenLevel, boundingBox, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            RoomDefinition roomDefinition2 = this.roomDefinition;
            for (int i = 1; i <= 5; i += 4) {
                if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 2, i, 0, 2, i + 2, 0, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 5, i, 0, 5, i + 2, 0, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 3, i + 2, 0, 4, i + 2, 0, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 0, i, 0, 7, i + 2, 0, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, i + 1, 0, 7, i + 1, 0, BASE_GRAY, BASE_GRAY, false);
                }
                if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 2, i, 7, 2, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 5, i, 7, 5, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 3, i + 2, 7, 4, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 0, i, 7, 7, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, i + 1, 7, 7, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }
                if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 0, i, 2, 0, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, i, 5, 0, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, i + 2, 3, 0, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 0, i, 0, 0, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 0, i + 1, 0, 0, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }
                if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
                    generateBox(worldGenLevel, boundingBox, 7, i, 2, 7, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 7, i, 5, 7, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 7, i + 2, 3, 7, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    generateBox(worldGenLevel, boundingBox, 7, i, 0, 7, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, 7, i + 1, 0, 7, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }
                roomDefinition2 = roomDefinition;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentDoubleXRoom.class */
    public static class OceanMonumentDoubleXRoom extends OceanMonumentPiece {
        public OceanMonumentDoubleXRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, direction, roomDefinition, 2, 1, 1);
        }

        public OceanMonumentDoubleXRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 8, 0, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition2.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 7, 4, 6, BASE_GRAY);
            }
            if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 8, 4, 1, 14, 4, 6, BASE_GRAY);
            }
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 3, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 9, 2, 3, boundingBox);
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 11, 1, 0, 12, 2, 0);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 11, 1, 7, 12, 2, 7);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 15, 1, 3, 15, 2, 4);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentDoubleZRoom.class */
    public static class OceanMonumentDoubleZRoom extends OceanMonumentPiece {
        public OceanMonumentDoubleZRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, direction, roomDefinition, 1, 1, 2);
        }

        public OceanMonumentDoubleZRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 8, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition2.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 7, BASE_GRAY);
            }
            if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 8, 6, 4, 14, BASE_GRAY);
            }
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
            placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 5, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 5, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 10, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 10, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 2, 3, 5, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 5, 3, 5, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 2, 3, 10, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 5, 3, 10, boundingBox);
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 15, 4, 2, 15);
            }
            if (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 11, 0, 2, 12);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 7, 1, 11, 7, 2, 12);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentDoubleXYRoom.class */
    public static class OceanMonumentDoubleXYRoom extends OceanMonumentPiece {
        public OceanMonumentDoubleXYRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, direction, roomDefinition, 2, 2, 1);
        }

        public OceanMonumentDoubleXYRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            RoomDefinition roomDefinition3 = roomDefinition2.connections[Direction.UP.get3DDataValue()];
            RoomDefinition roomDefinition4 = roomDefinition.connections[Direction.UP.get3DDataValue()];
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 8, 0, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition3.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 7, 8, 6, BASE_GRAY);
            }
            if (roomDefinition4.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 8, 8, 1, 14, 8, 6, BASE_GRAY);
            }
            for (int i = 1; i <= 7; i++) {
                BlockState blockState = BASE_LIGHT;
                if (i == 2 || i == 6) {
                    blockState = BASE_GRAY;
                }
                generateBox(worldGenLevel, boundingBox, 0, i, 0, 0, i, 7, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 15, i, 0, 15, i, 7, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 1, i, 0, 15, i, 0, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 1, i, 7, 14, i, 7, blockState, blockState, false);
            }
            generateBox(worldGenLevel, boundingBox, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            placeBlock(worldGenLevel, BASE_LIGHT, 6, 6, 2, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 9, 6, 2, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 6, 6, 5, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 9, 6, 5, boundingBox);
            generateBox(worldGenLevel, boundingBox, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
            placeBlock(worldGenLevel, LAMP_BLOCK, 5, 4, 2, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 5, 4, 5, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 10, 4, 2, boundingBox);
            placeBlock(worldGenLevel, LAMP_BLOCK, 10, 4, 5, boundingBox);
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 11, 1, 0, 12, 2, 0);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 11, 1, 7, 12, 2, 7);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 15, 1, 3, 15, 2, 4);
            }
            if (roomDefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 5, 0, 4, 6, 0);
            }
            if (roomDefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 5, 7, 4, 6, 7);
            }
            if (roomDefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 6, 4);
            }
            if (roomDefinition4.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 6, 0);
            }
            if (roomDefinition4.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 11, 5, 7, 12, 6, 7);
            }
            if (roomDefinition4.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 15, 5, 3, 15, 6, 4);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentDoubleYZRoom.class */
    public static class OceanMonumentDoubleYZRoom extends OceanMonumentPiece {
        public OceanMonumentDoubleYZRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, direction, roomDefinition, 1, 2, 2);
        }

        public OceanMonumentDoubleYZRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            RoomDefinition roomDefinition3 = roomDefinition.connections[Direction.UP.get3DDataValue()];
            RoomDefinition roomDefinition4 = roomDefinition2.connections[Direction.UP.get3DDataValue()];
            if (this.roomDefinition.index / 25 > 0) {
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 8, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition4.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 6, 8, 7, BASE_GRAY);
            }
            if (roomDefinition3.connections[Direction.UP.get3DDataValue()] == null) {
                generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 8, 6, 8, 14, BASE_GRAY);
            }
            for (int i = 1; i <= 7; i++) {
                BlockState blockState = BASE_LIGHT;
                if (i == 2 || i == 6) {
                    blockState = BASE_GRAY;
                }
                generateBox(worldGenLevel, boundingBox, 0, i, 0, 0, i, 15, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 7, i, 0, 7, i, 15, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 1, i, 0, 6, i, 0, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 1, i, 15, 6, i, 15, blockState, blockState, false);
            }
            for (int i2 = 1; i2 <= 7; i2++) {
                BlockState blockState2 = BASE_BLACK;
                if (i2 == 2 || i2 == 6) {
                    blockState2 = LAMP_BLOCK;
                }
                generateBox(worldGenLevel, boundingBox, 3, i2, 7, 4, i2, 8, blockState2, blockState2, false);
            }
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 1, 15, 4, 2, 15);
            }
            if (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 1, 11, 0, 2, 12);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 7, 1, 11, 7, 2, 12);
            }
            if (roomDefinition4.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 5, 0, 4, 6, 0);
            }
            if (roomDefinition4.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 7, 5, 3, 7, 6, 4);
                generateBox(worldGenLevel, boundingBox, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            }
            if (roomDefinition4.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 6, 4);
                generateBox(worldGenLevel, boundingBox, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            }
            if (roomDefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 3, 5, 15, 4, 6, 15);
            }
            if (roomDefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 0, 5, 11, 0, 6, 12);
                generateBox(worldGenLevel, boundingBox, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
            }
            if (roomDefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
                generateWaterBox(worldGenLevel, boundingBox, 7, 5, 11, 7, 6, 12);
                generateBox(worldGenLevel, boundingBox, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentCoreRoom.class */
    public static class OceanMonumentCoreRoom extends OceanMonumentPiece {
        public OceanMonumentCoreRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, direction, roomDefinition, 2, 2, 2);
        }

        public OceanMonumentCoreRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 0, 14, 8, 14, BASE_GRAY);
            BlockState blockState = BASE_LIGHT;
            generateBox(worldGenLevel, boundingBox, 0, 7, 0, 0, 7, 15, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 15, 7, 0, 15, 7, 15, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 1, 7, 0, 15, 7, 0, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 1, 7, 15, 14, 7, 15, blockState, blockState, false);
            for (int i = 1; i <= 6; i++) {
                BlockState blockState2 = BASE_LIGHT;
                if (i == 2 || i == 6) {
                    blockState2 = BASE_GRAY;
                }
                for (int i2 = 0; i2 <= 15; i2 += 15) {
                    generateBox(worldGenLevel, boundingBox, i2, i, 0, i2, i, 1, blockState2, blockState2, false);
                    generateBox(worldGenLevel, boundingBox, i2, i, 6, i2, i, 9, blockState2, blockState2, false);
                    generateBox(worldGenLevel, boundingBox, i2, i, 14, i2, i, 15, blockState2, blockState2, false);
                }
                generateBox(worldGenLevel, boundingBox, 1, i, 0, 1, i, 0, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 6, i, 0, 9, i, 0, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 14, i, 0, 14, i, 0, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 1, i, 15, 14, i, 15, blockState2, blockState2, false);
            }
            generateBox(worldGenLevel, boundingBox, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);
            for (int i3 = 3; i3 <= 6; i3 += 3) {
                for (int i4 = 6; i4 <= 9; i4 += 3) {
                    placeBlock(worldGenLevel, LAMP_BLOCK, i4, i3, 6, boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, i4, i3, 9, boundingBox);
                }
            }
            generateBox(worldGenLevel, boundingBox, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentWingRoom.class */
    public static class OceanMonumentWingRoom extends OceanMonumentPiece {
        private int mainDesign;

        public OceanMonumentWingRoom(Direction direction, BoundingBox boundingBox, int i) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, direction, boundingBox);
            this.mainDesign = i & 1;
        }

        public OceanMonumentWingRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.mainDesign == 0) {
                for (int i = 0; i < 4; i++) {
                    generateBox(worldGenLevel, boundingBox, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, BASE_LIGHT, BASE_LIGHT, false);
                }
                generateBox(worldGenLevel, boundingBox, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);
                for (int i2 = 18; i2 >= 7; i2 -= 3) {
                    placeBlock(worldGenLevel, LAMP_BLOCK, 6, 3, i2, boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, 16, 3, i2, boundingBox);
                }
                placeBlock(worldGenLevel, LAMP_BLOCK, 10, 0, 10, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 12, 0, 10, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 10, 0, 12, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 12, 0, 12, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 8, 3, 6, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 14, 3, 6, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 4, 2, 4, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 4, 1, 4, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 4, 0, 4, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 18, 2, 4, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 18, 1, 4, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 18, 0, 4, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 4, 2, 18, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 4, 1, 18, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 4, 0, 18, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 18, 2, 18, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 18, 1, 18, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 18, 0, 18, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 9, 7, 20, boundingBox);
                placeBlock(worldGenLevel, BASE_LIGHT, 13, 7, 20, boundingBox);
                generateBox(worldGenLevel, boundingBox, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                spawnElder(worldGenLevel, boundingBox, 11, 2, 16);
                return true;
            }
            if (this.mainDesign == 1) {
                generateBox(worldGenLevel, boundingBox, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
                generateBox(worldGenLevel, boundingBox, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
                int i3 = 9;
                for (int i4 = 0; i4 < 2; i4++) {
                    placeBlock(worldGenLevel, BASE_LIGHT, i3, 6, 20, boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, i3, 5, 20, boundingBox);
                    placeBlock(worldGenLevel, BASE_LIGHT, i3, 4, 20, boundingBox);
                    i3 = 13;
                }
                generateBox(worldGenLevel, boundingBox, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
                int i5 = 10;
                for (int i6 = 0; i6 < 2; i6++) {
                    generateBox(worldGenLevel, boundingBox, i5, 0, 10, i5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, i5, 0, 12, i5, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
                    placeBlock(worldGenLevel, LAMP_BLOCK, i5, 0, 10, boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, i5, 0, 12, boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, i5, 4, 10, boundingBox);
                    placeBlock(worldGenLevel, LAMP_BLOCK, i5, 4, 12, boundingBox);
                    i5 = 12;
                }
                int i7 = 8;
                for (int i8 = 0; i8 < 2; i8++) {
                    generateBox(worldGenLevel, boundingBox, i7, 0, 7, i7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    generateBox(worldGenLevel, boundingBox, i7, 0, 14, i7, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
                    i7 = 14;
                }
                generateBox(worldGenLevel, boundingBox, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
                generateBox(worldGenLevel, boundingBox, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
                spawnElder(worldGenLevel, boundingBox, 11, 5, 13);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$OceanMonumentPenthouse.class */
    public static class OceanMonumentPenthouse extends OceanMonumentPiece {
        public OceanMonumentPenthouse(Direction direction, BoundingBox boundingBox) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, direction, boundingBox);
        }

        public OceanMonumentPenthouse(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            for (int i = 2; i <= 11; i += 3) {
                placeBlock(worldGenLevel, LAMP_BLOCK, 0, 0, i, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, 13, 0, i, boundingBox);
                placeBlock(worldGenLevel, LAMP_BLOCK, i, 0, 0, boundingBox);
            }
            generateBox(worldGenLevel, boundingBox, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
            placeBlock(worldGenLevel, BASE_LIGHT, 5, 0, 8, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 8, 0, 8, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 10, 0, 10, boundingBox);
            placeBlock(worldGenLevel, BASE_LIGHT, 3, 0, 10, boundingBox);
            generateBox(worldGenLevel, boundingBox, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
            generateBox(worldGenLevel, boundingBox, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
            int i2 = 3;
            for (int i3 = 0; i3 < 2; i3++) {
                for (int i4 = 2; i4 <= 8; i4 += 3) {
                    generateBox(worldGenLevel, boundingBox, i2, 0, i4, i2, 2, i4, BASE_LIGHT, BASE_LIGHT, false);
                }
                i2 = 10;
            }
            generateBox(worldGenLevel, boundingBox, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            generateBox(worldGenLevel, boundingBox, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
            generateWaterBox(worldGenLevel, boundingBox, 6, -1, 3, 7, -1, 4);
            spawnElder(worldGenLevel, boundingBox, 6, 1, 6);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$RoomDefinition.class */
    static class RoomDefinition {
        private final int index;
        private final RoomDefinition[] connections = new RoomDefinition[6];
        private final boolean[] hasOpening = new boolean[6];
        private boolean claimed;
        private boolean isSource;
        private int scanIndex;

        public RoomDefinition(int i) {
            this.index = i;
        }

        public void setConnection(Direction direction, RoomDefinition roomDefinition) {
            this.connections[direction.get3DDataValue()] = roomDefinition;
            roomDefinition.connections[direction.getOpposite().get3DDataValue()] = this;
        }

        public void updateOpenings() {
            for (int i = 0; i < 6; i++) {
                this.hasOpening[i] = this.connections[i] != null;
            }
        }

        public boolean findSource(int i) {
            if (this.isSource) {
                return true;
            }
            this.scanIndex = i;
            for (int i2 = 0; i2 < 6; i2++) {
                if (this.connections[i2] != null && this.hasOpening[i2] && this.connections[i2].scanIndex != i && this.connections[i2].findSource(i)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSpecial() {
            return this.index >= 75;
        }

        public int countOpenings() {
            int i = 0;
            for (int i2 = 0; i2 < 6; i2++) {
                if (this.hasOpening[i2]) {
                    i++;
                }
            }
            return i;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitSimpleRoom.class */
    static class FitSimpleRoom implements MonumentRoomFitter {
        private FitSimpleRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            return true;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            roomDefinition.claimed = true;
            return new OceanMonumentSimpleRoom(direction, roomDefinition, random);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitSimpleTopRoom.class */
    static class FitSimpleTopRoom implements MonumentRoomFitter {
        private FitSimpleTopRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            return (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()] || roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] || roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] || roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()] || roomDefinition.hasOpening[Direction.UP.get3DDataValue()]) ? false : true;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            roomDefinition.claimed = true;
            return new OceanMonumentSimpleTopRoom(direction, roomDefinition);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitDoubleYRoom.class */
    static class FitDoubleYRoom implements MonumentRoomFitter {
        private FitDoubleYRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            return roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleYRoom(direction, roomDefinition);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitDoubleXRoom.class */
    static class FitDoubleXRoom implements MonumentRoomFitter {
        private FitDoubleXRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            return roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] && !roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleXRoom(direction, roomDefinition);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitDoubleZRoom.class */
    static class FitDoubleZRoom implements MonumentRoomFitter {
        private FitDoubleZRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            return roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            RoomDefinition roomDefinition2 = roomDefinition;
            if (!roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] || roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed) {
                roomDefinition2 = roomDefinition.connections[Direction.SOUTH.get3DDataValue()];
            }
            roomDefinition2.claimed = true;
            roomDefinition2.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleZRoom(direction, roomDefinition2);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitDoubleXYRoom.class */
    static class FitDoubleXYRoom implements MonumentRoomFitter {
        private FitDoubleXYRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] && !roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed && roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed) {
                RoomDefinition roomDefinition2 = roomDefinition.connections[Direction.EAST.get3DDataValue()];
                return roomDefinition2.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition2.connections[Direction.UP.get3DDataValue()].claimed;
            }
            return false;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleXYRoom(direction, roomDefinition);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanMonumentPieces$FitDoubleYZRoom.class */
    static class FitDoubleYZRoom implements MonumentRoomFitter {
        private FitDoubleYZRoom() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public boolean fits(RoomDefinition roomDefinition) {
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed && roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed) {
                RoomDefinition roomDefinition2 = roomDefinition.connections[Direction.NORTH.get3DDataValue()];
                return roomDefinition2.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition2.connections[Direction.UP.get3DDataValue()].claimed;
            }
            return false;
        }

        @Override // net.minecraft.world.level.levelgen.structure.OceanMonumentPieces.MonumentRoomFitter
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, Random random) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleYZRoom(direction, roomDefinition);
        }
    }
}
