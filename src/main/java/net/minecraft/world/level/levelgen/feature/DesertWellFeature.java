package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/DesertWellFeature.class */
public class DesertWellFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sandSlab;
    private final BlockState sandstone;
    private final BlockState water;

    public DesertWellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
        this.sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
        this.sandstone = Blocks.SANDSTONE.defaultBlockState();
        this.water = Blocks.WATER.defaultBlockState();
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        BlockPos blockPos2;
        BlockPos above = blockPos.above();
        while (true) {
            blockPos2 = above;
            if (!worldGenLevel.isEmptyBlock(blockPos2) || blockPos2.getY() <= 2) {
                break;
            }
            above = blockPos2.below();
        }
        if (!IS_SAND.test(worldGenLevel.getBlockState(blockPos2))) {
            return false;
        }
        for (int i = -2; i <= 2; i++) {
            for (int i2 = -2; i2 <= 2; i2++) {
                if (worldGenLevel.isEmptyBlock(blockPos2.offset(i, -1, i2)) && worldGenLevel.isEmptyBlock(blockPos2.offset(i, -2, i2))) {
                    return false;
                }
            }
        }
        for (int i3 = -1; i3 <= 0; i3++) {
            for (int i4 = -2; i4 <= 2; i4++) {
                for (int i5 = -2; i5 <= 2; i5++) {
                    worldGenLevel.setBlock(blockPos2.offset(i4, i3, i5), this.sandstone, 2);
                }
            }
        }
        worldGenLevel.setBlock(blockPos2, this.water, 2);
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            worldGenLevel.setBlock(blockPos2.relative(it.next()), this.water, 2);
        }
        for (int i6 = -2; i6 <= 2; i6++) {
            for (int i7 = -2; i7 <= 2; i7++) {
                if (i6 == -2 || i6 == 2 || i7 == -2 || i7 == 2) {
                    worldGenLevel.setBlock(blockPos2.offset(i6, 1, i7), this.sandstone, 2);
                }
            }
        }
        worldGenLevel.setBlock(blockPos2.offset(2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos2.offset(-2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos2.offset(0, 1, 2), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos2.offset(0, 1, -2), this.sandSlab, 2);
        for (int i8 = -1; i8 <= 1; i8++) {
            for (int i9 = -1; i9 <= 1; i9++) {
                if (i8 == 0 && i9 == 0) {
                    worldGenLevel.setBlock(blockPos2.offset(i8, 4, i9), this.sandstone, 2);
                } else {
                    worldGenLevel.setBlock(blockPos2.offset(i8, 4, i9), this.sandSlab, 2);
                }
            }
        }
        for (int i10 = 1; i10 <= 3; i10++) {
            worldGenLevel.setBlock(blockPos2.offset(-1, i10, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos2.offset(-1, i10, 1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos2.offset(1, i10, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos2.offset(1, i10, 1), this.sandstone, 2);
        }
        return true;
    }
}
