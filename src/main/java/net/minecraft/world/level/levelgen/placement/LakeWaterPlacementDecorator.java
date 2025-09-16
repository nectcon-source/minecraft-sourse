package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/LakeWaterPlacementDecorator.class */
public class LakeWaterPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public LakeWaterPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (random.nextInt(chanceDecoratorConfiguration.chance) == 0) {
            return Stream.of(new BlockPos(random.nextInt(16) + blockPos.getX(), random.nextInt(decorationContext.getGenDepth()), random.nextInt(16) + blockPos.getZ()));
        }
        return Stream.empty();
    }
}
