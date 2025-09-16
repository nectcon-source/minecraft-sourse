package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/RiverMixerLayer.class */
public enum RiverMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
    public int applyPixel(Context context, Area area, Area area2, int i, int i2) {
        int i3 = area.get(getParentX(i), getParentY(i2));
        int i4 = area2.get(getParentX(i), getParentY(i2));
        if (Layers.isOcean(i3)) {
            return i3;
        }
        if (i4 == 7) {
            if (i3 == 12) {
                return 11;
            }
            if (i3 == 14 || i3 == 15) {
                return 15;
            }
            return i4 & 255;
        }
        return i3;
    }
}
