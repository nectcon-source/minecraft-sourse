package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddIslandLayer.class */
public enum AddIslandLayer implements BishopTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.BishopTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        if (Layers.isShallowOcean(i5) && (!Layers.isShallowOcean(i4) || !Layers.isShallowOcean(i3) || !Layers.isShallowOcean(i) || !Layers.isShallowOcean(i2))) {
            int i6 = 1;
            int i7 = 1;
            if (!Layers.isShallowOcean(i4)) {
                i6 = 1 + 1;
                if (context.nextRandom(1) == 0) {
                    i7 = i4;
                }
            }
            if (!Layers.isShallowOcean(i3)) {
                int i8 = i6;
                i6++;
                if (context.nextRandom(i8) == 0) {
                    i7 = i3;
                }
            }
            if (!Layers.isShallowOcean(i)) {
                int i9 = i6;
                i6++;
                if (context.nextRandom(i9) == 0) {
                    i7 = i;
                }
            }
            if (!Layers.isShallowOcean(i2)) {
                int i10 = i6;
                int i11 = i6 + 1;
                if (context.nextRandom(i10) == 0) {
                    i7 = i2;
                }
            }
            if (context.nextRandom(3) == 0) {
                return i7;
            }
            if (i7 == 4) {
                return 4;
            }
            return i5;
        }
        if (!Layers.isShallowOcean(i5) && ((Layers.isShallowOcean(i4) || Layers.isShallowOcean(i) || Layers.isShallowOcean(i3) || Layers.isShallowOcean(i2)) && context.nextRandom(5) == 0)) {
            if (Layers.isShallowOcean(i4)) {
                if (i5 == 4) {
                    return 4;
                }
                return i4;
            }
            if (Layers.isShallowOcean(i)) {
                if (i5 == 4) {
                    return 4;
                }
                return i;
            }
            if (Layers.isShallowOcean(i3)) {
                if (i5 == 4) {
                    return 4;
                }
                return i3;
            }
            if (Layers.isShallowOcean(i2)) {
                if (i5 == 4) {
                    return 4;
                }
                return i2;
            }
        }
        return i5;
    }
}
