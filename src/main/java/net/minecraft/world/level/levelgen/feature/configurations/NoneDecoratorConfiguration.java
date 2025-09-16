package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.stateproviders.PlainFlowerProvider;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/NoneDecoratorConfiguration.class */
public class NoneDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<NoneDecoratorConfiguration> CODEC = Codec.unit(() -> NoneDecoratorConfiguration.INSTANCE);
    public static final NoneDecoratorConfiguration INSTANCE = new NoneDecoratorConfiguration();
}
