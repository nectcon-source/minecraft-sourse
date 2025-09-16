package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.CallbackI;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/LevelChunk.class */
public class LevelChunk implements ChunkAccess {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    public static final LevelChunkSection EMPTY_SECTION = null;
    private final LevelChunkSection[] sections;
    private ChunkBiomeContainer biomes;
    private final Map<BlockPos, CompoundTag> pendingBlockEntities;
    private boolean loaded;
    private final Level level;
    private final Map<Heightmap.Types, Heightmap> heightmaps;
    private final UpgradeData upgradeData;
    private final Map<BlockPos, BlockEntity> blockEntities;
    private final ClassInstanceMultiMap<Entity>[] entitySections;
    private final Map<StructureFeature<?>, StructureStart<?>> structureStarts;
    private final Map<StructureFeature<?>, LongSet> structuresRefences;
    private final ShortList[] postProcessing;
    private TickList<Block> blockTicks;
    private TickList<Fluid> liquidTicks;
    private boolean lastSaveHadEntities;
    private long lastSaveTime;
    private volatile boolean unsaved;
    private long inhabitedTime;

    @Nullable
    private Supplier<ChunkHolder.FullChunkStatus> fullStatus;

    @Nullable
    private Consumer<LevelChunk> postLoad;
    private final ChunkPos chunkPos;
    private volatile boolean isLightCorrect;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/LevelChunk$EntityCreationType.class */
    public enum EntityCreationType {
        IMMEDIATE,
        QUEUED,
        CHECK
    }

    public LevelChunk(Level level, ChunkPos chunkPos, ChunkBiomeContainer chunkBiomeContainer) {
        this(level, chunkPos, chunkBiomeContainer, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, null, null);
    }

    public LevelChunk(Level level, ChunkPos chunkPos, ChunkBiomeContainer chunkBiomeContainer, UpgradeData upgradeData, TickList<Block> tickList, TickList<Fluid> tickList2, long j, @Nullable LevelChunkSection[] levelChunkSectionArr, @Nullable Consumer<LevelChunk> consumer) {
        this.sections = new LevelChunkSection[16];
        this.pendingBlockEntities = Maps.newHashMap();
        this.heightmaps = Maps.newEnumMap(Heightmap.Types.class);
        this.blockEntities = Maps.newHashMap();
        this.structureStarts = Maps.newHashMap();
        this.structuresRefences = Maps.newHashMap();
        this.postProcessing = new ShortList[16];
        this.entitySections = new ClassInstanceMultiMap[16];
        this.level = level;
        this.chunkPos = chunkPos;
        this.upgradeData = upgradeData;
        for (Heightmap.Types types : Heightmap.Types.values()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(types)) {
                this.heightmaps.put(types, new Heightmap(this, types));
            }
        }
        for (int i = 0; i < this.entitySections.length; i++) {
            this.entitySections[i] = new ClassInstanceMultiMap<>(Entity.class);
        }
        this.biomes = chunkBiomeContainer;
        this.blockTicks = tickList;
        this.liquidTicks = tickList2;
        this.inhabitedTime = j;
        this.postLoad = consumer;
        if (levelChunkSectionArr != null) {
            if (this.sections.length == levelChunkSectionArr.length) {
                System.arraycopy(levelChunkSectionArr, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", Integer.valueOf(levelChunkSectionArr.length), Integer.valueOf(this.sections.length));
            }
        }
    }

