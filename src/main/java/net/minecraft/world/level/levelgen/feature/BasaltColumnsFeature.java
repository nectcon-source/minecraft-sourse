package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BasaltColumnsFeature.class */
public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);

    public BasaltColumnsFeature(Codec<ColumnFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ColumnFeatureConfiguration columnFeatureConfiguration) {
        int seaLevel = chunkGenerator.getSeaLevel();
        if (!canPlaceAt(worldGenLevel, seaLevel, blockPos.mutable())) {
            return false;
        }
        int sample = columnFeatureConfiguration.height().sample(random);
        boolean z = random.nextFloat() < 0.9f;
        int min = Math.min(sample, z ? 5 : 8);
        boolean z2 = false;
        for (BlockPos blockPos2 : BlockPos.randomBetweenClosed(random, z ? 50 : 15, blockPos.getX() - min, blockPos.getY(), blockPos.getZ() - min, blockPos.getX() + min, blockPos.getY(), blockPos.getZ() + min)) {
            int distManhattan = sample - blockPos2.distManhattan(blockPos);
            if (distManhattan >= 0) {
                z2 |= placeColumn(worldGenLevel, seaLevel, blockPos2, distManhattan, columnFeatureConfiguration.reach().sample(random));
            }
        }
        return z2;
    }

    private boolean placeColumn(LevelAccessor levelAccessor, int i, BlockPos blockPos, int i2, int i3) {
        boolean z = false;
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.getX() - i3, blockPos.getY(), blockPos.getZ() - i3, blockPos.getX() + i3, blockPos.getY(), blockPos.getZ() + i3)) {
            int distManhattan = blockPos2.distManhattan(blockPos);
            BlockPos findSurface = isAirOrLavaOcean(levelAccessor, i, blockPos2) ? findSurface(levelAccessor, i, blockPos2.mutable(), distManhattan) : findAir(levelAccessor, blockPos2.mutable(), distManhattan);
            if (findSurface != null) {
                BlockPos.MutableBlockPos mutable = findSurface.mutable();
                for (int i4 = i2 - (distManhattan / 2); i4 >= 0; i4--) {
                    if (isAirOrLavaOcean(levelAccessor, i, mutable)) {
                        setBlock(levelAccessor, mutable, Blocks.BASALT.defaultBlockState());
                        mutable.move(Direction.UP);
                        z = true;
                    } else if (levelAccessor.getBlockState(mutable).is(Blocks.BASALT)) {
                        mutable.move(Direction.UP);
                    }
                }
            }
        }
        return z;
    }

    @Nullable
    private static BlockPos findSurface(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos, int i2) {
        while (mutableBlockPos.getY() > 1 && i2 > 0) {
            i2--;
            if (canPlaceAt(levelAccessor, i, mutableBlockPos)) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean canPlaceAt(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos) {
        if (isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
            mutableBlockPos.move(Direction.UP);
            return (blockState.isAir() || CANNOT_PLACE_ON.contains(blockState.getBlock())) ? false : true;
        }
        return false;
    }

    @Nullable
    private static BlockPos findAir(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, int i) {
        while (mutableBlockPos.getY() < levelAccessor.getMaxBuildHeight() && i > 0) {
            i--;
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
            if (CANNOT_PLACE_ON.contains(blockState.getBlock())) {
                return null;
            }
            if (blockState.isAir()) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.UP);
        }
        return null;
    }

    private static boolean isAirOrLavaOcean(LevelAccessor levelAccessor, int i, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return blockState.isAir() || (blockState.is(Blocks.LAVA) && blockPos.getY() <= i);
    }
}
