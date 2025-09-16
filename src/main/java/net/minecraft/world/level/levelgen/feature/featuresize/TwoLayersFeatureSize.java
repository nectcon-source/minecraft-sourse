package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/featuresize/TwoLayersFeatureSize.class */
public class TwoLayersFeatureSize extends FeatureSize {
    public static final Codec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.intRange(0, 81).fieldOf("limit").orElse(1).forGetter(twoLayersFeatureSize -> {
            return Integer.valueOf(twoLayersFeatureSize.limit);
        }), Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter(twoLayersFeatureSize2 -> {
            return Integer.valueOf(twoLayersFeatureSize2.lowerSize);
        }), Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter(twoLayersFeatureSize3 -> {
            return Integer.valueOf(twoLayersFeatureSize3.upperSize);
        }), minClippedHeightCodec()).apply(instance, (v1, v2, v3, v4) -> {
            return new TwoLayersFeatureSize(v1, v2, v3, v4);
        });
    });
    private final int limit;
    private final int lowerSize;
    private final int upperSize;

    public TwoLayersFeatureSize(int i, int i2, int i3) {
        this(i, i2, i3, OptionalInt.empty());
    }

    public TwoLayersFeatureSize(int i, int i2, int i3, OptionalInt optionalInt) {
        super(optionalInt);
        this.limit = i;
        this.lowerSize = i2;
        this.upperSize = i3;
    }

    @Override // net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
    }

    @Override // net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize
    public int getSizeAtHeight(int i, int i2) {
        return i2 < this.limit ? this.lowerSize : this.upperSize;
    }
}
