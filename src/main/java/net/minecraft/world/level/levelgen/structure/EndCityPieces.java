package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/EndCityPieces.class */
public class EndCityPieces {
    private static final StructurePlaceSettings OVERWRITE = new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    private static final StructurePlaceSettings INSERT = new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
    private static final SectionGenerator HOUSE_TOWER_GENERATOR = new SectionGenerator() { // from class: net.minecraft.world.level.levelgen.structure.EndCityPieces.1
        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public void init() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public boolean generate(StructureManager structureManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, Random random) {
            if (i > 8) {
                return false;
            }
            Rotation rotation = endCityPiece.placeSettings.getRotation();
            EndCityPiece addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, endCityPiece, blockPos, "base_floor", rotation, true));
            int nextInt = random.nextInt(3);
            if (nextInt == 0) {
                EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(-1, 4, -1), "base_roof", rotation, true));
                return true;
            }
            if (nextInt == 1) {
                EndCityPieces.recursiveChildren(structureManager, EndCityPieces.TOWER_GENERATOR, i + 1, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false)), new BlockPos(-1, 8, -1), "second_roof", rotation, false)), null, list, random);
                return true;
            }
            if (nextInt == 2) {
                EndCityPieces.recursiveChildren(structureManager, EndCityPieces.TOWER_GENERATOR, i + 1, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false)), new BlockPos(-1, 4, -1), "third_floor_2", rotation, false)), new BlockPos(-1, 8, -1), "third_roof", rotation, true)), null, list, random);
                return true;
            }
            return true;
        }
    };
    private static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(new Tuple[]{new Tuple(Rotation.NONE, new BlockPos(1, -1, 0)), new Tuple(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Tuple(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Tuple(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6))});
    private static final SectionGenerator TOWER_GENERATOR = new SectionGenerator() { // from class: net.minecraft.world.level.levelgen.structure.EndCityPieces.2
        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public void init() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public boolean generate(StructureManager structureManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, Random random) {
            Rotation rotation = endCityPiece.placeSettings.getRotation();
            EndCityPiece addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, endCityPiece, new BlockPos(3 + random.nextInt(2), -3, 3 + random.nextInt(2)), "tower_base", rotation, true)), new BlockPos(0, 7, 0), "tower_piece", rotation, true));
            EndCityPiece endCityPiece2 = random.nextInt(3) == 0 ? addHelper : null;
            int nextInt = 1 + random.nextInt(3);
            for (int i2 = 0; i2 < nextInt; i2++) {
                addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(0, 4, 0), "tower_piece", rotation, true));
                if (i2 < nextInt - 1 && random.nextBoolean()) {
                    endCityPiece2 = addHelper;
                }
            }
            if (endCityPiece2 != null) {
                for (Tuple<Rotation, BlockPos> tuple : EndCityPieces.TOWER_BRIDGES) {
                    if (random.nextBoolean()) {
                        EndCityPieces.recursiveChildren(structureManager, EndCityPieces.TOWER_BRIDGE_GENERATOR, i + 1, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, endCityPiece2, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true)), null, list, random);
                    }
                }
                EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
                return true;
            }
            if (i == 7) {
                EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
                return true;
            }
            return EndCityPieces.recursiveChildren(structureManager, EndCityPieces.FAT_TOWER_GENERATOR, i + 1, addHelper, null, list, random);
        }
    };
    private static final SectionGenerator TOWER_BRIDGE_GENERATOR = new SectionGenerator() { // from class: net.minecraft.world.level.levelgen.structure.EndCityPieces.3
        public boolean shipCreated;

        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public void init() {
            this.shipCreated = false;
        }

        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public boolean generate(StructureManager structureManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, Random random) {
            int i2;
            Rotation rotation = endCityPiece.placeSettings.getRotation();
            int nextInt = random.nextInt(4) + 1;
            EndCityPiece addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, endCityPiece, new BlockPos(0, 0, -4), "bridge_piece", rotation, true));
            addHelper.genDepth = -1;
            int i3 = 0;
            for (int i4 = 0; i4 < nextInt; i4++) {
                if (random.nextBoolean()) {
                    addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(0, i3, -4), "bridge_piece", rotation, true));
                    i2 = 0;
                } else {
                    addHelper = random.nextBoolean() ? EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(0, i3, -4), "bridge_steep_stairs", rotation, true)) : EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(0, i3, -8), "bridge_gentle_stairs", rotation, true));
                    i2 = 4;
                }
                i3 = i2;
            }
            if (this.shipCreated || random.nextInt(10 - i) != 0) {
                if (!EndCityPieces.recursiveChildren(structureManager, EndCityPieces.HOUSE_TOWER_GENERATOR, i + 1, addHelper, new BlockPos(-3, i3 + 1, -11), list, random)) {
                    return false;
                }
            } else {
                EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos((-8) + random.nextInt(8), i3, (-70) + random.nextInt(10)), "ship", rotation, true));
                this.shipCreated = true;
            }
            EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(4, i3, 0), "bridge_end", rotation.getRotated(Rotation.CLOCKWISE_180), true)).genDepth = -1;
            return true;
        }
    };
    private static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(new Tuple[]{new Tuple(Rotation.NONE, new BlockPos(4, -1, 0)), new Tuple(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Tuple(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Tuple(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12))});
    private static final SectionGenerator FAT_TOWER_GENERATOR = new SectionGenerator() { // from class: net.minecraft.world.level.levelgen.structure.EndCityPieces.4
        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public void init() {
        }

        @Override // net.minecraft.world.level.levelgen.structure.EndCityPieces.SectionGenerator
        public boolean generate(StructureManager structureManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, Random random) {
            Rotation rotation = endCityPiece.placeSettings.getRotation();
            EndCityPiece addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, endCityPiece, new BlockPos(-3, 4, -3), "fat_tower_base", rotation, true)), new BlockPos(0, 4, 0), "fat_tower_middle", rotation, true));
            for (int i2 = 0; i2 < 2 && random.nextInt(3) != 0; i2++) {
                addHelper = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(0, 8, 0), "fat_tower_middle", rotation, true));
                for (Tuple<Rotation, BlockPos> tuple : EndCityPieces.FAT_TOWER_BRIDGES) {
                    if (random.nextBoolean()) {
                        EndCityPieces.recursiveChildren(structureManager, EndCityPieces.TOWER_BRIDGE_GENERATOR, i + 1, EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true)), null, list, random);
                    }
                }
            }
            EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureManager, addHelper, new BlockPos(-2, 8, -2), "fat_tower_top", rotation, true));
            return true;
        }
    };

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/EndCityPieces$SectionGenerator.class */
    interface SectionGenerator {
        void init();

        boolean generate(StructureManager structureManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, Random random);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static EndCityPiece addPiece(StructureManager structureManager, EndCityPiece endCityPiece, BlockPos blockPos, String str, Rotation rotation, boolean z) {
        EndCityPiece endCityPiece2 = new EndCityPiece(structureManager, str, endCityPiece.templatePosition, rotation, z);
        BlockPos calculateConnectedPosition = endCityPiece.template.calculateConnectedPosition(endCityPiece.placeSettings, blockPos, endCityPiece2.placeSettings, BlockPos.ZERO);
        endCityPiece2.move(calculateConnectedPosition.getX(), calculateConnectedPosition.getY(), calculateConnectedPosition.getZ());
        return endCityPiece2;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/EndCityPieces$EndCityPiece.class */
    public static class EndCityPiece extends TemplateStructurePiece {
        private final String templateName;
        private final Rotation rotation;
        private final boolean overwrite;

        public EndCityPiece(StructureManager structureManager, String str, BlockPos blockPos, Rotation rotation, boolean z) {
            super(StructurePieceType.END_CITY_PIECE, 0);
            this.templateName = str;
            this.templatePosition = blockPos;
            this.rotation = rotation;
            this.overwrite = z;
            loadTemplate(structureManager);
        }

        public EndCityPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.END_CITY_PIECE, compoundTag);
            this.templateName = compoundTag.getString("Template");
            this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            this.overwrite = compoundTag.getBoolean("OW");
            loadTemplate(structureManager);
        }

        private void loadTemplate(StructureManager structureManager) {
            setup(structureManager.getOrCreate(new ResourceLocation("end_city/" + this.templateName)), this.templatePosition, (this.overwrite ? EndCityPieces.OVERWRITE : EndCityPieces.INSERT).copy().setRotation(this.rotation));
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString("Template", this.templateName);
            compoundTag.putString("Rot", this.rotation.name());
            compoundTag.putBoolean("OW", this.overwrite);
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
        protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
            if (str.startsWith("Chest")) {
                BlockPos below = blockPos.below();
                if (boundingBox.isInside(below)) {
                    RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, below, BuiltInLootTables.END_CITY_TREASURE);
                    return;
                }
                return;
            }
            if (str.startsWith("Sentry")) {
                Shulker create = EntityType.SHULKER.create(serverLevelAccessor.getLevel());
                create.setPos(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d);
                create.setAttachPosition(blockPos);
                serverLevelAccessor.addFreshEntity(create);
                return;
            }
            if (str.startsWith("Elytra")) {
                ItemFrame itemFrame = new ItemFrame(serverLevelAccessor.getLevel(), blockPos, this.rotation.rotate(Direction.SOUTH));
                itemFrame.setItem(new ItemStack(Items.ELYTRA), false);
                serverLevelAccessor.addFreshEntity(itemFrame);
            }
        }
    }

    public static void startHouseTower(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, Random random) {
        FAT_TOWER_GENERATOR.init();
        HOUSE_TOWER_GENERATOR.init();
        TOWER_BRIDGE_GENERATOR.init();
        TOWER_GENERATOR.init();
        recursiveChildren(structureManager, TOWER_GENERATOR, 1, addHelper(list, addPiece(structureManager, addHelper(list, addPiece(structureManager, addHelper(list, addPiece(structureManager, addHelper(list, new EndCityPiece(structureManager, "base_floor", blockPos, rotation, true)), new BlockPos(-1, 0, -1), "second_floor_1", rotation, false)), new BlockPos(-1, 4, -1), "third_floor_1", rotation, false)), new BlockPos(-1, 8, -1), "third_roof", rotation, true)), null, list, random);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static EndCityPiece addHelper(List<StructurePiece> list, EndCityPiece endCityPiece) {
        list.add(endCityPiece);
        return endCityPiece;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean recursiveChildren(StructureManager structureManager, SectionGenerator sectionGenerator, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, Random random) {
        if (i > 8) {
            return false;
        }
        List<StructurePiece> newArrayList = Lists.newArrayList();
        if (sectionGenerator.generate(structureManager, i, endCityPiece, blockPos, newArrayList, random)) {
            boolean z = false;
            int nextInt = random.nextInt();
            Iterator<StructurePiece> it = newArrayList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                StructurePiece next = it.next();
                next.genDepth = nextInt;
                StructurePiece findCollisionPiece = StructurePiece.findCollisionPiece(list, next.getBoundingBox());
                if (findCollisionPiece != null && findCollisionPiece.genDepth != endCityPiece.genDepth) {
                    z = true;
                    break;
                }
            }
            if (!z) {
                list.addAll(newArrayList);
                return true;
            }
            return false;
        }
        return false;
    }
}
