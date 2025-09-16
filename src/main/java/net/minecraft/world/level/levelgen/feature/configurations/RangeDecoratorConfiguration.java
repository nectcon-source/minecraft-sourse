package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/RangeDecoratorConfiguration.class */
public class RangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("bottom_offset").orElse(0).forGetter(rangeDecoratorConfiguration -> {
            return Integer.valueOf(rangeDecoratorConfiguration.bottomOffset);
        }), Codec.INT.fieldOf("top_offset").orElse(0).forGetter(rangeDecoratorConfiguration2 -> {
            return Integer.valueOf(rangeDecoratorConfiguration2.topOffset);
        }), Codec.INT.fieldOf("maximum").orElse(0).forGetter(rangeDecoratorConfiguration3 -> {
            return Integer.valueOf(rangeDecoratorConfiguration3.maximum);
        })).apply(instance, (v1, v2, v3) -> {
            return new RangeDecoratorConfiguration(v1, v2, v3);
        });
    });
    public final int bottomOffset;
    public final int topOffset;
    public final int maximum;

    public RangeDecoratorConfiguration(int i, int i2, int i3) {
        this.bottomOffset = i;
        this.topOffset = i2;
        this.maximum = i3;
    }
}
