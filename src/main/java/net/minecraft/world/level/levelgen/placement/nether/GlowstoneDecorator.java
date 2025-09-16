package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/nether/GlowstoneDecorator.class */
public class GlowstoneDecorator extends SimpleFeatureDecorator<CountConfiguration> {
    public GlowstoneDecorator(Codec<CountConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
        return IntStream.range(0, random.nextInt(random.nextInt(countConfiguration.count().sample(random)) + 1)).mapToObj(i -> {
            return new BlockPos(random.nextInt(16) + blockPos.getX(), random.nextInt(120) + 4, random.nextInt(16) + blockPos.getZ());
        });
    }
}
