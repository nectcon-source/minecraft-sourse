package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/nether/MagmaDecorator.class */
public class MagmaDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public MagmaDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        return Stream.of(new BlockPos(blockPos.getX(), (decorationContext.getSeaLevel() - 5) + random.nextInt(10), blockPos.getZ()));
    }
}
