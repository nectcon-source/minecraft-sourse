package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/ChanceDecorator.class */
public class ChanceDecorator extends SimpleFeatureDecorator<ChanceDecoratorConfiguration> {
    public ChanceDecorator(Codec<ChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (random.nextFloat() < 1.0f / chanceDecoratorConfiguration.chance) {
            return Stream.of(blockPos);
        }
        return Stream.empty();
    }
}
