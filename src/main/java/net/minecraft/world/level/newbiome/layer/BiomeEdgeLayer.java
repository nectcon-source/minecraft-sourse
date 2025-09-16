package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/BiomeEdgeLayer.class */
public enum BiomeEdgeLayer implements CastleTransformer {
    INSTANCE;

    @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        int[] iArr = new int[1];
        if (checkEdge(iArr, i5) || checkEdgeStrict(iArr, i, i2, i3, i4, i5, 38, 37) || checkEdgeStrict(iArr, i, i2, i3, i4, i5, 39, 37) || checkEdgeStrict(iArr, i, i2, i3, i4, i5, 32, 5)) {
            return iArr[0];
        }
        if (i5 == 2 && (i == 12 || i2 == 12 || i4 == 12 || i3 == 12)) {
            return 34;
        }
        if (i5 == 6) {
            if (i == 2 || i2 == 2 || i4 == 2 || i3 == 2 || i == 30 || i2 == 30 || i4 == 30 || i3 == 30 || i == 12 || i2 == 12 || i4 == 12 || i3 == 12) {
                return 1;
            }
            if (i == 21 || i3 == 21 || i2 == 21 || i4 == 21 || i == 168 || i3 == 168 || i2 == 168 || i4 == 168) {
                return 23;
            }
        }
        return i5;
    }

    private boolean checkEdge(int[] iArr, int i) {
        if (!Layers.isSame(i, 3)) {
            return false;
        }
        iArr[0] = i;
        return true;
    }

    private boolean checkEdgeStrict(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (i5 != i6) {
            return false;
        }
        if (Layers.isSame(i, i6) && Layers.isSame(i2, i6) && Layers.isSame(i4, i6) && Layers.isSame(i3, i6)) {
            iArr[0] = i5;
            return true;
        }
        iArr[0] = i7;
        return true;
    }
}
