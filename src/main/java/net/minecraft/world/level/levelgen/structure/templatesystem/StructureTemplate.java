package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate.class */
public class StructureTemplate {
    private final List<Palette> palettes = Lists.newArrayList();
    private final List<StructureEntityInfo> entityInfoList = Lists.newArrayList();
    private BlockPos size = BlockPos.ZERO;
    private String author = "?";

    public BlockPos getSize() {
        return this.size;
    }

    public void setAuthor(String str) {
        this.author = str;
    }

    public String getAuthor() {
        return this.author;
    }

    public void fillFromWorld(Level level, BlockPos blockPos, BlockPos blockPos2, boolean z, @Nullable Block block) {
        StructureBlockInfo structureBlockInfo;
        if (blockPos2.getX() < 1 || blockPos2.getY() < 1 || blockPos2.getZ() < 1) {
            return;
        }
        BlockPos offset = blockPos.offset(blockPos2).offset(-1, -1, -1);
        List<StructureBlockInfo> newArrayList = Lists.newArrayList();
        List<StructureBlockInfo> newArrayList2 = Lists.newArrayList();
        List<StructureBlockInfo> newArrayList3 = Lists.newArrayList();
        BlockPos blockPos3 = new BlockPos(Math.min(blockPos.getX(), offset.getX()), Math.min(blockPos.getY(), offset.getY()), Math.min(blockPos.getZ(), offset.getZ()));
        BlockPos blockPos4 = new BlockPos(Math.max(blockPos.getX(), offset.getX()), Math.max(blockPos.getY(), offset.getY()), Math.max(blockPos.getZ(), offset.getZ()));
        this.size = blockPos2;
        for (BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
            BlockPos subtract = blockPos5.subtract(blockPos3);
            BlockState blockState = level.getBlockState(blockPos5);
            if (block == null || block != blockState.getBlock()) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos5);
                if (blockEntity != null) {
                    CompoundTag save = blockEntity.save(new CompoundTag());
                    save.remove("x");
                    save.remove("y");
                    save.remove("z");
                    structureBlockInfo = new StructureBlockInfo(subtract, blockState, save.copy());
                } else {
                    structureBlockInfo = new StructureBlockInfo(subtract, blockState, null);
                }
                addToLists(structureBlockInfo, newArrayList, newArrayList2, newArrayList3);
            }
        }
        List<StructureBlockInfo> buildInfoList = buildInfoList(newArrayList, newArrayList2, newArrayList3);
        this.palettes.clear();
        this.palettes.add(new Palette(buildInfoList));
        if (z) {
            fillEntityList(level, blockPos3, blockPos4.offset(1, 1, 1));
        } else {
            this.entityInfoList.clear();
        }
    }

    private static void addToLists(StructureBlockInfo structureBlockInfo, List<StructureBlockInfo> list, List<StructureBlockInfo> list2, List<StructureBlockInfo> list3) {
        if (structureBlockInfo.nbt != null) {
            list2.add(structureBlockInfo);
        } else if (!structureBlockInfo.state.getBlock().hasDynamicShape() && structureBlockInfo.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            list.add(structureBlockInfo);
        } else {
            list3.add(structureBlockInfo);
        }
    }

    private static List<StructureBlockInfo> buildInfoList(List<StructureBlockInfo> list, List<StructureBlockInfo> list2, List<StructureBlockInfo> list3) {
        Comparator<StructureBlockInfo> var3 = Comparator.<StructureTemplate.StructureBlockInfo>comparingInt((var0x) -> var0x.pos.getY()).thenComparingInt((var0x) -> var0x.pos.getX()).thenComparingInt((var0x) -> var0x.pos.getZ());
        list.sort(var3);
        list3.sort(var3);
        list2.sort(var3);
        List<StructureBlockInfo> var4 = Lists.newArrayList();
        var4.addAll(list);
        var4.addAll(list3);
        var4.addAll(list2);
        return var4;
    }

    private void fillEntityList(Level level, BlockPos blockPos, BlockPos blockPos2) {
        BlockPos blockPos3;
        List<Entity> entitiesOfClass = level.getEntitiesOfClass(Entity.class, new AABB(blockPos, blockPos2), entity -> {
            return !(entity instanceof Player);
        });
        this.entityInfoList.clear();
        for (Entity entity2 : entitiesOfClass) {
            Vec3 vec3 = new Vec3(entity2.getX() - blockPos.getX(), entity2.getY() - blockPos.getY(), entity2.getZ() - blockPos.getZ());
            CompoundTag compoundTag = new CompoundTag();
            entity2.save(compoundTag);
            if (entity2 instanceof Painting) {
                blockPos3 = ((Painting) entity2).getPos().subtract(blockPos);
            } else {
                blockPos3 = new BlockPos(vec3);
            }
            this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos3, compoundTag.copy()));
        }
    }

    public List<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block) {
        return filterBlocks(blockPos, structurePlaceSettings, block, true);
    }

    public List<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block, boolean z) {
        List<StructureBlockInfo> newArrayList = Lists.newArrayList();
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        if (this.palettes.isEmpty()) {
            return Collections.emptyList();
        }
        for (StructureBlockInfo structureBlockInfo : structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks(block)) {
            BlockPos offset = z ? calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos) : structureBlockInfo.pos;
            if (boundingBox == null || boundingBox.isInside(offset)) {
                newArrayList.add(new StructureBlockInfo(offset, structureBlockInfo.state.rotate(structurePlaceSettings.getRotation()), structureBlockInfo.nbt));
            }
        }
        return newArrayList;
    }

    public BlockPos calculateConnectedPosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings2, BlockPos blockPos2) {
        return calculateRelativePosition(structurePlaceSettings, blockPos).subtract(calculateRelativePosition(structurePlaceSettings2, blockPos2));
    }

    public static BlockPos calculateRelativePosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        return transform(blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot());
    }

    public void placeInWorldChunk(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Random random) {
        structurePlaceSettings.updateBoundingBoxFromChunkPos();
        placeInWorld(serverLevelAccessor, blockPos, structurePlaceSettings, random);
    }

    public void placeInWorld(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Random random) {
        placeInWorld(serverLevelAccessor, blockPos, blockPos, structurePlaceSettings, random, 2);
    }

    public boolean placeInWorld(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, Random random, int i) {
        BlockEntity blockEntity;
        BlockEntity blockEntity2;
        if (this.palettes.isEmpty()) {
            return false;
        }
        List<StructureBlockInfo> blocks = structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks();
        if ((blocks.isEmpty() && (structurePlaceSettings.isIgnoreEntities() || this.entityInfoList.isEmpty())) || this.size.getX() < 1 || this.size.getY() < 1 || this.size.getZ() < 1) {
            return false;
        }
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        List<BlockPos> newArrayListWithCapacity = Lists.newArrayListWithCapacity(structurePlaceSettings.shouldKeepLiquids() ? blocks.size() : 0);
        List<Pair<BlockPos, CompoundTag>> newArrayListWithCapacity2 = Lists.newArrayListWithCapacity(blocks.size());
        int i2 = Integer.MAX_VALUE;
        int i3 = Integer.MAX_VALUE;
        int i4 = Integer.MAX_VALUE;
        int i5 = Integer.MIN_VALUE;
        int i6 = Integer.MIN_VALUE;
        int i7 = Integer.MIN_VALUE;
        for (StructureBlockInfo structureBlockInfo : processBlockInfos(serverLevelAccessor, blockPos, blockPos2, structurePlaceSettings, blocks)) {
            BlockPos blockPos3 = structureBlockInfo.pos;
            if (boundingBox == null || boundingBox.isInside(blockPos3)) {
                FluidState fluidState = structurePlaceSettings.shouldKeepLiquids() ? serverLevelAccessor.getFluidState(blockPos3) : null;
                BlockState rotate = structureBlockInfo.state.mirror(structurePlaceSettings.getMirror()).rotate(structurePlaceSettings.getRotation());
                if (structureBlockInfo.nbt != null) {
                    Clearable.tryClear(serverLevelAccessor.getBlockEntity(blockPos3));
                    serverLevelAccessor.setBlock(blockPos3, Blocks.BARRIER.defaultBlockState(), 20);
                }
                if (serverLevelAccessor.setBlock(blockPos3, rotate, i)) {
                    i2 = Math.min(i2, blockPos3.getX());
                    i3 = Math.min(i3, blockPos3.getY());
                    i4 = Math.min(i4, blockPos3.getZ());
                    i5 = Math.max(i5, blockPos3.getX());
                    i6 = Math.max(i6, blockPos3.getY());
                    i7 = Math.max(i7, blockPos3.getZ());
                    newArrayListWithCapacity2.add(Pair.of(blockPos3, structureBlockInfo.nbt));
                    if (structureBlockInfo.nbt != null && (blockEntity2 = serverLevelAccessor.getBlockEntity(blockPos3)) != null) {
                        structureBlockInfo.nbt.putInt("x", blockPos3.getX());
                        structureBlockInfo.nbt.putInt("y", blockPos3.getY());
                        structureBlockInfo.nbt.putInt("z", blockPos3.getZ());
                        if (blockEntity2 instanceof RandomizableContainerBlockEntity) {
                            structureBlockInfo.nbt.putLong("LootTableSeed", random.nextLong());
                        }
                        blockEntity2.load(structureBlockInfo.state, structureBlockInfo.nbt);
                        blockEntity2.mirror(structurePlaceSettings.getMirror());
                        blockEntity2.rotate(structurePlaceSettings.getRotation());
                    }
                    if (fluidState != null && (rotate.getBlock() instanceof LiquidBlockContainer)) {
                        ((LiquidBlockContainer) rotate.getBlock()).placeLiquid(serverLevelAccessor, blockPos3, rotate, fluidState);
                        if (!fluidState.isSource()) {
                            newArrayListWithCapacity.add(blockPos3);
                        }
                    }
                }
            }
        }
        boolean z = true;
        Direction[] directionArr = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        while (z && !newArrayListWithCapacity.isEmpty()) {
            z = false;
            Iterator<BlockPos> it = newArrayListWithCapacity.iterator();
            while (it.hasNext()) {
                BlockPos next = it.next();
                BlockPos blockPos4 = next;
                FluidState fluidState2 = serverLevelAccessor.getFluidState(blockPos4);
                for (int i8 = 0; i8 < directionArr.length && !fluidState2.isSource(); i8++) {
                    BlockPos relative = blockPos4.relative(directionArr[i8]);
                    FluidState fluidState3 = serverLevelAccessor.getFluidState(relative);
                    if (fluidState3.getHeight(serverLevelAccessor, relative) > fluidState2.getHeight(serverLevelAccessor, blockPos4) || (fluidState3.isSource() && !fluidState2.isSource())) {
                        fluidState2 = fluidState3;
                        blockPos4 = relative;
                    }
                }
                if (fluidState2.isSource()) {
                    BlockState blockState = serverLevelAccessor.getBlockState(next);
                    ItemLike block = blockState.getBlock();
                    if (block instanceof LiquidBlockContainer) {
                        ((LiquidBlockContainer) block).placeLiquid(serverLevelAccessor, next, blockState, fluidState2);
                        z = true;
                        it.remove();
                    }
                }
            }
        }
        if (i2 <= i5) {
            if (!structurePlaceSettings.getKnownShape()) {
                DiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape((i5 - i2) + 1, (i6 - i3) + 1, (i7 - i4) + 1);
                int i9 = i2;
                int i10 = i3;
                int i11 = i4;
                Iterator<Pair<BlockPos, CompoundTag>> it2 = newArrayListWithCapacity2.iterator();
                while (it2.hasNext()) {
                    BlockPos blockPos5 = (BlockPos) it2.next().getFirst();
                    bitSetDiscreteVoxelShape.setFull(blockPos5.getX() - i9, blockPos5.getY() - i10, blockPos5.getZ() - i11, true, true);
                }
                updateShapeAtEdge(serverLevelAccessor, i, bitSetDiscreteVoxelShape, i9, i10, i11);
            }
            for (Pair<BlockPos, CompoundTag> pair : newArrayListWithCapacity2) {
                BlockPos blockPos6 = (BlockPos) pair.getFirst();
                if (!structurePlaceSettings.getKnownShape()) {
                    BlockState blockState2 = serverLevelAccessor.getBlockState(blockPos6);
                    BlockState updateFromNeighbourShapes = Block.updateFromNeighbourShapes(blockState2, serverLevelAccessor, blockPos6);
                    if (blockState2 != updateFromNeighbourShapes) {
                        serverLevelAccessor.setBlock(blockPos6, updateFromNeighbourShapes, (i & (-2)) | 16);
                    }
                    serverLevelAccessor.blockUpdated(blockPos6, updateFromNeighbourShapes.getBlock());
                }
                if (pair.getSecond() != null && (blockEntity = serverLevelAccessor.getBlockEntity(blockPos6)) != null) {
                    blockEntity.setChanged();
                }
            }
        }
        if (!structurePlaceSettings.isIgnoreEntities()) {
            placeEntities(serverLevelAccessor, blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), boundingBox, structurePlaceSettings.shouldFinalizeEntities());
            return true;
        }
        return true;
    }

    public static void updateShapeAtEdge(LevelAccessor levelAccessor, int i, DiscreteVoxelShape discreteVoxelShape, int i2, int i3, int i4) {
        discreteVoxelShape.forAllFaces((direction, i5, i6, i7) -> {
            BlockPos blockPos = new BlockPos(i2 + i5, i3 + i6, i4 + i7);
            BlockPos relative = blockPos.relative(direction);
            BlockState blockState = levelAccessor.getBlockState(blockPos);
            BlockState blockState2 = levelAccessor.getBlockState(relative);
            BlockState updateShape = blockState.updateShape(direction, blockState2, levelAccessor, blockPos, relative);
            if (blockState != updateShape) {
                levelAccessor.setBlock(blockPos, updateShape, i & (-2));
            }
            BlockState updateShape2 = blockState2.updateShape(direction.getOpposite(), updateShape, levelAccessor, relative, blockPos);
            if (blockState2 != updateShape2) {
                levelAccessor.setBlock(relative, updateShape2, i & (-2));
            }
        });
    }

    public static List<StructureBlockInfo> processBlockInfos(LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, List<StructureBlockInfo> list) {
        List<StructureBlockInfo> newArrayList = Lists.newArrayList();
        for (StructureBlockInfo structureBlockInfo : list) {
            StructureBlockInfo structureBlockInfo2 = new StructureBlockInfo(calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos), structureBlockInfo.state, structureBlockInfo.nbt != null ? structureBlockInfo.nbt.copy() : null);
            Iterator<StructureProcessor> it = structurePlaceSettings.getProcessors().iterator();
            while (structureBlockInfo2 != null && it.hasNext()) {
                structureBlockInfo2 = it.next().processBlock(levelAccessor, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlaceSettings);
            }
            if (structureBlockInfo2 != null) {
                newArrayList.add(structureBlockInfo2);
            }
        }
        return newArrayList;
    }

    private void placeEntities(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2, @Nullable BoundingBox boundingBox, boolean z) {
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            BlockPos offset = transform(structureEntityInfo.blockPos, mirror, rotation, blockPos2).offset(blockPos);
            if (boundingBox == null || boundingBox.isInside(offset)) {
                CompoundTag copy = structureEntityInfo.nbt.copy();
                Vec3 add = transform(structureEntityInfo.pos, mirror, rotation, blockPos2).add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                ListTag listTag = new ListTag();
                listTag.add(DoubleTag.valueOf(add.x));
                listTag.add(DoubleTag.valueOf(add.y));
                listTag.add(DoubleTag.valueOf(add.z));
                copy.put("Pos", listTag);
                copy.remove("UUID");
                createEntityIgnoreException(serverLevelAccessor, copy).ifPresent(entity -> {
                    entity.moveTo(add.x, add.y, add.z, entity.mirror(mirror) + (entity.yRot - entity.rotate(rotation)), entity.xRot);
                    if (z && (entity instanceof Mob)) {
                        ((Mob) entity).finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(new BlockPos(add)), MobSpawnType.STRUCTURE, null, copy);
                    }
                    serverLevelAccessor.addFreshEntityWithPassengers(entity);
                });
            }
        }
    }

    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor serverLevelAccessor, CompoundTag compoundTag) {
        try {
            return EntityType.create(compoundTag, serverLevelAccessor.getLevel());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public BlockPos getSize(Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
            default:
                return this.size;
        }
    }

    public static BlockPos transform(BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        boolean z2 = true;
        switch (mirror) {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
            default:
                z2 = false;
                break;
        }
        int x2 = blockPos2.getX();
        int z3 = blockPos2.getZ();
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new BlockPos((x2 - z3) + z, y, (x2 + z3) - x);
            case CLOCKWISE_90:
                return new BlockPos((x2 + z3) - z, y, (z3 - x2) + x);
            case CLOCKWISE_180:
                return new BlockPos((x2 + x2) - x, y, (z3 + z3) - z);
            default:
                return z2 ? new BlockPos(x, y, z) : blockPos;
        }
    }

    public static Vec3 transform(Vec3 vec3, Mirror mirror, Rotation rotation, BlockPos blockPos) {
        double d = vec3.x;
        double d2 = vec3.y;
        double d3 = vec3.z;
        boolean z = true;
        switch (mirror) {
            case LEFT_RIGHT:
                d3 = 1.0d - d3;
                break;
            case FRONT_BACK:
                d = 1.0d - d;
                break;
            default:
                z = false;
                break;
        }
        int x = blockPos.getX();
        int z2 = blockPos.getZ();
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new Vec3((x - z2) + d3, d2, ((x + z2) + 1) - d);
            case CLOCKWISE_90:
                return new Vec3(((x + z2) + 1) - d3, d2, (z2 - x) + d);
            case CLOCKWISE_180:
                return new Vec3(((x + x) + 1) - d, d2, ((z2 + z2) + 1) - d3);
            default:
                return z ? new Vec3(d, d2, d3) : vec3;
        }
    }

    public BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation) {
        return getZeroPositionWithTransform(blockPos, mirror, rotation, getSize().getX(), getSize().getZ());
    }

    public static BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation, int i, int i2) {
        int i3 = i - 1;
        int i4 = i2 - 1;
        int i5 = mirror == Mirror.FRONT_BACK ? i3 : 0;
        int i6 = mirror == Mirror.LEFT_RIGHT ? i4 : 0;
        BlockPos blockPos2 = blockPos;
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                blockPos2 = blockPos.offset(i6, 0, i3 - i5);
                break;
            case CLOCKWISE_90:
                blockPos2 = blockPos.offset(i4 - i6, 0, i5);
                break;
            case CLOCKWISE_180:
                blockPos2 = blockPos.offset(i3 - i5, 0, i4 - i6);
                break;
            case NONE:
                blockPos2 = blockPos.offset(i5, 0, i6);
                break;
        }
        return blockPos2;
    }

    public BoundingBox getBoundingBox(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        return getBoundingBox(blockPos, structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), structurePlaceSettings.getMirror());
    }

    public BoundingBox getBoundingBox(BlockPos blockPos, Rotation rotation, BlockPos blockPos2, Mirror mirror) {
        BlockPos size = getSize(rotation);
        int x = blockPos2.getX();
        int z = blockPos2.getZ();
        int x2 = size.getX() - 1;
        int y = size.getY() - 1;
        int z2 = size.getZ() - 1;
        BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                boundingBox = new BoundingBox(x - z, 0, (x + z) - z2, (x - z) + x2, y, x + z);
                break;
            case CLOCKWISE_90:
                boundingBox = new BoundingBox((x + z) - x2, 0, z - x, x + z, y, (z - x) + z2);
                break;
            case CLOCKWISE_180:
                boundingBox = new BoundingBox((x + x) - x2, 0, (z + z) - z2, x + x, y, z + z);
                break;
            case NONE:
                boundingBox = new BoundingBox(0, 0, 0, x2, y, z2);
                break;
        }
        switch (mirror) {
            case LEFT_RIGHT:
                mirrorAABB(rotation, z2, x2, boundingBox, Direction.NORTH, Direction.SOUTH);
                break;
            case FRONT_BACK:
                mirrorAABB(rotation, x2, z2, boundingBox, Direction.WEST, Direction.EAST);
                break;
        }
        boundingBox.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return boundingBox;
    }

    private void mirrorAABB(Rotation rotation, int i, int i2, BoundingBox boundingBox, Direction direction, Direction direction2) {
        BlockPos relative;
        BlockPos blockPos = BlockPos.ZERO;
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            relative = blockPos.relative(rotation.rotate(direction), i2);
        } else if (rotation == Rotation.CLOCKWISE_180) {
            relative = blockPos.relative(direction2, i);
        } else {
            relative = blockPos.relative(direction, i);
        }
        boundingBox.move(relative.getX(), 0, relative.getZ());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$SimplePalette.class */
    static class SimplePalette implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids;
        private int lastId;

        private SimplePalette() {
            this.ids = new IdMapper<>(16);
        }

        public int idFor(BlockState blockState) {
            int id = this.ids.getId(blockState);
            if (id == -1) {
                int i = this.lastId;
                this.lastId = i + 1;
                id = i;
                this.ids.addMapping(blockState, id);
            }
            return id;
        }

        @Nullable
        public BlockState stateFor(int i) {
            BlockState byId = this.ids.byId(i);
            return byId == null ? DEFAULT_BLOCK_STATE : byId;
        }

        @Override // java.lang.Iterable
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState blockState, int i) {
            this.ids.addMapping(blockState, i);
        }
    }

    public CompoundTag save(CompoundTag compoundTag) {
        if (this.palettes.isEmpty()) {
            compoundTag.put("blocks", new ListTag());
            compoundTag.put("palette", new ListTag());
        } else {
            List<SimplePalette> newArrayList = Lists.newArrayList();
            SimplePalette simplePalette = new SimplePalette();
            newArrayList.add(simplePalette);
            for (int i = 1; i < this.palettes.size(); i++) {
                newArrayList.add(new SimplePalette());
            }
            ListTag listTag = new ListTag();
            List<StructureBlockInfo> blocks = this.palettes.get(0).blocks();
            for (int i2 = 0; i2 < blocks.size(); i2++) {
                StructureBlockInfo structureBlockInfo = blocks.get(i2);
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.put("pos", newIntegerList(structureBlockInfo.pos.getX(), structureBlockInfo.pos.getY(), structureBlockInfo.pos.getZ()));
                int idFor = simplePalette.idFor(structureBlockInfo.state);
                compoundTag2.putInt("state", idFor);
                if (structureBlockInfo.nbt != null) {
                    compoundTag2.put("nbt", structureBlockInfo.nbt);
                }
                listTag.add(compoundTag2);
                for (int i3 = 1; i3 < this.palettes.size(); i3++) {
                    newArrayList.get(i3).addMapping(this.palettes.get(i3).blocks().get(i2).state, idFor);
                }
            }
            compoundTag.put("blocks", listTag);
            if (newArrayList.size() == 1) {
                ListTag listTag2 = new ListTag();
                Iterator<BlockState> it = simplePalette.iterator();
                while (it.hasNext()) {
                    listTag2.add(NbtUtils.writeBlockState(it.next()));
                }
                compoundTag.put("palette", listTag2);
            } else {
                ListTag listTag3 = new ListTag();
                for (SimplePalette simplePalette2 : newArrayList) {
                    ListTag listTag4 = new ListTag();
                    Iterator<BlockState> it2 = simplePalette2.iterator();
                    while (it2.hasNext()) {
                        listTag4.add(NbtUtils.writeBlockState(it2.next()));
                    }
                    listTag3.add(listTag4);
                }
                compoundTag.put("palettes", listTag3);
            }
        }
        ListTag listTag5 = new ListTag();
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.put("pos", newDoubleList(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
            compoundTag3.put("blockPos", newIntegerList(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
            if (structureEntityInfo.nbt != null) {
                compoundTag3.put("nbt", structureEntityInfo.nbt);
            }
            listTag5.add(compoundTag3);
        }
        compoundTag.put("entities", listTag5);
        compoundTag.put("size", newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        this.palettes.clear();
        this.entityInfoList.clear();
        ListTag list = compoundTag.getList("size", 3);
        this.size = new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2));
        ListTag list2 = compoundTag.getList("blocks", 10);
        if (compoundTag.contains("palettes", 9)) {
            ListTag list3 = compoundTag.getList("palettes", 9);
            for (int i = 0; i < list3.size(); i++) {
                loadPalette(list3.getList(i), list2);
            }
        } else {
            loadPalette(compoundTag.getList("palette", 10), list2);
        }
        ListTag list4 = compoundTag.getList("entities", 10);
        for (int i2 = 0; i2 < list4.size(); i2++) {
            CompoundTag compound = list4.getCompound(i2);
            ListTag list5 = compound.getList("pos", 6);
            Vec3 vec3 = new Vec3(list5.getDouble(0), list5.getDouble(1), list5.getDouble(2));
            ListTag list6 = compound.getList("blockPos", 3);
            BlockPos blockPos = new BlockPos(list6.getInt(0), list6.getInt(1), list6.getInt(2));
            if (compound.contains("nbt")) {
                this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos, compound.getCompound("nbt")));
            }
        }
    }

    private void loadPalette(ListTag listTag, ListTag listTag2) {
        CompoundTag compoundTag;
        SimplePalette simplePalette = new SimplePalette();
        for (int i = 0; i < listTag.size(); i++) {
            simplePalette.addMapping(NbtUtils.readBlockState(listTag.getCompound(i)), i);
        }
        List<StructureBlockInfo> newArrayList = Lists.newArrayList();
        List<StructureBlockInfo> newArrayList2 = Lists.newArrayList();
        List<StructureBlockInfo> newArrayList3 = Lists.newArrayList();
        for (int i2 = 0; i2 < listTag2.size(); i2++) {
            CompoundTag compound = listTag2.getCompound(i2);
            ListTag list = compound.getList("pos", 3);
            BlockPos blockPos = new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2));
            BlockState stateFor = simplePalette.stateFor(compound.getInt("state"));
            if (compound.contains("nbt")) {
                compoundTag = compound.getCompound("nbt");
            } else {
                compoundTag = null;
            }
            addToLists(new StructureBlockInfo(blockPos, stateFor, compoundTag), newArrayList, newArrayList2, newArrayList3);
        }
        this.palettes.add(new Palette(buildInfoList(newArrayList, newArrayList2, newArrayList3)));
    }

    private ListTag newIntegerList(int... iArr) {
        ListTag listTag = new ListTag();
        for (int i : iArr) {
            listTag.add(IntTag.valueOf(i));
        }
        return listTag;
    }

    private ListTag newDoubleList(double... dArr) {
        ListTag listTag = new ListTag();
        for (double d : dArr) {
            listTag.add(DoubleTag.valueOf(d));
        }
        return listTag;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo.class */
    public static class StructureBlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        public final CompoundTag nbt;

        public StructureBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag) {
            this.pos = blockPos;
            this.state = blockState;
            this.nbt = compoundTag;
        }

        public String toString() {
            return String.format("<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureEntityInfo.class */
    public static class StructureEntityInfo {
        public final Vec3 pos;
        public final BlockPos blockPos;
        public final CompoundTag nbt;

        public StructureEntityInfo(Vec3 vec3, BlockPos blockPos, CompoundTag compoundTag) {
            this.pos = vec3;
            this.blockPos = blockPos;
            this.nbt = compoundTag;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$Palette.class */
    public static final class Palette {
        private final List<StructureBlockInfo> blocks;
        private final Map<Block, List<StructureBlockInfo>> cache;

        private Palette(List<StructureBlockInfo> list) {
            this.cache = Maps.newHashMap();
            this.blocks = list;
        }

        public List<StructureBlockInfo> blocks() {
            return this.blocks;
        }

        public List<StructureBlockInfo> blocks(Block block) {
            return this.cache.computeIfAbsent(block, block2 -> {
                return  this.blocks.stream().filter(structureBlockInfo -> {
                    return structureBlockInfo.state.is(block2);
                }).collect(Collectors.toList());
            });
        }
    }
}
