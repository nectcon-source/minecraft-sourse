package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SeaPickleFeature.class */
public class SeaPickleFeature extends Feature<CountConfiguration> {
    public SeaPickleFeature(Codec<CountConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, CountConfiguration countConfiguration) {
        int i = 0;
        int sample = countConfiguration.count().sample(random);
        for (int i2 = 0; i2 < sample; i2++) {
            int nextInt = random.nextInt(8) - random.nextInt(8);
            int nextInt2 = random.nextInt(8) - random.nextInt(8);
            BlockPos blockPos2 = new BlockPos(blockPos.getX() + nextInt, worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + nextInt, blockPos.getZ() + nextInt2), blockPos.getZ() + nextInt2);
            BlockState blockState = (BlockState) Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(random.nextInt(4) + 1));
            if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER) && blockState.canSurvive(worldGenLevel, blockPos2)) {
                worldGenLevel.setBlock(blockPos2, blockState, 2);
                i++;
            }
        }
        return i > 0;
    }
}
