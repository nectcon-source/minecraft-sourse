package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces.class */
public class WoodlandMansionPieces {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$WoodlandMansionPiece.class */
    public static class WoodlandMansionPiece extends TemplateStructurePiece {
        private final String templateName;
        private final Rotation rotation;
        private final Mirror mirror;

        public WoodlandMansionPiece(StructureManager structureManager, String str, BlockPos blockPos, Rotation rotation) {
            this(structureManager, str, blockPos, rotation, Mirror.NONE);
        }

        public WoodlandMansionPiece(StructureManager structureManager, String str, BlockPos blockPos, Rotation rotation, Mirror mirror) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, 0);
            this.templateName = str;
            this.templatePosition = blockPos;
            this.rotation = rotation;
            this.mirror = mirror;
            loadTemplate(structureManager);
        }

        public WoodlandMansionPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, compoundTag);
            this.templateName = compoundTag.getString("Template");
            this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            this.mirror = Mirror.valueOf(compoundTag.getString("Mi"));
            loadTemplate(structureManager);
        }

        private void loadTemplate(StructureManager structureManager) {
            setup(structureManager.getOrCreate(new ResourceLocation("woodland_mansion/" + this.templateName)), this.templatePosition, new StructurePlaceSettings().setIgnoreEntities(true).setRotation(this.rotation).setMirror(this.mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK));
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString("Template", this.templateName);
            compoundTag.putString("Rot", this.placeSettings.getRotation().name());
            compoundTag.putString("Mi", this.placeSettings.getMirror().name());
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
        protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
            AbstractIllager create;
            if (str.startsWith("Chest")) {
                Rotation rotation = this.placeSettings.getRotation();
                BlockState defaultBlockState = Blocks.CHEST.defaultBlockState();
                if ("ChestWest".equals(str)) {
                    defaultBlockState = (BlockState) defaultBlockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.WEST));
                } else if ("ChestEast".equals(str)) {
                    defaultBlockState = (BlockState) defaultBlockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.EAST));
                } else if ("ChestSouth".equals(str)) {
                    defaultBlockState = (BlockState) defaultBlockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.SOUTH));
                } else if ("ChestNorth".equals(str)) {
                    defaultBlockState = (BlockState) defaultBlockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.NORTH));
                }
                createChest(serverLevelAccessor, boundingBox, random, blockPos, BuiltInLootTables.WOODLAND_MANSION, defaultBlockState);
                return;
            }
            switch (str) {
                case "Mage":
                    create = EntityType.EVOKER.create(serverLevelAccessor.getLevel());
                    break;
                case "Warrior":
                    create = EntityType.VINDICATOR.create(serverLevelAccessor.getLevel());
                    break;
                default:
                    return;
            }
            create.setPersistenceRequired();
            create.moveTo(blockPos, 0.0f, 0.0f);
            create.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(create.blockPosition()), MobSpawnType.STRUCTURE, null, null);
            serverLevelAccessor.addFreshEntityWithPassengers(create);
            serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    public static void generateMansion(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<WoodlandMansionPiece> list, Random random) {
        new MansionPiecePlacer(structureManager, random).createMansion(blockPos, rotation, list, new MansionGrid(random));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$PlacementData.class */
    static class PlacementData {
        public Rotation rotation;
        public BlockPos position;
        public String wallType;

        private PlacementData() {
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$MansionPiecePlacer.class */
    static class MansionPiecePlacer {
        private final StructureManager structureManager;
        private final Random random;
        private int startX;
        private int startY;

        public MansionPiecePlacer(StructureManager structureManager, Random random) {
            this.structureManager = structureManager;
            this.random = random;
        }

        public void createMansion(BlockPos blockPos, Rotation rotation, List<WoodlandMansionPiece> list, MansionGrid mansionGrid) {
            PlacementData placementData = new PlacementData();
            placementData.position = blockPos;
            placementData.rotation = rotation;
            placementData.wallType = "wall_flat";
            PlacementData placementData2 = new PlacementData();
            entrance(list, placementData);
            placementData2.position = placementData.position.above(8);
            placementData2.rotation = placementData.rotation;
            placementData2.wallType = "wall_window";
            if (!list.isEmpty()) {
            }
            SimpleGrid simpleGrid = mansionGrid.baseGrid;
            SimpleGrid simpleGrid2 = mansionGrid.thirdFloorGrid;
            this.startX = mansionGrid.entranceX + 1;
            this.startY = mansionGrid.entranceY + 1;
            int i = mansionGrid.entranceX + 1;
            int i2 = mansionGrid.entranceY;
            traverseOuterWalls(list, placementData, simpleGrid, Direction.SOUTH, this.startX, this.startY, i, i2);
            traverseOuterWalls(list, placementData2, simpleGrid, Direction.SOUTH, this.startX, this.startY, i, i2);
            PlacementData placementData3 = new PlacementData();
            placementData3.position = placementData.position.above(19);
            placementData3.rotation = placementData.rotation;
            placementData3.wallType = "wall_window";
            boolean z = false;
            for (int i3 = 0; i3 < simpleGrid2.height && !z; i3++) {
                for (int i4 = simpleGrid2.width - 1; i4 >= 0 && !z; i4--) {
                    if (MansionGrid.isHouse(simpleGrid2, i4, i3)) {
                        placementData3.position = placementData3.position.relative(rotation.rotate(Direction.SOUTH), 8 + ((i3 - this.startY) * 8));
                        placementData3.position = placementData3.position.relative(rotation.rotate(Direction.EAST), (i4 - this.startX) * 8);
                        traverseWallPiece(list, placementData3);
                        traverseOuterWalls(list, placementData3, simpleGrid2, Direction.SOUTH, i4, i3, i4, i3);
                        z = true;
                    }
                }
            }
            createRoof(list, blockPos.above(16), rotation, simpleGrid, simpleGrid2);
            createRoof(list, blockPos.above(27), rotation, simpleGrid2, null);
            if (!list.isEmpty()) {
            }
            FloorRoomCollection[] floorRoomCollectionArr = {new FirstFloorRoomCollection(), new SecondFloorRoomCollection(), new ThirdFloorRoomCollection()};
            int i5 = 0;
            while (i5 < 3) {
                BlockPos above = blockPos.above((8 * i5) + (i5 == 2 ? 3 : 0));
                SimpleGrid simpleGrid3 = mansionGrid.floorRooms[i5];
                SimpleGrid simpleGrid4 = i5 == 2 ? simpleGrid2 : simpleGrid;
                String str = i5 == 0 ? "carpet_south_1" : "carpet_south_2";
                String str2 = i5 == 0 ? "carpet_west_1" : "carpet_west_2";
                for (int i6 = 0; i6 < simpleGrid4.height; i6++) {
                    for (int i7 = 0; i7 < simpleGrid4.width; i7++) {
                        if (simpleGrid4.get(i7, i6) == 1) {
                            BlockPos relative = above.relative(rotation.rotate(Direction.SOUTH), 8 + ((i6 - this.startY) * 8)).relative(rotation.rotate(Direction.EAST), (i7 - this.startX) * 8);
                            list.add(new WoodlandMansionPiece(this.structureManager, "corridor_floor", relative, rotation));
                            if (simpleGrid4.get(i7, i6 - 1) == 1 || (simpleGrid3.get(i7, i6 - 1) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "carpet_north", relative.relative(rotation.rotate(Direction.EAST), 1).above(), rotation));
                            }
                            if (simpleGrid4.get(i7 + 1, i6) == 1 || (simpleGrid3.get(i7 + 1, i6) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "carpet_east", relative.relative(rotation.rotate(Direction.SOUTH), 1).relative(rotation.rotate(Direction.EAST), 5).above(), rotation));
                            }
                            if (simpleGrid4.get(i7, i6 + 1) == 1 || (simpleGrid3.get(i7, i6 + 1) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPiece(this.structureManager, str, relative.relative(rotation.rotate(Direction.SOUTH), 5).relative(rotation.rotate(Direction.WEST), 1), rotation));
                            }
                            if (simpleGrid4.get(i7 - 1, i6) == 1 || (simpleGrid3.get(i7 - 1, i6) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPiece(this.structureManager, str2, relative.relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.NORTH), 1), rotation));
                            }
                        }
                    }
                }
                String str3 = i5 == 0 ? "indoors_wall_1" : "indoors_wall_2";
                String str4 = i5 == 0 ? "indoors_door_1" : "indoors_door_2";
                List<Direction> newArrayList = Lists.newArrayList();
                for (int i8 = 0; i8 < simpleGrid4.height; i8++) {
                    for (int i9 = 0; i9 < simpleGrid4.width; i9++) {
                        boolean z2 = i5 == 2 && simpleGrid4.get(i9, i8) == 3;
                        if (simpleGrid4.get(i9, i8) == 2 || z2) {
                            int i10 = simpleGrid3.get(i9, i8);
                            int i11 = i10 & 983040;
                            int i12 = i10 & 65535;
                            boolean z3 = z2 && (i10 & 8388608) == 8388608;
                            newArrayList.clear();
                            if ((i10 & 2097152) == 2097152) {
                                Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
                                while (it.hasNext()) {
                                    Direction next = it.next();
                                    if (simpleGrid4.get(i9 + next.getStepX(), i8 + next.getStepZ()) == 1) {
                                        newArrayList.add(next);
                                    }
                                }
                            }
                            Direction direction = null;
                            if (!newArrayList.isEmpty()) {
                                direction = newArrayList.get(this.random.nextInt(newArrayList.size()));
                            } else if ((i10 & 1048576) == 1048576) {
                                direction = Direction.UP;
                            }
                            BlockPos relative2 = above.relative(rotation.rotate(Direction.SOUTH), 8 + ((i8 - this.startY) * 8)).relative(rotation.rotate(Direction.EAST), (-1) + ((i9 - this.startX) * 8));
                            if (MansionGrid.isHouse(simpleGrid4, i9 - 1, i8) && !mansionGrid.isRoomId(simpleGrid4, i9 - 1, i8, i5, i12)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, direction == Direction.WEST ? str4 : str3, relative2, rotation));
                            }
                            if (simpleGrid4.get(i9 + 1, i8) == 1 && !z3) {
                                list.add(new WoodlandMansionPiece(this.structureManager, direction == Direction.EAST ? str4 : str3, relative2.relative(rotation.rotate(Direction.EAST), 8), rotation));
                            }
                            if (MansionGrid.isHouse(simpleGrid4, i9, i8 + 1) && !mansionGrid.isRoomId(simpleGrid4, i9, i8 + 1, i5, i12)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, direction == Direction.SOUTH ? str4 : str3, relative2.relative(rotation.rotate(Direction.SOUTH), 7).relative(rotation.rotate(Direction.EAST), 7), rotation.getRotated(Rotation.CLOCKWISE_90)));
                            }
                            if (simpleGrid4.get(i9, i8 - 1) == 1 && !z3) {
                                list.add(new WoodlandMansionPiece(this.structureManager, direction == Direction.NORTH ? str4 : str3, relative2.relative(rotation.rotate(Direction.NORTH), 1).relative(rotation.rotate(Direction.EAST), 7), rotation.getRotated(Rotation.CLOCKWISE_90)));
                            }
                            if (i11 == 65536) {
                                addRoom1x1(list, relative2, rotation, direction, floorRoomCollectionArr[i5]);
                            } else if (i11 == 131072 && direction != null) {
                                addRoom1x2(list, relative2, rotation, mansionGrid.get1x2RoomDirection(simpleGrid4, i9, i8, i5, i12), direction, floorRoomCollectionArr[i5], (i10 & 4194304) == 4194304);
                            } else if (i11 == 262144 && direction != null && direction != Direction.UP) {
                                Direction clockWise = direction.getClockWise();
                                if (!mansionGrid.isRoomId(simpleGrid4, i9 + clockWise.getStepX(), i8 + clockWise.getStepZ(), i5, i12)) {
                                    clockWise = clockWise.getOpposite();
                                }
                                addRoom2x2(list, relative2, rotation, clockWise, direction, floorRoomCollectionArr[i5]);
                            } else if (i11 == 262144 && direction == Direction.UP) {
                                addRoom2x2Secret(list, relative2, rotation, floorRoomCollectionArr[i5]);
                            }
                        }
                    }
                }
                i5++;
            }
        }

        private void traverseOuterWalls(List<WoodlandMansionPiece> list, PlacementData placementData, SimpleGrid simpleGrid, Direction direction, int i, int i2, int i3, int i4) {
            int i5 = i;
            int i6 = i2;
            while (true) {
                if (!MansionGrid.isHouse(simpleGrid, i5 + direction.getStepX(), i6 + direction.getStepZ())) {
                    traverseTurn(list, placementData);
                    direction = direction.getClockWise();
                    if (i5 != i3 || i6 != i4 || direction != direction) {
                        traverseWallPiece(list, placementData);
                    }
                } else if (MansionGrid.isHouse(simpleGrid, i5 + direction.getStepX(), i6 + direction.getStepZ()) && MansionGrid.isHouse(simpleGrid, i5 + direction.getStepX() + direction.getCounterClockWise().getStepX(), i6 + direction.getStepZ() + direction.getCounterClockWise().getStepZ())) {
                    traverseInnerTurn(list, placementData);
                    i5 += direction.getStepX();
                    i6 += direction.getStepZ();
                    direction = direction.getCounterClockWise();
                } else {
                    i5 += direction.getStepX();
                    i6 += direction.getStepZ();
                    if (i5 != i3 || i6 != i4 || direction != direction) {
                        traverseWallPiece(list, placementData);
                    }
                }
                if (i5 == i3 && i6 == i4 && direction == direction) {
                    return;
                }
            }
        }

        private void createRoof(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, SimpleGrid simpleGrid, @Nullable SimpleGrid simpleGrid2) {
            for (int i = 0; i < simpleGrid.height; i++) {
                for (int i2 = 0; i2 < simpleGrid.width; i2++) {
                    BlockPos relative = blockPos.relative(rotation.rotate(Direction.SOUTH), 8 + ((i - this.startY) * 8)).relative(rotation.rotate(Direction.EAST), (i2 - this.startX) * 8);
                    boolean z = simpleGrid2 != null && MansionGrid.isHouse(simpleGrid2, i2, i);
                    if (MansionGrid.isHouse(simpleGrid, i2, i) && !z) {
                        list.add(new WoodlandMansionPiece(this.structureManager, "roof", relative.above(3), rotation));
                        if (!MansionGrid.isHouse(simpleGrid, i2 + 1, i)) {
                            list.add(new WoodlandMansionPiece(this.structureManager, "roof_front", relative.relative(rotation.rotate(Direction.EAST), 6), rotation));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, i2 - 1, i)) {
                            list.add(new WoodlandMansionPiece(this.structureManager, "roof_front", relative.relative(rotation.rotate(Direction.EAST), 0).relative(rotation.rotate(Direction.SOUTH), 7), rotation.getRotated(Rotation.CLOCKWISE_180)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, i2, i - 1)) {
                            list.add(new WoodlandMansionPiece(this.structureManager, "roof_front", relative.relative(rotation.rotate(Direction.WEST), 1), rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, i2, i + 1)) {
                            list.add(new WoodlandMansionPiece(this.structureManager, "roof_front", relative.relative(rotation.rotate(Direction.EAST), 6).relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                    }
                }
            }
            if (simpleGrid2 != null) {
                for (int i3 = 0; i3 < simpleGrid.height; i3++) {
                    for (int i4 = 0; i4 < simpleGrid.width; i4++) {
                        BlockPos relative2 = blockPos.relative(rotation.rotate(Direction.SOUTH), 8 + ((i3 - this.startY) * 8)).relative(rotation.rotate(Direction.EAST), (i4 - this.startX) * 8);
                        boolean isHouse = MansionGrid.isHouse(simpleGrid2, i4, i3);
                        if (MansionGrid.isHouse(simpleGrid, i4, i3) && isHouse) {
                            if (!MansionGrid.isHouse(simpleGrid, i4 + 1, i3)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "small_wall", relative2.relative(rotation.rotate(Direction.EAST), 7), rotation));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i4 - 1, i3)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "small_wall", relative2.relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.CLOCKWISE_180)));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i4, i3 - 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "small_wall", relative2.relative(rotation.rotate(Direction.WEST), 0).relative(rotation.rotate(Direction.NORTH), 1), rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i4, i3 + 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "small_wall", relative2.relative(rotation.rotate(Direction.EAST), 6).relative(rotation.rotate(Direction.SOUTH), 7), rotation.getRotated(Rotation.CLOCKWISE_90)));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i4 + 1, i3)) {
                                if (!MansionGrid.isHouse(simpleGrid, i4, i3 - 1)) {
                                    list.add(new WoodlandMansionPiece(this.structureManager, "small_wall_corner", relative2.relative(rotation.rotate(Direction.EAST), 7).relative(rotation.rotate(Direction.NORTH), 2), rotation));
                                }
                                if (!MansionGrid.isHouse(simpleGrid, i4, i3 + 1)) {
                                    list.add(new WoodlandMansionPiece(this.structureManager, "small_wall_corner", relative2.relative(rotation.rotate(Direction.EAST), 8).relative(rotation.rotate(Direction.SOUTH), 7), rotation.getRotated(Rotation.CLOCKWISE_90)));
                                }
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i4 - 1, i3)) {
                                if (!MansionGrid.isHouse(simpleGrid, i4, i3 - 1)) {
                                    list.add(new WoodlandMansionPiece(this.structureManager, "small_wall_corner", relative2.relative(rotation.rotate(Direction.WEST), 2).relative(rotation.rotate(Direction.NORTH), 1), rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                                }
                                if (!MansionGrid.isHouse(simpleGrid, i4, i3 + 1)) {
                                    list.add(new WoodlandMansionPiece(this.structureManager, "small_wall_corner", relative2.relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.SOUTH), 8), rotation.getRotated(Rotation.CLOCKWISE_180)));
                                }
                            }
                        }
                    }
                }
            }
            for (int i5 = 0; i5 < simpleGrid.height; i5++) {
                for (int i6 = 0; i6 < simpleGrid.width; i6++) {
                    BlockPos relative3 = blockPos.relative(rotation.rotate(Direction.SOUTH), 8 + ((i5 - this.startY) * 8)).relative(rotation.rotate(Direction.EAST), (i6 - this.startX) * 8);
                    boolean z2 = simpleGrid2 != null && MansionGrid.isHouse(simpleGrid2, i6, i5);
                    if (MansionGrid.isHouse(simpleGrid, i6, i5) && !z2) {
                        if (!MansionGrid.isHouse(simpleGrid, i6 + 1, i5)) {
                            BlockPos relative4 = relative3.relative(rotation.rotate(Direction.EAST), 6);
                            if (!MansionGrid.isHouse(simpleGrid, i6, i5 + 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_corner", relative4.relative(rotation.rotate(Direction.SOUTH), 6), rotation));
                            } else if (MansionGrid.isHouse(simpleGrid, i6 + 1, i5 + 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_inner_corner", relative4.relative(rotation.rotate(Direction.SOUTH), 5), rotation));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i6, i5 - 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_corner", relative4, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                            } else if (MansionGrid.isHouse(simpleGrid, i6 + 1, i5 - 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_inner_corner", relative3.relative(rotation.rotate(Direction.EAST), 9).relative(rotation.rotate(Direction.NORTH), 2), rotation.getRotated(Rotation.CLOCKWISE_90)));
                            }
                        }
                        if (!MansionGrid.isHouse(simpleGrid, i6 - 1, i5)) {
                            BlockPos relative5 = relative3.relative(rotation.rotate(Direction.EAST), 0).relative(rotation.rotate(Direction.SOUTH), 0);
                            if (!MansionGrid.isHouse(simpleGrid, i6, i5 + 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_corner", relative5.relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.CLOCKWISE_90)));
                            } else if (MansionGrid.isHouse(simpleGrid, i6 - 1, i5 + 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_inner_corner", relative5.relative(rotation.rotate(Direction.SOUTH), 8).relative(rotation.rotate(Direction.WEST), 3), rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, i6, i5 - 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_corner", relative5, rotation.getRotated(Rotation.CLOCKWISE_180)));
                            } else if (MansionGrid.isHouse(simpleGrid, i6 - 1, i5 - 1)) {
                                list.add(new WoodlandMansionPiece(this.structureManager, "roof_inner_corner", relative5.relative(rotation.rotate(Direction.SOUTH), 1), rotation.getRotated(Rotation.CLOCKWISE_180)));
                            }
                        }
                    }
                }
            }
        }

        private void entrance(List<WoodlandMansionPiece> list, PlacementData placementData) {
            list.add(new WoodlandMansionPiece(this.structureManager, "entrance", placementData.position.relative(placementData.rotation.rotate(Direction.WEST), 9), placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 16);
        }

        private void traverseWallPiece(List<WoodlandMansionPiece> list, PlacementData placementData) {
            list.add(new WoodlandMansionPiece(this.structureManager, placementData.wallType, placementData.position.relative(placementData.rotation.rotate(Direction.EAST), 7), placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 8);
        }

        private void traverseTurn(List<WoodlandMansionPiece> list, PlacementData placementData) {
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), -1);
            list.add(new WoodlandMansionPiece(this.structureManager, "wall_corner", placementData.position, placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), -7);
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.WEST), -6);
            placementData.rotation = placementData.rotation.getRotated(Rotation.CLOCKWISE_90);
        }

        private void traverseInnerTurn(List<WoodlandMansionPiece> list, PlacementData placementData) {
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 6);
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.EAST), 8);
            placementData.rotation = placementData.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
        }

        private void addRoom1x1(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, FloorRoomCollection floorRoomCollection) {
            Rotation rotation2 = Rotation.NONE;
            String str = floorRoomCollection.get1x1(this.random);
            if (direction != Direction.EAST) {
                if (direction == Direction.NORTH) {
                    rotation2 = rotation2.getRotated(Rotation.COUNTERCLOCKWISE_90);
                } else if (direction == Direction.WEST) {
                    rotation2 = rotation2.getRotated(Rotation.CLOCKWISE_180);
                } else if (direction == Direction.SOUTH) {
                    rotation2 = rotation2.getRotated(Rotation.CLOCKWISE_90);
                } else {
                    str = floorRoomCollection.get1x1Secret(this.random);
                }
            }
            BlockPos zeroPositionWithTransform = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation2, 7, 7);
            Rotation rotated = rotation2.getRotated(rotation);
            BlockPos rotate = zeroPositionWithTransform.rotate(rotation);
            list.add(new WoodlandMansionPiece(this.structureManager, str, blockPos.offset(rotate.getX(), 0, rotate.getZ()), rotated));
        }

        private void addRoom1x2(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, Direction direction2, FloorRoomCollection floorRoomCollection, boolean z) {
            if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 1), rotation));
                return;
            }
            if (direction2 == Direction.EAST && direction == Direction.NORTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 1).relative(rotation.rotate(Direction.SOUTH), 6), rotation, Mirror.LEFT_RIGHT));
                return;
            }
            if (direction2 == Direction.WEST && direction == Direction.NORTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 7).relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.CLOCKWISE_180)));
                return;
            }
            if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 7), rotation, Mirror.FRONT_BACK));
                return;
            }
            if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 1), rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
                return;
            }
            if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 7), rotation.getRotated(Rotation.CLOCKWISE_90)));
                return;
            }
            if (direction2 == Direction.NORTH && direction == Direction.WEST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 7).relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
                return;
            }
            if (direction2 == Direction.NORTH && direction == Direction.EAST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2SideEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 1).relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                return;
            }
            if (direction2 == Direction.SOUTH && direction == Direction.NORTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2FrontEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 1).relative(rotation.rotate(Direction.NORTH), 8), rotation));
                return;
            }
            if (direction2 == Direction.NORTH && direction == Direction.SOUTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2FrontEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 7).relative(rotation.rotate(Direction.SOUTH), 14), rotation.getRotated(Rotation.CLOCKWISE_180)));
                return;
            }
            if (direction2 == Direction.WEST && direction == Direction.EAST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2FrontEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.EAST), 15), rotation.getRotated(Rotation.CLOCKWISE_90)));
                return;
            }
            if (direction2 == Direction.EAST && direction == Direction.WEST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2FrontEntrance(this.random, z), blockPos.relative(rotation.rotate(Direction.WEST), 7).relative(rotation.rotate(Direction.SOUTH), 6), rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
            } else if (direction2 == Direction.UP && direction == Direction.EAST) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2Secret(this.random), blockPos.relative(rotation.rotate(Direction.EAST), 15), rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.UP && direction == Direction.SOUTH) {
                list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get1x2Secret(this.random), blockPos.relative(rotation.rotate(Direction.EAST), 1).relative(rotation.rotate(Direction.NORTH), 0), rotation));
            }
        }

        private void addRoom2x2(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, Direction direction2, FloorRoomCollection floorRoomCollection) {
            int i = 0;
            int i2 = 0;
            Rotation rotation2 = rotation;
            Mirror mirror = Mirror.NONE;
            if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
                i = -7;
            } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
                i = -7;
                i2 = 6;
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
                i = 1;
                i2 = 14;
                rotation2 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
                i = 7;
                i2 = 14;
                rotation2 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
                i = 7;
                i2 = -8;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_90);
            } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
                i = 1;
                i2 = -8;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
                i = 15;
                i2 = 6;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_180);
            } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
                i = 15;
                mirror = Mirror.FRONT_BACK;
            }
            list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get2x2(this.random), blockPos.relative(rotation.rotate(Direction.EAST), i).relative(rotation.rotate(Direction.SOUTH), i2), rotation2, mirror));
        }

        private void addRoom2x2Secret(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, FloorRoomCollection floorRoomCollection) {
            list.add(new WoodlandMansionPiece(this.structureManager, floorRoomCollection.get2x2Secret(this.random), blockPos.relative(rotation.rotate(Direction.EAST), 1), rotation, Mirror.NONE));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$MansionGrid.class */
    static class MansionGrid {
        private final Random random;
        private final SimpleGrid thirdFloorGrid;
        private final SimpleGrid[] floorRooms;
        private final int entranceX = 7;
        private final int entranceY = 4;
        private final SimpleGrid baseGrid = new SimpleGrid(11, 11, 5);

        public MansionGrid(Random random) {
            this.random = random;
            this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
            this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
            this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
            this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
            this.baseGrid.set(0, 0, 11, 1, 5);
            this.baseGrid.set(0, 9, 11, 11, 5);
            recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
            recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
            recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
            recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);
            while (cleanEdges(this.baseGrid)) {
            }
            this.floorRooms = new SimpleGrid[3];
            this.floorRooms[0] = new SimpleGrid(11, 11, 5);
            this.floorRooms[1] = new SimpleGrid(11, 11, 5);
            this.floorRooms[2] = new SimpleGrid(11, 11, 5);
            identifyRooms(this.baseGrid, this.floorRooms[0]);
            identifyRooms(this.baseGrid, this.floorRooms[1]);
            this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.thirdFloorGrid = new SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
            setupThirdFloor();
            identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
        }

        public static boolean isHouse(SimpleGrid simpleGrid, int i, int i2) {
            int i3 = simpleGrid.get(i, i2);
            return i3 == 1 || i3 == 2 || i3 == 3 || i3 == 4;
        }

        public boolean isRoomId(SimpleGrid simpleGrid, int i, int i2, int i3, int i4) {
            return (this.floorRooms[i3].get(i, i2) & 65535) == i4;
        }

        @Nullable
        public Direction get1x2RoomDirection(SimpleGrid simpleGrid, int i, int i2, int i3, int i4) {
            Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
            while (it.hasNext()) {
                Direction next = it.next();
                if (isRoomId(simpleGrid, i + next.getStepX(), i2 + next.getStepZ(), i3, i4)) {
                    return next;
                }
            }
            return null;
        }

        private void recursiveCorridor(SimpleGrid simpleGrid, int i, int i2, Direction direction, int i3) {
            if (i3 <= 0) {
                return;
            }
            simpleGrid.set(i, i2, 1);
            simpleGrid.setif(i + direction.getStepX(), i2 + direction.getStepZ(), 0, 1);
            int i4 = 0;
            while (true) {
                if (i4 >= 8) {
                    break;
                }
                Direction from2DDataValue = Direction.from2DDataValue(this.random.nextInt(4));
                if (from2DDataValue != direction.getOpposite() && (from2DDataValue != Direction.EAST || !this.random.nextBoolean())) {
                    int stepX = i + direction.getStepX();
                    int stepZ = i2 + direction.getStepZ();
                    if (simpleGrid.get(stepX + from2DDataValue.getStepX(), stepZ + from2DDataValue.getStepZ()) == 0 && simpleGrid.get(stepX + (from2DDataValue.getStepX() * 2), stepZ + (from2DDataValue.getStepZ() * 2)) == 0) {
                        recursiveCorridor(simpleGrid, i + direction.getStepX() + from2DDataValue.getStepX(), i2 + direction.getStepZ() + from2DDataValue.getStepZ(), from2DDataValue, i3 - 1);
                        break;
                    }
                }
                i4++;
            }
            Direction clockWise = direction.getClockWise();
            Direction counterClockWise = direction.getCounterClockWise();
            simpleGrid.setif(i + clockWise.getStepX(), i2 + clockWise.getStepZ(), 0, 2);
            simpleGrid.setif(i + counterClockWise.getStepX(), i2 + counterClockWise.getStepZ(), 0, 2);
            simpleGrid.setif(i + direction.getStepX() + clockWise.getStepX(), i2 + direction.getStepZ() + clockWise.getStepZ(), 0, 2);
            simpleGrid.setif(i + direction.getStepX() + counterClockWise.getStepX(), i2 + direction.getStepZ() + counterClockWise.getStepZ(), 0, 2);
            simpleGrid.setif(i + (direction.getStepX() * 2), i2 + (direction.getStepZ() * 2), 0, 2);
            simpleGrid.setif(i + (clockWise.getStepX() * 2), i2 + (clockWise.getStepZ() * 2), 0, 2);
            simpleGrid.setif(i + (counterClockWise.getStepX() * 2), i2 + (counterClockWise.getStepZ() * 2), 0, 2);
        }

        private boolean cleanEdges(SimpleGrid simpleGrid) {
            boolean z = false;
            for (int i = 0; i < simpleGrid.height; i++) {
                for (int i2 = 0; i2 < simpleGrid.width; i2++) {
                    if (simpleGrid.get(i2, i) == 0) {
                        int i3 = 0 + (isHouse(simpleGrid, i2 + 1, i) ? 1 : 0) + (isHouse(simpleGrid, i2 - 1, i) ? 1 : 0) + (isHouse(simpleGrid, i2, i + 1) ? 1 : 0) + (isHouse(simpleGrid, i2, i - 1) ? 1 : 0);
                        if (i3 >= 3) {
                            simpleGrid.set(i2, i, 2);
                            z = true;
                        } else if (i3 == 2) {
                            if (0 + (isHouse(simpleGrid, i2 + 1, i + 1) ? 1 : 0) + (isHouse(simpleGrid, i2 - 1, i + 1) ? 1 : 0) + (isHouse(simpleGrid, i2 + 1, i - 1) ? 1 : 0) + (isHouse(simpleGrid, i2 - 1, i - 1) ? 1 : 0) <= 1) {
                                simpleGrid.set(i2, i, 2);
                                z = true;
                            }
                        }
                    }
                }
            }
            return z;
        }

        private void setupThirdFloor() {
            List<Tuple<Integer, Integer>> newArrayList = Lists.newArrayList();
            SimpleGrid simpleGrid = this.floorRooms[1];
            for (int i = 0; i < this.thirdFloorGrid.height; i++) {
                for (int i2 = 0; i2 < this.thirdFloorGrid.width; i2++) {
                    int i3 = simpleGrid.get(i2, i);
                    if ((i3 & 983040) == 131072 && (i3 & 2097152) == 2097152) {
                        newArrayList.add(new Tuple<>(Integer.valueOf(i2), Integer.valueOf(i)));
                    }
                }
            }
            if (!newArrayList.isEmpty()) {
                Tuple<Integer, Integer> tuple = newArrayList.get(this.random.nextInt(newArrayList.size()));
                int i4 = simpleGrid.get(tuple.getA().intValue(), tuple.getB().intValue());
                simpleGrid.set(tuple.getA().intValue(), tuple.getB().intValue(), i4 | 4194304);
                Direction direction = get1x2RoomDirection(this.baseGrid, tuple.getA().intValue(), tuple.getB().intValue(), 1, i4 & 65535);
                int intValue = tuple.getA().intValue() + direction.getStepX();
                int intValue2 = tuple.getB().intValue() + direction.getStepZ();
                for (int i5 = 0; i5 < this.thirdFloorGrid.height; i5++) {
                    for (int i6 = 0; i6 < this.thirdFloorGrid.width; i6++) {
                        if (!isHouse(this.baseGrid, i6, i5)) {
                            this.thirdFloorGrid.set(i6, i5, 5);
                        } else if (i6 == tuple.getA().intValue() && i5 == tuple.getB().intValue()) {
                            this.thirdFloorGrid.set(i6, i5, 3);
                        } else if (i6 == intValue && i5 == intValue2) {
                            this.thirdFloorGrid.set(i6, i5, 3);
                            this.floorRooms[2].set(i6, i5, 8388608);
                        }
                    }
                }
                List<Direction> newArrayList2 = Lists.newArrayList();
                Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
                while (it.hasNext()) {
                    Direction next = it.next();
                    if (this.thirdFloorGrid.get(intValue + next.getStepX(), intValue2 + next.getStepZ()) == 0) {
                        newArrayList2.add(next);
                    }
                }
                if (!newArrayList2.isEmpty()) {
                    Direction direction2 = newArrayList2.get(this.random.nextInt(newArrayList2.size()));
                    recursiveCorridor(this.thirdFloorGrid, intValue + direction2.getStepX(), intValue2 + direction2.getStepZ(), direction2, 4);
                    while (cleanEdges(this.thirdFloorGrid)) {
                    }
                    return;
                } else {
                    this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                    simpleGrid.set(tuple.getA().intValue(), tuple.getB().intValue(), i4);
                    return;
                }
            }
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
        }

        private void identifyRooms(SimpleGrid simpleGrid, SimpleGrid simpleGrid2) {
            List<Tuple<Integer, Integer>> newArrayList = Lists.newArrayList();
            for (int i = 0; i < simpleGrid.height; i++) {
                for (int i2 = 0; i2 < simpleGrid.width; i2++) {
                    if (simpleGrid.get(i2, i) == 2) {
                        newArrayList.add(new Tuple<>(Integer.valueOf(i2), Integer.valueOf(i)));
                    }
                }
            }
            Collections.shuffle(newArrayList, this.random);
            int i3 = 10;
            for (Tuple<Integer, Integer> tuple : newArrayList) {
                int intValue = tuple.getA().intValue();
                int intValue2 = tuple.getB().intValue();
                if (simpleGrid2.get(intValue, intValue2) == 0) {
                    int i4 = intValue;
                    int i5 = intValue;
                    int i6 = intValue2;
                    int i7 = intValue2;
                    int i8 = 65536;
                    if (simpleGrid2.get(intValue + 1, intValue2) == 0 && simpleGrid2.get(intValue, intValue2 + 1) == 0 && simpleGrid2.get(intValue + 1, intValue2 + 1) == 0 && simpleGrid.get(intValue + 1, intValue2) == 2 && simpleGrid.get(intValue, intValue2 + 1) == 2 && simpleGrid.get(intValue + 1, intValue2 + 1) == 2) {
                        i5++;
                        i7++;
                        i8 = 262144;
                    } else if (simpleGrid2.get(intValue - 1, intValue2) == 0 && simpleGrid2.get(intValue, intValue2 + 1) == 0 && simpleGrid2.get(intValue - 1, intValue2 + 1) == 0 && simpleGrid.get(intValue - 1, intValue2) == 2 && simpleGrid.get(intValue, intValue2 + 1) == 2 && simpleGrid.get(intValue - 1, intValue2 + 1) == 2) {
                        i4--;
                        i7++;
                        i8 = 262144;
                    } else if (simpleGrid2.get(intValue - 1, intValue2) == 0 && simpleGrid2.get(intValue, intValue2 - 1) == 0 && simpleGrid2.get(intValue - 1, intValue2 - 1) == 0 && simpleGrid.get(intValue - 1, intValue2) == 2 && simpleGrid.get(intValue, intValue2 - 1) == 2 && simpleGrid.get(intValue - 1, intValue2 - 1) == 2) {
                        i4--;
                        i6--;
                        i8 = 262144;
                    } else if (simpleGrid2.get(intValue + 1, intValue2) == 0 && simpleGrid.get(intValue + 1, intValue2) == 2) {
                        i5++;
                        i8 = 131072;
                    } else if (simpleGrid2.get(intValue, intValue2 + 1) == 0 && simpleGrid.get(intValue, intValue2 + 1) == 2) {
                        i7++;
                        i8 = 131072;
                    } else if (simpleGrid2.get(intValue - 1, intValue2) == 0 && simpleGrid.get(intValue - 1, intValue2) == 2) {
                        i4--;
                        i8 = 131072;
                    } else if (simpleGrid2.get(intValue, intValue2 - 1) == 0 && simpleGrid.get(intValue, intValue2 - 1) == 2) {
                        i6--;
                        i8 = 131072;
                    }
                    int i9 = this.random.nextBoolean() ? i4 : i5;
                    int i10 = this.random.nextBoolean() ? i6 : i7;
                    int i11 = 2097152;
                    if (!simpleGrid.edgesTo(i9, i10, 1)) {
                        i9 = i9 == i4 ? i5 : i4;
                        i10 = i10 == i6 ? i7 : i6;
                        if (!simpleGrid.edgesTo(i9, i10, 1)) {
                            i10 = i10 == i6 ? i7 : i6;
                            if (!simpleGrid.edgesTo(i9, i10, 1)) {
                                i9 = i9 == i4 ? i5 : i4;
                                i10 = i10 == i6 ? i7 : i6;
                                if (!simpleGrid.edgesTo(i9, i10, 1)) {
                                    i11 = 0;
                                    i9 = i4;
                                    i10 = i6;
                                }
                            }
                        }
                    }
                    for (int i12 = i6; i12 <= i7; i12++) {
                        for (int i13 = i4; i13 <= i5; i13++) {
                            if (i13 == i9 && i12 == i10) {
                                simpleGrid2.set(i13, i12, 1048576 | i11 | i8 | i3);
                            } else {
                                simpleGrid2.set(i13, i12, i8 | i3);
                            }
                        }
                    }
                    i3++;
                }
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$SimpleGrid.class */
    static class SimpleGrid {
        private final int[][] grid;
        private final int width;
        private final int height;
        private final int valueIfOutside;

        public SimpleGrid(int i, int i2, int i3) {
            this.width = i;
            this.height = i2;
            this.valueIfOutside = i3;
            this.grid = new int[i][i2];
        }

        public void set(int i, int i2, int i3) {
            if (i >= 0 && i < this.width && i2 >= 0 && i2 < this.height) {
                this.grid[i][i2] = i3;
            }
        }

        public void set(int i, int i2, int i3, int i4, int i5) {
            for (int i6 = i2; i6 <= i4; i6++) {
                for (int i7 = i; i7 <= i3; i7++) {
                    set(i7, i6, i5);
                }
            }
        }

        public int get(int i, int i2) {
            if (i >= 0 && i < this.width && i2 >= 0 && i2 < this.height) {
                return this.grid[i][i2];
            }
            return this.valueIfOutside;
        }

        public void setif(int i, int i2, int i3, int i4) {
            if (get(i, i2) == i3) {
                set(i, i2, i4);
            }
        }

        public boolean edgesTo(int i, int i2, int i3) {
            return get(i - 1, i2) == i3 || get(i + 1, i2) == i3 || get(i, i2 + 1) == i3 || get(i, i2 - 1) == i3;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$FloorRoomCollection.class */
    static abstract class FloorRoomCollection {
        public abstract String get1x1(Random random);

        public abstract String get1x1Secret(Random random);

        public abstract String get1x2SideEntrance(Random random, boolean z);

        public abstract String get1x2FrontEntrance(Random random, boolean z);

        public abstract String get1x2Secret(Random random);

        public abstract String get2x2(Random random);

        public abstract String get2x2Secret(Random random);

        private FloorRoomCollection() {
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$FirstFloorRoomCollection.class */
    static class FirstFloorRoomCollection extends FloorRoomCollection {
        private FirstFloorRoomCollection() {
            super();
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x1(Random random) {
            return "1x1_a" + (random.nextInt(5) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x1Secret(Random random) {
            return "1x1_as" + (random.nextInt(4) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x2SideEntrance(Random random, boolean z) {
            return "1x2_a" + (random.nextInt(9) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x2FrontEntrance(Random random, boolean z) {
            return "1x2_b" + (random.nextInt(5) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x2Secret(Random random) {
            return "1x2_s" + (random.nextInt(2) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get2x2(Random random) {
            return "2x2_a" + (random.nextInt(4) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get2x2Secret(Random random) {
            return "2x2_s1";
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$SecondFloorRoomCollection.class */
    static class SecondFloorRoomCollection extends FloorRoomCollection {
        private SecondFloorRoomCollection() {
            super();
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x1(Random random) {
            return "1x1_b" + (random.nextInt(4) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x1Secret(Random random) {
            return "1x1_as" + (random.nextInt(4) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x2SideEntrance(Random random, boolean z) {
            if (z) {
                return "1x2_c_stairs";
            }
            return "1x2_c" + (random.nextInt(4) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x2FrontEntrance(Random random, boolean z) {
            if (z) {
                return "1x2_d_stairs";
            }
            return "1x2_d" + (random.nextInt(5) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get1x2Secret(Random random) {
            return "1x2_se" + (random.nextInt(1) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get2x2(Random random) {
            return "2x2_b" + (random.nextInt(5) + 1);
        }

        @Override // net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces.FloorRoomCollection
        public String get2x2Secret(Random random) {
            return "2x2_s1";
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/WoodlandMansionPieces$ThirdFloorRoomCollection.class */
    static class ThirdFloorRoomCollection extends SecondFloorRoomCollection {
        private ThirdFloorRoomCollection() {
            super();
        }
    }
}
