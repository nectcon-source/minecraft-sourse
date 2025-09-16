package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/ShipwreckConfiguration.class */
public class ShipwreckConfiguration implements FeatureConfiguration {
    public static final Codec<ShipwreckConfiguration> CODEC = Codec.BOOL.fieldOf("is_beached").orElse(false).xmap((v1) -> {
        return new ShipwreckConfiguration(v1);
    }, shipwreckConfiguration -> {
        return Boolean.valueOf(shipwreckConfiguration.isBeached);
    }).codec();
    public final boolean isBeached;

    public ShipwreckConfiguration(boolean z) {
        this.isBeached = z;
    }
}
