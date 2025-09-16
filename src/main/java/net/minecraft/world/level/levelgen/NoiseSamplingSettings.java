package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/NoiseSamplingSettings.class */
public class NoiseSamplingSettings {
    private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001d, 1000.0d);
    public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(SCALE_RANGE.fieldOf("xz_scale").forGetter((v0) -> {
            return v0.xzScale();
        }), SCALE_RANGE.fieldOf("y_scale").forGetter((v0) -> {
            return v0.yScale();
        }), SCALE_RANGE.fieldOf("xz_factor").forGetter((v0) -> {
            return v0.xzFactor();
        }), SCALE_RANGE.fieldOf("y_factor").forGetter((v0) -> {
            return v0.yFactor();
        })).apply(instance, (v1, v2, v3, v4) -> {
            return new NoiseSamplingSettings(v1, v2, v3, v4);
        });
    });
    private final double xzScale;
    private final double yScale;
    private final double xzFactor;
    private final double yFactor;

    public NoiseSamplingSettings(double d, double d2, double d3, double d4) {
        this.xzScale = d;
        this.yScale = d2;
        this.xzFactor = d3;
        this.yFactor = d4;
    }

    public double xzScale() {
        return this.xzScale;
    }

    public double yScale() {
        return this.yScale;
    }

    public double xzFactor() {
        return this.xzFactor;
    }

    public double yFactor() {
        return this.yFactor;
    }
}
