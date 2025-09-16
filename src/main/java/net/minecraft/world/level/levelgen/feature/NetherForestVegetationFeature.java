package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/NetherForestVegetationFeature.class */
public class NetherForestVegetationFeature extends Feature<BlockPileConfiguration> {
    public NetherForestVegetationFeature(Codec<BlockPileConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration) {
        return place(worldGenLevel, random, blockPos, blockPileConfiguration, 8, 4);
    }

    public static boolean place(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockPileConfiguration blockPileConfiguration, int i, int i2) {
        int y;
        if (!levelAccessor.getBlockState(blockPos.below()).getBlock().is(BlockTags.NYLIUM) || (y = blockPos.getY()) < 1 || y + 1 >= 256) {
            return false;
        }
        int i3 = 0;
        for (int i4 = 0; i4 < i * i; i4++) {
            BlockPos offset = blockPos.offset(random.nextInt(i) - random.nextInt(i), random.nextInt(i2) - random.nextInt(i2), random.nextInt(i) - random.nextInt(i));
            BlockState state = blockPileConfiguration.stateProvider.getState(random, offset);
            if (levelAccessor.isEmptyBlock(offset) && offset.getY() > 0 && state.canSurvive(levelAccessor, offset)) {
                levelAccessor.setBlock(offset, state, 2);
                i3++;
            }
        }
        return i3 > 0;
    }
}
