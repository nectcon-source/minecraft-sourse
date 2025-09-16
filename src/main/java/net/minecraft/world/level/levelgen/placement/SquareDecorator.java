package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/SquareDecorator.class */
public class SquareDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public SquareDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        return Stream.of(new BlockPos(random.nextInt(16) + blockPos.getX(), blockPos.getY(), random.nextInt(16) + blockPos.getZ()));
    }
}
