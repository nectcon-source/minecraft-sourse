package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/DepthAverageConfigation.class */
public class DepthAverageConfigation implements DecoratorConfiguration {
    public static final Codec<DepthAverageConfigation> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("baseline").forGetter(depthAverageConfigation -> {
            return Integer.valueOf(depthAverageConfigation.baseline);
        }), Codec.INT.fieldOf("spread").forGetter(depthAverageConfigation2 -> {
            return Integer.valueOf(depthAverageConfigation2.spread);
        })).apply(instance, (v1, v2) -> {
            return new DepthAverageConfigation(v1, v2);
        });
    });
    public final int baseline;
    public final int spread;

    public DepthAverageConfigation(int i, int i2) {
        this.baseline = i;
        this.spread = i2;
    }
}
