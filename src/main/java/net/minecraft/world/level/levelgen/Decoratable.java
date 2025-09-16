package net.minecraft.world.level.levelgen;

import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/Decoratable.class */
public interface Decoratable<R> {
    R decorated(ConfiguredDecorator<?> configuredDecorator);

    default R chance(int i) {
        return decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(i)));
    }

    default R count(UniformInt uniformInt) {
        return decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(uniformInt)));
    }

    default R count(int i) {
        return count(UniformInt.fixed(i));
    }

    default R countRandom(int i) {
        return count(UniformInt.of(0, i));
    }

    default R range(int i) {
        return decorated(FeatureDecorator.RANGE.configured(new RangeDecoratorConfiguration(0, 0, i)));
    }

    default R squared() {
        return decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
    }
}
