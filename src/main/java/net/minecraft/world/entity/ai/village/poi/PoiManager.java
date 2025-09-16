package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/poi/PoiManager.class */
public class PoiManager extends SectionStorage<PoiSection> {
    private final DistanceTracker distanceTracker;
    private final LongSet loadedChunks;

    public PoiManager(File file, DataFixer dataFixer, boolean z) {
        super(file, PoiSection::codec, PoiSection::new, dataFixer, DataFixTypes.POI_CHUNK, z);
        this.loadedChunks = new LongOpenHashSet();
        this.distanceTracker = new DistanceTracker();
    }

    public void add(BlockPos blockPos, PoiType poiType) {
        getOrCreate(SectionPos.of(blockPos).asLong()).add(blockPos, poiType);
    }

    public void remove(BlockPos blockPos) {
        getOrCreate(SectionPos.of(blockPos).asLong()).remove(blockPos);
    }

    public long getCountInRange(Predicate<PoiType> predicate, BlockPos blockPos, int i, Occupancy occupancy) {
        return getInRange(predicate, blockPos, i, occupancy).count();
    }

    public boolean existsAtPosition(PoiType poiType, BlockPos blockPos) {
        Optional<PoiType> type = getOrCreate(SectionPos.of(blockPos).asLong()).getType(blockPos);
        return type.isPresent() && type.get().equals(poiType);
    }

    public Stream<PoiRecord> getInSquare(Predicate<PoiType> predicate, BlockPos blockPos, int i, Occupancy occupancy) {
        return ChunkPos.rangeClosed(new ChunkPos(blockPos), Math.floorDiv(i, 16) + 1).flatMap(chunkPos -> {
            return getInChunk(predicate, chunkPos, occupancy);
        }).filter(poiRecord -> {
            BlockPos pos = poiRecord.getPos();
            return Math.abs(pos.getX() - blockPos.getX()) <= i && Math.abs(pos.getZ() - blockPos.getZ()) <= i;
        });
    }

    public Stream<PoiRecord> getInRange(Predicate<PoiType> predicate, BlockPos blockPos, int i, Occupancy occupancy) {
        int i2 = i * i;
        return getInSquare(predicate, blockPos, i, occupancy).filter(poiRecord -> {
            return poiRecord.getPos().distSqr(blockPos) <= ((double) i2);
        });
    }

    public Stream<PoiRecord> getInChunk(Predicate<PoiType> predicate, ChunkPos chunkPos, Occupancy occupancy) {
        return IntStream.range(0, 16).boxed().map(num -> {
            return getOrLoad(SectionPos.of(chunkPos, num.intValue()).asLong());
        }).filter((v0) -> {
            return v0.isPresent();
        }).flatMap(optional -> {
            return ((PoiSection) optional.get()).getRecords(predicate, occupancy);
        });
    }

    public Stream<BlockPos> findAll(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i, Occupancy occupancy) {
        return getInRange(predicate, blockPos, i, occupancy).map((v0) -> {
            return v0.getPos();
        }).filter(predicate2);
    }

    public Stream<BlockPos> findAllClosestFirst(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i, Occupancy occupancy) {
        return findAll(predicate, predicate2, blockPos, i, occupancy).sorted(Comparator.comparingDouble(blockPos2 -> {
            return blockPos2.distSqr(blockPos);
        }));
    }

    public Optional<BlockPos> find(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i, Occupancy occupancy) {
        return findAll(predicate, predicate2, blockPos, i, occupancy).findFirst();
    }

    public Optional<BlockPos> findClosest(Predicate<PoiType> predicate, BlockPos blockPos, int i, Occupancy occupancy) {
        return getInRange(predicate, blockPos, i, occupancy).map((v0) -> {
            return v0.getPos();
        }).min(Comparator.comparingDouble(blockPos2 -> {
            return blockPos2.distSqr(blockPos);
        }));
    }

    public Optional<BlockPos> take(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i) {
        return getInRange(predicate, blockPos, i, Occupancy.HAS_SPACE).filter(poiRecord -> {
            return predicate2.test(poiRecord.getPos());
        }).findFirst().map(poiRecord2 -> {
            poiRecord2.acquireTicket();
            return poiRecord2.getPos();
        });
    }

    public Optional<BlockPos> getRandom(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, Occupancy occupancy, BlockPos blockPos, int i, Random random) {
        List<PoiRecord> list = (List) getInRange(predicate, blockPos, i, occupancy).collect(Collectors.toList());
        Collections.shuffle(list, random);
        return list.stream().filter(poiRecord -> {
            return predicate2.test(poiRecord.getPos());
        }).findFirst().map((v0) -> {
            return v0.getPos();
        });
    }

    public boolean release(BlockPos blockPos) {
        return getOrCreate(SectionPos.of(blockPos).asLong()).release(blockPos);
    }

    public boolean exists(BlockPos blockPos, Predicate<PoiType> predicate) {
        return ((Boolean) getOrLoad(SectionPos.of(blockPos).asLong()).map(poiSection -> {
            return Boolean.valueOf(poiSection.exists(blockPos, predicate));
        }).orElse(false)).booleanValue();
    }

