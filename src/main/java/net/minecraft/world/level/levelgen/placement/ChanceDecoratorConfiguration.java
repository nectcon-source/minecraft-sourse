package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/ChanceDecoratorConfiguration.class */
public class ChanceDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<ChanceDecoratorConfiguration> CODEC = Codec.INT.fieldOf("chance").xmap((v1) -> {
        return new ChanceDecoratorConfiguration(v1);
    }, chanceDecoratorConfiguration -> {
        return Integer.valueOf(chanceDecoratorConfiguration.chance);
    }).codec();
    public final int chance;

    public ChanceDecoratorConfiguration(int i) {
        this.chance = i;
    }
}
