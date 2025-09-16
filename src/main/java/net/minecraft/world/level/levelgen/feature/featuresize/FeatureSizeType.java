package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/featuresize/FeatureSizeType.class */
public class FeatureSizeType<P extends FeatureSize> {
    public static final FeatureSizeType<TwoLayersFeatureSize> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayersFeatureSize.CODEC);
    public static final FeatureSizeType<ThreeLayersFeatureSize> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayersFeatureSize.CODEC);
    private final Codec<P> codec;

//    private static <P extends FeatureSize> FeatureSizeType<P> register(String str, Codec<P> codec) {
//        return (FeatureSizeType) Registry.register(Registry.FEATURE_SIZE_TYPES, str, new FeatureSizeType(codec));
//    }
private static <P extends FeatureSize> FeatureSizeType<P> register(String str, Codec<P> codec) {
    // Convert string to ResourceLocation
    ResourceLocation id = new ResourceLocation(str);

    // Create properly typed instance
    FeatureSizeType<P> type = new FeatureSizeType<>(codec);

    // Register with the registry
    return Registry.register(Registry.FEATURE_SIZE_TYPES, id, type);
}

    private FeatureSizeType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
