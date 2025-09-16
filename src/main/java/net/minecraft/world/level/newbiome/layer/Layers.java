package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.function.LongFunction;
import net.minecraft.Util;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.AddEdgeLayer;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/Layers.class */
public class Layers {
    private static final Int2IntMap CATEGORIES = (Int2IntMap) Util.make(new Int2IntOpenHashMap(), int2IntOpenHashMap -> {
        register(int2IntOpenHashMap, Category.BEACH, 16);
        register(int2IntOpenHashMap, Category.BEACH, 26);
        register(int2IntOpenHashMap, Category.DESERT, 2);
        register(int2IntOpenHashMap, Category.DESERT, 17);
        register(int2IntOpenHashMap, Category.DESERT, 130);
        register(int2IntOpenHashMap, Category.EXTREME_HILLS, 131);
        register(int2IntOpenHashMap, Category.EXTREME_HILLS, 162);
        register(int2IntOpenHashMap, Category.EXTREME_HILLS, 20);
        register(int2IntOpenHashMap, Category.EXTREME_HILLS, 3);
        register(int2IntOpenHashMap, Category.EXTREME_HILLS, 34);
        register(int2IntOpenHashMap, Category.FOREST, 27);
        register(int2IntOpenHashMap, Category.FOREST, 28);
        register(int2IntOpenHashMap, Category.FOREST, 29);
        register(int2IntOpenHashMap, Category.FOREST, 157);
        register(int2IntOpenHashMap, Category.FOREST, 132);
        register(int2IntOpenHashMap, Category.FOREST, 4);
        register(int2IntOpenHashMap, Category.FOREST, 155);
        register(int2IntOpenHashMap, Category.FOREST, 156);
        register(int2IntOpenHashMap, Category.FOREST, 18);
        register(int2IntOpenHashMap, Category.ICY, 140);
        register(int2IntOpenHashMap, Category.ICY, 13);
        register(int2IntOpenHashMap, Category.ICY, 12);
        register(int2IntOpenHashMap, Category.JUNGLE, 168);
        register(int2IntOpenHashMap, Category.JUNGLE, 169);
        register(int2IntOpenHashMap, Category.JUNGLE, 21);
        register(int2IntOpenHashMap, Category.JUNGLE, 23);
        register(int2IntOpenHashMap, Category.JUNGLE, 22);
        register(int2IntOpenHashMap, Category.JUNGLE, 149);
        register(int2IntOpenHashMap, Category.JUNGLE, 151);
        register(int2IntOpenHashMap, Category.MESA, 37);
        register(int2IntOpenHashMap, Category.MESA, 165);
        register(int2IntOpenHashMap, Category.MESA, 167);
        register(int2IntOpenHashMap, Category.MESA, 166);
        register(int2IntOpenHashMap, Category.BADLANDS_PLATEAU, 39);
        register(int2IntOpenHashMap, Category.BADLANDS_PLATEAU, 38);
        register(int2IntOpenHashMap, Category.MUSHROOM, 14);
        register(int2IntOpenHashMap, Category.MUSHROOM, 15);
        register(int2IntOpenHashMap, Category.NONE, 25);
        register(int2IntOpenHashMap, Category.OCEAN, 46);
        register(int2IntOpenHashMap, Category.OCEAN, 49);
        register(int2IntOpenHashMap, Category.OCEAN, 50);
        register(int2IntOpenHashMap, Category.OCEAN, 48);
        register(int2IntOpenHashMap, Category.OCEAN, 24);
        register(int2IntOpenHashMap, Category.OCEAN, 47);
        register(int2IntOpenHashMap, Category.OCEAN, 10);
        register(int2IntOpenHashMap, Category.OCEAN, 45);
        register(int2IntOpenHashMap, Category.OCEAN, 0);
        register(int2IntOpenHashMap, Category.OCEAN, 44);
        register(int2IntOpenHashMap, Category.PLAINS, 1);
        register(int2IntOpenHashMap, Category.PLAINS, 129);
        register(int2IntOpenHashMap, Category.RIVER, 11);
        register(int2IntOpenHashMap, Category.RIVER, 7);
        register(int2IntOpenHashMap, Category.SAVANNA, 35);
        register(int2IntOpenHashMap, Category.SAVANNA, 36);
        register(int2IntOpenHashMap, Category.SAVANNA, 163);
        register(int2IntOpenHashMap, Category.SAVANNA, 164);
        register(int2IntOpenHashMap, Category.SWAMP, 6);
        register(int2IntOpenHashMap, Category.SWAMP, 134);
        register(int2IntOpenHashMap, Category.TAIGA, 160);
        register(int2IntOpenHashMap, Category.TAIGA, 161);
        register(int2IntOpenHashMap, Category.TAIGA, 32);
        register(int2IntOpenHashMap, Category.TAIGA, 33);
        register(int2IntOpenHashMap, Category.TAIGA, 30);
        register(int2IntOpenHashMap, Category.TAIGA, 31);
        register(int2IntOpenHashMap, Category.TAIGA, 158);
        register(int2IntOpenHashMap, Category.TAIGA, 5);
        register(int2IntOpenHashMap, Category.TAIGA, 19);
        register(int2IntOpenHashMap, Category.TAIGA, 133);
    });

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/newbiome/layer/Layers$Category.class */
    enum Category {
        NONE,
        TAIGA,
        EXTREME_HILLS,
        JUNGLE,
        MESA,
        BADLANDS_PLATEAU,
        PLAINS,
        SAVANNA,
        ICY,
        BEACH,
        FOREST,
        OCEAN,
        DESERT,
        RIVER,
        SWAMP,
        MUSHROOM
    }

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> zoom(long j, AreaTransformer1 areaTransformer1, AreaFactory<T> areaFactory, int i, LongFunction<C> longFunction) {
        AreaFactory<T> areaFactory2 = areaFactory;
        for (int i2 = 0; i2 < i; i2++) {
            areaFactory2 = areaTransformer1.run(longFunction.apply(j + i2), areaFactory2);
        }
        return areaFactory2;
    }

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> getDefaultLayer(boolean z, int i, int i2, LongFunction<C> longFunction) {
        AreaFactory<T> run = RemoveTooMuchOceanLayer.INSTANCE.run(longFunction.apply(2L), AddIslandLayer.INSTANCE.run(longFunction.apply(70L), AddIslandLayer.INSTANCE.run(longFunction.apply(50L), AddIslandLayer.INSTANCE.run(longFunction.apply(2L), ZoomLayer.NORMAL.run(longFunction.apply(2001L), AddIslandLayer.INSTANCE.run(longFunction.apply(1L), ZoomLayer.FUZZY.run(longFunction.apply(2000L), IslandLayer.INSTANCE.run(longFunction.apply(1L)))))))));
        AreaFactory<T> zoom = zoom(2001L, ZoomLayer.NORMAL, OceanLayer.INSTANCE.run(longFunction.apply(2L)), 6, longFunction);
        AreaFactory<T> zoom2 = zoom(1000L, ZoomLayer.NORMAL, AddDeepOceanLayer.INSTANCE.run(longFunction.apply(4L), AddMushroomIslandLayer.INSTANCE.run(longFunction.apply(5L), AddIslandLayer.INSTANCE.run(longFunction.apply(4L), ZoomLayer.NORMAL.run(longFunction.apply(2003L), ZoomLayer.NORMAL.run(longFunction.apply(2002L), AddEdgeLayer.IntroduceSpecial.INSTANCE.run(longFunction.apply(3L), AddEdgeLayer.HeatIce.INSTANCE.run(longFunction.apply(2L), AddEdgeLayer.CoolWarm.INSTANCE.run(longFunction.apply(2L), AddIslandLayer.INSTANCE.run(longFunction.apply(3L), AddSnowLayer.INSTANCE.run(longFunction.apply(2L), run)))))))))), 0, longFunction);
        AreaFactory<T> run2 = RiverInitLayer.INSTANCE.run(longFunction.apply(100L), zoom(1000L, ZoomLayer.NORMAL, zoom2, 0, longFunction));
        AreaFactory<T> run3 = RegionHillsLayer.INSTANCE.run(longFunction.apply(1000L), BiomeEdgeLayer.INSTANCE.run(longFunction.apply(1000L), zoom(1000L, ZoomLayer.NORMAL, RareBiomeLargeLayer.INSTANCE.run(longFunction.apply(1001L), new BiomeInitLayer(z).run(longFunction.apply(200L), zoom2)), 2, longFunction)), zoom(1000L, ZoomLayer.NORMAL, run2, 2, longFunction));
        AreaFactory<T> run4 = SmoothLayer.INSTANCE.run(longFunction.apply(1000L), RiverLayer.INSTANCE.run(longFunction.apply(1L), zoom(1000L, ZoomLayer.NORMAL, zoom(1000L, ZoomLayer.NORMAL, run2, 2, longFunction), i2, longFunction)));
        AreaFactory<T> run5 = RareBiomeSpotLayer.INSTANCE.run(longFunction.apply(1001L), run3);
        for (int i3 = 0; i3 < i; i3++) {
            run5 = ZoomLayer.NORMAL.run(longFunction.apply(1000 + i3), run5);
            if (i3 == 0) {
                run5 = AddIslandLayer.INSTANCE.run(longFunction.apply(3L), run5);
            }
            if (i3 == 1 || i == 1) {
                run5 = ShoreLayer.INSTANCE.run(longFunction.apply(1000L), run5);
            }
        }
        return OceanMixerLayer.INSTANCE.run(longFunction.apply(100L), RiverMixerLayer.INSTANCE.run(longFunction.apply(100L), SmoothLayer.INSTANCE.run(longFunction.apply(1000L), run5), run4), zoom);
    }

    public static Layer getDefaultLayer(long j, boolean z, int i, int i2) {
        return new Layer(getDefaultLayer(z, i, i2, j2 -> {
            return new LazyAreaContext(25, j, j2);
        }));
    }

    public static boolean isSame(int i, int i2) {
        return i == i2 || CATEGORIES.get(i) == CATEGORIES.get(i2);
    }

    private static void register(Int2IntOpenHashMap int2IntOpenHashMap, Category category, int i) {
        int2IntOpenHashMap.put(i, category.ordinal());
    }

    protected static boolean isOcean(int i) {
        return i == 44 || i == 45 || i == 0 || i == 46 || i == 10 || i == 47 || i == 48 || i == 24 || i == 49 || i == 50;
    }

    protected static boolean isShallowOcean(int i) {
        return i == 44 || i == 45 || i == 0 || i == 46 || i == 10;
    }
}
