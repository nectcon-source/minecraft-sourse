package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/JigsawPlacement.class */
public class JigsawPlacement {
    private static final Logger LOGGER = LogManager.getLogger();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/JigsawPlacement$PieceFactory.class */
    public interface PieceFactory {
        PoolElementStructurePiece create(StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/JigsawPlacement$PieceState.class */
    static final class PieceState {
        private final PoolElementStructurePiece piece;
        private final MutableObject<VoxelShape> free;
        private final int boundsTop;
        private final int depth;

        private PieceState(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, int i2) {
            this.piece = poolElementStructurePiece;
            this.free = mutableObject;
            this.boundsTop = i;
            this.depth = i2;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/JigsawPlacement$Placer.class */
    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        private final Deque<PieceState> placing;

        private Placer(Registry<StructureTemplatePool> registry, int i, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> list, Random random) {
            this.placing = Queues.newArrayDeque();
            this.pools = registry;
            this.maxDepth = i;
            this.factory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.pieces = list;
            this.random = random;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, int i2, boolean z) {
            MutableObject<VoxelShape> mutableObject2;
            int i3;
            StructurePoolElement next;
            int i4;
            int i5;
            int groundLevelDelta;
            int i6;
            StructurePoolElement element = poolElementStructurePiece.getElement();
            BlockPos position = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = element.getProjection();
            boolean z2 = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject3 = new MutableObject<>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int i7 = boundingBox.y0;
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : element.getShuffledJigsawBlocks(this.structureManager, position, rotation, this.random)) {
                Direction frontFacing = JigsawBlock.getFrontFacing(structureBlockInfo.state);
                BlockPos blockPos = structureBlockInfo.pos;
                BlockPos relative = blockPos.relative(frontFacing);
                int y = blockPos.getY() - i7;
                int i8 = -1;
                ResourceLocation resourceLocation = new ResourceLocation(structureBlockInfo.nbt.getString("pool"));
                Optional<StructureTemplatePool> optional = this.pools.getOptional(resourceLocation);
                if (!optional.isPresent() || (optional.get().size() == 0 && !Objects.equals(resourceLocation, Pools.EMPTY.location()))) {
                    JigsawPlacement.LOGGER.warn("Empty or none existent pool: {}", resourceLocation);
                } else {
                    ResourceLocation fallback = optional.get().getFallback();
                    Optional<StructureTemplatePool> optional2 = this.pools.getOptional(fallback);
                    if (!optional2.isPresent() || (optional2.get().size() == 0 && !Objects.equals(fallback, Pools.EMPTY.location()))) {
                        JigsawPlacement.LOGGER.warn("Empty or none existent fallback pool: {}", fallback);
                    } else {
                        if (boundingBox.isInside(relative)) {
                            mutableObject2 = mutableObject3;
                            i3 = i7;
                            if (mutableObject3.getValue() == null) {
                                mutableObject3.setValue(Shapes.create(AABB.of(boundingBox)));
                            }
                        } else {
                            mutableObject2 = mutableObject;
                            i3 = i;
                        }
                        List<StructurePoolElement> newArrayList = Lists.newArrayList();
                        if (i2 != this.maxDepth) {
                            newArrayList.addAll(optional.get().getShuffledTemplates(this.random));
                        }
                        newArrayList.addAll(optional2.get().getShuffledTemplates(this.random));
                        Iterator<StructurePoolElement> it = newArrayList.iterator();
                        while (true) {
                            if (it.hasNext() && (next = it.next()) != EmptyPoolElement.INSTANCE) {
                                for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
                                    List<StructureTemplate.StructureBlockInfo> shuffledJigsawBlocks = next.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation2, this.random);
                                    BoundingBox boundingBox2 = next.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation2);
                                    if (!z || boundingBox2.getYSpan() > 16) {
                                        i4 = 0;
                                    } else {
                                        i4 = shuffledJigsawBlocks.stream().mapToInt(structureBlockInfo2 -> {
                                            if (!boundingBox2.isInside(structureBlockInfo2.pos.relative(JigsawBlock.getFrontFacing(structureBlockInfo2.state)))) {
                                                return 0;
                                            }
                                            Optional<StructureTemplatePool> optional3 = this.pools.getOptional(new ResourceLocation(structureBlockInfo2.nbt.getString("pool")));
                                            return Math.max(((Integer) optional3.map(structureTemplatePool -> {
                                                return Integer.valueOf(structureTemplatePool.getMaxSize(this.structureManager));
                                            }).orElse(0)).intValue(), ((Integer) optional3.flatMap(structureTemplatePool2 -> {
                                                return this.pools.getOptional(structureTemplatePool2.getFallback());
                                            }).map(structureTemplatePool3 -> {
                                                return Integer.valueOf(structureTemplatePool3.getMaxSize(this.structureManager));
                                            }).orElse(0)).intValue());
                                        }).max().orElse(0);
                                    }
                                    for (StructureTemplate.StructureBlockInfo structureBlockInfo3 : shuffledJigsawBlocks) {
                                        if (JigsawBlock.canAttach(structureBlockInfo, structureBlockInfo3)) {
                                            BlockPos blockPos2 = structureBlockInfo3.pos;
                                            BlockPos blockPos3 = new BlockPos(relative.getX() - blockPos2.getX(), relative.getY() - blockPos2.getY(), relative.getZ() - blockPos2.getZ());
                                            BoundingBox boundingBox3 = next.getBoundingBox(this.structureManager, blockPos3, rotation2);
                                            int i9 = boundingBox3.y0;
                                            StructureTemplatePool.Projection projection2 = next.getProjection();
                                            boolean z3 = projection2 == StructureTemplatePool.Projection.RIGID;
                                            int y2 = blockPos2.getY();
                                            int stepY = (y - y2) + JigsawBlock.getFrontFacing(structureBlockInfo.state).getStepY();
                                            if (z2 && z3) {
                                                i5 = i7 + stepY;
                                            } else {
                                                if (i8 == -1) {
                                                    i8 = this.chunkGenerator.getFirstFreeHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG);
                                                }
                                                i5 = i8 - y2;
                                            }
                                            int i10 = i5 - i9;
                                            BoundingBox moved = boundingBox3.moved(0, i10, 0);
                                            BlockPos offset = blockPos3.offset(0, i10, 0);
                                            if (i4 > 0) {
                                                moved.y1 = moved.y0 + Math.max(i4 + 1, moved.y1 - moved.y0);
                                            }
                                            if (!Shapes.joinIsNotEmpty((VoxelShape) mutableObject2.getValue(), Shapes.create(AABB.of(moved).deflate(0.25d)), BooleanOp.ONLY_SECOND)) {
                                                mutableObject2.setValue(Shapes.joinUnoptimized((VoxelShape) mutableObject2.getValue(), Shapes.create(AABB.of(moved)), BooleanOp.ONLY_FIRST));
                                                int groundLevelDelta2 = poolElementStructurePiece.getGroundLevelDelta();
                                                if (z3) {
                                                    groundLevelDelta = groundLevelDelta2 - stepY;
                                                } else {
                                                    groundLevelDelta = next.getGroundLevelDelta();
                                                }
                                                PoolElementStructurePiece create = this.factory.create(this.structureManager, next, offset, groundLevelDelta, rotation2, moved);
                                                if (z2) {
                                                    i6 = i7 + y;
                                                } else if (z3) {
                                                    i6 = i5 + y2;
                                                } else {
                                                    if (i8 == -1) {
                                                        i8 = this.chunkGenerator.getFirstFreeHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG);
                                                    }
                                                    i6 = i8 + (stepY / 2);
                                                }
                                                poolElementStructurePiece.addJunction(new JigsawJunction(relative.getX(), (i6 - y) + groundLevelDelta2, relative.getZ(), stepY, projection2));
                                                create.addJunction(new JigsawJunction(blockPos.getX(), (i6 - y2) + groundLevelDelta, blockPos.getZ(), -stepY, projection));
                                                this.pieces.add(create);
                                                if (i2 + 1 <= this.maxDepth) {
                                                    this.placing.addLast(new PieceState(create, mutableObject2, i3, i2 + 1));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void addPieces(RegistryAccess registryAccess, JigsawConfiguration jigsawConfiguration, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos blockPos, List<? super PoolElementStructurePiece> list, Random random, boolean z, boolean z2) {
        int y;
        StructureFeature.bootstrap();
        WritableRegistry<StructureTemplatePool> registryOrThrow = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation random2 = Rotation.getRandom(random);
        StructurePoolElement randomTemplate = jigsawConfiguration.startPool().get().getRandomTemplate(random);
        PoolElementStructurePiece create = pieceFactory.create(structureManager, randomTemplate, blockPos, randomTemplate.getGroundLevelDelta(), random2, randomTemplate.getBoundingBox(structureManager, blockPos, random2));
        BoundingBox boundingBox = create.getBoundingBox();
        int i = (boundingBox.x1 + boundingBox.x0) / 2;
        int i2 = (boundingBox.z1 + boundingBox.z0) / 2;
        if (z2) {
            y = blockPos.getY() + chunkGenerator.getFirstFreeHeight(i, i2, Heightmap.Types.WORLD_SURFACE_WG);
        } else {
            y = blockPos.getY();
        }
        create.move(0, y - (boundingBox.y0 + create.getGroundLevelDelta()), 0);
        list.add(create);
        if (jigsawConfiguration.maxDepth() <= 0) {
            return;
        }
        AABB aabb = new AABB(i - 80, y - 80, i2 - 80, i + 80 + 1, y + 80 + 1, i2 + 80 + 1);
        Placer placer = new Placer(registryOrThrow, jigsawConfiguration.maxDepth(), pieceFactory, chunkGenerator, structureManager, list, random);
        placer.placing.addLast(new PieceState(create, new MutableObject(Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST)), y + 80, 0));
        while (!placer.placing.isEmpty()) {
            PieceState pieceState = (PieceState) placer.placing.removeFirst();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.boundsTop, pieceState.depth, z);
        }
    }

    public static void addPieces(RegistryAccess registryAccess, PoolElementStructurePiece poolElementStructurePiece, int i, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> list, Random random) {
        Placer placer = new Placer(registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY), i, pieceFactory, chunkGenerator, structureManager, list, random);
        placer.placing.addLast(new PieceState(poolElementStructurePiece, new MutableObject(Shapes.INFINITY), 0, 0));
        while (!placer.placing.isEmpty()) {
            PieceState pieceState = (PieceState) placer.placing.removeFirst();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.boundsTop, pieceState.depth, false);
        }
    }
}
