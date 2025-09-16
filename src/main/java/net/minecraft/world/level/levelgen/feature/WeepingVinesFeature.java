package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/WeepingVinesFeature.class */
public class WeepingVinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public WeepingVinesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        if (!worldGenLevel.isEmptyBlock(blockPos)) {
            return false;
        }
        BlockState blockState = worldGenLevel.getBlockState(blockPos.above());
        if (!blockState.is(Blocks.NETHERRACK) && !blockState.is(Blocks.NETHER_WART_BLOCK)) {
            return false;
        }
        placeRoofNetherWart(worldGenLevel, random, blockPos);
        placeRoofWeepingVines(worldGenLevel, random, blockPos);
        return true;
    }

    private void placeRoofNetherWart(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
        levelAccessor.setBlock(blockPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 200; i++) {
            mutableBlockPos.setWithOffset(blockPos, random.nextInt(6) - random.nextInt(6), random.nextInt(2) - random.nextInt(5), random.nextInt(6) - random.nextInt(6));
            if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
                int i2 = 0;
                for (Direction direction : DIRECTIONS) {
                    BlockState blockState = levelAccessor.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, direction));
                    if (blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.NETHER_WART_BLOCK)) {
                        i2++;
                    }
                    if (i2 > 1) {
                        break;
                    }
                }
                if (i2 == 1) {
                    levelAccessor.setBlock(mutableBlockPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
                }
            }
        }
    }

    private void placeRoofWeepingVines(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 100; i++) {
            mutableBlockPos.setWithOffset(blockPos, random.nextInt(8) - random.nextInt(8), random.nextInt(2) - random.nextInt(7), random.nextInt(8) - random.nextInt(8));
            if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
                BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.above());
                if (blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.NETHER_WART_BLOCK)) {
                    int nextInt = Mth.nextInt(random, 1, 8);
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
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int i2, int i3) {
        for (int i4 = 0; i4 <= i; i4++) {
            if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
                if (i4 == i || !levelAccessor.isEmptyBlock(mutableBlockPos.below())) {
                    levelAccessor.setBlock(mutableBlockPos, (BlockState) Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(random, i2, i3))), 2);
                    return;
                }
                levelAccessor.setBlock(mutableBlockPos, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
            }
            mutableBlockPos.move(Direction.DOWN);
        }
    }
}
