package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/NoiseSettings.class */
public class NoiseSettings {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.intRange(0, 256).fieldOf("height").forGetter((v0) -> {
            return v0.height();
        }), NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter((v0) -> {
            return v0.noiseSamplingSettings();
        }), NoiseSlideSettings.CODEC.fieldOf("top_slide").forGetter((v0) -> {
            return v0.topSlideSettings();
        }), NoiseSlideSettings.CODEC.fieldOf("bottom_slide").forGetter((v0) -> {
            return v0.bottomSlideSettings();
        }), Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter((v0) -> {
            return v0.noiseSizeHorizontal();
        }), Codec.intRange(1, 4).fieldOf("size_vertical").forGetter((v0) -> {
            return v0.noiseSizeVertical();
        }), Codec.DOUBLE.fieldOf("density_factor").forGetter((v0) -> {
            return v0.densityFactor();
        }), Codec.DOUBLE.fieldOf("density_offset").forGetter((v0) -> {
            return v0.densityOffset();
        }), Codec.BOOL.fieldOf("simplex_surface_noise").forGetter((v0) -> {
            return v0.useSimplexSurfaceNoise();
        }), Codec.BOOL.optionalFieldOf("random_density_offset", false, Lifecycle.experimental()).forGetter((v0) -> {
            return v0.randomDensityOffset();
        }), Codec.BOOL.optionalFieldOf("island_noise_override", false, Lifecycle.experimental()).forGetter((v0) -> {
            return v0.islandNoiseOverride();
        }), Codec.BOOL.optionalFieldOf("amplified", false, Lifecycle.experimental()).forGetter((v0) -> {
            return v0.isAmplified();
        })).apply(instance, (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12) -> {
            return new NoiseSettings(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
        });
    });
    private final int height;
    private final NoiseSamplingSettings noiseSamplingSettings;
    private final NoiseSlideSettings topSlideSettings;
    private final NoiseSlideSettings bottomSlideSettings;
    private final int noiseSizeHorizontal;
    private final int noiseSizeVertical;
    private final double densityFactor;
    private final double densityOffset;
    private final boolean useSimplexSurfaceNoise;
    private final boolean randomDensityOffset;
    private final boolean islandNoiseOverride;
    private final boolean isAmplified;

    public NoiseSettings(int i, NoiseSamplingSettings noiseSamplingSettings, NoiseSlideSettings noiseSlideSettings, NoiseSlideSettings noiseSlideSettings2, int i2, int i3, double d, double d2, boolean z, boolean z2, boolean z3, boolean z4) {
        this.height = i;
        this.noiseSamplingSettings = noiseSamplingSettings;
        this.topSlideSettings = noiseSlideSettings;
        this.bottomSlideSettings = noiseSlideSettings2;
        this.noiseSizeHorizontal = i2;
        this.noiseSizeVertical = i3;
        this.densityFactor = d;
        this.densityOffset = d2;
        this.useSimplexSurfaceNoise = z;
        this.randomDensityOffset = z2;
        this.islandNoiseOverride = z3;
        this.isAmplified = z4;
    }

    public int height() {
        return this.height;
    }

    public NoiseSamplingSettings noiseSamplingSettings() {
        return this.noiseSamplingSettings;
    }

    public NoiseSlideSettings topSlideSettings() {
        return this.topSlideSettings;
    }

    public NoiseSlideSettings bottomSlideSettings() {
        return this.bottomSlideSettings;
    }

    public int noiseSizeHorizontal() {
        return this.noiseSizeHorizontal;
    }

    public int noiseSizeVertical() {
        return this.noiseSizeVertical;
    }

    public double densityFactor() {
        return this.densityFactor;
    }

    public double densityOffset() {
        return this.densityOffset;
    }

    @Deprecated
    public boolean useSimplexSurfaceNoise() {
        return this.useSimplexSurfaceNoise;
    }

    @Deprecated
    public boolean randomDensityOffset() {
        return this.randomDensityOffset;
    }

    @Deprecated
    public boolean islandNoiseOverride() {
        return this.islandNoiseOverride;
    }

    @Deprecated
    public boolean isAmplified() {
        return this.isAmplified;
    }
}
