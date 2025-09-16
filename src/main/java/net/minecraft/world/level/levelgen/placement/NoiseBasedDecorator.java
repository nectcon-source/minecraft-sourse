package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/NoiseBasedDecorator.class */
public class NoiseBasedDecorator extends SimpleFeatureDecorator<NoiseCountFactorDecoratorConfiguration> {
    public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, NoiseCountFactorDecoratorConfiguration noiseCountFactorDecoratorConfiguration, BlockPos blockPos) {
        return IntStream.range(0, (int) Math.ceil((Biome.BIOME_INFO_NOISE.getValue(blockPos.getX() / noiseCountFactorDecoratorConfiguration.noiseFactor, blockPos.getZ() / noiseCountFactorDecoratorConfiguration.noiseFactor, false) + noiseCountFactorDecoratorConfiguration.noiseOffset) * noiseCountFactorDecoratorConfiguration.noiseToCountRatio)).mapToObj(i -> {
            return blockPos;
        });
    }
}
