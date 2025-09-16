package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset1Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/RegionHillsLayer.class */
public enum RegionHillsLayer implements AreaTransformer2, DimensionOffset1Transformer {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Int2IntMap MUTATIONS = (Int2IntMap) Util.make(new Int2IntOpenHashMap(), int2IntOpenHashMap -> {
        int2IntOpenHashMap.put(1, 129);
        int2IntOpenHashMap.put(2, 130);
        int2IntOpenHashMap.put(3, 131);
        int2IntOpenHashMap.put(4, 132);
        int2IntOpenHashMap.put(5, 133);
        int2IntOpenHashMap.put(6, 134);
        int2IntOpenHashMap.put(12, 140);
        int2IntOpenHashMap.put(21, 149);
        int2IntOpenHashMap.put(23, 151);
        int2IntOpenHashMap.put(27, 155);
        int2IntOpenHashMap.put(28, 156);
        int2IntOpenHashMap.put(29, 157);
        int2IntOpenHashMap.put(30, 158);
        int2IntOpenHashMap.put(32, 160);
        int2IntOpenHashMap.put(33, 161);
        int2IntOpenHashMap.put(34, 162);
        int2IntOpenHashMap.put(35, 163);
        int2IntOpenHashMap.put(36, 164);
        int2IntOpenHashMap.put(37, 165);
        int2IntOpenHashMap.put(38, 166);
        int2IntOpenHashMap.put(39, 167);
    });

    @Override // net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
    public int applyPixel(Context context, Area area, Area area2, int i, int i2) {
        int i3 = area.get(getParentX(i + 1), getParentY(i2 + 1));
        int i4 = area2.get(getParentX(i + 1), getParentY(i2 + 1));
        if (i3 > 255) {
            LOGGER.debug("old! {}", Integer.valueOf(i3));
        }
        int i5 = (i4 - 2) % 29;
        if (!Layers.isShallowOcean(i3) && i4 >= 2 && i5 == 1) {
            return MUTATIONS.getOrDefault(i3, i3);
        }
        if (context.nextRandom(3) == 0 || i5 == 0) {
            int i6 = i3;
            if (i3 == 2) {
                i6 = 17;
            } else if (i3 == 4) {
                i6 = 18;
            } else if (i3 == 27) {
                i6 = 28;
            } else if (i3 == 29) {
                i6 = 1;
            } else if (i3 == 5) {
                i6 = 19;
            } else if (i3 == 32) {
                i6 = 33;
            } else if (i3 == 30) {
                i6 = 31;
            } else if (i3 == 1) {
                i6 = context.nextRandom(3) == 0 ? 18 : 4;
            } else if (i3 == 12) {
                i6 = 13;
            } else if (i3 == 21) {
                i6 = 22;
            } else if (i3 == 168) {
                i6 = 169;
            } else if (i3 == 0) {
                i6 = 24;
            } else if (i3 == 45) {
                i6 = 48;
            } else if (i3 == 46) {
                i6 = 49;
            } else if (i3 == 10) {
                i6 = 50;
            } else if (i3 == 3) {
                i6 = 34;
            } else if (i3 == 35) {
                i6 = 36;
            } else if (Layers.isSame(i3, 38)) {
                i6 = 37;
            } else if ((i3 == 24 || i3 == 48 || i3 == 49 || i3 == 50) && context.nextRandom(3) == 0) {
                i6 = context.nextRandom(2) == 0 ? 1 : 4;
            }
            if (i5 == 0 && i6 != i3) {
                i6 = MUTATIONS.getOrDefault(i6, i3);
            }
            if (i6 != i3) {
                int i7 = 0;
                if (Layers.isSame(area.get(getParentX(i + 1), getParentY(i2 + 0)), i3)) {
                    i7 = 0 + 1;
                }
                if (Layers.isSame(area.get(getParentX(i + 2), getParentY(i2 + 1)), i3)) {
                    i7++;
                }
                if (Layers.isSame(area.get(getParentX(i + 0), getParentY(i2 + 1)), i3)) {
                    i7++;
                }
                if (Layers.isSame(area.get(getParentX(i + 1), getParentY(i2 + 2)), i3)) {
                    i7++;
                }
                if (i7 >= 3) {
                    return i6;
                }
            }
        }
        return i3;
    }
}
