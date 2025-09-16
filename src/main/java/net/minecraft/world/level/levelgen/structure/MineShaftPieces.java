package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/MineShaftPieces.class */
public class MineShaftPieces {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/MineShaftPieces$MineShaftPiece.class */
    static abstract class MineShaftPiece extends StructurePiece {
        protected MineshaftFeature.Type type;

        public MineShaftPiece(StructurePieceType structurePieceType, int i, MineshaftFeature.Type type) {
            super(structurePieceType, i);
            this.type = type;
        }

        public MineShaftPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.type = MineshaftFeature.Type.byId(compoundTag.getInt("MST"));
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            compoundTag.putInt("MST", this.type.ordinal());
        }

        protected BlockState getPlanksBlock() {
            switch (this.type) {
                case NORMAL:
                default:
                    return Blocks.OAK_PLANKS.defaultBlockState();
                case MESA:
                    return Blocks.DARK_OAK_PLANKS.defaultBlockState();
            }
        }

        protected BlockState getFenceBlock() {
            switch (this.type) {
                case NORMAL:
                default:
                    return Blocks.OAK_FENCE.defaultBlockState();
                case MESA:
                    return Blocks.DARK_OAK_FENCE.defaultBlockState();
            }
        }

        protected boolean isSupportingBox(BlockGetter blockGetter, BoundingBox boundingBox, int i, int i2, int i3, int i4) {
            for (int i5 = i; i5 <= i2; i5++) {
                if (getBlock(blockGetter, i5, i3 + 1, i4, boundingBox).isAir()) {
                    return false;
                }
            }
            return true;
        }
    }

    private static MineShaftPiece createRandomShaftPiece(List<StructurePiece> list, Random random, int i, int i2, int i3, @Nullable Direction direction, int i4, MineshaftFeature.Type type) {
        int nextInt = random.nextInt(100);
        if (nextInt >= 80) {
            BoundingBox findCrossing = MineShaftCrossing.findCrossing(list, random, i, i2, i3, direction);
            if (findCrossing != null) {
                return new MineShaftCrossing(i4, findCrossing, direction, type);
            }
            return null;
        }
        if (nextInt >= 70) {
            BoundingBox findStairs = MineShaftStairs.findStairs(list, random, i, i2, i3, direction);
            if (findStairs != null) {
                return new MineShaftStairs(i4, findStairs, direction, type);
            }
            return null;
        }
        BoundingBox findCorridorSize = MineShaftCorridor.findCorridorSize(list, random, i, i2, i3, direction);
        if (findCorridorSize != null) {
            return new MineShaftCorridor(i4, random, findCorridorSize, direction, type);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static MineShaftPiece generateAndAddPiece(StructurePiece structurePiece, List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction, int i4) {
        if (i4 > 8 || Math.abs(i - structurePiece.getBoundingBox().x0) > 80 || Math.abs(i3 - structurePiece.getBoundingBox().z0) > 80) {
            return null;
        }
        MineShaftPiece createRandomShaftPiece = createRandomShaftPiece(list, random, i, i2, i3, direction, i4 + 1, ((MineShaftPiece) structurePiece).type);
        if (createRandomShaftPiece != null) {
            list.add(createRandomShaftPiece);
            createRandomShaftPiece.addChildren(structurePiece, list, random);
        }
        return createRandomShaftPiece;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/MineShaftPieces$MineShaftRoom.class */
    public static class MineShaftRoom extends MineShaftPiece {
        private final List<BoundingBox> childEntranceBoxes;

        public MineShaftRoom(int i, Random random, int i2, int i3, MineshaftFeature.Type type) {
            super(StructurePieceType.MINE_SHAFT_ROOM, i, type);
            this.childEntranceBoxes = Lists.newLinkedList();
            this.type = type;
            this.boundingBox = new BoundingBox(i2, 50, i3, i2 + 7 + random.nextInt(6), 54 + random.nextInt(6), i3 + 7 + random.nextInt(6));
        }

        public MineShaftRoom(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_ROOM, compoundTag);
            this.childEntranceBoxes = Lists.newLinkedList();
            ListTag list = compoundTag.getList("Entrances", 11);
            for (int i = 0; i < list.size(); i++) {
                this.childEntranceBoxes.add(new BoundingBox(list.getIntArray(i)));
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            int genDepth = getGenDepth();
            int ySpan = (this.boundingBox.getYSpan() - 3) - 1;
            if (ySpan <= 0) {
                ySpan = 1;
            }
            int i = 0;
            while (i < this.boundingBox.getXSpan()) {
                int nextInt = i + random.nextInt(this.boundingBox.getXSpan());
                if (nextInt + 3 > this.boundingBox.getXSpan()) {
                    break;
                }
                MineShaftPiece generateAndAddPiece = MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + nextInt, this.boundingBox.y0 + random.nextInt(ySpan) + 1, this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                if (generateAndAddPiece != null) {
                    BoundingBox boundingBox = generateAndAddPiece.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(boundingBox.x0, boundingBox.y0, this.boundingBox.z0, boundingBox.x1, boundingBox.y1, this.boundingBox.z0 + 1));
                }
                i = nextInt + 4;
            }
            int i2 = 0;
            while (i2 < this.boundingBox.getXSpan()) {
                int nextInt2 = i2 + random.nextInt(this.boundingBox.getXSpan());
                if (nextInt2 + 3 > this.boundingBox.getXSpan()) {
                    break;
                }
                MineShaftPiece generateAndAddPiece2 = MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + nextInt2, this.boundingBox.y0 + random.nextInt(ySpan) + 1, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                if (generateAndAddPiece2 != null) {
                    BoundingBox boundingBox2 = generateAndAddPiece2.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(boundingBox2.x0, boundingBox2.y0, this.boundingBox.z1 - 1, boundingBox2.x1, boundingBox2.y1, this.boundingBox.z1));
                }
                i2 = nextInt2 + 4;
            }
            int i3 = 0;
            while (i3 < this.boundingBox.getZSpan()) {
                int nextInt3 = i3 + random.nextInt(this.boundingBox.getZSpan());
                if (nextInt3 + 3 > this.boundingBox.getZSpan()) {
                    break;
                }
                MineShaftPiece generateAndAddPiece3 = MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + random.nextInt(ySpan) + 1, this.boundingBox.z0 + nextInt3, Direction.WEST, genDepth);
                if (generateAndAddPiece3 != null) {
                    BoundingBox boundingBox3 = generateAndAddPiece3.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.x0, boundingBox3.y0, boundingBox3.z0, this.boundingBox.x0 + 1, boundingBox3.y1, boundingBox3.z1));
                }
                i3 = nextInt3 + 4;
            }
            int i4 = 0;
            while (i4 < this.boundingBox.getZSpan()) {
                int nextInt4 = i4 + random.nextInt(this.boundingBox.getZSpan());
                if (nextInt4 + 3 <= this.boundingBox.getZSpan()) {
                    StructurePiece generateAndAddPiece4 = MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + random.nextInt(ySpan) + 1, this.boundingBox.z0 + nextInt4, Direction.EAST, genDepth);
                    if (generateAndAddPiece4 != null) {
                        BoundingBox boundingBox4 = generateAndAddPiece4.getBoundingBox();
                        this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.x1 - 1, boundingBox4.y0, boundingBox4.z0, this.boundingBox.x1, boundingBox4.y1, boundingBox4.z1));
                    }
                    i4 = nextInt4 + 4;
                } else {
                    return;
                }
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            generateBox(worldGenLevel, boundingBox, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z0, this.boundingBox.x1, this.boundingBox.y0, this.boundingBox.z1, Blocks.DIRT.defaultBlockState(), CAVE_AIR, true);
            generateBox(worldGenLevel, boundingBox, this.boundingBox.x0, this.boundingBox.y0 + 1, this.boundingBox.z0, this.boundingBox.x1, Math.min(this.boundingBox.y0 + 3, this.boundingBox.y1), this.boundingBox.z1, CAVE_AIR, CAVE_AIR, false);
            for (BoundingBox boundingBox2 : this.childEntranceBoxes) {
                generateBox(worldGenLevel, boundingBox, boundingBox2.x0, boundingBox2.y1 - 2, boundingBox2.z0, boundingBox2.x1, boundingBox2.y1, boundingBox2.z1, CAVE_AIR, CAVE_AIR, false);
            }
            generateUpperHalfSphere(worldGenLevel, boundingBox, this.boundingBox.x0, this.boundingBox.y0 + 4, this.boundingBox.z0, this.boundingBox.x1, this.boundingBox.y1, this.boundingBox.z1, CAVE_AIR, false);
            return true;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void move(int i, int i2, int i3) {
            super.move(i, i2, i3);
            Iterator<BoundingBox> it = this.childEntranceBoxes.iterator();
            while (it.hasNext()) {
                it.next().move(i, i2, i3);
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.MineShaftPieces.MineShaftPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            ListTag listTag = new ListTag();
            Iterator<BoundingBox> it = this.childEntranceBoxes.iterator();
            while (it.hasNext()) {
                listTag.add(it.next().createTag());
            }
            compoundTag.put("Entrances", listTag);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/MineShaftPieces$MineShaftCorridor.class */
    public static class MineShaftCorridor extends MineShaftPiece {
        private final boolean hasRails;
        private final boolean spiderCorridor;
        private boolean hasPlacedSpider;
        private final int numSections;

        public MineShaftCorridor(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, compoundTag);
            this.hasRails = compoundTag.getBoolean("hr");
            this.spiderCorridor = compoundTag.getBoolean("sc");
            this.hasPlacedSpider = compoundTag.getBoolean("hps");
            this.numSections = compoundTag.getInt("Num");
        }

        @Override // net.minecraft.world.level.levelgen.structure.MineShaftPieces.MineShaftPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("hr", this.hasRails);
            compoundTag.putBoolean("sc", this.spiderCorridor);
            compoundTag.putBoolean("hps", this.hasPlacedSpider);
            compoundTag.putInt("Num", this.numSections);
        }

        public MineShaftCorridor(int i, Random random, BoundingBox boundingBox, Direction direction, MineshaftFeature.Type type) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, i, type);
            setOrientation(direction);
            this.boundingBox = boundingBox;
            this.hasRails = random.nextInt(3) == 0;
            this.spiderCorridor = !this.hasRails && random.nextInt(23) == 0;
            if (getOrientation().getAxis() == Direction.Axis.Z) {
                this.numSections = boundingBox.getZSpan() / 5;
            } else {
                this.numSections = boundingBox.getXSpan() / 5;
            }
        }


        public static net.minecraft.world.level.levelgen.structure.BoundingBox findCorridorSize(java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> var0, java.util.Random var1, int var2, int var3, int var4, net.minecraft.core.Direction var5) {
            BoundingBox var6 = new BoundingBox(var2, var3, var4, var2, var3 + 3 - 1, var4);

            int var7;
            for(var7 = var1.nextInt(3) + 2; var7 > 0; --var7) {
                int var8x = var7 * 5;
                switch(var5) {
                    case NORTH:
                    default:
                        var6.x1 = var2 + 3 - 1;
                        var6.z0 = var4 - (var8x - 1);
                        break;
                    case SOUTH:
                        var6.x1 = var2 + 3 - 1;
                        var6.z1 = var4 + var8x - 1;
                        break;
                    case WEST:
                        var6.x0 = var2 - (var8x - 1);
                        var6.z1 = var4 + 3 - 1;
                        break;
                    case EAST:
                        var6.x1 = var2 + var8x - 1;
                        var6.z1 = var4 + 3 - 1;
                }

                if (StructurePiece.findCollisionPiece(var0, var6) == null) {
                    break;
                }
            }

            return var7 > 0 ? var6 : null;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            int genDepth = getGenDepth();
            int nextInt = random.nextInt(4);
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                    case NORTH:
                    default:
                        if (nextInt <= 1) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0 - 1, orientation, genDepth);
                            break;
                        } else if (nextInt == 2) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0, Direction.WEST, genDepth);
                            break;
                        } else {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0, Direction.EAST, genDepth);
                            break;
                        }
                    case SOUTH:
                        if (nextInt <= 1) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z1 + 1, orientation, genDepth);
                            break;
                        } else if (nextInt == 2) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z1 - 3, Direction.WEST, genDepth);
                            break;
                        } else {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z1 - 3, Direction.EAST, genDepth);
                            break;
                        }
                    case WEST:
                        if (nextInt <= 1) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0, orientation, genDepth);
                            break;
                        } else if (nextInt == 2) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                            break;
                        } else {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                            break;
                        }
                    case EAST:
                        if (nextInt <= 1) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0, orientation, genDepth);
                            break;
                        } else if (nextInt == 2) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 - 3, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                            break;
                        } else {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 - 3, (this.boundingBox.y0 - 1) + random.nextInt(3), this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                            break;
                        }
                }
            }
            if (genDepth < 8) {
                if (orientation == Direction.NORTH || orientation == Direction.SOUTH) {
                    for (int i = this.boundingBox.z0 + 3; i + 3 <= this.boundingBox.z1; i += 5) {
                        int nextInt2 = random.nextInt(5);
                        if (nextInt2 == 0) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, i, Direction.WEST, genDepth + 1);
                        } else if (nextInt2 == 1) {
                            MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, i, Direction.EAST, genDepth + 1);
                        }
                    }
                    return;
                }
                for (int i2 = this.boundingBox.x0 + 3; i2 + 3 <= this.boundingBox.x1; i2 += 5) {
                    int nextInt3 = random.nextInt(5);
                    if (nextInt3 == 0) {
                        MineShaftPieces.generateAndAddPiece(structurePiece, list, random, i2, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, genDepth + 1);
                    } else if (nextInt3 == 1) {
                        MineShaftPieces.generateAndAddPiece(structurePiece, list, random, i2, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth + 1);
                    }
                }
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        protected boolean createChest(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, int i, int i2, int i3, ResourceLocation resourceLocation) {
            BlockPos blockPos = new BlockPos(getWorldX(i, i3), getWorldY(i2), getWorldZ(i, i3));
            if (boundingBox.isInside(blockPos) && worldGenLevel.getBlockState(blockPos).isAir() && !worldGenLevel.getBlockState(blockPos.below()).isAir()) {
                placeBlock(worldGenLevel, (BlockState) Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST), i, i2, i3, boundingBox);
                MinecartChest minecartChest = new MinecartChest(worldGenLevel.getLevel(), blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d);
                minecartChest.setLootTable(resourceLocation, random.nextLong());
                worldGenLevel.addFreshEntity(minecartChest);
                return true;
            }
            return false;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            int i = (this.numSections * 5) - 1;
            BlockState planksBlock = getPlanksBlock();
            generateBox(worldGenLevel, boundingBox, 0, 0, 0, 2, 1, i, CAVE_AIR, CAVE_AIR, false);
            generateMaybeBox(worldGenLevel, boundingBox, random, 0.8f, 0, 2, 0, 2, 2, i, CAVE_AIR, CAVE_AIR, false, false);
            if (this.spiderCorridor) {
                generateMaybeBox(worldGenLevel, boundingBox, random, 0.6f, 0, 0, 0, 2, 1, i, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
            }
            for (int i2 = 0; i2 < this.numSections; i2++) {
                int i3 = 2 + (i2 * 5);
                placeSupport(worldGenLevel, boundingBox, 0, 0, i3, 2, 2, random);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.1f, 0, 2, i3 - 1);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.1f, 2, 2, i3 - 1);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.1f, 0, 2, i3 + 1);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.1f, 2, 2, i3 + 1);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.05f, 0, 2, i3 - 2);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.05f, 2, 2, i3 - 2);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.05f, 0, 2, i3 + 2);
                placeCobWeb(worldGenLevel, boundingBox, random, 0.05f, 2, 2, i3 + 2);
                if (random.nextInt(100) == 0) {
                    createChest(worldGenLevel, boundingBox, random, 2, 0, i3 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                }
                if (random.nextInt(100) == 0) {
                    createChest(worldGenLevel, boundingBox, random, 0, 0, i3 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                }
                if (this.spiderCorridor && !this.hasPlacedSpider) {
                    int worldY = getWorldY(0);
                    int nextInt = (i3 - 1) + random.nextInt(3);
                    BlockPos blockPos2 = new BlockPos(getWorldX(1, nextInt), worldY, getWorldZ(1, nextInt));
                    if (boundingBox.isInside(blockPos2) && isInterior(worldGenLevel, 1, 0, nextInt, boundingBox)) {
                        this.hasPlacedSpider = true;
                        worldGenLevel.setBlock(blockPos2, Blocks.SPAWNER.defaultBlockState(), 2);
                        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
                        if (blockEntity instanceof SpawnerBlockEntity) {
                            ((SpawnerBlockEntity) blockEntity).getSpawner().setEntityId(EntityType.CAVE_SPIDER);
                        }
                    }
                }
            }
            for (int i4 = 0; i4 <= 2; i4++) {
                for (int i5 = 0; i5 <= i; i5++) {
                    if (getBlock(worldGenLevel, i4, -1, i5, boundingBox).isAir() && isInterior(worldGenLevel, i4, -1, i5, boundingBox)) {
                        placeBlock(worldGenLevel, planksBlock, i4, -1, i5, boundingBox);
                    }
                }
            }
            if (this.hasRails) {
                BlockState blockState = (BlockState) Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);
                for (int i6 = 0; i6 <= i; i6++) {
                    BlockState block = getBlock(worldGenLevel, 1, -1, i6, boundingBox);
                    if (!block.isAir() && block.isSolidRender(worldGenLevel, new BlockPos(getWorldX(1, i6), getWorldY(-1), getWorldZ(1, i6)))) {
                        maybeGenerateBlock(worldGenLevel, boundingBox, random, isInterior(worldGenLevel, 1, 0, i6, boundingBox) ? 0.7f : 0.9f, 1, 0, i6, blockState);
                    }
                }
                return true;
            }
            return true;
        }

        private void placeSupport(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, Random random) {
            if (!isSupportingBox(worldGenLevel, boundingBox, i, i5, i4, i3)) {
                return;
            }
            BlockState planksBlock = getPlanksBlock();
            BlockState fenceBlock = getFenceBlock();
            generateBox(worldGenLevel, boundingBox, i, i2, i3, i, i4 - 1, i3, (BlockState) fenceBlock.setValue(FenceBlock.WEST, true), CAVE_AIR, false);
            generateBox(worldGenLevel, boundingBox, i5, i2, i3, i5, i4 - 1, i3, (BlockState) fenceBlock.setValue(FenceBlock.EAST, true), CAVE_AIR, false);
            if (random.nextInt(4) == 0) {
                generateBox(worldGenLevel, boundingBox, i, i4, i3, i, i4, i3, planksBlock, CAVE_AIR, false);
                generateBox(worldGenLevel, boundingBox, i5, i4, i3, i5, i4, i3, planksBlock, CAVE_AIR, false);
            } else {
                generateBox(worldGenLevel, boundingBox, i, i4, i3, i5, i4, i3, planksBlock, CAVE_AIR, false);
                maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.05f, i + 1, i4, i3 - 1, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH));
                maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.05f, i + 1, i4, i3 + 1, (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));
            }
        }

        private void placeCobWeb(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, float f, int i, int i2, int i3) {
            if (isInterior(worldGenLevel, i, i2, i3, boundingBox)) {
                maybeGenerateBlock(worldGenLevel, boundingBox, random, f, i, i2, i3, Blocks.COBWEB.defaultBlockState());
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/MineShaftPieces$MineShaftCrossing.class */
    public static class MineShaftCrossing extends MineShaftPiece {
        private final Direction direction;
        private final boolean isTwoFloored;

        public MineShaftCrossing(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, compoundTag);
            this.isTwoFloored = compoundTag.getBoolean("tf");
            this.direction = Direction.from2DDataValue(compoundTag.getInt("D"));
        }

        @Override // net.minecraft.world.level.levelgen.structure.MineShaftPieces.MineShaftPiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putBoolean("tf", this.isTwoFloored);
            compoundTag.putInt("D", this.direction.get2DDataValue());
        }

        public MineShaftCrossing(int i, BoundingBox boundingBox, @Nullable Direction direction, MineshaftFeature.Type type) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, i, type);
            this.direction = direction;
            this.boundingBox = boundingBox;
            this.isTwoFloored = boundingBox.getYSpan() > 3;
        }

        public static BoundingBox findCrossing(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction) {
            BoundingBox boundingBox = new BoundingBox(i, i2, i3, i, (i2 + 3) - 1, i3);
            if (random.nextInt(4) == 0) {
                boundingBox.y1 += 4;
            }
            switch (direction) {
                case NORTH:
                default:
                    boundingBox.x0 = i - 1;
                    boundingBox.x1 = i + 3;
                    boundingBox.z0 = i3 - 4;
                    break;
                case SOUTH:
                    boundingBox.x0 = i - 1;
                    boundingBox.x1 = i + 3;
                    boundingBox.z1 = i3 + 3 + 1;
                    break;
                case WEST:
                    boundingBox.x0 = i - 4;
                    boundingBox.z0 = i3 - 1;
                    boundingBox.z1 = i3 + 3;
                    break;
                case EAST:
                    boundingBox.x1 = i + 3 + 1;
                    boundingBox.z0 = i3 - 1;
                    boundingBox.z1 = i3 + 3;
                    break;
            }
            if (StructurePiece.findCollisionPiece(list, boundingBox) != null) {
                return null;
            }
            return boundingBox;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            int genDepth = getGenDepth();
            switch (this.direction) {
                case NORTH:
                default:
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, genDepth);
                    break;
                case SOUTH:
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, genDepth);
                    break;
                case WEST:
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, genDepth);
                    break;
                case EAST:
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, genDepth);
                    break;
            }
            if (this.isTwoFloored) {
                if (random.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                }
                if (random.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 + 1, Direction.WEST, genDepth);
                }
                if (random.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 + 1, Direction.EAST, genDepth);
                }
                if (random.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                }
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            BlockState planksBlock = getPlanksBlock();
            if (this.isTwoFloored) {
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0, this.boundingBox.x1 - 1, (this.boundingBox.y0 + 3) - 1, this.boundingBox.z1, CAVE_AIR, CAVE_AIR, false);
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.x1, (this.boundingBox.y0 + 3) - 1, this.boundingBox.z1 - 1, CAVE_AIR, CAVE_AIR, false);
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y1 - 2, this.boundingBox.z0, this.boundingBox.x1 - 1, this.boundingBox.y1, this.boundingBox.z1, CAVE_AIR, CAVE_AIR, false);
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0, this.boundingBox.y1 - 2, this.boundingBox.z0 + 1, this.boundingBox.x1, this.boundingBox.y1, this.boundingBox.z1 - 1, CAVE_AIR, CAVE_AIR, false);
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3, this.boundingBox.z0 + 1, this.boundingBox.x1 - 1, this.boundingBox.y0 + 3, this.boundingBox.z1 - 1, CAVE_AIR, CAVE_AIR, false);
            } else {
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0, this.boundingBox.x1 - 1, this.boundingBox.y1, this.boundingBox.z1, CAVE_AIR, CAVE_AIR, false);
                generateBox(worldGenLevel, boundingBox, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.x1, this.boundingBox.y1, this.boundingBox.z1 - 1, CAVE_AIR, CAVE_AIR, false);
            }
            placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.y1);
            placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 - 1, this.boundingBox.y1);
            placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.x1 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.y1);
            placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.x1 - 1, this.boundingBox.y0, this.boundingBox.z1 - 1, this.boundingBox.y1);
            for (int i = this.boundingBox.x0; i <= this.boundingBox.x1; i++) {
                for (int i2 = this.boundingBox.z0; i2 <= this.boundingBox.z1; i2++) {
                    if (getBlock(worldGenLevel, i, this.boundingBox.y0 - 1, i2, boundingBox).isAir() && isInterior(worldGenLevel, i, this.boundingBox.y0 - 1, i2, boundingBox)) {
                        placeBlock(worldGenLevel, planksBlock, i, this.boundingBox.y0 - 1, i2, boundingBox);
                    }
                }
            }
            return true;
        }

        private void placeSupportPillar(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4) {
            if (!getBlock(worldGenLevel, i, i4 + 1, i3, boundingBox).isAir()) {
                generateBox(worldGenLevel, boundingBox, i, i2, i3, i, i4, i3, getPlanksBlock(), CAVE_AIR, false);
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/MineShaftPieces$MineShaftStairs.class */
    public static class MineShaftStairs extends MineShaftPiece {
        public MineShaftStairs(int i, BoundingBox boundingBox, Direction direction, MineshaftFeature.Type type) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, i, type);
            setOrientation(direction);
            this.boundingBox = boundingBox;
        }

        public MineShaftStairs(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, compoundTag);
        }

        public static BoundingBox findStairs(List<StructurePiece> list, Random random, int i, int i2, int i3, Direction direction) {
            BoundingBox boundingBox = new BoundingBox(i, i2 - 5, i3, i, (i2 + 3) - 1, i3);
            switch (direction) {
                case NORTH:
                default:
                    boundingBox.x1 = (i + 3) - 1;
                    boundingBox.z0 = i3 - 8;
                    break;
                case SOUTH:
                    boundingBox.x1 = (i + 3) - 1;
                    boundingBox.z1 = i3 + 8;
                    break;
                case WEST:
                    boundingBox.x0 = i - 8;
                    boundingBox.z1 = (i3 + 3) - 1;
                    break;
                case EAST:
                    boundingBox.x1 = i + 8;
                    boundingBox.z1 = (i3 + 3) - 1;
                    break;
            }
            if (StructurePiece.findCollisionPiece(list, boundingBox) != null) {
                return null;
            }
            return boundingBox;
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
            int genDepth = getGenDepth();
            Direction orientation = getOrientation();
            if (orientation != null) {
                switch (orientation) {
                    case NORTH:
                    default:
                        MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, genDepth);
                        break;
                    case SOUTH:
                        MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, genDepth);
                        break;
                    case WEST:
                        MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0, Direction.WEST, genDepth);
                        break;
                    case EAST:
                        MineShaftPieces.generateAndAddPiece(structurePiece, list, random, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0, Direction.EAST, genDepth);
                        break;
                }
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            generateBox(worldGenLevel, boundingBox, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
            generateBox(worldGenLevel, boundingBox, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);
            int i = 0;
            while (i < 5) {
                generateBox(worldGenLevel, boundingBox, 0, (5 - i) - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
                i++;
            }
            return true;
        }
    }
}
