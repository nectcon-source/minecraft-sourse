package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/StructurePoolElementType.class */
public interface StructurePoolElementType<P extends StructurePoolElement> {
    public static final StructurePoolElementType<SinglePoolElement> SINGLE = register("single_pool_element", SinglePoolElement.CODEC);
    public static final StructurePoolElementType<ListPoolElement> LIST = register("list_pool_element", ListPoolElement.CODEC);
    public static final StructurePoolElementType<FeaturePoolElement> FEATURE = register("feature_pool_element", FeaturePoolElement.CODEC);
    public static final StructurePoolElementType<EmptyPoolElement> EMPTY = register("empty_pool_element", EmptyPoolElement.CODEC);
    public static final StructurePoolElementType<LegacySinglePoolElement> LEGACY = register("legacy_single_pool_element", LegacySinglePoolElement.CODEC);

    Codec<P> codec();

//    static <P extends StructurePoolElement> StructurePoolElementType<P> register(String str, Codec<P> codec) {
//        return (StructurePoolElementType) Registry.register(Registry.STRUCTURE_POOL_ELEMENT, str, () -> {
//            return codec;
//        });
//    }
static <P extends StructurePoolElement> StructurePoolElementType<P> register(String str, Codec<P> codec) {
    return (StructurePoolElementType<P>) Registry.register(
            Registry.STRUCTURE_POOL_ELEMENT,
            str,
            () -> (Codec<StructurePoolElement>)codec
    );
}
}
