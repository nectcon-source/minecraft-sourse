package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/featuresize/FeatureSize.class */
public abstract class FeatureSize {
    public static final Codec<FeatureSize> CODEC = Registry.FEATURE_SIZE_TYPES.dispatch((v0) -> {
        return v0.type();
    }, (v0) -> {
        return v0.codec();
    });
    protected final OptionalInt minClippedHeight;

    protected abstract FeatureSizeType<?> type();

    public abstract int getSizeAtHeight(int i, int i2);

    protected static <S extends FeatureSize> RecordCodecBuilder<S, OptionalInt> minClippedHeightCodec() {
        return Codec.intRange(0, 80).optionalFieldOf("min_clipped_height").xmap(optional -> {
            return (OptionalInt) optional.map((v0) -> {
                return OptionalInt.of(v0);
            }).orElse(OptionalInt.empty());
        }, optionalInt -> {
            return optionalInt.isPresent() ? Optional.of(Integer.valueOf(optionalInt.getAsInt())) : Optional.empty();
        }).forGetter(featureSize -> {
            return featureSize.minClippedHeight;
        });
    }

    public FeatureSize(OptionalInt optionalInt) {
        this.minClippedHeight = optionalInt;
    }

    public OptionalInt minClippedHeight() {
        return this.minClippedHeight;
    }
}
