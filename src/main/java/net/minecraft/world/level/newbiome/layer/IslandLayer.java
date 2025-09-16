package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/IslandLayer.class */
public enum IslandLayer implements AreaTransformer0 {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0
    public int applyPixel(Context context, int i, int i2) {
        return ((i == 0 && i2 == 0) || context.nextRandom(10) == 0) ? 1 : 0;
    }
}
