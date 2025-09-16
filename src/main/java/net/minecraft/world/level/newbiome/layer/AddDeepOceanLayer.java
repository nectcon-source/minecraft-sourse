package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddDeepOceanLayer.class */
public enum AddDeepOceanLayer implements CastleTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        if (Layers.isShallowOcean(i5)) {
            int i6 = 0;
            if (Layers.isShallowOcean(i)) {
                i6 = 0 + 1;
            }
            if (Layers.isShallowOcean(i2)) {
                i6++;
            }
            if (Layers.isShallowOcean(i4)) {
                i6++;
            }
            if (Layers.isShallowOcean(i3)) {
                i6++;
            }
            if (i6 > 3) {
                if (i5 == 44) {
                    return 47;
                }
                if (i5 == 45) {
                    return 48;
                }
                if (i5 == 0) {
                    return 24;
                }
                if (i5 == 46) {
                    return 49;
                }
                if (i5 == 10) {
                    return 50;
                }
                return 24;
            }
        }
        return i5;
    }
}
