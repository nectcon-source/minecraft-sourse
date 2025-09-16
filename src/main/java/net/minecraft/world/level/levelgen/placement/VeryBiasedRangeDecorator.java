package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/VeryBiasedRangeDecorator.class */
public class VeryBiasedRangeDecorator extends SimpleFeatureDecorator<RangeDecoratorConfiguration> {
    public VeryBiasedRangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, BlockPos blockPos) {
        return Stream.of(new BlockPos(blockPos.getX(), random.nextInt(random.nextInt(random.nextInt(rangeDecoratorConfiguration.maximum - rangeDecoratorConfiguration.topOffset) + rangeDecoratorConfiguration.bottomOffset) + rangeDecoratorConfiguration.bottomOffset), blockPos.getZ()));
    }
}
