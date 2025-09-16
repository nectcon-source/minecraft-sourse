package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/LakeLavaPlacementDecorator.class */
public class LakeLavaPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos) {
        if (random.nextInt(chanceDecoratorConfiguration.chance / 10) == 0) {
            int nextInt = random.nextInt(16) + blockPos.getX();
            int nextInt2 = random.nextInt(16) + blockPos.getZ();
            int nextInt3 = random.nextInt(random.nextInt(decorationContext.getGenDepth() - 8) + 8);
            if (nextInt3 < decorationContext.getSeaLevel() || random.nextInt(chanceDecoratorConfiguration.chance / 8) == 0) {
                return Stream.of(new BlockPos(nextInt, nextInt3, nextInt2));
            }
        }
        return Stream.empty();
    }
}
