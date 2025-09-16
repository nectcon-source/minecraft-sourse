package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/CoralFeature.class */
public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
    protected abstract boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState);

    public CoralFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        return placeFeature(worldGenLevel, random, blockPos, BlockTags.CORAL_BLOCKS.getRandomElement(random).defaultBlockState());
    }

    protected boolean placeCoralBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
        BlockPos above = blockPos.above();
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if ((!blockState2.is(Blocks.WATER) && !blockState2.is(BlockTags.CORALS)) || !levelAccessor.getBlockState(above).is(Blocks.WATER)) {
            return false;
        }
        levelAccessor.setBlock(blockPos, blockState, 3);
        if (random.nextFloat() < 0.25f) {
            levelAccessor.setBlock(above, BlockTags.CORALS.getRandomElement(random).defaultBlockState(), 2);
        } else if (random.nextFloat() < 0.05f) {
            levelAccessor.setBlock(above, (BlockState) Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(random.nextInt(4) + 1)), 2);
        }
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (random.nextFloat() < 0.2f) {
                BlockPos relative = blockPos.relative(next);
                if (levelAccessor.getBlockState(relative).is(Blocks.WATER)) {
                    levelAccessor.setBlock(relative, (BlockState) BlockTags.WALL_CORALS.getRandomElement(random).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, next), 2);
                }
            }
        }
        return true;
    }
}
