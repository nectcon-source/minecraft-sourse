package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/TwistingVinesFeature.class */
public class TwistingVinesFeature extends Feature<NoneFeatureConfiguration> {
    public TwistingVinesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        return place(worldGenLevel, random, blockPos, 8, 4, 8);
    }

    public static boolean place(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, int i2, int i3) {
        if (isInvalidPlacementLocation(levelAccessor, blockPos)) {
            return false;
        }
        placeTwistingVines(levelAccessor, random, blockPos, i, i2, i3);
        return true;
    }

    private static void placeTwistingVines(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, int i2, int i3) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i4 = 0; i4 < i * i; i4++) {
            mutableBlockPos.set(blockPos).move(Mth.nextInt(random, -i, i), Mth.nextInt(random, -i2, i2), Mth.nextInt(random, -i, i));
            if (findFirstAirBlockAboveGround(levelAccessor, mutableBlockPos) && !isInvalidPlacementLocation(levelAccessor, mutableBlockPos)) {
                int nextInt = Mth.nextInt(random, 1, i3);
                if (random.nextInt(6) == 0) {
                    nextInt *= 2;
                }
                if (random.nextInt(5) == 0) {
                    nextInt = 1;
                }
                placeWeepingVinesColumn(levelAccessor, random, mutableBlockPos, nextInt, 17, 25);
            }
        }
    }

    private static boolean findFirstAirBlockAboveGround(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos) {
        do {
            mutableBlockPos.move(0, -1, 0);
            if (Level.isOutsideBuildHeight(mutableBlockPos)) {
                return false;
            }
        } while (levelAccessor.getBlockState(mutableBlockPos).isAir());
        mutableBlockPos.move(0, 1, 0);
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int i2, int i3) {
        for (int i4 = 1; i4 <= i; i4++) {
            if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
                if (i4 == i || !levelAccessor.isEmptyBlock(mutableBlockPos.above())) {
                    levelAccessor.setBlock(mutableBlockPos, (BlockState) Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(random, i2, i3))), 2);
                    return;
                }
                levelAccessor.setBlock(mutableBlockPos, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
            }
            mutableBlockPos.move(Direction.UP);
        }
    }

    private static boolean isInvalidPlacementLocation(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!levelAccessor.isEmptyBlock(blockPos)) {
            return true;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos.below());
        return (blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.WARPED_NYLIUM) || blockState.is(Blocks.WARPED_WART_BLOCK)) ? false : true;
    }
}
