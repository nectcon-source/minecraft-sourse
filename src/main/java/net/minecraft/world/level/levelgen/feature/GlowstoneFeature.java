package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/GlowstoneFeature.class */
public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        if (!worldGenLevel.isEmptyBlock(blockPos)) {
            return false;
        }
        BlockState blockState = worldGenLevel.getBlockState(blockPos.above());
        if (!blockState.is(Blocks.NETHERRACK) && !blockState.is(Blocks.BASALT) && !blockState.is(Blocks.BLACKSTONE)) {
            return false;
        }
        worldGenLevel.setBlock(blockPos, Blocks.GLOWSTONE.defaultBlockState(), 2);
        for (int i = 0; i < 1500; i++) {
            BlockPos offset = blockPos.offset(random.nextInt(8) - random.nextInt(8), -random.nextInt(12), random.nextInt(8) - random.nextInt(8));
            if (worldGenLevel.getBlockState(offset).isAir()) {
                int i2 = 0;
                for (Direction direction : Direction.values()) {
                    if (worldGenLevel.getBlockState(offset.relative(direction)).is(Blocks.GLOWSTONE)) {
                        i2++;
                    }
                    if (i2 > 1) {
                        break;
                    }
                }
                if (i2 == 1) {
                    worldGenLevel.setBlock(offset, Blocks.GLOWSTONE.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}
