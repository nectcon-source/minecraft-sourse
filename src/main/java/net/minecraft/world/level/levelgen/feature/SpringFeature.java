package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SpringFeature.class */
public class SpringFeature extends Feature<SpringConfiguration> {
    public SpringFeature(Codec<SpringConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SpringConfiguration springConfiguration) {
        if (!springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.above()).getBlock())) {
            return false;
        }
        if (springConfiguration.requiresBlockBelow && !springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.below()).getBlock())) {
            return false;
        }
        BlockState blockState = worldGenLevel.getBlockState(blockPos);
        if (!blockState.isAir() && !springConfiguration.validBlocks.contains(blockState.getBlock())) {
            return false;
        }
        int i = 0;
        int i2 = 0;
        if (springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.west()).getBlock())) {
            i2 = 0 + 1;
        }
        if (springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.east()).getBlock())) {
            i2++;
        }
        if (springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.north()).getBlock())) {
            i2++;
        }
        if (springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.south()).getBlock())) {
            i2++;
        }
        if (springConfiguration.validBlocks.contains(worldGenLevel.getBlockState(blockPos.below()).getBlock())) {
            i2++;
        }
        int i3 = 0;
        if (worldGenLevel.isEmptyBlock(blockPos.west())) {
            i3 = 0 + 1;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.east())) {
            i3++;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.north())) {
            i3++;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.south())) {
            i3++;
        }
        if (worldGenLevel.isEmptyBlock(blockPos.below())) {
            i3++;
        }
        if (i2 == springConfiguration.rockCount && i3 == springConfiguration.holeCount) {
            worldGenLevel.setBlock(blockPos, springConfiguration.state.createLegacyBlock(), 2);
            worldGenLevel.getLiquidTicks().scheduleTick(blockPos, springConfiguration.state.getType(), 0);
            i = 0 + 1;
        }
        return i > 0;
    }
}
