package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/ReplaceBlockFeature.class */
public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
    public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ReplaceBlockConfiguration replaceBlockConfiguration) {
        if (worldGenLevel.getBlockState(blockPos).is(replaceBlockConfiguration.target.getBlock())) {
            worldGenLevel.setBlock(blockPos, replaceBlockConfiguration.state, 2);
            return true;
        }
        return true;
    }
}
