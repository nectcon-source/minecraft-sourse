package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/NoiseSlideSettings.class */
public class NoiseSlideSettings {
    public static final Codec<NoiseSlideSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("target").forGetter((v0) -> {
            return v0.target();
        }), Codec.intRange(0, 256).fieldOf("size").forGetter((v0) -> {
            return v0.size();
        }), Codec.INT.fieldOf("offset").forGetter((v0) -> {
            return v0.offset();
        })).apply(instance, (v1, v2, v3) -> {
            return new NoiseSlideSettings(v1, v2, v3);
        });
    });
    private final int target;
    private final int size;
    private final int offset;

    public NoiseSlideSettings(int i, int i2, int i3) {
        this.target = i;
        this.size = i2;
        this.offset = i3;
    }

    public int target() {
        return this.target;
    }

    public int size() {
        return this.size;
    }

    public int offset() {
        return this.offset;
    }
}
