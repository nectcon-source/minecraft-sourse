package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.DataLayerStorageMap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/DataLayerStorageMap.class */
public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
    private final long[] lastSectionKeys = new long[2];
    private final DataLayer[] lastSections = new DataLayer[2];
    private boolean cacheEnabled;
    protected final Long2ObjectOpenHashMap<DataLayer> map;

    public abstract M copy();

    protected DataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap) {
        this.map = long2ObjectOpenHashMap;
        clearCache();
        this.cacheEnabled = true;
    }

    public void copyDataLayer(long j) {
        this.map.put(j, ((DataLayer) this.map.get(j)).copy());
        clearCache();
    }

    public boolean hasLayer(long j) {
        return this.map.containsKey(j);
    }

    @Nullable
    public DataLayer getLayer(long j) {
        if (this.cacheEnabled) {
            for (int i = 0; i < 2; i++) {
                if (j == this.lastSectionKeys[i]) {
                    return this.lastSections[i];
                }
            }
        }
        DataLayer dataLayer = (DataLayer) this.map.get(j);
        if (dataLayer != null) {
            if (this.cacheEnabled) {
                for (int i2 = 1; i2 > 0; i2--) {
                    this.lastSectionKeys[i2] = this.lastSectionKeys[i2 - 1];
                    this.lastSections[i2] = this.lastSections[i2 - 1];
                }
                this.lastSectionKeys[0] = j;
                this.lastSections[0] = dataLayer;
            }
            return dataLayer;
        }
        return null;
    }

    @Nullable
    public DataLayer removeLayer(long j) {
        return (DataLayer) this.map.remove(j);
    }

    public void setLayer(long j, DataLayer dataLayer) {
        this.map.put(j, dataLayer);
    }

    public void clearCache() {
        for (int i = 0; i < 2; i++) {
            this.lastSectionKeys[i] = Long.MAX_VALUE;
            this.lastSections[i] = null;
        }
    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}
