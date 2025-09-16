package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/ShoreLayer.class */
public enum ShoreLayer implements CastleTransformer {
    INSTANCE;

    private static final IntSet SNOWY = new IntOpenHashSet(new int[]{26, 11, 12, 13, 140, 30, 31, 158, 10});
    private static final IntSet JUNGLES = new IntOpenHashSet(new int[]{168, 169, 21, 22, 23, 149, 151});

    @Override // net.minecraft.world.level.newbiome.layer.traits.CastleTransformer
    public int apply(Context context, int i, int i2, int i3, int i4, int i5) {
        if (i5 == 14) {
            if (Layers.isShallowOcean(i) || Layers.isShallowOcean(i2) || Layers.isShallowOcean(i3) || Layers.isShallowOcean(i4)) {
                return 15;
            }
        } else if (JUNGLES.contains(i5)) {
            if (!isJungleCompatible(i) || !isJungleCompatible(i2) || !isJungleCompatible(i3) || !isJungleCompatible(i4)) {
                return 23;
            }
            if (Layers.isOcean(i) || Layers.isOcean(i2) || Layers.isOcean(i3) || Layers.isOcean(i4)) {
                return 16;
            }
        } else if (i5 == 3 || i5 == 34 || i5 == 20) {
            if (!Layers.isOcean(i5) && (Layers.isOcean(i) || Layers.isOcean(i2) || Layers.isOcean(i3) || Layers.isOcean(i4))) {
                return 25;
            }
        } else if (SNOWY.contains(i5)) {
            if (!Layers.isOcean(i5) && (Layers.isOcean(i) || Layers.isOcean(i2) || Layers.isOcean(i3) || Layers.isOcean(i4))) {
                return 26;
            }
        } else if (i5 == 37 || i5 == 38) {
            if (!Layers.isOcean(i) && !Layers.isOcean(i2) && !Layers.isOcean(i3) && !Layers.isOcean(i4) && (!isMesa(i) || !isMesa(i2) || !isMesa(i3) || !isMesa(i4))) {
                return 2;
            }
        } else if (!Layers.isOcean(i5) && i5 != 7 && i5 != 6 && (Layers.isOcean(i) || Layers.isOcean(i2) || Layers.isOcean(i3) || Layers.isOcean(i4))) {
            return 16;
        }
        return i5;
    }

    private static boolean isJungleCompatible(int i) {
        return JUNGLES.contains(i) || i == 4 || i == 5 || Layers.isOcean(i);
    }

    private boolean isMesa(int i) {
        return i == 37 || i == 38 || i == 39 || i == 165 || i == 166 || i == 167;
    }
}