    public LevelChunk(Level level, ProtoChunk protoChunk) {
        this(level, protoChunk.getPos(), protoChunk.getBiomes(), protoChunk.getUpgradeData(), protoChunk.getBlockTicks(), protoChunk.getLiquidTicks(), protoChunk.getInhabitedTime(), protoChunk.getSections(), null);
        Iterator<CompoundTag> it = protoChunk.getEntities().iterator();
        while (it.hasNext()) {
            EntityType.loadEntityRecursive(it.next(), level, entity -> {
                addEntity(entity);
                return entity;
            });
        }
        Iterator<BlockEntity> it2 = protoChunk.getBlockEntities().values().iterator();
        while (it2.hasNext()) {
            addBlockEntity(it2.next());
        }
        this.pendingBlockEntities.putAll(protoChunk.getBlockEntityNbts());
        for (int i = 0; i < protoChunk.getPostProcessing().length; i++) {
            this.postProcessing[i] = protoChunk.getPostProcessing()[i];
        }
        setAllStarts(protoChunk.getAllStarts());
        setAllReferences(protoChunk.getAllReferences());
        for (Map.Entry<Heightmap.Types, Heightmap> entry : protoChunk.getHeightmaps()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
                getOrCreateHeightmapUnprimed(entry.getKey()).setRawData(entry.getValue().getRawData());
            }
        }
        setLightCorrect(protoChunk.isLightCorrect());
        this.unsaved = true;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
        return this.heightmaps.computeIfAbsent(types, types2 -> {
            return new Heightmap(this, types2);
        });
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public Set<BlockPos> getBlockEntitiesPos() {
        Set<BlockPos> newHashSet = Sets.newHashSet(this.pendingBlockEntities.keySet());
        newHashSet.addAll(this.blockEntities.keySet());
        return newHashSet;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    @Override // net.minecraft.world.level.BlockGetter
    public BlockState getBlockState(BlockPos blockPos) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        if (this.level.isDebug()) {
            BlockState blockState = null;
            if (y == 60) {
                blockState = Blocks.BARRIER.defaultBlockState();
            }
            if (y == 70) {
                blockState = DebugLevelSource.getBlockStateFor(x, z);
            }
            return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
        }
        if (y >= 0) {
            try {
                if ((y >> 4) < this.sections.length) {
                    LevelChunkSection levelChunkSection = this.sections[y >> 4];
                    if (!LevelChunkSection.isEmpty(levelChunkSection)) {
                        return levelChunkSection.getBlockState(x & 15, y & 15, z & 15);
                    }
                }
            } catch (Throwable th) {
                CrashReport forThrowable = CrashReport.forThrowable(th, "Getting block state");
                forThrowable.addCategory("Block being got").setDetail("Location", () -> {
                    return CrashReportCategory.formatLocation(x, y, z);
                });
                throw new ReportedException(forThrowable);
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override // net.minecraft.world.level.BlockGetter
    public FluidState getFluidState(BlockPos blockPos) {
        return getFluidState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public FluidState getFluidState(int i, int i2, int i3) {
        if (i2 >= 0) {
            try {
                if ((i2 >> 4) < this.sections.length) {
                    LevelChunkSection levelChunkSection = this.sections[i2 >> 4];
                    if (!LevelChunkSection.isEmpty(levelChunkSection)) {
                        return levelChunkSection.getFluidState(i & 15, i2 & 15, i3 & 15);
                    }
                }
            } catch (Throwable th) {
                CrashReport forThrowable = CrashReport.forThrowable(th, "Getting fluid state");
                forThrowable.addCategory("Block being got").setDetail("Location", () -> {
                    return CrashReportCategory.formatLocation(i, i2, i3);
                });
                throw new ReportedException(forThrowable);
            }
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // net.minecraft.world.level.chunk.ChunkAccess
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean z) {
        BlockEntity blockEntity;
        int x = blockPos.getX() & 15;
        int y = blockPos.getY();
        int z2 = blockPos.getZ() & 15;
        LevelChunkSection levelChunkSection = this.sections[y >> 4];
        if (levelChunkSection == EMPTY_SECTION) {
            if (blockState.isAir()) {
                return null;
            }
            levelChunkSection = new LevelChunkSection((y >> 4) << 4);
            this.sections[y >> 4] = levelChunkSection;
        }
        boolean isEmpty = levelChunkSection.isEmpty();
        BlockState blockState2 = levelChunkSection.setBlockState(x, y & 15, z2, blockState);
        if (blockState2 == blockState) {
            return null;
        }
        Block block = blockState.getBlock();
        Block block2 = blockState2.getBlock();
        this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(x, y, z2, blockState);
        this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(x, y, z2, blockState);
        this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(x, y, z2, blockState);
        this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(x, y, z2, blockState);
        boolean isEmpty2 = levelChunkSection.isEmpty();
        if (isEmpty != isEmpty2) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus(blockPos, isEmpty2);
        }
        if (!this.level.isClientSide) {
            blockState2.onRemove(this.level, blockPos, blockState, z);
        } else if (block2 != block && (block2 instanceof EntityBlock)) {
            this.level.removeBlockEntity(blockPos);
        }
        if (!levelChunkSection.getBlockState(x, y & 15, z2).is(block)) {
            return null;
        }
        if ((block2 instanceof EntityBlock) && (blockEntity = getBlockEntity(blockPos, EntityCreationType.CHECK)) != null) {
            blockEntity.clearCache();
        }
        if (!this.level.isClientSide) {
            blockState.onPlace(this.level, blockPos, blockState2, z);
        }
        if (block instanceof EntityBlock) {
            BlockEntity blockEntity2 = getBlockEntity(blockPos, EntityCreationType.CHECK);
            if (blockEntity2 == null) {
                this.level.setBlockEntity(blockPos, ((EntityBlock) block).newBlockEntity(this.level));
            } else {
                blockEntity2.clearCache();
            }
        }
        this.unsaved = true;
        return blockState2;
    }

    @Nullable
    public LevelLightEngine getLightEngine() {
        return this.level.getChunkSource().getLightEngine();
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void addEntity(Entity entity) {
        this.lastSaveHadEntities = true;
        int floor = Mth.floor(entity.getX() / 16.0d);
        int floor2 = Mth.floor(entity.getZ() / 16.0d);
        if (floor != this.chunkPos.x || floor2 != this.chunkPos.z) {
            LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", Integer.valueOf(floor), Integer.valueOf(floor2), Integer.valueOf(this.chunkPos.x), Integer.valueOf(this.chunkPos.z), entity);
            entity.removed = true;
        }
        int floor3 = Mth.floor(entity.getY() / 16.0d);
        if (floor3 < 0) {
            floor3 = 0;
        }
        if (floor3 >= this.entitySections.length) {
            floor3 = this.entitySections.length - 1;
        }
        entity.inChunk = true;
        entity.xChunk = this.chunkPos.x;
        entity.yChunk = floor3;
        entity.zChunk = this.chunkPos.z;
        this.entitySections[floor3].add(entity);
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setHeightmap(Heightmap.Types types, long[] jArr) {
        this.heightmaps.get(types).setRawData(jArr);
    }

    public void removeEntity(Entity entity) {
        removeEntity(entity, entity.yChunk);
    }

    public void removeEntity(Entity entity, int i) {
        if (i < 0) {
            i = 0;
        }
        if (i >= this.entitySections.length) {
            i = this.entitySections.length - 1;
        }
        this.entitySections[i].remove(entity);
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public int getHeight(Heightmap.Types types, int i, int i2) {
        return this.heightmaps.get(types).getFirstAvailable(i & 15, i2 & 15) - 1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Nullable
    private BlockEntity createBlockEntity(BlockPos blockPos) {
        Block block = getBlockState(blockPos).getBlock();
        if (!block.isEntityBlock()) {
            return null;
        }
        return ((EntityBlock) block).newBlockEntity(this.level);
    }

    @Override // net.minecraft.world.level.BlockGetter
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return getBlockEntity(blockPos, EntityCreationType.CHECK);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos, EntityCreationType entityCreationType) {
        CompoundTag remove;
        BlockEntity promotePendingBlockEntity;
        BlockEntity blockEntity = this.blockEntities.get(blockPos);
        if (blockEntity == null && (remove = this.pendingBlockEntities.remove(blockPos)) != null && (promotePendingBlockEntity = promotePendingBlockEntity(blockPos, remove)) != null) {
            return promotePendingBlockEntity;
        }
        if (blockEntity == null) {
            if (entityCreationType == EntityCreationType.IMMEDIATE) {
                blockEntity = createBlockEntity(blockPos);
                this.level.setBlockEntity(blockPos, blockEntity);
            }
        } else if (blockEntity.isRemoved()) {
            this.blockEntities.remove(blockPos);
            return null;
        }
        return blockEntity;
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        setBlockEntity(blockEntity.getBlockPos(), blockEntity);
        if (this.loaded || this.level.isClientSide()) {
            this.level.setBlockEntity(blockEntity.getBlockPos(), blockEntity);
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        if (!(getBlockState(blockPos).getBlock() instanceof EntityBlock)) {
            return;
        }
        blockEntity.setLevelAndPosition(this.level, blockPos);
        blockEntity.clearRemoved();
        BlockEntity put = this.blockEntities.put(blockPos.immutable(), blockEntity);
        if (put != null && put != blockEntity) {
            put.setRemoved();
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setBlockEntityNbt(CompoundTag compoundTag) {
        this.pendingBlockEntities.put(new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z")), compoundTag);
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
        BlockEntity blockEntity = getBlockEntity(blockPos);
        if (blockEntity != null && !blockEntity.isRemoved()) {
            CompoundTag save = blockEntity.save(new CompoundTag());
            save.putBoolean("keepPacked", false);
            return save;
        }
        CompoundTag compoundTag = this.pendingBlockEntities.get(blockPos);
        if (compoundTag != null) {
            compoundTag = compoundTag.copy();
            compoundTag.putBoolean("keepPacked", true);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity remove;
        if ((this.loaded || this.level.isClientSide()) && (remove = this.blockEntities.remove(blockPos)) != null) {
            remove.setRemoved();
        }
    }

    public void runPostLoad() {
        if (this.postLoad != null) {
            this.postLoad.accept(this);
            this.postLoad = null;
        }
    }

    public void markUnsaved() {
        this.unsaved = true;
    }

    public void getEntities(@Nullable Entity entity, AABB aabb, List<Entity> list, @Nullable Predicate<? super Entity> predicate) {
        int floor = Mth.floor((aabb.minY - 2.0d) / 16.0d);
        int floor2 = Mth.floor((aabb.maxY + 2.0d) / 16.0d);
        int clamp = Mth.clamp(floor, 0, this.entitySections.length - 1);
        int clamp2 = Mth.clamp(floor2, 0, this.entitySections.length - 1);
        for (int i = clamp; i <= clamp2; i++) {
            List<Entity> allInstances = this.entitySections[i].getAllInstances();
            int size = allInstances.size();
            for (int i2 = 0; i2 < size; i2++) {
                Entity entity2 = allInstances.get(i2);
                if (entity2.getBoundingBox().intersects(aabb) && entity2 != entity) {
                    if (predicate == null || predicate.test(entity2)) {
                        list.add(entity2);
                    }
                    if (entity2 instanceof EnderDragon) {
                        for (EnderDragonPart enderDragonPart : ((EnderDragon) entity2).getSubEntities()) {
                            if (enderDragonPart != entity && enderDragonPart.getBoundingBox().intersects(aabb) && (predicate == null || predicate.test(enderDragonPart))) {
                                list.add(enderDragonPart);
                            }
                        }
                    }
                }
            }
        }
    }

    public <T extends Entity> void getEntities(@Nullable EntityType<?> entityType, AABB aabb, List<? super T> list, Predicate<? super T> predicate) {
        int var5 = Mth.floor((aabb.minY - 2.0) / 16.0);
        int var6x = Mth.floor((aabb.maxY + 2.0) / 16.0);
        var5 = Mth.clamp(var5, 0, this.entitySections.length - 1);
        var6x = Mth.clamp(var6x, 0, this.entitySections.length - 1);

        for(int var7xx = var5; var7xx <= var6x; ++var7xx) {
            for(Entity entity : this.entitySections[var7xx].find(Entity.class)) {
                if ((entityType == null || entity.getType() == entityType) && entity.getBoundingBox().intersects(aabb) && predicate.test((T)entity)) {
                    list.add((T)entity);
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public <T extends Entity> void getEntitiesOfClass(Class<? extends T> cls, AABB aabb, List<T> list, @Nullable Predicate<? super T> predicate) {
        int var5 = Mth.floor((aabb.minY - (double)2.0F) / (double)16.0F);
        int var6 = Mth.floor((aabb.maxY + (double)2.0F) / (double)16.0F);
        var5 = Mth.clamp(var5, 0, this.entitySections.length - 1);
        var6 = Mth.clamp(var6, 0, this.entitySections.length - 1);

        for(int var7 = var5; var7 <= var6; ++var7) {
            for(T var9 : this.entitySections[var7].find(cls)) {
                if (var9.getBoundingBox().intersects(aabb) && (predicate == null || predicate.test(var9))) {
                    list.add(var9);
                }
            }
        }
    }

    public boolean isEmpty() {
        return false;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public ChunkPos getPos() {
        return this.chunkPos;
    }

    public void replaceWithPacketData(@Nullable ChunkBiomeContainer chunkBiomeContainer, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, int i) {
        boolean z = chunkBiomeContainer != null;
                Sets.newHashSet(this.blockEntities.keySet()).stream().filter(z ? (var0) -> true : (var1x) -> (i & 1 << (var1x.getY() >> 4)) != 0).forEach(this.level::removeBlockEntity);
        for (int i2 = 0; i2 < this.sections.length; i2++) {
            LevelChunkSection levelChunkSection = this.sections[i2];
            if ((i & (1 << i2)) == 0) {
                if (z && levelChunkSection != EMPTY_SECTION) {
                    this.sections[i2] = EMPTY_SECTION;
                }
            } else {
                if (levelChunkSection == EMPTY_SECTION) {
                    levelChunkSection = new LevelChunkSection(i2 << 4);
                    this.sections[i2] = levelChunkSection;
                }
                levelChunkSection.read(friendlyByteBuf);
            }
        }
        if (chunkBiomeContainer != null) {
            this.biomes = chunkBiomeContainer;
        }
        for (Heightmap.Types types : Heightmap.Types.values()) {
            String serializationKey = types.getSerializationKey();
            if (compoundTag.contains(serializationKey, 12)) {
                setHeightmap(types, compoundTag.getLongArray(serializationKey));
            }
        }
        Iterator<BlockEntity> it = this.blockEntities.values().iterator();
        while (it.hasNext()) {
            it.next().clearCache();
        }
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public ChunkBiomeContainer getBiomes() {
        return this.biomes;
    }

    public void setLoaded(boolean z) {
        this.loaded = z;
    }

    public Level getLevel() {
        return this.level;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public ClassInstanceMultiMap<Entity>[] getEntitySections() {
        return this.entitySections;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.pendingBlockEntities.get(blockPos);
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public Stream<BlockPos> getLights() {
        return StreamSupport.stream(BlockPos.betweenClosed(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), 255, this.chunkPos.getMaxBlockZ()).spliterator(), false).filter(blockPos -> {
            return getBlockState(blockPos).getLightEmission() != 0;
        });
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public TickList<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public TickList<Fluid> getLiquidTicks() {
        return this.liquidTicks;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setUnsaved(boolean z) {
        this.unsaved = z;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public boolean isUnsaved() {
        return this.unsaved || (this.lastSaveHadEntities && this.level.getGameTime() != this.lastSaveTime);
    }

    public void setLastSaveHadEntities(boolean z) {
        this.lastSaveHadEntities = z;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setLastSaveTime(long j) {
        this.lastSaveTime = j;
    }

    @Override // net.minecraft.world.level.chunk.FeatureAccess
    @Nullable
    public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
        return this.structureStarts.get(structureFeature);
    }

    @Override // net.minecraft.world.level.chunk.FeatureAccess
    public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
        this.structureStarts.put(structureFeature, structureStart);
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return this.structureStarts;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map) {
        this.structureStarts.clear();
        this.structureStarts.putAll(map);
    }

    @Override // net.minecraft.world.level.chunk.FeatureAccess
    public LongSet getReferencesForFeature(StructureFeature<?> structureFeature) {
        return this.structuresRefences.computeIfAbsent(structureFeature, structureFeature2 -> {
            return new LongOpenHashSet();
        });
    }

    @Override // net.minecraft.world.level.chunk.FeatureAccess
    public void addReferenceForFeature(StructureFeature<?> structureFeature, long j) {
        this.structuresRefences.computeIfAbsent(structureFeature, structureFeature2 -> {
            return new LongOpenHashSet();
        }).add(j);
    }

    @Override // net.minecraft.world.level.chunk.FeatureAccess
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return this.structuresRefences;
    }

    @Override // net.minecraft.world.level.chunk.FeatureAccess
    public void setAllReferences(Map<StructureFeature<?>, LongSet> map) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(map);
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setInhabitedTime(long j) {
        this.inhabitedTime = j;
    }

    public void postProcessGeneration() {
        ChunkPos pos = getPos();
        for (int i = 0; i < this.postProcessing.length; i++) {
            if (this.postProcessing[i] != null) {
                ShortListIterator it = this.postProcessing[i].iterator();
                while (it.hasNext()) {
                    BlockPos unpackOffsetCoordinates = ProtoChunk.unpackOffsetCoordinates(((Short) it.next()).shortValue(), i, pos);
                    this.level.setBlock(unpackOffsetCoordinates, Block.updateFromNeighbourShapes(getBlockState(unpackOffsetCoordinates), this.level, unpackOffsetCoordinates), 20);
                }
                this.postProcessing[i].clear();
            }
        }
        unpackTicks();
        Iterator it2 = Sets.newHashSet(this.pendingBlockEntities.keySet()).iterator();
        while (it2.hasNext()) {
            getBlockEntity((BlockPos) it2.next());
        }
        this.pendingBlockEntities.clear();
        this.upgradeData.upgrade(this);
    }

    @Nullable
    private BlockEntity promotePendingBlockEntity(BlockPos blockPos, CompoundTag compoundTag) {
        BlockEntity loadStatic;
        BlockState blockState = getBlockState(blockPos);
        if ("DUMMY".equals(compoundTag.getString("id"))) {
            ItemLike block = blockState.getBlock();
            if (block instanceof EntityBlock) {
                loadStatic = ((EntityBlock) block).newBlockEntity(this.level);
            } else {
                loadStatic = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", blockPos, blockState);
            }
        } else {
            loadStatic = BlockEntity.loadStatic(blockState, compoundTag);
        }
        if (loadStatic != null) {
            loadStatic.setLevelAndPosition(this.level, blockPos);
            addBlockEntity(loadStatic);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", blockState, blockPos);
        }
        return loadStatic;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void unpackTicks() {
        if (this.blockTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.blockTicks).copyOut(this.level.getBlockTicks(), var1 -> this.getBlockState((BlockPos) var1).getBlock());
            this.blockTicks = EmptyTickList.empty();
        } else if (this.blockTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
            this.blockTicks = EmptyTickList.empty();
        }

        if (this.liquidTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), var1 -> this.getFluidState((BlockPos) var1).getType());
            this.liquidTicks = EmptyTickList.empty();
        } else if (this.liquidTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
            this.liquidTicks = EmptyTickList.empty();
        }
    }

//    public void packTicks(ServerLevel serverLevel) {
//        if (this.blockTicks == EmptyTickList.empty()) {
//            this.blockTicks = new ChunkTickList<>(Registry.BLOCK::getKey, serverLevel.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false), serverLevel.getGameTime());
//            this.setUnsaved(true);
//        }
//
//        if (this.liquidTicks == EmptyTickList.empty()) {
//            this.liquidTicks = new ChunkTickList<>(Registry.FLUID::getKey, serverLevel.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false), serverLevel.getGameTime());
//            this.setUnsaved(true);
//        }
//    }
public void packTicks(ServerLevel serverLevel) {
    if (this.blockTicks instanceof EmptyTickList) {
        this.blockTicks = new ChunkTickList<>(Registry.BLOCK::getKey,
                serverLevel.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false),
                serverLevel.getGameTime());
        this.setUnsaved(true);
    }

    if (this.liquidTicks instanceof EmptyTickList) {
        this.liquidTicks = new ChunkTickList<>(Registry.FLUID::getKey,
                serverLevel.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false),
                serverLevel.getGameTime());
        this.setUnsaved(true);
    }
}

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public ChunkHolder.FullChunkStatus getFullStatus() {
        if (this.fullStatus == null) {
            return ChunkHolder.FullChunkStatus.BORDER;
        }
        return this.fullStatus.get();
    }

    public void setFullStatus(Supplier<ChunkHolder.FullChunkStatus> supplier) {
        this.fullStatus = supplier;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public boolean isLightCorrect() {
        return this.isLightCorrect;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public void setLightCorrect(boolean z) {
        this.isLightCorrect = z;
        setUnsaved(true);
    }
}
