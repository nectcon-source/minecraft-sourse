package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/SmoothLayer.class */
public enum SmoothLayer implements CastleTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        boolean z = i2 == i4;
        if (z != (i == i3)) {
            return z ? i4 : i;
        }
        if (z) {
            return context.nextRandom(2) == 0 ? i4 : i;
        }
        return i5;
    }
}
