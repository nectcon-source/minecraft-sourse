package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/context/BigContext.class */
public interface BigContext<R extends Area> extends Context {
    void initRandom(long j, long j2);

    R createResult(PixelTransformer pixelTransformer);

    default R createResult(PixelTransformer pixelTransformer, R r) {
        return createResult(pixelTransformer);
    }

    default R createResult(PixelTransformer pixelTransformer, R r, R r2) {
        return createResult(pixelTransformer);
    }

    default int random(int i, int i2) {
        return nextRandom(2) == 0 ? i : i2;
    }

    default int random(int i, int i2, int i3, int i4) {
        int nextRandom = nextRandom(4);
        if (nextRandom == 0) {
            return i;
        }
        if (nextRandom == 1) {
            return i2;
        }
        if (nextRandom == 2) {
            return i3;
        }
        return i4;
    }
}
