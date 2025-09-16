package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/EndGatewayPlacementDecorator.class */
public class EndGatewayPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        int nextInt;
        int nextInt2;
        int height;
        if (random.nextInt(700) == 0 && (height = decorationContext.getHeight(Heightmap.Types.MOTION_BLOCKING, (nextInt = random.nextInt(16) + blockPos.getX()), (nextInt2 = random.nextInt(16) + blockPos.getZ()))) > 0) {
            return Stream.of(new BlockPos(nextInt, height + 3 + random.nextInt(7), nextInt2));
        }
        return Stream.empty();
    }
}
