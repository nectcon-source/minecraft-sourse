package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/featuresize/ThreeLayersFeatureSize.class */
public class ThreeLayersFeatureSize extends FeatureSize {
    public static final Codec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.intRange(0, 80).fieldOf("limit").orElse(1).forGetter(threeLayersFeatureSize -> {
            return Integer.valueOf(threeLayersFeatureSize.limit);
        }), Codec.intRange(0, 80).fieldOf("upper_limit").orElse(1).forGetter(threeLayersFeatureSize2 -> {
            return Integer.valueOf(threeLayersFeatureSize2.upperLimit);
        }), Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter(threeLayersFeatureSize3 -> {
            return Integer.valueOf(threeLayersFeatureSize3.lowerSize);
        }), Codec.intRange(0, 16).fieldOf("middle_size").orElse(1).forGetter(threeLayersFeatureSize4 -> {
            return Integer.valueOf(threeLayersFeatureSize4.middleSize);
        }), Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter(threeLayersFeatureSize5 -> {
            return Integer.valueOf(threeLayersFeatureSize5.upperSize);
        }), minClippedHeightCodec()).apply(instance, (v1, v2, v3, v4, v5, v6) -> {
            return new ThreeLayersFeatureSize(v1, v2, v3, v4, v5, v6);
        });
    });
    private final int limit;
    private final int upperLimit;
    private final int lowerSize;
    private final int middleSize;
    private final int upperSize;

    public ThreeLayersFeatureSize(int i, int i2, int i3, int i4, int i5, OptionalInt optionalInt) {
        super(optionalInt);
        this.limit = i;
        this.upperLimit = i2;
        this.lowerSize = i3;
        this.middleSize = i4;
        this.upperSize = i5;
    }

    @Override // net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
    }

    @Override // net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize
    public int getSizeAtHeight(int i, int i2) {
        if (i2 < this.limit) {
            return this.lowerSize;
        }
        if (i2 >= i - this.upperLimit) {
            return this.upperSize;
        }
        return this.middleSize;
    }
}
