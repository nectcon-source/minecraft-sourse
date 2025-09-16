package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/SimpleFeatureDecorator.class */
public abstract class SimpleFeatureDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
    protected abstract Stream<BlockPos> place(Random random, DC dc, BlockPos blockPos);

    public SimpleFeatureDecorator(Codec<DC> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public final Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC dc, BlockPos blockPos) {
        return place(random, dc, blockPos);
    }
}
