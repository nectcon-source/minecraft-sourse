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
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces.class */
public class NetherBridgePieces {
    private static final PieceWeight[] BRIDGE_PIECE_WEIGHTS = {new PieceWeight(BridgeStraight.class, 30, 0, true), new PieceWeight(BridgeCrossing.class, 10, 4), new PieceWeight(RoomCrossing.class, 10, 4), new PieceWeight(StairsRoom.class, 10, 3), new PieceWeight(MonsterThrone.class, 5, 2), new PieceWeight(CastleEntrance.class, 5, 1)};
    private static final PieceWeight[] CASTLE_PIECE_WEIGHTS = {new PieceWeight(CastleSmallCorridorPiece.class, 25, 0, true), new PieceWeight(CastleSmallCorridorCrossingPiece.class, 15, 5), new PieceWeight(CastleSmallCorridorRightTurnPiece.class, 5, 10), new PieceWeight(CastleSmallCorridorLeftTurnPiece.class, 5, 10), new PieceWeight(CastleCorridorStairsPiece.class, 10, 3, true), new PieceWeight(CastleCorridorTBalconyPiece.class, 7, 2), new PieceWeight(CastleStalkRoom.class, 5, 2)};

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$PieceWeight.class */
    static class PieceWeight {
        public final Class<? extends NetherBridgePiece> pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;
        public final boolean allowInRow;

        public PieceWeight(Class<? extends NetherBridgePiece> cls, int i, int i2, boolean z) {
            this.pieceClass = cls;
            this.weight = i;
            this.maxPlaceCount = i2;
            this.allowInRow = z;
        }

        public PieceWeight(Class<? extends NetherBridgePiece> cls, int i, int i2) {
            this(cls, i, i2, false);
        }

