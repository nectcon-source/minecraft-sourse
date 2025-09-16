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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BasaltPillarFeature.class */
public class BasaltPillarFeature extends Feature<NoneFeatureConfiguration> {
    public BasaltPillarFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        if (!worldGenLevel.isEmptyBlock(blockPos) || worldGenLevel.isEmptyBlock(blockPos.above())) {
            return false;
        }
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        BlockPos.MutableBlockPos mutable2 = blockPos.mutable();
        boolean z = true;
        boolean z2 = true;
        boolean z3 = true;
        boolean z4 = true;
        while (worldGenLevel.isEmptyBlock(mutable)) {
            if (Level.isOutsideBuildHeight(mutable)) {
                return true;
            }
            worldGenLevel.setBlock(mutable, Blocks.BASALT.defaultBlockState(), 2);
            z = z && placeHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.NORTH));
            z2 = z2 && placeHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.SOUTH));
            z3 = z3 && placeHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.WEST));
            z4 = z4 && placeHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.EAST));
            mutable.move(Direction.DOWN);
        }
        mutable.move(Direction.UP);
        placeBaseHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.NORTH));
        placeBaseHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.SOUTH));
        placeBaseHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.WEST));
        placeBaseHangOff(worldGenLevel, random, mutable2.setWithOffset(mutable, Direction.EAST));
        mutable.move(Direction.DOWN);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = -3; i < 4; i++) {
            for (int i2 = -3; i2 < 4; i2++) {
                if (random.nextInt(10) < 10 - (Mth.abs(i) * Mth.abs(i2))) {
                    mutableBlockPos.set(mutable.offset(i, 0, i2));
                    int i3 = 3;
                    while (worldGenLevel.isEmptyBlock(mutable2.setWithOffset(mutableBlockPos, Direction.DOWN))) {
                        mutableBlockPos.move(Direction.DOWN);
                        i3--;
                        if (i3 <= 0) {
                            break;
                        }
                    }
                    if (!worldGenLevel.isEmptyBlock(mutable2.setWithOffset(mutableBlockPos, Direction.DOWN))) {
                        worldGenLevel.setBlock(mutableBlockPos, Blocks.BASALT.defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }

    private void placeBaseHangOff(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
        if (random.nextBoolean()) {
            levelAccessor.setBlock(blockPos, Blocks.BASALT.defaultBlockState(), 2);
        }
    }

    private boolean placeHangOff(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
        if (random.nextInt(10) != 0) {
            levelAccessor.setBlock(blockPos, Blocks.BASALT.defaultBlockState(), 2);
            return true;
        }
        return false;
    }
}
