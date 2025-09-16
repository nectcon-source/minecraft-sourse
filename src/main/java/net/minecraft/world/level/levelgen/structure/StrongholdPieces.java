package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces.class */
public class StrongholdPieces {
    private static List<PieceWeight> currentPieces;
    private static Class<? extends StrongholdPiece> imposedPiece;
    private static int totalWeight;
    private static final PieceWeight[] STRONGHOLD_PIECE_WEIGHTS = {new PieceWeight(Straight.class, 40, 0), new PieceWeight(PrisonHall.class, 5, 5), new PieceWeight(LeftTurn.class, 20, 0), new PieceWeight(RightTurn.class, 20, 0), new PieceWeight(RoomCrossing.class, 10, 6), new PieceWeight(StraightStairsDown.class, 5, 5), new PieceWeight(StairsDown.class, 5, 5), new PieceWeight(FiveCrossing.class, 5, 4), new PieceWeight(ChestCorridor.class, 5, 4), new PieceWeight(Library.class, 10, 2) { // from class: net.minecraft.world.level.levelgen.structure.StrongholdPieces.1
        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.PieceWeight
        public boolean doPlace(int i) {
            return super.doPlace(i) && i > 4;
        }
    }, new PieceWeight(PortalRoom.class, 20, 1) { // from class: net.minecraft.world.level.levelgen.structure.StrongholdPieces.2
        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.PieceWeight
        public boolean doPlace(int i) {
            return super.doPlace(i) && i > 5;
        }
    }};
    private static final SmoothStoneSelector SMOOTH_STONE_SELECTOR = new SmoothStoneSelector();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$PieceWeight.class */
    static class PieceWeight {
        public final Class<? extends StrongholdPiece> pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;

        public PieceWeight(Class<? extends StrongholdPiece> cls, int i, int i2) {
            this.pieceClass = cls;
            this.weight = i;
            this.maxPlaceCount = i2;
        }

        public boolean doPlace(int i) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    public static void resetPieces() {
        currentPieces = Lists.newArrayList();
        for (PieceWeight pieceWeight : STRONGHOLD_PIECE_WEIGHTS) {
            pieceWeight.placeCount = 0;
            currentPieces.add(pieceWeight);
        }
        imposedPiece = null;
    }

    private static boolean updatePieceWeight() {
        boolean z = false;
        totalWeight = 0;
        for (PieceWeight pieceWeight : currentPieces) {
            if (pieceWeight.maxPlaceCount > 0 && pieceWeight.placeCount < pieceWeight.maxPlaceCount) {
                z = true;
            }
            totalWeight += pieceWeight.weight;
        }
        return z;
    }

    private static StrongholdPiece findAndCreatePieceFactory(Class<? extends StrongholdPiece> cls, List<StructurePiece> list, Random random, int i, int i2, int i3, @Nullable Direction direction, int i4) {
        StrongholdPiece strongholdPiece = null;
        if (cls == Straight.class) {
            strongholdPiece = Straight.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == PrisonHall.class) {
            strongholdPiece = PrisonHall.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == LeftTurn.class) {
            strongholdPiece = LeftTurn.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == RightTurn.class) {
            strongholdPiece = RightTurn.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == RoomCrossing.class) {
            strongholdPiece = RoomCrossing.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == StraightStairsDown.class) {
            strongholdPiece = StraightStairsDown.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == StairsDown.class) {
            strongholdPiece = StairsDown.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == FiveCrossing.class) {
            strongholdPiece = FiveCrossing.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == ChestCorridor.class) {
            strongholdPiece = ChestCorridor.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == Library.class) {
            strongholdPiece = Library.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == PortalRoom.class) {
            strongholdPiece = PortalRoom.createPiece(list, i, i2, i3, direction, i4);
        }
        return strongholdPiece;
    }

