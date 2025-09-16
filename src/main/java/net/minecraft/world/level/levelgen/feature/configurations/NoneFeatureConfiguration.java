package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/NoneFeatureConfiguration.class */
public class NoneFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<NoneFeatureConfiguration> CODEC = Codec.unit(() -> NoneFeatureConfiguration.INSTANCE);
    public static final NoneFeatureConfiguration INSTANCE = new NoneFeatureConfiguration();
}
