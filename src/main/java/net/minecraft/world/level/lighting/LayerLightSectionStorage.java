package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/LayerLightSectionStorage.class */
public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> extends SectionTracker {
    protected static final DataLayer EMPTY_DATA = new DataLayer();
    private static final Direction[] DIRECTIONS = Direction.values();
    private final LightLayer layer;
    private final LightChunkGetter chunkSource;
    protected final LongSet dataSectionSet;
    protected final LongSet toMarkNoData;
    protected final LongSet toMarkData;
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections;
    protected final LongSet sectionsAffectedByLightUpdates;
    protected final Long2ObjectMap<DataLayer> queuedSections;
    private final LongSet untrustedSections;
    private final LongSet columnsToRetainQueuedDataFor;
    private final LongSet toRemove;
    protected volatile boolean hasToRemove;

    protected abstract int getLightValue(long j);

    protected LayerLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, M m) {
        super(3, 16, 256);
        this.dataSectionSet = new LongOpenHashSet();
        this.toMarkNoData = new LongOpenHashSet();
        this.toMarkData = new LongOpenHashSet();
        this.changedSections = new LongOpenHashSet();
        this.sectionsAffectedByLightUpdates = new LongOpenHashSet();
        this.queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
        this.untrustedSections = new LongOpenHashSet();
        this.columnsToRetainQueuedDataFor = new LongOpenHashSet();
        this.toRemove = new LongOpenHashSet();
        this.layer = lightLayer;
        this.chunkSource = lightChunkGetter;
        this.updatingSectionData = m;
        this.visibleSectionData = (M) m.copy();
        this.visibleSectionData.disableCache();
    }

    protected boolean storingLightForSection(long j) {
        return getDataLayer(j, true) != null;
    }

    @Nullable
    protected DataLayer getDataLayer(long j, boolean z) {
        return getDataLayer((z ? this.updatingSectionData : this.visibleSectionData), j);
    }

    @Nullable
    protected DataLayer getDataLayer(M m, long j) {
        return m.getLayer(j);
    }

    @Nullable
    public DataLayer getDataLayerData(long j) {
        DataLayer dataLayer = (DataLayer) this.queuedSections.get(j);
        if (dataLayer != null) {
            return dataLayer;
        }
        return getDataLayer(j, false);
    }

    protected int getStoredLevel(long j) {
        return getDataLayer(SectionPos.blockToSection(j), true).get(SectionPos.sectionRelative(BlockPos.getX(j)), SectionPos.sectionRelative(BlockPos.getY(j)), SectionPos.sectionRelative(BlockPos.getZ(j)));
    }

    protected void setStoredLevel(long j, int i) {
        long blockToSection = SectionPos.blockToSection(j);
        if (this.changedSections.add(blockToSection)) {
            this.updatingSectionData.copyDataLayer(blockToSection);
        }
        getDataLayer(blockToSection, true).set(SectionPos.sectionRelative(BlockPos.getX(j)), SectionPos.sectionRelative(BlockPos.getY(j)), SectionPos.sectionRelative(BlockPos.getZ(j)), i);
        for (int i2 = -1; i2 <= 1; i2++) {
            for (int i3 = -1; i3 <= 1; i3++) {
                for (int i4 = -1; i4 <= 1; i4++) {
                    this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(j, i3, i4, i2)));
                }
            }
        }
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int getLevel(long j) {
        if (j == Long.MAX_VALUE) {
            return 2;
        }
        if (this.dataSectionSet.contains(j)) {
            return 0;
        }
        if (!this.toRemove.contains(j) && this.updatingSectionData.hasLayer(j)) {
            return 1;
        }
        return 2;
    }

    @Override // net.minecraft.server.level.SectionTracker
    protected int getLevelFromSource(long j) {
        if (this.toMarkNoData.contains(j)) {
            return 2;
        }
        if (this.dataSectionSet.contains(j) || this.toMarkData.contains(j)) {
            return 0;
        }
        return 2;
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected void setLevel(long j, int i) {
        int level = getLevel(j);
        if (level != 0 && i == 0) {
            this.dataSectionSet.add(j);
            this.toMarkData.remove(j);
        }
        if (level == 0 && i != 0) {
            this.dataSectionSet.remove(j);
            this.toMarkNoData.remove(j);
        }
        if (level >= 2 && i != 2) {
            if (this.toRemove.contains(j)) {
                this.toRemove.remove(j);
            } else {
                this.updatingSectionData.setLayer(j, createDataLayer(j));
                this.changedSections.add(j);
                onNodeAdded(j);
                for (int i2 = -1; i2 <= 1; i2++) {
                    for (int i3 = -1; i3 <= 1; i3++) {
                        for (int i4 = -1; i4 <= 1; i4++) {
                            this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(j, i3, i4, i2)));
                        }
                    }
                }
            }
        }
        if (level != 2 && i >= 2) {
            this.toRemove.add(j);
        }
        this.hasToRemove = !this.toRemove.isEmpty();
    }

    protected DataLayer createDataLayer(long j) {
        DataLayer dataLayer = (DataLayer) this.queuedSections.get(j);
        if (dataLayer != null) {
            return dataLayer;
        }
        return new DataLayer();
    }

    protected void clearQueuedSectionBlocks(LayerLightEngine<?, ?> layerLightEngine, long j) {
        if (layerLightEngine.getQueueSize() < 8192) {
            layerLightEngine.removeIf(j2 -> {
                return SectionPos.blockToSection(j2) == j;
            });
            return;
        }
        int sectionToBlockCoord = SectionPos.sectionToBlockCoord(SectionPos.x(j));
        int sectionToBlockCoord2 = SectionPos.sectionToBlockCoord(SectionPos.y(j));
        int sectionToBlockCoord3 = SectionPos.sectionToBlockCoord(SectionPos.z(j));
        for (int i = 0; i < 16; i++) {
            for (int i2 = 0; i2 < 16; i2++) {
                for (int i3 = 0; i3 < 16; i3++) {
                    layerLightEngine.removeFromQueue(BlockPos.asLong(sectionToBlockCoord + i, sectionToBlockCoord2 + i2, sectionToBlockCoord3 + i3));
                }
            }
        }
    }

    protected boolean hasInconsistencies() {
        return this.hasToRemove;
    }

    protected void markNewInconsistencies(LayerLightEngine<M, ?> layerLightEngine, boolean z, boolean z2) {
        DataLayer dataLayer;
        if (!hasInconsistencies() && this.queuedSections.isEmpty()) {
            return;
        }
        LongIterator it = this.toRemove.iterator();
        while (it.hasNext()) {
            long longValue =  it.next().longValue();
            clearQueuedSectionBlocks(layerLightEngine, longValue);
            DataLayer dataLayer2 =  this.queuedSections.remove(longValue);
            DataLayer removeLayer = this.updatingSectionData.removeLayer(longValue);
            if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(longValue))) {
                if (dataLayer2 != null) {
                    this.queuedSections.put(longValue, dataLayer2);
                } else if (removeLayer != null) {
                    this.queuedSections.put(longValue, removeLayer);
                }
            }
        }
        this.updatingSectionData.clearCache();
        LongIterator it2 = this.toRemove.iterator();
        while (it2.hasNext()) {
            onNodeRemoved(((Long) it2.next()).longValue());
        }
        this.toRemove.clear();
        this.hasToRemove = false;
        ObjectIterator it3 = this.queuedSections.long2ObjectEntrySet().iterator();
        while (it3.hasNext()) {
            Long2ObjectMap.Entry<DataLayer> entry = (Long2ObjectMap.Entry) it3.next();
            long longKey = entry.getLongKey();
            if (storingLightForSection(longKey) && this.updatingSectionData.getLayer(longKey) != (dataLayer = (DataLayer) entry.getValue())) {
                clearQueuedSectionBlocks(layerLightEngine, longKey);
                this.updatingSectionData.setLayer(longKey, dataLayer);
                this.changedSections.add(longKey);
            }
        }
        this.updatingSectionData.clearCache();
        if (!z2) {
            LongIterator it4 = this.queuedSections.keySet().iterator();
            while (it4.hasNext()) {
                checkEdgesForSection(layerLightEngine,  it4.next().longValue());
            }
        } else {
            LongIterator it5 = this.untrustedSections.iterator();
            while (it5.hasNext()) {
                checkEdgesForSection(layerLightEngine,  it5.next().longValue());
            }
        }
        this.untrustedSections.clear();
        ObjectIterator<Long2ObjectMap.Entry<DataLayer>> it6 = this.queuedSections.long2ObjectEntrySet().iterator();
        while (it6.hasNext()) {
            if (storingLightForSection( it6.next().getLongKey())) {
                it6.remove();
            }
        }
    }

    private void checkEdgesForSection(LayerLightEngine<M, ?> layerLightEngine, long j) {
        long asLong;
        long asLong2;
        if (!storingLightForSection(j)) {
            return;
        }
        int sectionToBlockCoord = SectionPos.sectionToBlockCoord(SectionPos.x(j));
        int sectionToBlockCoord2 = SectionPos.sectionToBlockCoord(SectionPos.y(j));
        int sectionToBlockCoord3 = SectionPos.sectionToBlockCoord(SectionPos.z(j));
        for (Direction direction : DIRECTIONS) {
            long offset = SectionPos.offset(j, direction);
            if (!this.queuedSections.containsKey(offset) && storingLightForSection(offset)) {
                for (int i = 0; i < 16; i++) {
                    for (int i2 = 0; i2 < 16; i2++) {
                        switch (direction) {
                            case DOWN:
                                asLong = BlockPos.asLong(sectionToBlockCoord + i2, sectionToBlockCoord2, sectionToBlockCoord3 + i);
                                asLong2 = BlockPos.asLong(sectionToBlockCoord + i2, sectionToBlockCoord2 - 1, sectionToBlockCoord3 + i);
                                break;
                            case UP:
                                asLong = BlockPos.asLong(sectionToBlockCoord + i2, (sectionToBlockCoord2 + 16) - 1, sectionToBlockCoord3 + i);
                                asLong2 = BlockPos.asLong(sectionToBlockCoord + i2, sectionToBlockCoord2 + 16, sectionToBlockCoord3 + i);
                                break;
                            case NORTH:
                                asLong = BlockPos.asLong(sectionToBlockCoord + i, sectionToBlockCoord2 + i2, sectionToBlockCoord3);
                                asLong2 = BlockPos.asLong(sectionToBlockCoord + i, sectionToBlockCoord2 + i2, sectionToBlockCoord3 - 1);
                                break;
                            case SOUTH:
                                asLong = BlockPos.asLong(sectionToBlockCoord + i, sectionToBlockCoord2 + i2, (sectionToBlockCoord3 + 16) - 1);
                                asLong2 = BlockPos.asLong(sectionToBlockCoord + i, sectionToBlockCoord2 + i2, sectionToBlockCoord3 + 16);
                                break;
                            case WEST:
                                asLong = BlockPos.asLong(sectionToBlockCoord, sectionToBlockCoord2 + i, sectionToBlockCoord3 + i2);
                                asLong2 = BlockPos.asLong(sectionToBlockCoord - 1, sectionToBlockCoord2 + i, sectionToBlockCoord3 + i2);
                                break;
                            default:
                                asLong = BlockPos.asLong((sectionToBlockCoord + 16) - 1, sectionToBlockCoord2 + i, sectionToBlockCoord3 + i2);
                                asLong2 = BlockPos.asLong(sectionToBlockCoord + 16, sectionToBlockCoord2 + i, sectionToBlockCoord3 + i2);
                                break;
                        }
                        long j2 = asLong2;
                        layerLightEngine.checkEdge(asLong, j2, layerLightEngine.computeLevelFromNeighbor(asLong, j2, layerLightEngine.getLevel(asLong)), false);
                        layerLightEngine.checkEdge(j2, asLong, layerLightEngine.computeLevelFromNeighbor(j2, asLong, layerLightEngine.getLevel(j2)), false);
                    }
                }
            }
        }
    }

    protected void onNodeAdded(long j) {
    }

    protected void onNodeRemoved(long j) {
    }

    protected void enableLightSources(long j, boolean z) {
    }

    public void retainData(long j, boolean z) {
        if (z) {
            this.columnsToRetainQueuedDataFor.add(j);
        } else {
            this.columnsToRetainQueuedDataFor.remove(j);
        }
    }

    protected void queueSectionData(long j, @Nullable DataLayer dataLayer, boolean z) {
        if (dataLayer != null) {
            this.queuedSections.put(j, dataLayer);
            if (!z) {
                this.untrustedSections.add(j);
                return;
            }
            return;
        }
        this.queuedSections.remove(j);
    }

    protected void updateSectionStatus(long j, boolean z) {
        boolean contains = this.dataSectionSet.contains(j);
        if (!contains && !z) {
            this.toMarkData.add(j);
            checkEdge(Long.MAX_VALUE, j, 0, true);
        }
        if (contains && z) {
            this.toMarkNoData.add(j);
            checkEdge(Long.MAX_VALUE, j, 2, false);
        }
    }

    protected void runAllUpdates() {
        if (hasWork()) {
            runUpdates(Integer.MAX_VALUE);
        }
    }

    protected void swapSectionMap() {
        if (!this.changedSections.isEmpty()) {
            M m = (M) this.updatingSectionData.copy();
            m.disableCache();
            this.visibleSectionData = m;
            this.changedSections.clear();
        }
        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            LongIterator it = this.sectionsAffectedByLightUpdates.iterator();
            while (it.hasNext()) {
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(it.nextLong()));
            }
            this.sectionsAffectedByLightUpdates.clear();
        }
    }
}
