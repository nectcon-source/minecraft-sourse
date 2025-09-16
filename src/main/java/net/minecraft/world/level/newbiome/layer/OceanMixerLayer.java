package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/OceanMixerLayer.class */
public enum OceanMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
    public int applyPixel(Context context, Area area, Area area2, int i, int i2) {
        int i3 = area.get(getParentX(i), getParentY(i2));
        int i4 = area2.get(getParentX(i), getParentY(i2));
        if (!Layers.isOcean(i3)) {
            return i3;
        }
        for (int i5 = -8; i5 <= 8; i5 += 4) {
            for (int i6 = -8; i6 <= 8; i6 += 4) {
                if (!Layers.isOcean(area.get(getParentX(i + i5), getParentY(i2 + i6)))) {
                    if (i4 == 44) {
                        return 45;
                    }
                    if (i4 == 10) {
                        return 46;
                    }
                }
            }
        }
        if (i3 == 24) {
            if (i4 == 45) {
                return 48;
            }
            if (i4 == 0) {
                return 24;
            }
            if (i4 == 46) {
                return 49;
            }
            if (i4 == 10) {
                return 50;
            }
        }
        return i4;
    }
}
