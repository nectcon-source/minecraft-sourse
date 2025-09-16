package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/TrunkPlacerType.class */
public class TrunkPlacerType<P extends TrunkPlacer> {
    public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER = register("straight_trunk_placer", StraightTrunkPlacer.CODEC);
    public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER = register("forking_trunk_placer", ForkingTrunkPlacer.CODEC);
    public static final TrunkPlacerType<GiantTrunkPlacer> GIANT_TRUNK_PLACER = register("giant_trunk_placer", GiantTrunkPlacer.CODEC);
    public static final TrunkPlacerType<MegaJungleTrunkPlacer> MEGA_JUNGLE_TRUNK_PLACER = register("mega_jungle_trunk_placer", MegaJungleTrunkPlacer.CODEC);
    public static final TrunkPlacerType<DarkOakTrunkPlacer> DARK_OAK_TRUNK_PLACER = register("dark_oak_trunk_placer", DarkOakTrunkPlacer.CODEC);
    public static final TrunkPlacerType<FancyTrunkPlacer> FANCY_TRUNK_PLACER = register("fancy_trunk_placer", FancyTrunkPlacer.CODEC);
    private final Codec<P> codec;

//    private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String str, Codec<P> codec) {
//        return (TrunkPlacerType) Registry.register(Registry.TRUNK_PLACER_TYPES, str, new TrunkPlacerType(codec));
//    }
private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String str, Codec<P> codec) {
    // Преобразуем строку в ResourceLocation
    ResourceLocation id = new ResourceLocation(str);

    // Создаем экземпляр TrunkPlacerType с указанием типа
    TrunkPlacerType<P> type = new TrunkPlacerType<>(codec);

    // Регистрируем и возвращаем
    return Registry.register(Registry.TRUNK_PLACER_TYPES, id, type);
}

    private TrunkPlacerType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
