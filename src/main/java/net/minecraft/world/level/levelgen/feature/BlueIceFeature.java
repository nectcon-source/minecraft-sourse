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
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BlueIceFeature.class */
public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
    public BlueIceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        if (blockPos.getY() > worldGenLevel.getSeaLevel() - 1) {
            return false;
        }
        if (!worldGenLevel.getBlockState(blockPos).is(Blocks.WATER) && !worldGenLevel.getBlockState(blockPos.below()).is(Blocks.WATER)) {
            return false;
        }
        boolean z = false;
        Direction[] values = Direction.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Direction direction = values[i];
            if (direction == Direction.DOWN || !worldGenLevel.getBlockState(blockPos.relative(direction)).is(Blocks.PACKED_ICE)) {
                i++;
            } else {
                z = true;
                break;
            }
        }
        if (!z) {
            return false;
        }
        worldGenLevel.setBlock(blockPos, Blocks.BLUE_ICE.defaultBlockState(), 2);
        for (int i2 = 0; i2 < 200; i2++) {
            int nextInt = random.nextInt(5) - random.nextInt(6);
            int i3 = 3;
            if (nextInt < 2) {
                i3 = 3 + (nextInt / 2);
            }
            if (i3 >= 1) {
                BlockPos offset = blockPos.offset(random.nextInt(i3) - random.nextInt(i3), nextInt, random.nextInt(i3) - random.nextInt(i3));
                BlockState blockState = worldGenLevel.getBlockState(offset);
                if (blockState.getMaterial() == Material.AIR || blockState.is(Blocks.WATER) || blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.ICE)) {
                    Direction[] values2 = Direction.values();
                    int length2 = values2.length;
                    int i4 = 0;
                    while (true) {
                        if (i4 >= length2) {
                            break;
                        }
                        if (!worldGenLevel.getBlockState(offset.relative(values2[i4])).is(Blocks.BLUE_ICE)) {
                            i4++;
                        } else {
                            worldGenLevel.setBlock(offset, Blocks.BLUE_ICE.defaultBlockState(), 2);
                            break;
                        }
                    }
                }
            }
        }
        return true;
    }
}
