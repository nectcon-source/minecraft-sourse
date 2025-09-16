package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/OceanLayer.class */
public enum OceanLayer implements AreaTransformer0 {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0
    public int applyPixel(Context context, int i, int i2) {
        double noise = context.getBiomeNoise().noise(i / 8.0d, i2 / 8.0d, 0.0d, 0.0d, 0.0d);
        if (noise > 0.4d) {
            return 44;
        }
        if (noise > 0.2d) {
            return 45;
        }
        if (noise < -0.4d) {
            return 10;
        }
        if (noise < -0.2d) {
            return 46;
        }
        return 0;
    }
}
