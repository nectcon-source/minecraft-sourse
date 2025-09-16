package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/RiverLayer.class */
public enum RiverLayer implements CastleTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        int riverFilter = riverFilter(i5);
        if (riverFilter == riverFilter(i4) && riverFilter == riverFilter(i) && riverFilter == riverFilter(i2) && riverFilter == riverFilter(i3)) {
            return -1;
        }
        return 7;
    }

    private static int riverFilter(int i) {
        if (i >= 2) {
            return 2 + (i & 1);
        }
        return i;
    }
}
