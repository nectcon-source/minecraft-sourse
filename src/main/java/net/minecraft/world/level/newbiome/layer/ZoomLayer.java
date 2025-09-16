package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/ZoomLayer.class */
public enum ZoomLayer implements AreaTransformer1 {
    NORMAL,
    FUZZY { // from class: net.minecraft.world.level.newbiome.layer.ZoomLayer.1
        @Override // net.minecraft.world.level.newbiome.layer.ZoomLayer
        protected int modeOrRandom(BigContext<?> bigContext, int i, int i2, int i3, int i4) {
            return bigContext.random(i, i2, i3, i4);
        }
    };

    @Override // net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer
    public int getParentX(int i) {
        return i >> 1;
    }

    @Override // net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer
    public int getParentY(int i) {
        return i >> 1;
    }

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1
    public int applyPixel(BigContext<?> bigContext, Area area, int i, int i2) {
        int i3 = area.get(getParentX(i), getParentY(i2));
        bigContext.initRandom((i >> 1) << 1, (i2 >> 1) << 1);
        int i4 = i & 1;
        int i5 = i2 & 1;
        if (i4 == 0 && i5 == 0) {
            return i3;
        }
        int i6 = area.get(getParentX(i), getParentY(i2 + 1));
        int random = bigContext.random(i3, i6);
        if (i4 == 0 && i5 == 1) {
            return random;
        }
        int i7 = area.get(getParentX(i + 1), getParentY(i2));
        int random2 = bigContext.random(i3, i7);
        if (i4 == 1 && i5 == 0) {
            return random2;
        }
        return modeOrRandom(bigContext, i3, i7, i6, area.get(getParentX(i + 1), getParentY(i2 + 1)));
    }

    protected int modeOrRandom(BigContext<?> bigContext, int i, int i2, int i3, int i4) {
        if (i2 == i3 && i3 == i4) {
            return i2;
        }
        if (i == i2 && i == i3) {
            return i;
        }
        if (i == i2 && i == i4) {
            return i;
        }
        if (i == i3 && i == i4) {
            return i;
        }
        if (i == i2 && i3 != i4) {
            return i;
        }
        if (i == i3 && i2 != i4) {
            return i;
        }
        if (i == i4 && i2 != i3) {
            return i;
        }
        if (i2 == i3 && i != i4) {
            return i2;
        }
        if (i2 == i4 && i != i3) {
            return i2;
        }
        if (i3 == i4 && i != i2) {
            return i3;
        }
        return bigContext.random(i, i2, i3, i4);
    }
}