        public boolean doPlace(int i) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static NetherBridgePiece findAndCreateBridgePieceFactory(PieceWeight pieceWeight, List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
        Class<? extends NetherBridgePiece> cls = pieceWeight.pieceClass;
        NetherBridgePiece netherBridgePiece = null;
        if (cls == BridgeStraight.class) {
            netherBridgePiece = BridgeStraight.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == BridgeCrossing.class) {
            netherBridgePiece = BridgeCrossing.createPiece(list, i, i2, i3, direction, i4);
        } else if (cls == RoomCrossing.class) {
            netherBridgePiece = RoomCrossing.createPiece(list, i, i2, i3, direction, i4);
        } else if (cls == StairsRoom.class) {
            netherBridgePiece = StairsRoom.createPiece(list, i, i2, i3, i4, direction);
        } else if (cls == MonsterThrone.class) {
            netherBridgePiece = MonsterThrone.createPiece(list, i, i2, i3, i4, direction);
        } else if (cls == CastleEntrance.class) {
            netherBridgePiece = CastleEntrance.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == CastleSmallCorridorPiece.class) {
            netherBridgePiece = CastleSmallCorridorPiece.createPiece(list, i, i2, i3, direction, i4);
        } else if (cls == CastleSmallCorridorRightTurnPiece.class) {
            netherBridgePiece = CastleSmallCorridorRightTurnPiece.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == CastleSmallCorridorLeftTurnPiece.class) {
            netherBridgePiece = CastleSmallCorridorLeftTurnPiece.createPiece(list, random, i, i2, i3, direction, i4);
        } else if (cls == CastleCorridorStairsPiece.class) {
            netherBridgePiece = CastleCorridorStairsPiece.createPiece(list, i, i2, i3, direction, i4);
        } else if (cls == CastleCorridorTBalconyPiece.class) {
            netherBridgePiece = CastleCorridorTBalconyPiece.createPiece(list, i, i2, i3, direction, i4);
        } else if (cls == CastleSmallCorridorCrossingPiece.class) {
            netherBridgePiece = CastleSmallCorridorCrossingPiece.createPiece(list, i, i2, i3, direction, i4);
        } else if (cls == CastleStalkRoom.class) {
            netherBridgePiece = CastleStalkRoom.createPiece(list, i, i2, i3, direction, i4);
        }
        return netherBridgePiece;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$NetherBridgePiece.class */
    static abstract class NetherBridgePiece extends StructurePiece {
        protected NetherBridgePiece(StructurePieceType structurePieceType, int i) {
            super(structurePieceType, i);
        }

        public NetherBridgePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
        }

        private int updatePieceWeight(List<PieceWeight> list) {
            boolean z = false;
            int i = 0;
            for (PieceWeight pieceWeight : list) {
                if (pieceWeight.maxPlaceCount > 0 && pieceWeight.placeCount < pieceWeight.maxPlaceCount) {
                    z = true;
                }
                i += pieceWeight.weight;
            }
            if (z) {
                return i;
            }
            return -1;
        }

        private NetherBridgePiece generatePiece(StartPiece startPiece, List<PieceWeight> list, List<StructurePiece> list2, Random random, int i, int i2, int i3, Direction direction, int i4) {
            int updatePieceWeight = updatePieceWeight(list);
            boolean z = updatePieceWeight > 0 && i4 <= 30;
            int i5 = 0;
            while (i5 < 5 && z) {
                i5++;
                int nextInt = random.nextInt(updatePieceWeight);
                for (PieceWeight pieceWeight : list) {
                    nextInt -= pieceWeight.weight;
                    if (nextInt < 0) {
                        if (pieceWeight.doPlace(i4) && (pieceWeight != startPiece.previousPiece || pieceWeight.allowInRow)) {
                            NetherBridgePiece findAndCreateBridgePieceFactory = NetherBridgePieces.findAndCreateBridgePieceFactory(pieceWeight, list2, random, i, i2, i3, direction, i4);
                            if (findAndCreateBridgePieceFactory != null) {
                                pieceWeight.placeCount++;
                                startPiece.previousPiece = pieceWeight;
                                if (!pieceWeight.isValid()) {
                                    list.remove(pieceWeight);
                                }
                                return findAndCreateBridgePieceFactory;
                            }
                        }
                    }
                }
            }
            return BridgeEndFiller.createPiece(list2, random, i, i2, i3, direction, i4);
        }

        private StructurePiece generateAndAddPiece(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2, int i3, @Nullable Direction direction, int i4, boolean z) {
            if (Math.abs(i - startPiece.getBoundingBox().x0) > 112 || Math.abs(i3 - startPiece.getBoundingBox().z0) > 112) {
                return BridgeEndFiller.createPiece(list, random, i, i2, i3, direction, i4);
            }
            List<PieceWeight> list2 = startPiece.availableBridgePieces;
            if (z) {
                list2 = startPiece.availableCastlePieces;
            }
            StructurePiece generatePiece = generatePiece(startPiece, list2, list, random, i, i2, i3, direction, i4 + 1);
            if (generatePiece != null) {
                list.add(generatePiece);
                startPiece.pendingChildren.add(generatePiece);
            }
            return generatePiece;
        }

        @Nullable
        protected StructurePiece generateChildForward(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2, boolean z) {
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                    case NORTH:
                        return generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 + i, this.boundingBox.y0 + i2, this.boundingBox.z0 - 1, orientation, getGenDepth(), z);
                    case SOUTH:
                        return generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 + i, this.boundingBox.y0 + i2, this.boundingBox.z1 + 1, orientation, getGenDepth(), z);
                    case WEST:
                        return generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + i2, this.boundingBox.z0 + i, orientation, getGenDepth(), z);
                    case EAST:
                        return generateAndAddPiece(startPiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + i2, this.boundingBox.z0 + i, orientation, getGenDepth(), z);
                    default:
                        return null;
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateChildLeft(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2, boolean z) {
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                }
                return generateAndAddPiece(startPiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + i, this.boundingBox.z0 + i2, Direction.WEST, getGenDepth(), z);
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateChildRight(StartPiece startPiece, List<StructurePiece> list, Random random, int i, int i2, boolean z) {
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                }
                return generateAndAddPiece(startPiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + i, this.boundingBox.z0 + i2, Direction.EAST, getGenDepth(), z);
            }
            return null;
        }

        protected static boolean isOkBox(BoundingBox boundingBox) {
            return boundingBox != null && boundingBox.y0 > 10;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$StartPiece.class */
    public static class StartPiece extends BridgeCrossing {
        public PieceWeight previousPiece;
        public List<PieceWeight> availableBridgePieces;
        public List<PieceWeight> availableCastlePieces;
        public final List<StructurePiece> pendingChildren;

        public StartPiece(Random random, int i, int i2) {
            super(random, i, i2);
            this.pendingChildren = Lists.newArrayList();
            this.availableBridgePieces = Lists.newArrayList();
            for (PieceWeight pieceWeight : NetherBridgePieces.BRIDGE_PIECE_WEIGHTS) {
                pieceWeight.placeCount = 0;
                this.availableBridgePieces.add(pieceWeight);
            }
            this.availableCastlePieces = Lists.newArrayList();
            for (PieceWeight pieceWeight2 : NetherBridgePieces.CASTLE_PIECE_WEIGHTS) {
                pieceWeight2.placeCount = 0;
                this.availableCastlePieces.add(pieceWeight2);
            }
        }

        public StartPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_START, compoundTag);
            this.pendingChildren = Lists.newArrayList();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$BridgeStraight.class */
    public static class BridgeStraight extends NetherBridgePiece {
        public BridgeStraight(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public BridgeStraight(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 1, 3, false);
        }

        public static BridgeStraight createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -3, 0, 5, 10, 19, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new BridgeStraight(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; i++) {
                for (int i2 = 0; i2 <= 2; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - i2, boundingBox);
                }
            }
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            BlockState blockState2 = (BlockState) blockState.setValue(FenceBlock.EAST, true);
            BlockState blockState3 = (BlockState) blockState.setValue(FenceBlock.WEST, true);
            generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 4, 1, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 4, 0, 4, 4, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 14, 0, 4, 14, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 0, 1, 17, 0, 4, 17, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 4, 1, blockState3, blockState3, false);
            generateBox(worldGenLevel, boundingBox, 4, 3, 4, 4, 4, 4, blockState3, blockState3, false);
            generateBox(worldGenLevel, boundingBox, 4, 3, 14, 4, 4, 14, blockState3, blockState3, false);
            generateBox(worldGenLevel, boundingBox, 4, 1, 17, 4, 4, 17, blockState3, blockState3, false);
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$BridgeEndFiller.class */
    public static class BridgeEndFiller extends NetherBridgePiece {
        private final int selfSeed;

        public BridgeEndFiller(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
            this.selfSeed = random.nextInt();
        }

        public BridgeEndFiller(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, compoundTag);
            this.selfSeed = compoundTag.getInt("Seed");
        }

        public static BridgeEndFiller createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -3, 0, 5, 10, 8, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new BridgeEndFiller(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.NetherBridgePieces.NetherBridgePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putInt("Seed", this.selfSeed);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            Random random2 = new Random(this.selfSeed);
            for (int i = 0; i <= 4; i++) {
                for (int i2 = 3; i2 <= 4; i2++) {
                    generateBox(worldGenLevel, boundingBox, i, i2, 0, i, i2, random2.nextInt(8), Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
            }
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 0, 5, random2.nextInt(8), Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 5, 0, 4, 5, random2.nextInt(8), Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i3 = 0; i3 <= 4; i3++) {
                generateBox(worldGenLevel, boundingBox, i3, 2, 0, i3, 2, random2.nextInt(5), Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }
            for (int i4 = 0; i4 <= 4; i4++) {
                for (int i5 = 0; i5 <= 1; i5++) {
                    generateBox(worldGenLevel, boundingBox, i4, i5, 0, i4, i5, random2.nextInt(3), Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$BridgeCrossing.class */
    public static class BridgeCrossing extends NetherBridgePiece {
        public BridgeCrossing(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        protected BridgeCrossing(Random random, int i, int i2) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0);
            setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(random));
            if (getOrientation().getAxis() == Direction.Axis.Z) {
                this.boundingBox = new BoundingBox(i, 64, i2, (i + 19) - 1, 73, (i2 + 19) - 1);
            } else {
                this.boundingBox = new BoundingBox(i, 64, i2, (i + 19) - 1, 73, (i2 + 19) - 1);
            }
        }

        protected BridgeCrossing(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }

        public BridgeCrossing(StructureManager structureManager, CompoundTag compoundTag) {
            this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 8, 3, false);
            generateChildLeft((StartPiece) structurePiece, list, random, 3, 8, false);
            generateChildRight((StartPiece) structurePiece, list, random, 3, 8, false);
        }

        public static BridgeCrossing createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -8, -3, 0, 19, 10, 19, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new BridgeCrossing(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 7; i <= 11; i++) {
                for (int i2 = 0; i2 <= 2; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - i2, boundingBox);
                }
            }
            generateBox(worldGenLevel, boundingBox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i3 = 0; i3 <= 2; i3++) {
                for (int i4 = 7; i4 <= 11; i4++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i3, -1, i4, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - i3, -1, i4, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$RoomCrossing.class */
    public static class RoomCrossing extends NetherBridgePiece {
        public RoomCrossing(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public RoomCrossing(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 2, 0, false);
            generateChildLeft((StartPiece) structurePiece, list, random, 0, 2, false);
            generateChildRight((StartPiece) structurePiece, list, random, 0, 2, false);
        }

        public static RoomCrossing createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -2, 0, 0, 7, 9, 7, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new RoomCrossing(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            generateBox(worldGenLevel, boundingBox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 6, 4, 5, 6, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 2, 0, 5, 4, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 5, 2, 6, 5, 4, blockState2, blockState2, false);
            for (int i = 0; i <= 6; i++) {
                for (int i2 = 0; i2 <= 6; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$StairsRoom.class */
    public static class StairsRoom extends NetherBridgePiece {
        public StairsRoom(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public StairsRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildRight((StartPiece) structurePiece, list, random, 6, 2, false);
        }

        public static StairsRoom createPiece(List<StructurePiece> list, int i, int i2, int i3, int i4, Direction direction) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -2, 0, 0, 7, 11, 7, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new StairsRoom(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            generateBox(worldGenLevel, boundingBox, 0, 3, 2, 0, 5, 4, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 6, 3, 2, 6, 5, 2, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 6, 3, 4, 6, 5, 4, blockState2, blockState2, false);
            placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
            generateBox(worldGenLevel, boundingBox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);
            for (int i = 0; i <= 6; i++) {
                for (int i2 = 0; i2 <= 6; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$MonsterThrone.class */
    public static class MonsterThrone extends NetherBridgePiece {
        private boolean hasPlacedSpawner;

        public MonsterThrone(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public MonsterThrone(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, compoundTag);
            this.hasPlacedSpawner = compoundTag.getBoolean("Mob");
        }

        @Override // net.minecraft.world.level.levelgen.structure.NetherBridgePieces.NetherBridgePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Mob", this.hasPlacedSpawner);
        }

        public static MonsterThrone createPiece(List<StructurePiece> list, int i, int i2, int i3, int i4, Direction direction) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -2, 0, 0, 7, 8, 9, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new MonsterThrone(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            placeBlock(worldGenLevel, (BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 1, 6, 3, boundingBox);
            placeBlock(worldGenLevel, (BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 5, 6, 3, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)).setValue(FenceBlock.NORTH, true), 0, 6, 3, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.NORTH, true), 6, 6, 3, boundingBox);
            generateBox(worldGenLevel, boundingBox, 0, 6, 4, 0, 6, 7, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 6, 6, 4, 6, 6, 7, blockState2, blockState2, false);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)).setValue(FenceBlock.SOUTH, true), 0, 6, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.SOUTH, true), 6, 6, 8, boundingBox);
            generateBox(worldGenLevel, boundingBox, 1, 6, 8, 5, 6, 8, blockState, blockState, false);
            placeBlock(worldGenLevel, (BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 1, 7, 8, boundingBox);
            generateBox(worldGenLevel, boundingBox, 2, 7, 8, 4, 7, 8, blockState, blockState, false);
            placeBlock(worldGenLevel, (BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 5, 7, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 2, 8, 8, boundingBox);
            placeBlock(worldGenLevel, blockState, 3, 8, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 4, 8, 8, boundingBox);
            if (!this.hasPlacedSpawner) {
                BlockPos blockPos2 = new BlockPos(getWorldX(3, 5), getWorldY(5), getWorldZ(3, 5));
                if (boundingBox.isInside(blockPos2)) {
                    this.hasPlacedSpawner = true;
                    worldGenLevel.setBlock(blockPos2, Blocks.SPAWNER.defaultBlockState(), 2);
                    BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
                    if (blockEntity instanceof SpawnerBlockEntity) {
                        ((SpawnerBlockEntity) blockEntity).getSpawner().setEntityId(EntityType.BLAZE);
                    }
                }
            }
            for (int i = 0; i <= 6; i++) {
                for (int i2 = 0; i2 <= 6; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleEntrance.class */
    public static class CastleEntrance extends NetherBridgePiece {
        public CastleEntrance(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public CastleEntrance(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 5, 3, true);
        }

        public static CastleEntrance createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -5, -3, 0, 13, 14, 13, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleEntrance(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            for (int i = 1; i <= 11; i += 2) {
                generateBox(worldGenLevel, boundingBox, i, 10, 0, i, 11, 0, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, i, 10, 12, i, 11, 12, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 0, 10, i, 0, 11, i, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 12, 10, i, 12, 11, i, blockState2, blockState2, false);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, boundingBox);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, boundingBox);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, boundingBox);
                if (i != 11) {
                    placeBlock(worldGenLevel, blockState, i + 1, 13, 0, boundingBox);
                    placeBlock(worldGenLevel, blockState, i + 1, 13, 12, boundingBox);
                    placeBlock(worldGenLevel, blockState2, 0, 13, i + 1, boundingBox);
                    placeBlock(worldGenLevel, blockState2, 12, 13, i + 1, boundingBox);
                }
            }
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 0, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 0, boundingBox);
            for (int i2 = 3; i2 <= 9; i2 += 2) {
                generateBox(worldGenLevel, boundingBox, 1, 7, i2, 1, 8, i2, (BlockState) blockState2.setValue(FenceBlock.WEST, true), (BlockState) blockState2.setValue(FenceBlock.WEST, true), false);
                generateBox(worldGenLevel, boundingBox, 11, 7, i2, 11, 8, i2, (BlockState) blockState2.setValue(FenceBlock.EAST, true), (BlockState) blockState2.setValue(FenceBlock.EAST, true), false);
            }
            generateBox(worldGenLevel, boundingBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i3 = 4; i3 <= 8; i3++) {
                for (int i4 = 0; i4 <= 2; i4++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i3, -1, i4, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i3, -1, 12 - i4, boundingBox);
                }
            }
            for (int i5 = 0; i5 <= 2; i5++) {
                for (int i6 = 4; i6 <= 8; i6++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i5, -1, i6, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i5, -1, i6, boundingBox);
                }
            }
            generateBox(worldGenLevel, boundingBox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, boundingBox);
            placeBlock(worldGenLevel, Blocks.LAVA.defaultBlockState(), 6, 5, 6, boundingBox);
            BlockPos blockPos2 = new BlockPos(getWorldX(6, 6), getWorldY(5), getWorldZ(6, 6));
            if (boundingBox.isInside(blockPos2)) {
                worldGenLevel.getLiquidTicks().scheduleTick(blockPos2, Fluids.LAVA, 0);
                return true;
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleStalkRoom.class */
    public static class CastleStalkRoom extends NetherBridgePiece {
        public CastleStalkRoom(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public CastleStalkRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 5, 3, true);
            generateChildForward((StartPiece) structurePiece, list, random, 5, 11, true);
        }

        public static CastleStalkRoom createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -5, -3, 0, 13, 14, 13, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleStalkRoom(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            BlockState blockState3 = (BlockState) blockState2.setValue(FenceBlock.WEST, true);
            BlockState blockState4 = (BlockState) blockState2.setValue(FenceBlock.EAST, true);
            for (int i = 1; i <= 11; i += 2) {
                generateBox(worldGenLevel, boundingBox, i, 10, 0, i, 11, 0, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, i, 10, 12, i, 11, 12, blockState, blockState, false);
                generateBox(worldGenLevel, boundingBox, 0, 10, i, 0, 11, i, blockState2, blockState2, false);
                generateBox(worldGenLevel, boundingBox, 12, 10, i, 12, 11, i, blockState2, blockState2, false);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, boundingBox);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, boundingBox);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, boundingBox);
                placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, boundingBox);
                if (i != 11) {
                    placeBlock(worldGenLevel, blockState, i + 1, 13, 0, boundingBox);
                    placeBlock(worldGenLevel, blockState, i + 1, 13, 12, boundingBox);
                    placeBlock(worldGenLevel, blockState2, 0, 13, i + 1, boundingBox);
                    placeBlock(worldGenLevel, blockState2, 12, 13, i + 1, boundingBox);
                }
            }
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 0, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 12, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 0, boundingBox);
            for (int i2 = 3; i2 <= 9; i2 += 2) {
                generateBox(worldGenLevel, boundingBox, 1, 7, i2, 1, 8, i2, blockState3, blockState3, false);
                generateBox(worldGenLevel, boundingBox, 11, 7, i2, 11, 8, i2, blockState4, blockState4, false);
            }
            BlockState blockState5 = (BlockState) Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            for (int i3 = 0; i3 <= 6; i3++) {
                int i4 = i3 + 4;
                for (int i5 = 5; i5 <= 7; i5++) {
                    placeBlock(worldGenLevel, blockState5, i5, 5 + i3, i4, boundingBox);
                }
                if (i4 >= 5 && i4 <= 8) {
                    generateBox(worldGenLevel, boundingBox, 5, 5, i4, 7, i3 + 4, i4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                } else if (i4 >= 9 && i4 <= 10) {
                    generateBox(worldGenLevel, boundingBox, 5, 8, i4, 7, i3 + 4, i4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
                if (i3 >= 1) {
                    generateBox(worldGenLevel, boundingBox, 5, 6 + i3, i4, 7, 9 + i3, i4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
                }
            }
            for (int i6 = 5; i6 <= 7; i6++) {
                placeBlock(worldGenLevel, blockState5, i6, 12, 11, boundingBox);
            }
            generateBox(worldGenLevel, boundingBox, 5, 6, 7, 5, 7, 7, blockState4, blockState4, false);
            generateBox(worldGenLevel, boundingBox, 7, 6, 7, 7, 7, 7, blockState3, blockState3, false);
            generateBox(worldGenLevel, boundingBox, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState6 = (BlockState) blockState5.setValue(StairBlock.FACING, Direction.EAST);
            BlockState blockState7 = (BlockState) blockState5.setValue(StairBlock.FACING, Direction.WEST);
            placeBlock(worldGenLevel, blockState7, 4, 5, 2, boundingBox);
            placeBlock(worldGenLevel, blockState7, 4, 5, 3, boundingBox);
            placeBlock(worldGenLevel, blockState7, 4, 5, 9, boundingBox);
            placeBlock(worldGenLevel, blockState7, 4, 5, 10, boundingBox);
            placeBlock(worldGenLevel, blockState6, 8, 5, 2, boundingBox);
            placeBlock(worldGenLevel, blockState6, 8, 5, 3, boundingBox);
            placeBlock(worldGenLevel, blockState6, 8, 5, 9, boundingBox);
            placeBlock(worldGenLevel, blockState6, 8, 5, 10, boundingBox);
            generateBox(worldGenLevel, boundingBox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i7 = 4; i7 <= 8; i7++) {
                for (int i8 = 0; i8 <= 2; i8++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i7, -1, i8, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i7, -1, 12 - i8, boundingBox);
                }
            }
            for (int i9 = 0; i9 <= 2; i9++) {
                for (int i10 = 4; i10 <= 8; i10++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i9, -1, i10, boundingBox);
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i9, -1, i10, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleSmallCorridorPiece.class */
    public static class CastleSmallCorridorPiece extends NetherBridgePiece {
        public CastleSmallCorridorPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public CastleSmallCorridorPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 1, 0, true);
        }

        public static CastleSmallCorridorPiece createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, 0, 0, 5, 7, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleSmallCorridorPiece(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 4, 1, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 4, 3, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 4, 3, 1, 4, 4, 1, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 4, 3, 3, 4, 4, 3, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; i++) {
                for (int i2 = 0; i2 <= 4; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleSmallCorridorCrossingPiece.class */
    public static class CastleSmallCorridorCrossingPiece extends NetherBridgePiece {
        public CastleSmallCorridorCrossingPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public CastleSmallCorridorCrossingPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 1, 0, true);
            generateChildLeft((StartPiece) structurePiece, list, random, 0, 1, true);
            generateChildRight((StartPiece) structurePiece, list, random, 0, 1, true);
        }

        public static CastleSmallCorridorCrossingPiece createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, 0, 0, 5, 7, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleSmallCorridorCrossingPiece(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; i++) {
                for (int i2 = 0; i2 <= 4; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleSmallCorridorRightTurnPiece.class */
    public static class CastleSmallCorridorRightTurnPiece extends NetherBridgePiece {
        private boolean isNeedingChest;

        public CastleSmallCorridorRightTurnPiece(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
            this.isNeedingChest = random.nextInt(3) == 0;
        }

        public CastleSmallCorridorRightTurnPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, compoundTag);
            this.isNeedingChest = compoundTag.getBoolean("Chest");
        }

        @Override // net.minecraft.world.level.levelgen.structure.NetherBridgePieces.NetherBridgePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Chest", this.isNeedingChest);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildRight((StartPiece) structurePiece, list, random, 0, 1, true);
        }

        public static CastleSmallCorridorRightTurnPiece createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, 0, 0, 5, 7, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleSmallCorridorRightTurnPiece(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 4, 1, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 4, 3, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
            if (this.isNeedingChest && boundingBox.isInside(new BlockPos(getWorldX(1, 3), getWorldY(2), getWorldZ(1, 3)))) {
                this.isNeedingChest = false;
                createChest(worldGenLevel, boundingBox, random, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }
            generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; i++) {
                for (int i2 = 0; i2 <= 4; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleSmallCorridorLeftTurnPiece.class */
    public static class CastleSmallCorridorLeftTurnPiece extends NetherBridgePiece {
        private boolean isNeedingChest;

        public CastleSmallCorridorLeftTurnPiece(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
            this.isNeedingChest = random.nextInt(3) == 0;
        }

        public CastleSmallCorridorLeftTurnPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, compoundTag);
            this.isNeedingChest = compoundTag.getBoolean("Chest");
        }

        @Override // net.minecraft.world.level.levelgen.structure.NetherBridgePieces.NetherBridgePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("Chest", this.isNeedingChest);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildLeft((StartPiece) structurePiece, list, random, 0, 1, true);
        }

        public static CastleSmallCorridorLeftTurnPiece createPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, 0, 0, 5, 7, 5, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleSmallCorridorLeftTurnPiece(i4, random, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 4, 3, 1, 4, 4, 1, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 4, 3, 3, 4, 4, 3, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
            if (this.isNeedingChest && boundingBox.isInside(new BlockPos(getWorldX(3, 3), getWorldY(2), getWorldZ(3, 3)))) {
                this.isNeedingChest = false;
                createChest(worldGenLevel, boundingBox, random, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }
            generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; i++) {
                for (int i2 = 0; i2 <= 4; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleCorridorStairsPiece.class */
    public static class CastleCorridorStairsPiece extends NetherBridgePiece {
        public CastleCorridorStairsPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public CastleCorridorStairsPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            generateChildForward((StartPiece) structurePiece, list, random, 1, 0, true);
        }

        public static CastleCorridorStairsPiece createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -1, -7, 0, 5, 14, 10, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleCorridorStairsPiece(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockState blockState = (BlockState) Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            for (int i = 0; i <= 9; i++) {
                int max = Math.max(1, 7 - i);
                int min = Math.min(Math.max(max + 5, 14 - i), 13);
                int i2 = i;
                generateBox(worldGenLevel, boundingBox, 0, 0, i2, 4, max, i2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 1, max + 1, i2, 3, min - 1, i2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
                if (i <= 6) {
                    placeBlock(worldGenLevel, blockState, 1, max + 1, i2, boundingBox);
                    placeBlock(worldGenLevel, blockState, 2, max + 1, i2, boundingBox);
                    placeBlock(worldGenLevel, blockState, 3, max + 1, i2, boundingBox);
                }
                generateBox(worldGenLevel, boundingBox, 0, min, i2, 4, min, i2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 0, max + 1, i2, 0, min - 1, i2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                generateBox(worldGenLevel, boundingBox, 4, max + 1, i2, 4, min - 1, i2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                if ((i & 1) == 0) {
                    generateBox(worldGenLevel, boundingBox, 0, max + 2, i2, 0, max + 3, i2, blockState2, blockState2, false);
                    generateBox(worldGenLevel, boundingBox, 4, max + 2, i2, 4, max + 3, i2, blockState2, blockState2, false);
                }
                for (int i3 = 0; i3 <= 4; i3++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i3, -1, i2, boundingBox);
                }
            }
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/NetherBridgePieces$CastleCorridorTBalconyPiece.class */
    public static class CastleCorridorTBalconyPiece extends NetherBridgePiece {
        public CastleCorridorTBalconyPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, i);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public CastleCorridorTBalconyPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, compoundTag);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            int i = 1;
            Direction orientation = getOrientation();
            if (orientation == Direction.WEST || orientation == Direction.NORTH) {
                i = 5;
            }
            generateChildLeft((StartPiece) structurePiece, list, random, 0, i, random.nextInt(8) > 0);
            generateChildRight((StartPiece) structurePiece, list, random, 0, i, random.nextInt(8) > 0);
        }

        public static CastleCorridorTBalconyPiece createPiece(List<StructurePiece> list, int i, int i2, int i3, Direction direction, int i4) {
            BoundingBox orientBox = BoundingBox.orientBox(i, i2, i3, -3, 0, 0, 9, 7, 9, direction);
            if (!isOkBox(orientBox) || StructurePiece.findCollisionPiece(list, orientBox) != null) {
                return null;
            }
            return new CastleCorridorTBalconyPiece(i4, orientBox, direction);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockState blockState = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            BlockState blockState2 = (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 0, 1, 4, 0, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 4, 0, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 8, 7, 3, 8, blockState2, blockState2, false);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)).setValue(FenceBlock.SOUTH, true), 0, 3, 8, boundingBox);
            placeBlock(worldGenLevel, (BlockState) ((BlockState) Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.SOUTH, true), 8, 3, 8, boundingBox);
            generateBox(worldGenLevel, boundingBox, 0, 3, 6, 0, 3, 7, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 8, 3, 6, 8, 3, 7, blockState, blockState, false);
            generateBox(worldGenLevel, boundingBox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            generateBox(worldGenLevel, boundingBox, 1, 4, 5, 1, 5, 5, blockState2, blockState2, false);
            generateBox(worldGenLevel, boundingBox, 7, 4, 5, 7, 5, 5, blockState2, blockState2, false);
            for (int i = 0; i <= 5; i++) {
                for (int i2 = 0; i2 <= 8; i2++) {
                    fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i2, -1, i, boundingBox);
                }
            }
            return true;
        }
    }
}
