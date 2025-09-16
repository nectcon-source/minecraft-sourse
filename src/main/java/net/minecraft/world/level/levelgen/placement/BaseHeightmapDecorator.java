package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/BaseHeightmapDecorator.class */
public abstract class BaseHeightmapDecorator<DC extends DecoratorConfiguration> extends EdgeDecorator<DC> {
    public BaseHeightmapDecorator(Codec<DC> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC dc, BlockPos blockPos) {
        int x = blockPos.getX();
        int z = blockPos.getZ();
        int height = decorationContext.getHeight(type(dc), x, z);
        if (height > 0) {
            return Stream.of(new BlockPos(x, height, z));
        }
        return Stream.of(new BlockPos[0]);
    }
}
