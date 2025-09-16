package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/CountNoiseDecorator.class */
public class CountNoiseDecorator extends FeatureDecorator<NoiseDependantDecoratorConfiguration> {
    public CountNoiseDecorator(Codec<NoiseDependantDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, NoiseDependantDecoratorConfiguration noiseDependantDecoratorConfiguration, BlockPos blockPos) {
        return IntStream.range(0, Biome.BIOME_INFO_NOISE.getValue(((double) blockPos.getX()) / 200.0d, ((double) blockPos.getZ()) / 200.0d, false) < noiseDependantDecoratorConfiguration.noiseLevel ? noiseDependantDecoratorConfiguration.belowNoise : noiseDependantDecoratorConfiguration.aboveNoise).mapToObj(i -> {
            return blockPos;
        });
    }
}
