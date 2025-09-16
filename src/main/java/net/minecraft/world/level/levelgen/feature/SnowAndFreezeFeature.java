package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SnowAndFreezeFeature.class */
public class SnowAndFreezeFeature extends Feature<NoneFeatureConfiguration> {
    public SnowAndFreezeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 16; i++) {
            for (int i2 = 0; i2 < 16; i2++) {
                int x = blockPos.getX() + i;
                int z = blockPos.getZ() + i2;
                mutableBlockPos.set(x, worldGenLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
                mutableBlockPos2.set(mutableBlockPos).move(Direction.DOWN, 1);
                Biome biome = worldGenLevel.getBiome(mutableBlockPos);
                if (biome.shouldFreeze(worldGenLevel, mutableBlockPos2, false)) {
                    worldGenLevel.setBlock(mutableBlockPos2, Blocks.ICE.defaultBlockState(), 2);
                }
                if (biome.shouldSnow(worldGenLevel, mutableBlockPos)) {
                    worldGenLevel.setBlock(mutableBlockPos, Blocks.SNOW.defaultBlockState(), 2);
                    BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos2);
                    if (blockState.hasProperty(SnowyDirtBlock.SNOWY)) {
                        worldGenLevel.setBlock(mutableBlockPos2, (BlockState) blockState.setValue(SnowyDirtBlock.SNOWY, true), 2);
                    }
                }
            }
        }
        return true;
    }
}
