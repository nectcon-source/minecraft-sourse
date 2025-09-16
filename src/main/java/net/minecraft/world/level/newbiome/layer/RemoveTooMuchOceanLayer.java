package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/RemoveTooMuchOceanLayer.class */
public enum RemoveTooMuchOceanLayer implements CastleTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        if (Layers.isShallowOcean(i5) && Layers.isShallowOcean(i) && Layers.isShallowOcean(i2) && Layers.isShallowOcean(i4) && Layers.isShallowOcean(i3) && context.nextRandom(2) == 0) {
            return 1;
        }
        return i5;
    }
}
