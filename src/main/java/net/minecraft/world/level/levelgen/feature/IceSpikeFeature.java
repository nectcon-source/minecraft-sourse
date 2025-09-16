package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/IceSpikeFeature.class */
public class IceSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public IceSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > 2) {
            blockPos = blockPos.below();
        }
        if (!worldGenLevel.getBlockState(blockPos).is(Blocks.SNOW_BLOCK)) {
            return false;
        }
        BlockPos above = blockPos.above(random.nextInt(4));
        int nextInt = random.nextInt(4) + 7;
        int nextInt2 = (nextInt / 4) + random.nextInt(2);
        if (nextInt2 > 1 && random.nextInt(60) == 0) {
            above = above.above(10 + random.nextInt(30));
        }
        for (int i = 0; i < nextInt; i++) {
            float f = (1.0f - (i / nextInt)) * nextInt2;
            int ceil = Mth.ceil(f);
            for (int i2 = -ceil; i2 <= ceil; i2++) {
                float abs = Mth.abs(i2) - 0.25f;
                for (int i3 = -ceil; i3 <= ceil; i3++) {
                    float abs2 = Mth.abs(i3) - 0.25f;
                    if (((i2 == 0 && i3 == 0) || (abs * abs) + (abs2 * abs2) <= f * f) && ((i2 != (-ceil) && i2 != ceil && i3 != (-ceil) && i3 != ceil) || random.nextFloat() <= 0.75f)) {
                        BlockState blockState = worldGenLevel.getBlockState(above.offset(i2, i, i3));
                        Block block = blockState.getBlock();
                        if (blockState.isAir() || isDirt(block) || block == Blocks.SNOW_BLOCK || block == Blocks.ICE) {
                            setBlock(worldGenLevel, above.offset(i2, i, i3), Blocks.PACKED_ICE.defaultBlockState());
                        }
                        if (i != 0 && ceil > 1) {
                            BlockState blockState2 = worldGenLevel.getBlockState(above.offset(i2, -i, i3));
                            Block block2 = blockState2.getBlock();
                            if (blockState2.isAir() || isDirt(block2) || block2 == Blocks.SNOW_BLOCK || block2 == Blocks.ICE) {
                                setBlock(worldGenLevel, above.offset(i2, -i, i3), Blocks.PACKED_ICE.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
        int i4 = nextInt2 - 1;
        if (i4 < 0) {
            i4 = 0;
        } else if (i4 > 1) {
            i4 = 1;
        }
        for (int i5 = -i4; i5 <= i4; i5++) {
            for (int i6 = -i4; i6 <= i4; i6++) {
                BlockPos offset = above.offset(i5, -1, i6);
                int i7 = 50;
                if (Math.abs(i5) == 1 && Math.abs(i6) == 1) {
                    i7 = random.nextInt(5);
                }
                while (offset.getY() > 50) {
                    BlockState blockState3 = worldGenLevel.getBlockState(offset);
                    Block block3 = blockState3.getBlock();
                    if (blockState3.isAir() || isDirt(block3) || block3 == Blocks.SNOW_BLOCK || block3 == Blocks.ICE || block3 == Blocks.PACKED_ICE) {
                        setBlock(worldGenLevel, offset, Blocks.PACKED_ICE.defaultBlockState());
                        offset = offset.below();
                        i7--;
                        if (i7 <= 0) {
                            offset = offset.below(random.nextInt(5) + 1);
                            i7 = random.nextInt(5);
                        }
                    }
                }
            }
        }
        return true;
    }
}
