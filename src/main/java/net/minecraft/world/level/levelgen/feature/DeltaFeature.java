package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/DeltaFeature.class */
public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
    private static final Direction[] DIRECTIONS = Direction.values();

    public DeltaFeature(Codec<DeltaFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DeltaFeatureConfiguration deltaFeatureConfiguration) {
        boolean z = false;
        boolean z2 = random.nextDouble() < 0.9d;
        int sample = z2 ? deltaFeatureConfiguration.rimSize().sample(random) : 0;
        int sample2 = z2 ? deltaFeatureConfiguration.rimSize().sample(random) : 0;
        boolean z3 = (!z2 || sample == 0 || sample2 == 0) ? false : true;
        int sample3 = deltaFeatureConfiguration.size().sample(random);
        int sample4 = deltaFeatureConfiguration.size().sample(random);
        int max = Math.max(sample3, sample4);
        for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, sample3, 0, sample4)) {
            if (blockPos2.distManhattan(blockPos) > max) {
                break;
            }
            if (isClear(worldGenLevel, blockPos2, deltaFeatureConfiguration)) {
                if (z3) {
                    z = true;
                    setBlock(worldGenLevel, blockPos2, deltaFeatureConfiguration.rim());
                }
                BlockPos offset = blockPos2.offset(sample, 0, sample2);
                if (isClear(worldGenLevel, offset, deltaFeatureConfiguration)) {
                    z = true;
                    setBlock(worldGenLevel, offset, deltaFeatureConfiguration.contents());
                }
            }
        }
        return z;
    }

    private static boolean isClear(LevelAccessor levelAccessor, BlockPos blockPos, DeltaFeatureConfiguration deltaFeatureConfiguration) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.is(deltaFeatureConfiguration.contents().getBlock()) || CANNOT_REPLACE.contains(blockState.getBlock())) {
            return false;
        }
        for (Direction direction : DIRECTIONS) {
            boolean isAir = levelAccessor.getBlockState(blockPos.relative(direction)).isAir();
            if (isAir && direction != Direction.UP) {
                return false;
            }
            if (!isAir && direction == Direction.UP) {
                return false;
            }
        }
        return true;
    }
}