    public Optional<PoiType> getType(BlockPos blockPos) {
        return getOrCreate(SectionPos.of(blockPos).asLong()).getType(blockPos);
    }

    public int sectionsToVillage(SectionPos sectionPos) {
        this.distanceTracker.runAllUpdates();
        return this.distanceTracker.getLevel(sectionPos.asLong());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isVillageCenter(long j) {
        Optional<PoiSection> optional = get(j);
        if (optional == null) {
            return false;
        }
        return ((Boolean) optional.map(poiSection -> {
            return Boolean.valueOf(poiSection.getRecords(PoiType.ALL, Occupancy.IS_OCCUPIED).count() > 0);
        }).orElse(false)).booleanValue();
    }

    @Override // net.minecraft.world.level.chunk.storage.SectionStorage
    public void tick(BooleanSupplier booleanSupplier) {
        super.tick(booleanSupplier);
        this.distanceTracker.runAllUpdates();
    }

    @Override // net.minecraft.world.level.chunk.storage.SectionStorage
    protected void setDirty(long j) {
        super.setDirty(j);
        this.distanceTracker.update(j, this.distanceTracker.getLevelFromSource(j), false);
    }

    @Override // net.minecraft.world.level.chunk.storage.SectionStorage
    protected void onSectionLoad(long j) {
        this.distanceTracker.update(j, this.distanceTracker.getLevelFromSource(j), false);
    }

    public void checkConsistencyWithBlocks(ChunkPos chunkPos, LevelChunkSection levelChunkSection) {
        SectionPos m37of = SectionPos.of(chunkPos, levelChunkSection.bottomBlockY() >> 4);
        Util.ifElse(getOrLoad(m37of.asLong()), poiSection -> {
            poiSection.refresh(biConsumer -> {
                if (mayHavePoi(levelChunkSection)) {
                    updateFromSection(levelChunkSection, m37of, biConsumer);
                }
            });
        }, () -> {
            if (mayHavePoi(levelChunkSection)) {
                PoiSection orCreate = getOrCreate(m37of.asLong());
                orCreate.getClass();
                updateFromSection(levelChunkSection, m37of, orCreate::add);
            }
        });
    }

    private static boolean mayHavePoi(LevelChunkSection levelChunkSection) {
        return levelChunkSection.maybeHas(PoiType.ALL_STATES::contains);
    }

    private void updateFromSection(LevelChunkSection levelChunkSection, SectionPos sectionPos, BiConsumer<BlockPos, PoiType> biConsumer) {
        sectionPos.blocksInside().forEach(blockPos -> {
            PoiType.forState(levelChunkSection.getBlockState(SectionPos.sectionRelative(blockPos.getX()), SectionPos.sectionRelative(blockPos.getY()), SectionPos.sectionRelative(blockPos.getZ()))).ifPresent(poiType -> {
                biConsumer.accept(blockPos, poiType);
            });
        });
    }

    public void ensureLoadedAndValid(LevelReader levelReader, BlockPos blockPos, int i) {
        SectionPos.aroundChunk(new ChunkPos(blockPos), Math.floorDiv(i, 16))
                .map(var1x -> Pair.of(var1x, this.getOrLoad(var1x.asLong())))
                .filter(var0 -> !(var0.getSecond()).map(PoiSection::isValid).orElse(false))
                .map(var0 -> (var0.getFirst()).chunk())
                .filter(var1x -> this.loadedChunks.add(var1x.toLong()))
                .forEach(var1x -> levelReader.getChunk(var1x.x, var1x.z, ChunkStatus.EMPTY));
    }



    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/village/poi/PoiManager$DistanceTracker.class */
    final class DistanceTracker extends SectionTracker {
        private final Long2ByteMap levels;

        protected DistanceTracker() {
            super(7, 16, 256);
            this.levels = new Long2ByteOpenHashMap();
            this.levels.defaultReturnValue((byte) 7);
        }

        @Override // net.minecraft.server.level.SectionTracker
        protected int getLevelFromSource(long j) {
            return PoiManager.this.isVillageCenter(j) ? 0 : 7;
        }

        @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
        protected int getLevel(long j) {
            return this.levels.get(j);
        }

        @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
        protected void setLevel(long j, int i) {
            if (i > 6) {
                this.levels.remove(j);
            } else {
                this.levels.put(j, (byte) i);
            }
        }

        public void runAllUpdates() {
            super.runUpdates(Integer.MAX_VALUE);
        }
    }
    public static enum Occupancy {
        HAS_SPACE(PoiRecord::hasSpace),
        IS_OCCUPIED(PoiRecord::isOccupied),
        ANY(var0 -> true);

        private final Predicate<? super PoiRecord> test;

        private Occupancy(Predicate<? super PoiRecord> var3) {
            this.test = var3;
        }

        public Predicate<? super PoiRecord> getTest() {
            return this.test;
        }
    }
}
