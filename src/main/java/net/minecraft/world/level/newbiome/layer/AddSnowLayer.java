package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddSnowLayer.class */
public enum AddSnowLayer implements C1Transformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.C1Transformer
    public int apply(Context context, int i) {
        if (Layers.isShallowOcean(i)) {
            return i;
        }
        int nextRandom = context.nextRandom(6);
        if (nextRandom == 0) {
            return 4;
        }
        if (nextRandom == 1) {
            return 3;
        }
        return 1;
    }
}
