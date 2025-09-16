package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/nether/CountMultiLayerDecorator.class */
public class CountMultiLayerDecorator extends FeatureDecorator<CountConfiguration> {
    public CountMultiLayerDecorator(Codec<CountConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.FeatureDecorator
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
        boolean z;
        List<BlockPos> newArrayList = Lists.newArrayList();
        int i = 0;
        do {
            z = false;
            for (int i2 = 0; i2 < countConfiguration.count().sample(random); i2++) {
                int nextInt = random.nextInt(16) + blockPos.getX();
                int nextInt2 = random.nextInt(16) + blockPos.getZ();
                int findOnGroundYPosition = findOnGroundYPosition(decorationContext, nextInt, decorationContext.getHeight(Heightmap.Types.MOTION_BLOCKING, nextInt, nextInt2), nextInt2, i);
                if (findOnGroundYPosition != Integer.MAX_VALUE) {
                    newArrayList.add(new BlockPos(nextInt, findOnGroundYPosition, nextInt2));
                    z = true;
                }
            }
            i++;
        } while (z);
        return newArrayList.stream();
    }

    private static int findOnGroundYPosition(DecorationContext decorationContext, int i, int i2, int i3, int i4) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, i2, i3);
        int i5 = 0;
        BlockState blockState = decorationContext.getBlockState(mutableBlockPos);
        for (int i6 = i2; i6 >= 1; i6--) {
            mutableBlockPos.setY(i6 - 1);
            BlockState blockState2 = decorationContext.getBlockState(mutableBlockPos);
            if (!isEmpty(blockState2) && isEmpty(blockState) && !blockState2.is(Blocks.BEDROCK)) {
                if (i5 == i4) {
                    return mutableBlockPos.getY() + 1;
                }
                i5++;
            }
            blockState = blockState2;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA);
    }
}
