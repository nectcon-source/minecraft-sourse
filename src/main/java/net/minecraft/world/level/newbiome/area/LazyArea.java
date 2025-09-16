package net.minecraft.world.level.newbiome.area;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/area/LazyArea.class */
public final class LazyArea implements Area {
    private final PixelTransformer transformer;
    private final Long2IntLinkedOpenHashMap cache;
    private final int maxCache;

    public LazyArea(Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap, int i, PixelTransformer pixelTransformer) {
        this.cache = long2IntLinkedOpenHashMap;
        this.maxCache = i;
        this.transformer = pixelTransformer;
    }

    @Override // net.minecraft.world.level.newbiome.area.Area
    public int get(int i, int i2) {
        long asLong = ChunkPos.asLong(i, i2);
        synchronized (this.cache) {
            int i3 = this.cache.get(asLong);
            if (i3 != Integer.MIN_VALUE) {
                return i3;
            }
            int apply = this.transformer.apply(i, i2);
            this.cache.put(asLong, apply);
            if (this.cache.size() > this.maxCache) {
                for (int i4 = 0; i4 < this.maxCache / 16; i4++) {
                    this.cache.removeFirstInt();
                }
            }
            return apply;
        }
    }

    public int getMaxCache() {
        return this.maxCache;
    }
}
