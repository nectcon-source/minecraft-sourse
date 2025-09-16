package net.minecraft.world.level.newbiome.context;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.Random;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/context/LazyAreaContext.class */
public class LazyAreaContext implements BigContext<LazyArea> {
    private final Long2IntLinkedOpenHashMap cache = new Long2IntLinkedOpenHashMap(16, 0.25f);
    private final int maxCache;
    private final ImprovedNoise biomeNoise;
    private final long seed;
    private long rval;

    public LazyAreaContext(int i, long j, long j2) {
        this.seed = mixSeed(j, j2);
        this.biomeNoise = new ImprovedNoise(new Random(j));
        this.cache.defaultReturnValue(Integer.MIN_VALUE);
        this.maxCache = i;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.level.newbiome.context.BigContext
    public LazyArea createResult(PixelTransformer pixelTransformer) {
        return new LazyArea(this.cache, this.maxCache, pixelTransformer);
    }

    @Override // net.minecraft.world.level.newbiome.context.BigContext
    public LazyArea createResult(PixelTransformer pixelTransformer, LazyArea lazyArea) {
        return new LazyArea(this.cache, Math.min(1024, lazyArea.getMaxCache() * 4), pixelTransformer);
    }

    @Override // net.minecraft.world.level.newbiome.context.BigContext
    public LazyArea createResult(PixelTransformer pixelTransformer, LazyArea lazyArea, LazyArea lazyArea2) {
        return new LazyArea(this.cache, Math.min(1024, Math.max(lazyArea.getMaxCache(), lazyArea2.getMaxCache()) * 4), pixelTransformer);
    }

    @Override // net.minecraft.world.level.newbiome.context.BigContext
    public void initRandom(long j, long j2) {
        this.rval = LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(this.seed, j), j2), j), j2);
    }

    @Override // net.minecraft.world.level.newbiome.context.Context
    public int nextRandom(int i) {
        int floorMod = (int) Math.floorMod(this.rval >> 24, i);
        this.rval = LinearCongruentialGenerator.next(this.rval, this.seed);
        return floorMod;
    }

    @Override // net.minecraft.world.level.newbiome.context.Context
    public ImprovedNoise getBiomeNoise() {
        return this.biomeNoise;
    }

    private static long mixSeed(long j, long j2) {
        long next = LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(j2, j2), j2), j2);
        return LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(LinearCongruentialGenerator.next(j, next), next), next);
    }
}
