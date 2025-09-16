package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.Decoratable;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/ConfiguredDecorator.class */
public class ConfiguredDecorator<DC extends DecoratorConfiguration> implements Decoratable<ConfiguredDecorator<?>> {
    public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR.dispatch("type", configuredDecorator -> {
        return configuredDecorator.decorator;
    }, (v0) -> {
        return v0.configuredCodec();
    });
    private final FeatureDecorator<DC> decorator;
    private final DC config;




    public ConfiguredDecorator(FeatureDecorator<DC> featureDecorator, DC dc) {
        this.decorator = featureDecorator;
        this.config = dc;
    }

    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, BlockPos blockPos) {
        return this.decorator.getPositions(decorationContext, random, this.config, blockPos);
    }

    public String toString() {
        return String.format("[%s %s]", Registry.DECORATOR.getKey(this.decorator), this.config);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.level.levelgen.Decoratable
    public ConfiguredDecorator<?> decorated(ConfiguredDecorator<?> configuredDecorator) {
        return new ConfiguredDecorator<>(FeatureDecorator.DECORATED, new DecoratedDecoratorConfiguration(configuredDecorator, this));
    }

    public DC config() {
        return this.config;
    }
}
