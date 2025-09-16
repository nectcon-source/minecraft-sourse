package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/DecoratedDecoratorConfiguration.class */
public class DecoratedDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<DecoratedDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ConfiguredDecorator.CODEC.fieldOf("outer").forGetter((v0) -> {
            return v0.outer();
        }), ConfiguredDecorator.CODEC.fieldOf("inner").forGetter((v0) -> {
            return v0.inner();
        })).apply(instance, DecoratedDecoratorConfiguration::new);
    });
    private final ConfiguredDecorator<?> outer;
    private final ConfiguredDecorator<?> inner;

    public DecoratedDecoratorConfiguration(ConfiguredDecorator<?> configuredDecorator, ConfiguredDecorator<?> configuredDecorator2) {
        this.outer = configuredDecorator;
        this.inner = configuredDecorator2;
    }

    public ConfiguredDecorator<?> outer() {
        return this.outer;
    }

    public ConfiguredDecorator<?> inner() {
        return this.inner;
    }
}
