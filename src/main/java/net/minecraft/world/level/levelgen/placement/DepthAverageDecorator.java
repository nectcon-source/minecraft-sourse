package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/DepthAverageDecorator.class */
public class DepthAverageDecorator extends SimpleFeatureDecorator<DepthAverageConfigation> {
    public DepthAverageDecorator(Codec<DepthAverageConfigation> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, DepthAverageConfigation depthAverageConfigation, BlockPos blockPos) {
        int i = depthAverageConfigation.baseline;
        int i2 = depthAverageConfigation.spread;
        return Stream.of(new BlockPos(blockPos.getX(), ((random.nextInt(i2) + random.nextInt(i2)) - i2) + i, blockPos.getZ()));
    }
}
