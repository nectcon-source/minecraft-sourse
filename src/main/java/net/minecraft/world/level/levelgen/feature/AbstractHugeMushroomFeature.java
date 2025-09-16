package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/AbstractHugeMushroomFeature.class */
public abstract class AbstractHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
    protected abstract int getTreeRadiusForHeight(int i, int i2, int i3, int i4);

    protected abstract void makeCap(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration);

    public AbstractHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    protected void placeTrunk(LevelAccessor levelAccessor, Random random, BlockPos blockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration, int i, BlockPos.MutableBlockPos mutableBlockPos) {
        for (int i2 = 0; i2 < i; i2++) {
            mutableBlockPos.set(blockPos).move(Direction.UP, i2);
            if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
                setBlock(levelAccessor, mutableBlockPos, hugeMushroomFeatureConfiguration.stemProvider.getState(random, blockPos));
            }
        }
    }

    protected int getTreeHeight(Random random) {
        int nextInt = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            nextInt *= 2;
        }
        return nextInt;
    }

    protected boolean isValidPosition(LevelAccessor levelAccessor, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int y = blockPos.getY();
        if (y < 1 || y + i + 1 >= 256) {
            return false;
        }
        Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
        if (!isDirt(block) && !block.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
        }
        for (int i2 = 0; i2 <= i; i2++) {
            int treeRadiusForHeight = getTreeRadiusForHeight(-1, -1, hugeMushroomFeatureConfiguration.foliageRadius, i2);
            for (int i3 = -treeRadiusForHeight; i3 <= treeRadiusForHeight; i3++) {
                for (int i4 = -treeRadiusForHeight; i4 <= treeRadiusForHeight; i4++) {
                    BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, i3, i2, i4));
                    if (!blockState.isAir() && !blockState.is(BlockTags.LEAVES)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int treeHeight = getTreeHeight(random);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        if (!isValidPosition(worldGenLevel, blockPos, treeHeight, mutableBlockPos, hugeMushroomFeatureConfiguration)) {
            return false;
        }
        makeCap(worldGenLevel, random, blockPos, treeHeight, mutableBlockPos, hugeMushroomFeatureConfiguration);
        placeTrunk(worldGenLevel, random, blockPos, hugeMushroomFeatureConfiguration, treeHeight, mutableBlockPos);
        return true;
    }
}
