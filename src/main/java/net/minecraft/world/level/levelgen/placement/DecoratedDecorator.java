package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/DecoratedDecorator.class */
public class DecoratedDecorator extends FeatureDecorator<DecoratedDecoratorConfiguration> {
    public DecoratedDecorator(Codec<DecoratedDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DecoratedDecoratorConfiguration decoratedDecoratorConfiguration, BlockPos blockPos) {
        return decoratedDecoratorConfiguration.outer().getPositions(decorationContext, random, blockPos).flatMap(blockPos2 -> {
            return decoratedDecoratorConfiguration.inner().getPositions(decorationContext, random, blockPos2);
        });
    }
}
