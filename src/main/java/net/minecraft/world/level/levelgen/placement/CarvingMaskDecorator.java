package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/CarvingMaskDecorator.class */
public class CarvingMaskDecorator extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
    public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, CarvingMaskDecoratorConfiguration carvingMaskDecoratorConfiguration, BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BitSet carvingMask = decorationContext.getCarvingMask(chunkPos, carvingMaskDecoratorConfiguration.step);
        return IntStream.range(0, carvingMask.length()).filter(i -> {
            return carvingMask.get(i) && random.nextFloat() < carvingMaskDecoratorConfiguration.probability;
        }).mapToObj(i2 -> {
            int i2_ = (i2 >> 4) & 15;
            return new BlockPos(chunkPos.getMinBlockX() + (i2_ & 15), i2_ >> 8, chunkPos.getMinBlockZ() + i2_);
        });
    }
}
