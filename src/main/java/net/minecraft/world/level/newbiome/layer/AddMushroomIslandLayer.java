package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/AddMushroomIslandLayer.class */
public enum AddMushroomIslandLayer implements BishopTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.BishopTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        if (Layers.isShallowOcean(i5) && Layers.isShallowOcean(i4) && Layers.isShallowOcean(i) && Layers.isShallowOcean(i3) && Layers.isShallowOcean(i2) && context.nextRandom(100) == 0) {
            return 14;
        }
        return i5;
    }
}
