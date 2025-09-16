package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/StrongholdConfiguration.class */
public class StrongholdConfiguration {
    public static final Codec<StrongholdConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.intRange(0, 1023).fieldOf("distance").forGetter((v0) -> {
            return v0.distance();
        }), Codec.intRange(0, 1023).fieldOf("spread").forGetter((v0) -> {
            return v0.spread();
        }), Codec.intRange(1, 4095).fieldOf("count").forGetter((v0) -> {
            return v0.count();
        })).apply(instance, (v1, v2, v3) -> {
            return new StrongholdConfiguration(v1, v2, v3);
        });
    });
    private final int distance;
    private final int spread;
    private final int count;

    public StrongholdConfiguration(int i, int i2, int i3) {
        this.distance = i;
        this.spread = i2;
        this.count = i3;
    }

    public int distance() {
        return this.distance;
    }

    public int spread() {
        return this.spread;
    }

    public int count() {
        return this.count;
    }
}
