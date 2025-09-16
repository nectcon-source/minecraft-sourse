package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/HugeFungusFeature.class */
public class HugeFungusFeature extends Feature<HugeFungusConfiguration> {
    public HugeFungusFeature(Codec<HugeFungusConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, HugeFungusConfiguration hugeFungusConfiguration) {
        BlockPos blockPos2 = null;
        if (worldGenLevel.getBlockState(blockPos.below()).getBlock() == hugeFungusConfiguration.validBaseState.getBlock()) {
            blockPos2 = blockPos;
        }
        if (blockPos2 == null) {
            return false;
        }
        int nextInt = Mth.nextInt(random, 4, 13);
        if (random.nextInt(12) == 0) {
            nextInt *= 2;
        }
        if (!hugeFungusConfiguration.planted) {
            if (blockPos2.getY() + nextInt + 1 >= chunkGenerator.getGenDepth()) {
                return false;
            }
        }
        boolean z = !hugeFungusConfiguration.planted && random.nextFloat() < 0.06f;
        worldGenLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
        placeStem(worldGenLevel, random, hugeFungusConfiguration, blockPos2, nextInt, z);
        placeHat(worldGenLevel, random, hugeFungusConfiguration, blockPos2, nextInt, z);
        return true;
    }

    private static boolean isReplaceable(LevelAccessor levelAccessor, BlockPos blockPos, boolean z) {
        return levelAccessor.isStateAtPosition(blockPos, blockState -> {
            return blockState.getMaterial().isReplaceable() || (z && blockState.getMaterial() == Material.PLANT);
        });
    }

    private void placeStem(LevelAccessor levelAccessor, Random random, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int i, boolean z) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockState blockState = hugeFungusConfiguration.stemState;
        int i2 = z ? 1 : 0;
        for (int i3 = -i2; i3 <= i2; i3++) {
            for (int i4 = -i2; i4 <= i2; i4++) {
                boolean z2 = z && Mth.abs(i3) == i2 && Mth.abs(i4) == i2;
                for (int i5 = 0; i5 < i; i5++) {
                    mutableBlockPos.setWithOffset(blockPos, i3, i5, i4);
                    if (isReplaceable(levelAccessor, mutableBlockPos, true)) {
                        if (hugeFungusConfiguration.planted) {
                            if (!levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
                                levelAccessor.destroyBlock(mutableBlockPos, true);
                            }
                            levelAccessor.setBlock(mutableBlockPos, blockState, 3);
                        } else if (!z2) {
                            setBlock(levelAccessor, mutableBlockPos, blockState);
                        } else if (random.nextFloat() < 0.1f) {
                            setBlock(levelAccessor, mutableBlockPos, blockState);
                        }
                    }
                }
            }
        }
    }

    private void placeHat(LevelAccessor levelAccessor, Random random, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int i, boolean z) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        boolean is = hugeFungusConfiguration.hatState.is(Blocks.NETHER_WART_BLOCK);
        int min = Math.min(random.nextInt(1 + (i / 3)) + 5, i);
        int i2 = i - min;
        int i3 = i2;
        while (i3 <= i) {
            int i4 = i3 < i - random.nextInt(3) ? 2 : 1;
            if (min > 8 && i3 < i2 + 4) {
                i4 = 3;
            }
            if (z) {
                i4++;
            }
            int i5 = -i4;
            while (i5 <= i4) {
                int i6 = -i4;
                while (i6 <= i4) {
                    boolean z2 = i5 == (-i4) || i5 == i4;
                    boolean z3 = i6 == (-i4) || i6 == i4;
                    boolean z4 = (z2 || z3 || i3 == i) ? false : true;
                    boolean z5 = z2 && z3;
                    boolean z6 = i3 < i2 + 3;
                    mutableBlockPos.setWithOffset(blockPos, i5, i3, i6);
                    if (isReplaceable(levelAccessor, mutableBlockPos, false)) {
                        if (hugeFungusConfiguration.planted && !levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
                            levelAccessor.destroyBlock(mutableBlockPos, true);
                        }
                        if (z6) {
                            if (!z4) {
                                placeHatDropBlock(levelAccessor, random, mutableBlockPos, hugeFungusConfiguration.hatState, is);
                            }
                        } else if (z4) {
                            placeHatBlock(levelAccessor, random, hugeFungusConfiguration, mutableBlockPos, 0.1f, 0.2f, is ? 0.1f : 0.0f);
                        } else if (z5) {
                            placeHatBlock(levelAccessor, random, hugeFungusConfiguration, mutableBlockPos, 0.01f, 0.7f, is ? 0.083f : 0.0f);
                        } else {
                            placeHatBlock(levelAccessor, random, hugeFungusConfiguration, mutableBlockPos, 5.0E-4f, 0.98f, is ? 0.07f : 0.0f);
                        }
                    }
                    i6++;
                }
                i5++;
            }
            i3++;
        }
    }

    private void placeHatBlock(LevelAccessor levelAccessor, Random random, HugeFungusConfiguration hugeFungusConfiguration, BlockPos.MutableBlockPos mutableBlockPos, float f, float f2, float f3) {
        if (random.nextFloat() < f) {
            setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.decorState);
        } else if (random.nextFloat() < f2) {
            setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.hatState);
            if (random.nextFloat() < f3) {
                tryPlaceWeepingVines(mutableBlockPos, levelAccessor, random);
            }
        }
    }

    private void placeHatDropBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState, boolean z) {
        if (levelAccessor.getBlockState(blockPos.below()).is(blockState.getBlock())) {
            setBlock(levelAccessor, blockPos, blockState);
            return;
        }
        if (random.nextFloat() < 0.15d) {
            setBlock(levelAccessor, blockPos, blockState);
            if (z && random.nextInt(11) == 0) {
                tryPlaceWeepingVines(blockPos, levelAccessor, random);
            }
        }
    }

    private static void tryPlaceWeepingVines(BlockPos blockPos, LevelAccessor levelAccessor, Random random) {
        BlockPos.MutableBlockPos move = blockPos.mutable().move(Direction.DOWN);
        if (!levelAccessor.isEmptyBlock(move)) {
            return;
        }
        int nextInt = Mth.nextInt(random, 1, 5);
        if (random.nextInt(7) == 0) {
            nextInt *= 2;
        }
        WeepingVinesFeature.placeWeepingVinesColumn(levelAccessor, random, move, nextInt, 23, 25);
    }
}