    private static StrongholdPiece generatePieceFromSmallDoor(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
        if (!updatePieceWeight()) {
            return null;
        }
        if (imposedPiece != null) {
            StrongholdPiece findAndCreatePieceFactory = findAndCreatePieceFactory(imposedPiece, list, random, i, i2, i3, direction, i4);
            imposedPiece = null;
            if (findAndCreatePieceFactory != null) {
                return findAndCreatePieceFactory;
            }
        }
        int i5 = 0;
        while (i5 < 5) {
            i5++;
            int nextInt = random.nextInt(totalWeight);
            for (PieceWeight pieceWeight : currentPieces) {
                nextInt -= pieceWeight.weight;
                if (nextInt < 0) {
                    if (pieceWeight.doPlace(i4) && pieceWeight != startPiece.previousPiece) {
                        StrongholdPiece findAndCreatePieceFactory2 = findAndCreatePieceFactory(pieceWeight.pieceClass, list, random, i, i2, i3, direction, i4);
                        if (findAndCreatePieceFactory2 != null) {
                            pieceWeight.placeCount++;
                            startPiece.previousPiece = pieceWeight;
                            if (!pieceWeight.isValid()) {
                                currentPieces.remove(pieceWeight);
                            }
                            return findAndCreatePieceFactory2;
                        }
                    }
                }
            }
        }
        BoundingBox findPieceBox = FillerCorridor.findPieceBox(list, random, i, i2, i3, direction);
        if (findPieceBox != null && findPieceBox.y0 > 1) {
            return new FillerCorridor(i4, findPieceBox, direction);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static StructurePiece generateAndAddPiece(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2, int i3, @Nullable Direction direction, int i4) {
        if (i4 > 50 || Math.abs(i - startPiece.getBoundingBox().x0) > 112 || Math.abs(i3 - startPiece.getBoundingBox().z0) > 112) {
            return null;
        }
        StructurePiece generatePieceFromSmallDoor = generatePieceFromSmallDoor(startPiece, list, random, i, i2, i3, direction, i4 + 1);
        if (generatePieceFromSmallDoor != null) {
            list.add(generatePieceFromSmallDoor);
            startPiece.pendingChildren.add(generatePieceFromSmallDoor);
        }
        return generatePieceFromSmallDoor;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$StrongholdPiece.class */
    static abstract class StrongholdPiece extends StructurePiece {
        protected SmallDoorType entryDoor;

        /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$StrongholdPiece$SmallDoorType.class */
        public enum SmallDoorType {
            OPENING,
            WOOD_DOOR,
            GRATES,
            IRON_DOOR
        }

        protected StrongholdPiece(StructurePieceType structurePieceType, int i) {
            super(structurePieceType, i);
            this.entryDoor = SmallDoorType.OPENING;
        }

        public StrongholdPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.entryDoor = SmallDoorType.OPENING;
            this.entryDoor = SmallDoorType.valueOf(compoundTag.getString("EntryDoor"));
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            compoundTag.putString("EntryDoor", this.entryDoor.name());
        }

        protected void generateSmallDoor(WorldGenLevel worldGenLevel, Random random, BoundingBox boundingBox, SmallDoorType smallDoorType, int i, int i2, int i3) {
            switch (smallDoorType) {
                case OPENING:
                    generateBox(worldGenLevel, boundingBox, i, i2, i3, (i + 3) - 1, (i2 + 3) - 1, i3, CAVE_AIR, CAVE_AIR, false);
                    break;
                case WOOD_DOOR:
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.OAK_DOOR.defaultBlockState(), i + 1, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, i2 + 1, i3, boundingBox);
                    break;
                case GRATES:
                    placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), i + 1, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), i + 1, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true), i, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true), i, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true)).setValue(IronBarsBlock.WEST, true), i, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true)).setValue(IronBarsBlock.WEST, true), i + 1, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true)).setValue(IronBarsBlock.WEST, true), i + 2, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true), i + 2, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true), i + 2, i2, i3, boundingBox);
                    break;
                case IRON_DOOR:
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, i2 + 2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, Blocks.IRON_DOOR.defaultBlockState(), i + 1, i2, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, i2 + 1, i3, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH), i + 2, i2 + 1, i3 + 1, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH), i + 2, i2 + 1, i3 - 1, boundingBox);
                    break;
            }
        }

        protected SmallDoorType randomSmallDoor(Random random) {
            switch (random.nextInt(5)) {
                case 0:
                case 1:
                default:
                    return SmallDoorType.OPENING;
                case 2:
                    return SmallDoorType.WOOD_DOOR;
                case 3:
                    return SmallDoorType.GRATES;
                case 4:
                    return SmallDoorType.IRON_DOOR;
            }
        }

        @Nullable
        protected StructurePiece generateSmallDoorChildForward(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2) {
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                    case NORTH:
                        return StrongholdPieces.generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 + i, this.boundingBox.y0 + i2, this.boundingBox.z0 - 1, orientation, getGenDepth());
                    case SOUTH:
                        return StrongholdPieces.generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 + i, this.boundingBox.y0 + i2, this.boundingBox.z1 + 1, orientation, getGenDepth());
                    case WEST:
                        return StrongholdPieces.generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + i2, this.boundingBox.z0 + i, orientation, getGenDepth());
                    case EAST:
                        return StrongholdPieces.generateAndAddPiece(startPiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + i2, this.boundingBox.z0 + i, orientation, getGenDepth());
                    default:
                        return null;
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateSmallDoorChildLeft(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2) {
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                }
                return StrongholdPieces.generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + i, this.boundingBox.z0 + i2, Direction.WEST, getGenDepth());
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateSmallDoorChildRight(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2) {
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                }
                return StrongholdPieces.generateAndAddPiece(startPiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + i, this.boundingBox.z0 + i2, Direction.EAST, getGenDepth());
            }
            return null;
        }

        protected static boolean isOkBox(BoundingBox boundingBox) {
            return boundingBox != null && boundingBox.y0 > 10;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$FillerCorridor.class */
    public static class FillerCorridor extends StrongholdPiece {
        private final int steps;

        public FillerCorridor(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
            this.steps = (direction == Direction.NORTH || direction == Direction.SOUTH) ? boundingBox.getZSpan() : boundingBox.getXSpan();
        }

        public FillerCorridor(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, compoundTag);
            this.steps = compoundTag.getInt("Steps");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putInt("Steps", this.steps);
        }

        public static BoundingBox findPieceBox(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, 4, direction);
            StructurePiece findCollisionPiece = StructurePiece.findCollisionPiece(list, orientBox);
            if (findCollisionPiece != null && findCollisionPiece.getBoundingBox().y0 == orientBox.y0) {
                for (int i4 = 3; i4 >= 1; i4--) {
                    if (!findCollisionPiece.getBoundingBox().intersects(BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, i4 - 1, direction))) {
                        return BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, i4, direction);
                    }
                }
                return null;
            }
            return null;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            for (int i = 0; i < this.steps; i++) {
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, i, boundingBox);
                for (int i2 = 1; i2 <= 3; i2++) {
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, i2, i, boundingBox);
                    placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 1, i2, i, boundingBox);
                    placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 2, i2, i, boundingBox);
                    placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 3, i2, i, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, i2, i, boundingBox);
                }
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, i, boundingBox);
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$StairsDown.class */
    public static class StairsDown extends StrongholdPiece {
        private final boolean isSource;

        public StairsDown(StructurePieceType structurePieceType, int i, Random random, int i2, int i3) {
            super(structurePieceType, i);
            this.isSource = true;
            setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(random));
            this.entryDoor = StrongholdPiece.SmallDoorType.OPENING;
            if (getOrientation().getAxis() == Direction.Axis.Z) {
                this.boundingBox = new BoundingBox(i2, 64, i3, (i2 + 5) - 1, 74, (i3 + 5) - 1);
            } else {
                this.boundingBox = new BoundingBox(i2, 64, i3, (i2 + 5) - 1, 74, (i3 + 5) - 1);
            }
        }

        public StairsDown(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_STAIRS_DOWN, i);
            this.isSource = false;
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
        }

        public StairsDown(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.isSource = compoundTag.getBoolean("Source");
        }

        public StairsDown(StructureManager structureManager, CompoundTag compoundTag) {
            this(StructurePieceType.STRONGHOLD_STAIRS_DOWN, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Source", this.isSource);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            if (this.isSource) {
                Class unused = StrongholdPieces.imposedPiece = FiveCrossing.class;
            }
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 1, 1);
        }

        public static StairsDown createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -7, 0, 5, 11, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new StairsDown(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 10, 4, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 7, 0);
            generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 4);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, boundingBox);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$StartPiece.class */
    public static class StartPiece extends StairsDown {
        public PieceWeight previousPiece;

        @Nullable
        public PortalRoom portalRoomPiece;
        public final List<StructurePiece> pendingChildren;

        public StartPiece(Random random, int i, int i2) {
            super(StructurePieceType.STRONGHOLD_START, 0, random, i, i2);
            this.pendingChildren = Lists.newArrayList();
        }

        public StartPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_START, compoundTag);
            this.pendingChildren = Lists.newArrayList();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$Straight.class */
    public static class Straight extends StrongholdPiece {
        private final boolean leftChild;
        private final boolean rightChild;

        public Straight(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
            this.leftChild = random.nextInt(2) == 0;
            this.rightChild = random.nextInt(2) == 0;
        }

        public Straight(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT, compoundTag);
            this.leftChild = compoundTag.getBoolean("Left");
            this.rightChild = compoundTag.getBoolean("Right");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Left", this.leftChild);
            compoundTag.putBoolean("Right", this.rightChild);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 1, 1);
            if (this.leftChild) {
                generateSmallDoorChildLeft((StartPiece) structurePiece, list, random, 1, 2);
            }
            if (this.rightChild) {
                generateSmallDoorChildRight((StartPiece) structurePiece, list, random, 1, 2);
            }
        }

        public static Straight createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, 7, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new Straight(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 6, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
            generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
            BlockState blockState = (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
            BlockState blockState2 = (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
            maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1f, 1, 2, 1, blockState);
            maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1f, 3, 2, 1, blockState2);
            maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1f, 1, 2, 5, blockState);
            maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.1f, 3, 2, 5, blockState2);
            if (this.leftChild) {
                generateBox(worldGenLevel, boundingBox, 0, 1, 2, 0, 3, 4, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.rightChild) {
                generateBox(worldGenLevel, boundingBox, 4, 1, 2, 4, 3, 4, CAVE_AIR, CAVE_AIR, false);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$ChestCorridor.class */
    public static class ChestCorridor extends StrongholdPiece {
        private boolean hasPlacedChest;

        public ChestCorridor(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
        }

        public ChestCorridor(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, compoundTag);
            this.hasPlacedChest = compoundTag.getBoolean("Chest");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Chest", this.hasPlacedChest);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 1, 1);
        }

        public static ChestCorridor createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, 7, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new ChestCorridor(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 6, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
            generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
            generateBox(worldGenLevel, boundingBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
            placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, boundingBox);
            placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, boundingBox);
            for (int i = 2; i <= 4; i++) {
                placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, i, boundingBox);
            }
            if (!this.hasPlacedChest && boundingBox.isInside(new BlockPos(getWorldX(3, 3), getWorldY(2), getWorldZ(3, 3)))) {
                this.hasPlacedChest = true;
                createChest(worldGenLevel, boundingBox, random, 3, 2, 3, BuiltInLootTables.STRONGHOLD_CORRIDOR);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$StraightStairsDown.class */
    public static class StraightStairsDown extends StrongholdPiece {
        public StraightStairsDown(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
        }

        public StraightStairsDown(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 1, 1);
        }

        public static StraightStairsDown createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -7, 0, 5, 11, 8, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new StraightStairsDown(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 10, 7, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 7, 0);
            generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 7);
            BlockState blockState = (BlockState) Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            for (int i = 0; i < 6; i++) {
                placeBlock(worldGenLevel, blockState, 1, 6 - i, 1 + i, boundingBox);
                placeBlock(worldGenLevel, blockState, 2, 6 - i, 1 + i, boundingBox);
                placeBlock(worldGenLevel, blockState, 3, 6 - i, 1 + i, boundingBox);
                if (i < 5) {
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - i, 1 + i, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - i, 1 + i, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - i, 1 + i, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$Turn.class */
    public static abstract class Turn extends StrongholdPiece {
        protected Turn(StructurePieceType structurePieceType, int i) {
            super(structurePieceType, i);
        }

        public Turn(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$LeftTurn.class */
    public static class LeftTurn extends Turn {
        public LeftTurn(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_LEFT_TURN, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
        }

        public LeftTurn(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_LEFT_TURN, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            Direction orientation = getOrientation();
            if (orientation == Direction.NORTH || orientation == Direction.EAST) {
                generateSmallDoorChildLeft((StartPiece) structurePiece, list, random, 1, 1);
            } else {
                generateSmallDoorChildRight((StartPiece) structurePiece, list, random, 1, 1);
            }
        }

        public static LeftTurn createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new LeftTurn(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 4, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
            Direction orientation = getOrientation();
            if (orientation == Direction.NORTH || orientation == Direction.EAST) {
                generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
                return true;
            }
            generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$RightTurn.class */
    public static class RightTurn extends Turn {
        public RightTurn(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_RIGHT_TURN, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
        }

        public RightTurn(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_RIGHT_TURN, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            Direction orientation = getOrientation();
            if (orientation == Direction.NORTH || orientation == Direction.EAST) {
                generateSmallDoorChildRight((StartPiece) structurePiece, list, random, 1, 1);
            } else {
                generateSmallDoorChildLeft((StartPiece) structurePiece, list, random, 1, 1);
            }
        }

        public static RightTurn createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 5, 5, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new RightTurn(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 4, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
            Direction orientation = getOrientation();
            if (orientation == Direction.NORTH || orientation == Direction.EAST) {
                generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
                return true;
            }
            generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$RoomCrossing.class */
    public static class RoomCrossing extends StrongholdPiece {
        protected final int type;

        public RoomCrossing(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
            this.type = random.nextInt(5);
        }

        public RoomCrossing(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, compoundTag);
            this.type = compoundTag.getInt("Type");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putInt("Type", this.type);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 4, 1);
            generateSmallDoorChildLeft((StartPiece) structurePiece, list, random, 1, 4);
            generateSmallDoorChildRight((StartPiece) structurePiece, list, random, 1, 4);
        }

        public static RoomCrossing createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -4, -1, 0, 11, 7, 11, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new RoomCrossing(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 10, 6, 10, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 4, 1, 0);
            generateBox(worldGenLevel, boundingBox, 4, 1, 10, 6, 3, 10, CAVE_AIR, CAVE_AIR, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 4, 0, 3, 6, CAVE_AIR, CAVE_AIR, false);
            generateBox(worldGenLevel, boundingBox, 10, 1, 4, 10, 3, 6, CAVE_AIR, CAVE_AIR, false);
            switch (this.type) {
                case 0:
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, boundingBox);
                    placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, boundingBox);
                    break;
                case 1:
                    for (int i = 0; i < 5; i++) {
                        placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + i, boundingBox);
                        placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + i, boundingBox);
                        placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 3, boundingBox);
                        placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 7, boundingBox);
                    }
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.WATER.defaultBlockState(), 5, 4, 5, boundingBox);
                    break;
                case 2:
                    for (int i2 = 1; i2 <= 9; i2++) {
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, i2, boundingBox);
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, i2, boundingBox);
                    }
                    for (int i3 = 1; i3 <= 9; i3++) {
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), i3, 3, 1, boundingBox);
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), i3, 3, 9, boundingBox);
                    }
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, boundingBox);
                    placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, boundingBox);
                    for (int i4 = 1; i4 <= 3; i4++) {
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, i4, 4, boundingBox);
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, i4, 4, boundingBox);
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, i4, 6, boundingBox);
                        placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, i4, 6, boundingBox);
                    }
                    placeBlock(worldGenLevel, Blocks.TORCH.defaultBlockState(), 5, 3, 5, boundingBox);
                    for (int i5 = 2; i5 <= 8; i5++) {
                        placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, i5, boundingBox);
                        placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, i5, boundingBox);
                        if (i5 <= 3 || i5 >= 7) {
                            placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, i5, boundingBox);
                            placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, i5, boundingBox);
                            placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, i5, boundingBox);
                        }
                        placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, i5, boundingBox);
                        placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, i5, boundingBox);
                    }
                    BlockState blockState = (BlockState) Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
                    placeBlock(worldGenLevel, blockState, 9, 1, 3, boundingBox);
                    placeBlock(worldGenLevel, blockState, 9, 2, 3, boundingBox);
                    placeBlock(worldGenLevel, blockState, 9, 3, 3, boundingBox);
                    createChest(worldGenLevel, boundingBox, random, 3, 4, 8, BuiltInLootTables.STRONGHOLD_CROSSING);
                    break;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$PrisonHall.class */
    public static class PrisonHall extends StrongholdPiece {
        public PrisonHall(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_PRISON_HALL, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
        }

        public PrisonHall(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_PRISON_HALL, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 1, 1);
        }

        public static PrisonHall createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -1, 0, 9, 5, 11, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new PrisonHall(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 8, 4, 10, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 1, 1, 0);
            generateBox(worldGenLevel, boundingBox, 1, 1, 10, 3, 3, 10, CAVE_AIR, CAVE_AIR, false);
            generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 1, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 1, 3, 4, 3, 3, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 1, 7, 4, 3, 7, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 1, 9, 4, 3, 9, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            for (int i = 1; i <= 3; i++) {
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, i, 4, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true)).setValue(IronBarsBlock.EAST, true), 4, i, 5, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, i, 6, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true), 5, i, 5, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true), 6, i, 5, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true), 7, i, 5, boundingBox);
            }
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, 3, 2, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, 3, 8, boundingBox);
            BlockState blockState = (BlockState) Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST)).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
            placeBlock(worldGenLevel, blockState, 4, 1, 2, boundingBox);
            placeBlock(worldGenLevel, blockState2, 4, 2, 2, boundingBox);
            placeBlock(worldGenLevel, blockState, 4, 1, 8, boundingBox);
            placeBlock(worldGenLevel, blockState2, 4, 2, 8, boundingBox);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$Library.class */
    public static class Library extends StrongholdPiece {
        private final boolean isTall;

        public Library(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_LIBRARY, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
            this.isTall = boundingBox.getYSpan() > 6;
        }

        public Library(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_LIBRARY, compoundTag);
            this.isTall = compoundTag.getBoolean("Tall");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Tall", this.isTall);
        }

        public static Library createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -4, -1, 0, 14, 11, 15, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                orientBox = BoundingBox.orientBox(i, i2, i3, -4, -1, 0, 14, 6, 15, direction);
                if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                    return null;
                }
            }
            return new Library(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int i = 11;
            if (!this.isTall) {
                i = 6;
            }
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 13, i - 1, 14, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 4, 1, 0);
            generateMaybeBox(worldGenLevel, boundingBox, random, 0.07f, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false);
            for (int i2 = 1; i2 <= 13; i2++) {
                if ((i2 - 1) % 4 == 0) {
                    generateBox(worldGenLevel, boundingBox, 1, 1, i2, 1, 4, i2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    generateBox(worldGenLevel, boundingBox, 12, 1, i2, 12, 4, i2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2, 3, i2, boundingBox);
                    placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 11, 3, i2, boundingBox);
                    if (this.isTall) {
                        generateBox(worldGenLevel, boundingBox, 1, 6, i2, 1, 9, i2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                        generateBox(worldGenLevel, boundingBox, 12, 6, i2, 12, 9, i2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    }
                } else {
                    generateBox(worldGenLevel, boundingBox, 1, 1, i2, 1, 4, i2, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                    generateBox(worldGenLevel, boundingBox, 12, 1, i2, 12, 4, i2, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                    if (this.isTall) {
                        generateBox(worldGenLevel, boundingBox, 1, 6, i2, 1, 9, i2, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                        generateBox(worldGenLevel, boundingBox, 12, 6, i2, 12, 9, i2, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                    }
                }
            }
            for (int i3 = 3; i3 < 12; i3 += 2) {
                generateBox(worldGenLevel, boundingBox, 3, 1, i3, 4, 3, i3, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 6, 1, i3, 7, 3, i3, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 9, 1, i3, 10, 3, i3, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            }
            if (this.isTall) {
                generateBox(worldGenLevel, boundingBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, boundingBox);
                placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, boundingBox);
                placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, boundingBox);
                BlockState blockState = (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
                BlockState blockState2 = (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
                generateBox(worldGenLevel, boundingBox, 3, 6, 3, 3, 6, 11, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 10, 6, 3, 10, 6, 9, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 4, 6, 2, 9, 6, 2, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 4, 6, 12, 7, 6, 12, blockState, blockState, false);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 3, 6, 2, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.EAST, true), 3, 6, 12, boundingBox);
                placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.WEST, true), 10, 6, 2, boundingBox);
                for (int i4 = 0; i4 <= 2; i4++) {
                    placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.WEST, true), 8 + i4, 6, 12 - i4, boundingBox);
                    if (i4 != 2) {
                        placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 8 + i4, 6, 11 - i4, boundingBox);
                    }
                }
                BlockState blockState3 = (BlockState) Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
                placeBlock(worldGenLevel, blockState3, 10, 1, 13, boundingBox);
                placeBlock(worldGenLevel, blockState3, 10, 2, 13, boundingBox);
                placeBlock(worldGenLevel, blockState3, 10, 3, 13, boundingBox);
                placeBlock(worldGenLevel, blockState3, 10, 4, 13, boundingBox);
                placeBlock(worldGenLevel, blockState3, 10, 5, 13, boundingBox);
                placeBlock(worldGenLevel, blockState3, 10, 6, 13, boundingBox);
                placeBlock(worldGenLevel, blockState3, 10, 7, 13, boundingBox);
                BlockState blockState4 = (BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true);
                placeBlock(worldGenLevel, blockState4, 6, 9, 7, boundingBox);
                BlockState blockState5 = (BlockState) Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true);
                placeBlock(worldGenLevel, blockState5, 7, 9, 7, boundingBox);
                placeBlock(worldGenLevel, blockState4, 6, 8, 7, boundingBox);
                placeBlock(worldGenLevel, blockState5, 7, 8, 7, boundingBox);
                BlockState blockState6 = (BlockState) ((BlockState) blockState2.setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
                placeBlock(worldGenLevel, blockState6, 6, 7, 7, boundingBox);
                placeBlock(worldGenLevel, blockState6, 7, 7, 7, boundingBox);
                placeBlock(worldGenLevel, blockState4, 5, 7, 7, boundingBox);
                placeBlock(worldGenLevel, blockState5, 8, 7, 7, boundingBox);
                placeBlock(worldGenLevel, (BlockState) blockState4.setValue(FenceBlock.NORTH, true), 6, 7, 6, boundingBox);
                placeBlock(worldGenLevel, (BlockState) blockState4.setValue(FenceBlock.SOUTH, true), 6, 7, 8, boundingBox);
                placeBlock(worldGenLevel, (BlockState) blockState5.setValue(FenceBlock.NORTH, true), 7, 7, 6, boundingBox);
                placeBlock(worldGenLevel, (BlockState) blockState5.setValue(FenceBlock.SOUTH, true), 7, 7, 8, boundingBox);
                BlockState defaultBlockState = Blocks.TORCH.defaultBlockState();
                placeBlock(worldGenLevel, defaultBlockState, 5, 8, 7, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 8, 8, 7, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 6, 8, 6, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 6, 8, 8, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 7, 8, 6, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 7, 8, 8, boundingBox);
            }
            createChest(worldGenLevel, boundingBox, random, 3, 3, 5, BuiltInLootTables.STRONGHOLD_LIBRARY);
            if (this.isTall) {
                placeBlock(worldGenLevel, CAVE_AIR, 12, 9, 1, boundingBox);
                createChest(worldGenLevel, boundingBox, random, 12, 8, 1, BuiltInLootTables.STRONGHOLD_LIBRARY);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$FiveCrossing.class */
    public static class FiveCrossing extends StrongholdPiece {
        private final boolean leftLow;
        private final boolean leftHigh;
        private final boolean rightLow;
        private final boolean rightHigh;

        public FiveCrossing(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, i);
            setOrientation(direction);
            this.entryDoor = randomSmallDoor(random);
            this.boundingBox = boundingBox;
            this.leftLow = random.nextBoolean();
            this.leftHigh = random.nextBoolean();
            this.rightLow = random.nextBoolean();
            this.rightHigh = random.nextInt(3) > 0;
        }

        public FiveCrossing(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, compoundTag);
            this.leftLow = compoundTag.getBoolean("leftLow");
            this.leftHigh = compoundTag.getBoolean("leftHigh");
            this.rightLow = compoundTag.getBoolean("rightLow");
            this.rightHigh = compoundTag.getBoolean("rightHigh");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("leftLow", this.leftLow);
            compoundTag.putBoolean("leftHigh", this.leftHigh);
            compoundTag.putBoolean("rightLow", this.rightLow);
            compoundTag.putBoolean("rightHigh", this.rightHigh);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            int i = 3;
            int i2 = 5;
            Direction orientation = getOrientation();
            if (orientation == Direction.WEST || orientation == Direction.NORTH) {
                i = 8 - 3;
                i2 = 8 - 5;
            }
            generateSmallDoorChildForward((StartPiece) structurePiece, list, random, 5, 1);
            if (this.leftLow) {
                generateSmallDoorChildLeft((StartPiece) structurePiece, list, random, i, 1);
            }
            if (this.leftHigh) {
                generateSmallDoorChildLeft((StartPiece) structurePiece, list, random, i2, 7);
            }
            if (this.rightLow) {
                generateSmallDoorChildRight((StartPiece) structurePiece, list, random, i, 1);
            }
            if (this.rightHigh) {
                generateSmallDoorChildRight((StartPiece) structurePiece, list, random, i2, 7);
            }
        }

        public static FiveCrossing createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -4, -3, 0, 10, 9, 11, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new FiveCrossing(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 9, 8, 10, true, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, this.entryDoor, 4, 3, 0);
            if (this.leftLow) {
                generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 5, 3, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.rightLow) {
                generateBox(worldGenLevel, boundingBox, 9, 3, 1, 9, 5, 3, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.leftHigh) {
                generateBox(worldGenLevel, boundingBox, 0, 5, 7, 0, 7, 9, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.rightHigh) {
                generateBox(worldGenLevel, boundingBox, 9, 5, 7, 9, 7, 9, CAVE_AIR, CAVE_AIR, false);
            }
            generateBox(worldGenLevel, boundingBox, 5, 1, 10, 7, 3, 10, CAVE_AIR, CAVE_AIR, false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 1, 8, 2, 6, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 1, 5, 4, 4, 9, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 8, 1, 5, 8, 4, 9, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 1, 4, 7, 3, 4, 9, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 1, 3, 5, 3, 3, 6, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 1, 7, 7, 1, 8, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 5, 7, 7, 5, 9, (BlockState) Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), (BlockState) Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), false);
            placeBlock(worldGenLevel, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, boundingBox);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$PortalRoom.class */
    public static class PortalRoom extends StrongholdPiece {
        private boolean hasPlacedSpawner;

        public PortalRoom(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public PortalRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, compoundTag);
            this.hasPlacedSpawner = compoundTag.getBoolean("Mob");
        }

        @Override // net.minecraft.world.level.levelgen.structure.StrongholdPieces.StrongholdPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Mob", this.hasPlacedSpawner);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            if (structurePiece != null) {
                ((StartPiece) structurePiece).portalRoomPiece = this;
            }
        }

        public static PortalRoom createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -4, -1, 0, 11, 8, 16, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new PortalRoom(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 10, 7, 15, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateSmallDoor(worldGenLevel, random, boundingBox, StrongholdPiece.SmallDoorType.GRATES, 4, 1, 0);
            generateBox(worldGenLevel, boundingBox, 1, 6, 1, 1, 6, 14, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 9, 6, 1, 9, 6, 14, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 2, 6, 1, 8, 6, 2, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 2, 6, 14, 8, 6, 14, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 1, 1, 1, 2, 1, 4, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 8, 1, 1, 9, 1, 4, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 3, 1, 8, 7, 1, 12, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true);
            for (int i = 3; i < 14; i += 2) {
                generateBox(worldGenLevel, boundingBox, 0, 3, i, 0, 4, i, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 10, 3, i, 10, 4, i, blockState, blockState, false);
            }
            for (int i2 = 2; i2 < 9; i2 += 2) {
                generateBox(worldGenLevel, boundingBox, i2, 3, 15, i2, 4, 15, blockState2, blockState2, false);
            }
            BlockState blockState3 = (BlockState) Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            generateBox(worldGenLevel, boundingBox, 4, 1, 5, 6, 1, 7, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 2, 6, 6, 2, 7, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            generateBox(worldGenLevel, boundingBox, 4, 3, 7, 6, 3, 7, false, random, (StructurePiece.BlockSelector) StrongholdPieces.SMOOTH_STONE_SELECTOR);
            for (int i3 = 4; i3 <= 6; i3++) {
                placeBlock(worldGenLevel, blockState3, i3, 1, 4, boundingBox);
                placeBlock(worldGenLevel, blockState3, i3, 2, 5, boundingBox);
                placeBlock(worldGenLevel, blockState3, i3, 3, 6, boundingBox);
            }
            BlockState blockState4 = (BlockState) Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
            BlockState blockState5 = (BlockState) Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
            BlockState blockState6 = (BlockState) Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
            BlockState blockState7 = (BlockState) Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
            boolean z = true;
            boolean[] zArr = new boolean[12];
            for (int i4 = 0; i4 < zArr.length; i4++) {
                zArr[i4] = random.nextFloat() > 0.9f;
                z &= zArr[i4];
            }
            placeBlock(worldGenLevel, (BlockState) blockState4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[0])), 4, 3, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[1])), 5, 3, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[2])), 6, 3, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[3])), 4, 3, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[4])), 5, 3, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[5])), 6, 3, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[6])), 3, 3, 9, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[7])), 3, 3, 10, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[8])), 3, 3, 11, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState7.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[9])), 7, 3, 9, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState7.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[10])), 7, 3, 10, boundingBox);
            placeBlock(worldGenLevel, (BlockState) blockState7.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(zArr[11])), 7, 3, 11, boundingBox);
            if (z) {
                BlockState defaultBlockState = Blocks.END_PORTAL.defaultBlockState();
                placeBlock(worldGenLevel, defaultBlockState, 4, 3, 9, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 5, 3, 9, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 6, 3, 9, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 4, 3, 10, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 5, 3, 10, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 6, 3, 10, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 4, 3, 11, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 5, 3, 11, boundingBox);
                placeBlock(worldGenLevel, defaultBlockState, 6, 3, 11, boundingBox);
            }
            if (!this.hasPlacedSpawner) {
                BlockPos blockPos2 = new BlockPos(getWorldX(5, 6), getWorldY(3), getWorldZ(5, 6));
                if (boundingBox.isInside(blockPos2)) {
                    this.hasPlacedSpawner = true;
                    worldGenLevel.setBlock(blockPos2, Blocks.SPAWNER.defaultBlockState(), 2);
                    BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
                    if (blockEntity instanceof SpawnerBlockEntity) {
                        ((SpawnerBlockEntity) blockEntity).getSpawner().setEntityId(EntityType.SILVERFISH);
                        return true;
                    }
                    return true;
                }
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StrongholdPieces$SmoothStoneSelector.class */
    static class SmoothStoneSelector extends StructurePiece.BlockSelector {
        private SmoothStoneSelector() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece.BlockSelector
        public void next(Random random, int i, int i2, int i3, boolean z) {
            if (z) {
                float nextFloat = random.nextFloat();
                if (nextFloat < 0.2f) {
                    this.next = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                    return;
                }
                if (nextFloat < 0.5f) {
                    this.next = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                    return;
                } else if (nextFloat < 0.55f) {
                    this.next = Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
                    return;
                } else {
                    this.next = Blocks.STONE_BRICKS.defaultBlockState();
                    return;
                }
            }
            this.next = Blocks.CAVE_AIR.defaultBlockState();
        }
    }
}
