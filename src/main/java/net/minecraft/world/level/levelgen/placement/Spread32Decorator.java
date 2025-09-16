package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/Spread32Decorator.class */
public class Spread32Decorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public Spread32Decorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        return Stream.of(new BlockPos(blockPos.getX(), random.nextInt(blockPos.getY() + 32), blockPos.getZ()));
    }
}
