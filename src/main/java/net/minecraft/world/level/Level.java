package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/Level.class */
public abstract class Level implements LevelAccessor, AutoCloseable {
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_REGISTRY), ResourceKey::location);
    public static final ResourceKey<Level> OVERWORLD = ResourceKey.<Level>create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.<Level>create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<Level> END = ResourceKey.<Level>create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_end"));
    private static final Direction[] DIRECTIONS = Direction.values();
    private final Thread thread;
    private final boolean isDebug;
    private int skyDarken;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    private final DimensionType dimensionType;
    protected final WritableLevelData levelData;
    private final Supplier<ProfilerFiller> profiler;
    public final boolean isClientSide;
    protected boolean updatingBlockEntities;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;
    private final ResourceKey<Level> dimension;
    public final List<BlockEntity> blockEntityList = Lists.newArrayList();
    public final List<BlockEntity> tickableBlockEntities = Lists.newArrayList();
    protected final List<BlockEntity> pendingBlockEntities = Lists.newArrayList();
    protected final List<BlockEntity> blockEntitiesToUnload = Lists.newArrayList();
    protected int randValue = new Random().nextInt();
    protected final int addend = 1013904223;
    public final Random random = new Random();

    public abstract void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i);

    public abstract void playSound(@Nullable Player player, double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2);

    public abstract void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float f2);

    @Nullable
    public abstract Entity getEntity(int i);

    @Nullable
    public abstract MapItemSavedData getMapData(String str);

    public abstract void setMapData(MapItemSavedData mapItemSavedData);

    public abstract int getFreeMapId();

    public abstract void destroyBlockProgress(int i, BlockPos blockPos, int i2);

    public abstract Scoreboard getScoreboard();

    public abstract RecipeManager getRecipeManager();

    public abstract TagContainer getTagManager();

    protected Level(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, final DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean z, boolean z2, long j) {
        this.profiler = supplier;
        this.levelData = writableLevelData;
        this.dimensionType = dimensionType;
        this.dimension = resourceKey;
        this.isClientSide = z;
        if (dimensionType.coordinateScale() != 1.0d) {
            this.worldBorder = new WorldBorder() { // from class: net.minecraft.world.level.Level.1
                @Override // net.minecraft.world.level.border.WorldBorder
                public double getCenterX() {
                    return super.getCenterX() / dimensionType.coordinateScale();
                }

                @Override // net.minecraft.world.level.border.WorldBorder
                public double getCenterZ() {
                    return super.getCenterZ() / dimensionType.coordinateScale();
                }
            };
        } else {
            this.worldBorder = new WorldBorder();
        }
        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, j, dimensionType.getBiomeZoomer());
        this.isDebug = z2;
    }

    @Override // net.minecraft.world.level.LevelReader
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Nullable
    public MinecraftServer getServer() {
        return null;
    }

    public static boolean isInWorldBounds(BlockPos blockPos) {
        return !isOutsideBuildHeight(blockPos) && isInWorldBoundsHorizontal(blockPos);
    }

    public static boolean isInSpawnableBounds(BlockPos blockPos) {
        return !isOutsideSpawnableHeight(blockPos.getY()) && isInWorldBoundsHorizontal(blockPos);
    }

    private static boolean isInWorldBoundsHorizontal(BlockPos blockPos) {
        return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 && blockPos.getX() < 30000000 && blockPos.getZ() < 30000000;
    }

    private static boolean isOutsideSpawnableHeight(int i) {
        return i < -20000000 || i >= 20000000;
    }

    public static boolean isOutsideBuildHeight(BlockPos blockPos) {
        return isOutsideBuildHeight(blockPos.getY());
    }

    public static boolean isOutsideBuildHeight(int i) {
        return i < 0 || i >= 256;
    }

    public LevelChunk getChunkAt(BlockPos blockPos) {
        return getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    @Override // net.minecraft.world.level.LevelReader
    public LevelChunk getChunk(int i, int i2) {
        return (LevelChunk) getChunk(i, i2, ChunkStatus.FULL);
    }

    @Override // net.minecraft.world.level.LevelReader
    public ChunkAccess getChunk(int i, int i2, ChunkStatus chunkStatus, boolean z) {
        ChunkAccess chunk = getChunkSource().getChunk(i, i2, chunkStatus, z);
        if (chunk == null && z) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        return chunk;
    }

    @Override // net.minecraft.world.level.LevelWriter
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i) {
        return setBlock(blockPos, blockState, i, 512);
    }

    @Override // net.minecraft.world.level.LevelWriter
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int i2) {
        if (isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (!this.isClientSide && isDebug()) {
            return false;
        }
        LevelChunk chunkAt = getChunkAt(blockPos);
        Block block = blockState.getBlock();
        BlockState blockState2 = chunkAt.setBlockState(blockPos, blockState, (i & 64) != 0);
        if (blockState2 != null) {
            BlockState blockState3 = getBlockState(blockPos);
            if ((i & 128) == 0 && blockState3 != blockState2 && (blockState3.getLightBlock(this, blockPos) != blockState2.getLightBlock(this, blockPos) || blockState3.getLightEmission() != blockState2.getLightEmission() || blockState3.useShapeForLightOcclusion() || blockState2.useShapeForLightOcclusion())) {
                getProfiler().push("queueCheckLight");
                getChunkSource().getLightEngine().checkBlock(blockPos);
                getProfiler().pop();
            }
            if (blockState3 == blockState) {
                if (blockState2 != blockState3) {
                    setBlocksDirty(blockPos, blockState2, blockState3);
                }
                if ((i & 2) != 0 && ((!this.isClientSide || (i & 4) == 0) && (this.isClientSide || (chunkAt.getFullStatus() != null && chunkAt.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))))) {
                    sendBlockUpdated(blockPos, blockState2, blockState, i);
                }
                if ((i & 1) != 0) {
                    blockUpdated(blockPos, blockState2.getBlock());
                    if (!this.isClientSide && blockState.hasAnalogOutputSignal()) {
                        updateNeighbourForOutputSignal(blockPos, block);
                    }
                }
                if ((i & 16) == 0 && i2 > 0) {
                    int i3 = i & (-34);
                    blockState2.updateIndirectNeighbourShapes(this, blockPos, i3, i2 - 1);
                    blockState.updateNeighbourShapes(this, blockPos, i3, i2 - 1);
                    blockState.updateIndirectNeighbourShapes(this, blockPos, i3, i2 - 1);
                }
                onBlockStateChange(blockPos, blockState2, blockState3);
                return true;
            }
            return true;
        }
        return false;
    }

    public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
    }

    @Override // net.minecraft.world.level.LevelWriter
    public boolean removeBlock(BlockPos blockPos, boolean z) {
        return setBlock(blockPos, getFluidState(blockPos).createLegacyBlock(), 3 | (z ? 64 : 0));
    }

    @Override // net.minecraft.world.level.LevelWriter
    public boolean destroyBlock(BlockPos blockPos, boolean z, @Nullable Entity entity, int i) {
        BlockState blockState = getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        FluidState fluidState = getFluidState(blockPos);
        if (!(blockState.getBlock() instanceof BaseFireBlock)) {
            levelEvent(2001, blockPos, Block.getId(blockState));
        }
        if (z) {
            Block.dropResources(blockState, this, blockPos, blockState.getBlock().isEntityBlock() ? getBlockEntity(blockPos) : null, entity, ItemStack.EMPTY);
        }
        return setBlock(blockPos, fluidState.createLegacyBlock(), 3, i);
    }

    public boolean setBlockAndUpdate(BlockPos blockPos, BlockState blockState) {
        return setBlock(blockPos, blockState, 3);
    }

    public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
    }

    public void updateNeighborsAt(BlockPos blockPos, Block block) {
        neighborChanged(blockPos.west(), block, blockPos);
        neighborChanged(blockPos.east(), block, blockPos);
        neighborChanged(blockPos.below(), block, blockPos);
        neighborChanged(blockPos.above(), block, blockPos);
        neighborChanged(blockPos.north(), block, blockPos);
        neighborChanged(blockPos.south(), block, blockPos);
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction) {
        if (direction != Direction.WEST) {
            neighborChanged(blockPos.west(), block, blockPos);
        }
        if (direction != Direction.EAST) {
            neighborChanged(blockPos.east(), block, blockPos);
        }
        if (direction != Direction.DOWN) {
            neighborChanged(blockPos.below(), block, blockPos);
        }
        if (direction != Direction.UP) {
            neighborChanged(blockPos.above(), block, blockPos);
        }
        if (direction != Direction.NORTH) {
            neighborChanged(blockPos.north(), block, blockPos);
        }
        if (direction != Direction.SOUTH) {
            neighborChanged(blockPos.south(), block, blockPos);
        }
    }

    public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
        if (this.isClientSide) {
            return;
        }
        BlockState blockState = getBlockState(blockPos);
        try {
            blockState.neighborChanged(this, blockPos, block, blockPos2, false);
        } catch (Throwable th) {
            CrashReport forThrowable = CrashReport.forThrowable(th, "Exception while updating neighbours");
            CrashReportCategory addCategory = forThrowable.addCategory("Block being updated");
            addCategory.setDetail("Source block type", () -> {
                try {
                    return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                } catch (Throwable th2) {
                    return "ID #" + Registry.BLOCK.getKey(block);
                }
            });
            CrashReportCategory.populateBlockDetails(addCategory, blockPos, blockState);
            throw new ReportedException(forThrowable);
        }
    }

    @Override // net.minecraft.world.level.LevelReader
    public int getHeight(Heightmap.Types types, int i, int i2) {
        int seaLevel;
        if (i < -30000000 || i2 < -30000000 || i >= 30000000 || i2 >= 30000000) {
            seaLevel = getSeaLevel() + 1;
        } else if (hasChunk(i >> 4, i2 >> 4)) {
            seaLevel = getChunk(i >> 4, i2 >> 4).getHeight(types, i & 15, i2 & 15) + 1;
        } else {
            seaLevel = 0;
        }
        return seaLevel;
    }

    @Override // net.minecraft.world.level.BlockAndTintGetter
    public LevelLightEngine getLightEngine() {
        return getChunkSource().getLightEngine();
    }

    @Override // net.minecraft.world.level.BlockGetter
    public BlockState getBlockState(BlockPos blockPos) {
        if (isOutsideBuildHeight(blockPos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        return getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4).getBlockState(blockPos);
    }

    @Override // net.minecraft.world.level.BlockGetter
    public FluidState getFluidState(BlockPos blockPos) {
        if (isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return getChunkAt(blockPos).getFluidState(blockPos);
    }

    public boolean isDay() {
        return !dimensionType().hasFixedTime() && this.skyDarken < 4;
    }

//    public boolean isNight() {
//        return (dimensionType().hasFixedTime() || isDay()) ? false : true;
//    }
    public boolean isNight() {
        return !dimensionType().hasFixedTime() && !isDay();
    }

    @Override // net.minecraft.world.level.LevelAccessor
    public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        playSound(player, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, soundEvent, soundSource, f, f2);
    }

    public void playLocalSound(double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2, boolean z) {
    }

    @Override // net.minecraft.world.level.LevelAccessor
    public void addParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public void addParticle(ParticleOptions particleOptions, boolean z, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean z, double d, double d2, double d3, double d4, double d5, double d6) {
    }

    public float getSunAngle(float f) {
        return getTimeOfDay(f) * 6.2831855f;
    }

    public boolean addBlockEntity(BlockEntity blockEntity) {
        if (this.updatingBlockEntities) {
            Logger logger = LOGGER;
            blockEntity.getClass();
            logger.error("Adding block entity while ticking: {} @ {}", new org.apache.logging.log4j.util.Supplier[]{() -> {
                return Registry.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
            }, blockEntity::getBlockPos});
        }
        boolean add = this.blockEntityList.add(blockEntity);
        if (add && (blockEntity instanceof TickableBlockEntity)) {
            this.tickableBlockEntities.add(blockEntity);
        }
        if (this.isClientSide) {
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockState blockState = getBlockState(blockPos);
            sendBlockUpdated(blockPos, blockState, blockState, 2);
        }
        return add;
    }

    public void addAllPendingBlockEntities(Collection<BlockEntity> collection) {
        if (this.updatingBlockEntities) {
            this.pendingBlockEntities.addAll(collection);
            return;
        }
        Iterator<BlockEntity> it = collection.iterator();
        while (it.hasNext()) {
            addBlockEntity(it.next());
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void tickBlockEntities() {
        ProfilerFiller profiler = getProfiler();
        profiler.push("blockEntities");
        if (!this.blockEntitiesToUnload.isEmpty()) {
            this.tickableBlockEntities.removeAll(this.blockEntitiesToUnload);
            this.blockEntityList.removeAll(this.blockEntitiesToUnload);
            this.blockEntitiesToUnload.clear();
        }
        this.updatingBlockEntities = true;
        Iterator<BlockEntity> it = this.tickableBlockEntities.iterator();
        while (it.hasNext()) {
            BlockEntity next = it.next();
            if (!next.isRemoved() && next.hasLevel()) {
                BlockPos blockPos = next.getBlockPos();
                if (getChunkSource().isTickingChunk(blockPos) && getWorldBorder().isWithinBounds(blockPos)) {
                    try {
                        profiler.push(() -> {
                            return String.valueOf(BlockEntityType.getKey(next.getType()));
                        });
                        if (next.getType().isValid(getBlockState(blockPos).getBlock())) {
                            ((TickableBlockEntity) next).tick();
                        } else {
                            next.logInvalidState();
                        }
                        profiler.pop();
                    } catch (Throwable th) {
                        CrashReport forThrowable = CrashReport.forThrowable(th, "Ticking block entity");
                        next.fillCrashReportCategory(forThrowable.addCategory("Block entity being ticked"));
                        throw new ReportedException(forThrowable);
                    }
                }
            }
            if (next.isRemoved()) {
                it.remove();
                this.blockEntityList.remove(next);
                if (hasChunkAt(next.getBlockPos())) {
                    getChunkAt(next.getBlockPos()).removeBlockEntity(next.getBlockPos());
                }
            }
        }
        this.updatingBlockEntities = false;
        profiler.popPush("pendingBlockEntities");
        if (!this.pendingBlockEntities.isEmpty()) {
            for (int i = 0; i < this.pendingBlockEntities.size(); i++) {
                BlockEntity blockEntity = this.pendingBlockEntities.get(i);
                if (!blockEntity.isRemoved()) {
                    if (!this.blockEntityList.contains(blockEntity)) {
                        addBlockEntity(blockEntity);
                    }
                    if (hasChunkAt(blockEntity.getBlockPos())) {
                        LevelChunk chunkAt = getChunkAt(blockEntity.getBlockPos());
                        BlockState blockState = chunkAt.getBlockState(blockEntity.getBlockPos());
                        chunkAt.setBlockEntity(blockEntity.getBlockPos(), blockEntity);
                        sendBlockUpdated(blockEntity.getBlockPos(), blockState, blockState, 3);
                    }
                }
            }
            this.pendingBlockEntities.clear();
        }
        profiler.pop();
    }

    public void guardEntityTick(Consumer<Entity> consumer, Entity entity) {
        try {
            consumer.accept(entity);
        } catch (Throwable th) {
            CrashReport forThrowable = CrashReport.forThrowable(th, "Ticking entity");
            entity.fillCrashReportCategory(forThrowable.addCategory("Entity being ticked"));
            throw new ReportedException(forThrowable);
        }
    }

    public Explosion explode(@Nullable Entity entity, double d, double d2, double d3, float f, Explosion.BlockInteraction blockInteraction) {
        return explode(entity, null, null, d, d2, d3, f, false, blockInteraction);
    }

    public Explosion explode(@Nullable Entity entity, double d, double d2, double d3, float f, boolean z, Explosion.BlockInteraction blockInteraction) {
        return explode(entity, null, null, d, d2, d3, f, z, blockInteraction);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean z, Explosion.BlockInteraction blockInteraction) {
        Explosion explosion = new Explosion(this, entity, damageSource, explosionDamageCalculator, d, d2, d3, f, z, blockInteraction);
        explosion.explode();
        explosion.finalizeExplosion(true);
        return explosion;
    }

    public String gatherChunkSourceStats() {
        return getChunkSource().gatherStats();
    }

    @Override // net.minecraft.world.level.BlockGetter
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        if (isOutsideBuildHeight(blockPos)) {
            return null;
        }
        if (!this.isClientSide && Thread.currentThread() != this.thread) {
            return null;
        }
        BlockEntity blockEntity = null;
        if (this.updatingBlockEntities) {
            blockEntity = getPendingBlockEntityAt(blockPos);
        }
        if (blockEntity == null) {
            blockEntity = getChunkAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
        }
        if (blockEntity == null) {
            blockEntity = getPendingBlockEntityAt(blockPos);
        }
        return blockEntity;
    }

    @Nullable
    private BlockEntity getPendingBlockEntityAt(BlockPos blockPos) {
        for (int i = 0; i < this.pendingBlockEntities.size(); i++) {
            BlockEntity blockEntity = this.pendingBlockEntities.get(i);
            if (!blockEntity.isRemoved() && blockEntity.getBlockPos().equals(blockPos)) {
                return blockEntity;
            }
        }
        return null;
    }

    public void setBlockEntity(BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        if (!isOutsideBuildHeight(blockPos) && blockEntity != null && !blockEntity.isRemoved()) {
            if (this.updatingBlockEntities) {
                blockEntity.setLevelAndPosition(this, blockPos);
                Iterator<BlockEntity> it = this.pendingBlockEntities.iterator();
                while (it.hasNext()) {
                    BlockEntity next = it.next();
                    if (next.getBlockPos().equals(blockPos)) {
                        next.setRemoved();
                        it.remove();
                    }
                }
                this.pendingBlockEntities.add(blockEntity);
                return;
            }
            getChunkAt(blockPos).setBlockEntity(blockPos, blockEntity);
            addBlockEntity(blockEntity);
        }
    }

    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity blockEntity = getBlockEntity(blockPos);
        if (blockEntity != null && this.updatingBlockEntities) {
            blockEntity.setRemoved();
            this.pendingBlockEntities.remove(blockEntity);
            return;
        }
        if (blockEntity != null) {
            this.pendingBlockEntities.remove(blockEntity);
            this.blockEntityList.remove(blockEntity);
            this.tickableBlockEntities.remove(blockEntity);
        }
        getChunkAt(blockPos).removeBlockEntity(blockPos);
    }

    public boolean isLoaded(BlockPos blockPos) {
        if (isOutsideBuildHeight(blockPos)) {
            return false;
        }
        return getChunkSource().hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPos blockPos, Entity entity, Direction direction) {
        ChunkAccess chunk;
        if (isOutsideBuildHeight(blockPos) || (chunk = getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, false)) == null) {
            return false;
        }
        return chunk.getBlockState(blockPos).entityCanStandOnFace(this, blockPos, entity, direction);
    }

    public boolean loadedAndEntityCanStandOn(BlockPos blockPos, Entity entity) {
        return loadedAndEntityCanStandOnFace(blockPos, entity, Direction.UP);
    }

    public void updateSkyBrightness() {
        this.skyDarken = (int) ((1.0d - (((0.5d + (2.0d * Mth.clamp(Mth.cos(getTimeOfDay(1.0f) * 6.2831855f), -0.25d, 0.25d))) * (1.0d - ((getRainLevel(1.0f) * 5.0f) / 16.0d))) * (1.0d - ((getThunderLevel(1.0f) * 5.0f) / 16.0d)))) * 11.0d);
    }

    public void setSpawnSettings(boolean z, boolean z2) {
        getChunkSource().setSpawnSettings(z, z2);
    }

    protected void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0f;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0f;
            }
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        getChunkSource().close();
    }

    @Override // net.minecraft.world.level.LevelReader, net.minecraft.world.level.CollisionGetter
    @Nullable
    public BlockGetter getChunkForCollisions(int i, int i2) {
        return getChunk(i, i2, ChunkStatus.FULL, false);
    }

    @Override // net.minecraft.world.level.EntityGetter
    public List<Entity> getEntities(@Nullable Entity entity, AABB aabb, @Nullable Predicate<? super Entity> predicate) {
        getProfiler().incrementCounter("getEntities");
        List<Entity> newArrayList = Lists.newArrayList();
        int floor = Mth.floor((aabb.minX - 2.0d) / 16.0d);
        int floor2 = Mth.floor((aabb.maxX + 2.0d) / 16.0d);
        int floor3 = Mth.floor((aabb.minZ - 2.0d) / 16.0d);
        int floor4 = Mth.floor((aabb.maxZ + 2.0d) / 16.0d);
        ChunkSource chunkSource = getChunkSource();
        for (int i = floor; i <= floor2; i++) {
            for (int i2 = floor3; i2 <= floor4; i2++) {
                LevelChunk chunk = chunkSource.getChunk(i, i2, false);
                if (chunk != null) {
                    chunk.getEntities(entity, aabb, newArrayList, predicate);
                }
            }
        }
        return newArrayList;
    }

    public <T extends Entity> List<T> getEntities(@Nullable EntityType<T> entityType, AABB aabb, Predicate<? super T> predicate) {
        getProfiler().incrementCounter("getEntities");
        int floor = Mth.floor((aabb.minX - 2.0d) / 16.0d);
        int ceil = Mth.ceil((aabb.maxX + 2.0d) / 16.0d);
        int floor2 = Mth.floor((aabb.minZ - 2.0d) / 16.0d);
        int ceil2 = Mth.ceil((aabb.maxZ + 2.0d) / 16.0d);
        ArrayList newArrayList = Lists.newArrayList();
        for (int i = floor; i < ceil; i++) {
            for (int i2 = floor2; i2 < ceil2; i2++) {
                LevelChunk chunk = getChunkSource().getChunk(i, i2, false);
                if (chunk != null) {
                    chunk.getEntities( entityType, aabb,  newArrayList,  predicate);
                }
            }
        }
        return newArrayList;
    }

    @Override // net.minecraft.world.level.EntityGetter
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> cls, AABB aabb, @Nullable Predicate<? super T> predicate) {
        getProfiler().incrementCounter("getEntities");
        int floor = Mth.floor((aabb.minX - 2.0d) / 16.0d);
        int ceil = Mth.ceil((aabb.maxX + 2.0d) / 16.0d);
        int floor2 = Mth.floor((aabb.minZ - 2.0d) / 16.0d);
        int ceil2 = Mth.ceil((aabb.maxZ + 2.0d) / 16.0d);
        ArrayList newArrayList = Lists.newArrayList();
        ChunkSource chunkSource = getChunkSource();
        for (int i = floor; i < ceil; i++) {
            for (int i2 = floor2; i2 < ceil2; i2++) {
                LevelChunk chunk = chunkSource.getChunk(i, i2, false);
                if (chunk != null) {
                    chunk.getEntitiesOfClass(cls, aabb, newArrayList, predicate);
                }
            }
        }
        return newArrayList;
    }

    @Override // net.minecraft.world.level.EntityGetter
    public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> cls, AABB aabb, @Nullable Predicate<? super T> predicate) {
        getProfiler().incrementCounter("getLoadedEntities");
        int floor = Mth.floor((aabb.minX - 2.0d) / 16.0d);
        int ceil = Mth.ceil((aabb.maxX + 2.0d) / 16.0d);
        int floor2 = Mth.floor((aabb.minZ - 2.0d) / 16.0d);
        int ceil2 = Mth.ceil((aabb.maxZ + 2.0d) / 16.0d);
        ArrayList newArrayList = Lists.newArrayList();
        ChunkSource chunkSource = getChunkSource();
        for (int i = floor; i < ceil; i++) {
            for (int i2 = floor2; i2 < ceil2; i2++) {
                LevelChunk chunkNow = chunkSource.getChunkNow(i, i2);
                if (chunkNow != null) {
                    chunkNow.getEntitiesOfClass(cls, aabb, newArrayList, predicate);
                }
            }
        }
        return newArrayList;
    }

    public void blockEntityChanged(BlockPos blockPos, BlockEntity blockEntity) {
        if (hasChunkAt(blockPos)) {
            getChunkAt(blockPos).markUnsaved();
        }
    }

    @Override // net.minecraft.world.level.LevelReader
    public int getSeaLevel() {
        return 63;
    }

    public int getDirectSignalTo(BlockPos blockPos) {
        int max = Math.max(0, getDirectSignal(blockPos.below(), Direction.DOWN));
        if (max >= 15) {
            return max;
        }
        int max2 = Math.max(max, getDirectSignal(blockPos.above(), Direction.UP));
        if (max2 >= 15) {
            return max2;
        }
        int max3 = Math.max(max2, getDirectSignal(blockPos.north(), Direction.NORTH));
        if (max3 >= 15) {
            return max3;
        }
        int max4 = Math.max(max3, getDirectSignal(blockPos.south(), Direction.SOUTH));
        if (max4 >= 15) {
            return max4;
        }
        int max5 = Math.max(max4, getDirectSignal(blockPos.west(), Direction.WEST));
        if (max5 >= 15) {
            return max5;
        }
        int max6 = Math.max(max5, getDirectSignal(blockPos.east(), Direction.EAST));
        if (max6 >= 15) {
            return max6;
        }
        return max6;
    }

    public boolean hasSignal(BlockPos blockPos, Direction direction) {
        return getSignal(blockPos, direction) > 0;
    }

    public int getSignal(BlockPos blockPos, Direction direction) {
        BlockState blockState = getBlockState(blockPos);
        int signal = blockState.getSignal(this, blockPos, direction);
        if (blockState.isRedstoneConductor(this, blockPos)) {
            return Math.max(signal, getDirectSignalTo(blockPos));
        }
        return signal;
    }

    public boolean hasNeighborSignal(BlockPos blockPos) {
        if (getSignal(blockPos.below(), Direction.DOWN) > 0 || getSignal(blockPos.above(), Direction.UP) > 0 || getSignal(blockPos.north(), Direction.NORTH) > 0 || getSignal(blockPos.south(), Direction.SOUTH) > 0 || getSignal(blockPos.west(), Direction.WEST) > 0 || getSignal(blockPos.east(), Direction.EAST) > 0) {
            return true;
        }
        return false;
    }

    public int getBestNeighborSignal(BlockPos blockPos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int signal = getSignal(blockPos.relative(direction), direction);
            if (signal >= 15) {
                return 15;
            }
            if (signal > i) {
                i = signal;
            }
        }
        return i;
    }

    public void disconnect() {
    }

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public boolean mayInteract(Player player, BlockPos blockPos) {
        return true;
    }

    public void broadcastEntityEvent(Entity entity, byte b) {
    }

    public void blockEvent(BlockPos blockPos, Block block, int i, int i2) {
        getBlockState(blockPos).triggerEvent(this, blockPos, i, i2);
    }

    @Override // net.minecraft.world.level.LevelAccessor
    public LevelData getLevelData() {
        return this.levelData;
    }

    public GameRules getGameRules() {
        return this.levelData.getGameRules();
    }

    public float getThunderLevel(float f) {
        return Mth.lerp(f, this.oThunderLevel, this.thunderLevel) * getRainLevel(f);
    }

    public void setThunderLevel(float f) {
        this.oThunderLevel = f;
        this.thunderLevel = f;
    }

    public float getRainLevel(float f) {
        return Mth.lerp(f, this.oRainLevel, this.rainLevel);
    }

    public void setRainLevel(float f) {
        this.oRainLevel = f;
        this.rainLevel = f;
    }

    public boolean isThundering() {
        return dimensionType().hasSkyLight() && !dimensionType().hasCeiling() && ((double) getThunderLevel(1.0f)) > 0.9d;
    }

    public boolean isRaining() {
        return ((double) getRainLevel(1.0f)) > 0.2d;
    }

    public boolean isRainingAt(BlockPos blockPos) {
        if (!isRaining() || !canSeeSky(blockPos) || getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > blockPos.getY()) {
            return false;
        }
        Biome biome = getBiome(blockPos);
        return biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.getTemperature(blockPos) >= 0.15f;
    }

    public boolean isHumidAt(BlockPos blockPos) {
        return getBiome(blockPos).isHumid();
    }

    public void globalLevelEvent(int i, BlockPos blockPos, int i2) {
    }

    public CrashReportCategory fillReportDetails(CrashReport crashReport) {
        CrashReportCategory addCategory = crashReport.addCategory("Affected level", 1);
        addCategory.setDetail("All players", () -> {
            return players().size() + " total; " + players();
        });
        ChunkSource chunkSource = getChunkSource();
        chunkSource.getClass();
        addCategory.setDetail("Chunk stats", chunkSource::gatherStats);
        addCategory.setDetail("Level dimension", () -> {
            return dimension().location().toString();
        });
        try {
            this.levelData.fillCrashReportCategory(addCategory);
        } catch (Throwable th) {
            addCategory.setDetailError("Level Data Unobtainable", th);
        }
        return addCategory;
    }

    public void createFireworks(double d, double d2, double d3, double d4, double d5, double d6, @Nullable CompoundTag compoundTag) {
    }

    public void updateNeighbourForOutputSignal(BlockPos blockPos, Block block) {
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            BlockPos relative = blockPos.relative(next);
            if (hasChunkAt(relative)) {
                BlockState blockState = getBlockState(relative);
                if (blockState.is(Blocks.COMPARATOR)) {
                    blockState.neighborChanged(this, relative, block, blockPos, false);
                } else if (blockState.isRedstoneConductor(this, relative)) {
                    BlockPos relative2 = relative.relative(next);
                    BlockState blockState2 = getBlockState(relative2);
                    if (blockState2.is(Blocks.COMPARATOR)) {
                        blockState2.neighborChanged(this, relative2, block, blockPos, false);
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.level.LevelAccessor
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        long j = 0;
        float f = 0.0f;
        if (hasChunkAt(blockPos)) {
            f = getMoonBrightness();
            j = getChunkAt(blockPos).getInhabitedTime();
        }
        return new DifficultyInstance(getDifficulty(), getDayTime(), j, f);
    }

    @Override // net.minecraft.world.level.LevelReader
    public int getSkyDarken() {
        return this.skyDarken;
    }

    public void setSkyFlashTime(int i) {
    }

    @Override // net.minecraft.world.level.CollisionGetter
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override // net.minecraft.world.level.LevelReader
    public DimensionType dimensionType() {
        return this.dimensionType;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    @Override // net.minecraft.world.level.LevelAccessor
    public Random getRandom() {
        return this.random;
    }

    @Override // net.minecraft.world.level.LevelSimulatedReader
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        return predicate.test(getBlockState(blockPos));
    }

    public BlockPos getBlockRandomPos(int i, int i2, int i3, int i4) {
        this.randValue = (this.randValue * 3) + 1013904223;
        int i5 = this.randValue >> 2;
        return new BlockPos(i + (i5 & 15), i2 + ((i5 >> 16) & i4), i3 + ((i5 >> 8) & 15));
    }

    public boolean noSave() {
        return false;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler.get();
    }

    public Supplier<ProfilerFiller> getProfilerSupplier() {
        return this.profiler;
    }

    @Override // net.minecraft.world.level.LevelReader
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    public final boolean isDebug() {
        return this.isDebug;
    }
}
