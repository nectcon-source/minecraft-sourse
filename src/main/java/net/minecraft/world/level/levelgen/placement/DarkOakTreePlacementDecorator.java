package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/DarkOakTreePlacementDecorator.class */
public class DarkOakTreePlacementDecorator extends EdgeDecorator<NoneDecoratorConfiguration> {
    public DarkOakTreePlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
        super(codec);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.placement.EdgeDecorator
    public Heightmap.Types type(NoneDecoratorConfiguration noneDecoratorConfiguration) {
        return Heightmap.Types.MOTION_BLOCKING;
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos) {
        return IntStream.range(0, 16).mapToObj(i -> {
            int nextInt = ((i / 4) * 4) + 1 + random.nextInt(3) + blockPos.getX();
            int nextInt2 = ((i % 4) * 4) + 1 + random.nextInt(3) + blockPos.getZ();
            return new BlockPos(nextInt, decorationContext.getHeight(type(noneDecoratorConfiguration), nextInt, nextInt2), nextInt2);
        });
    }
}
