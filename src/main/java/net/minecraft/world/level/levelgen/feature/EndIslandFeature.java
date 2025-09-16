package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/EndIslandFeature.class */
public class EndIslandFeature extends Feature<NoneFeatureConfiguration> {
    public EndIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        float nextInt = random.nextInt(3) + 4;
        int i = 0;
        while (nextInt > 0.5f) {
            for (int floor = Mth.floor(-nextInt); floor <= Mth.ceil(nextInt); floor++) {
                for (int floor2 = Mth.floor(-nextInt); floor2 <= Mth.ceil(nextInt); floor2++) {
                    if ((floor * floor) + (floor2 * floor2) <= (nextInt + 1.0f) * (nextInt + 1.0f)) {
                        setBlock(worldGenLevel, blockPos.offset(floor, i, floor2), Blocks.END_STONE.defaultBlockState());
                    }
                }
            }
            nextInt = (float) (nextInt - (random.nextInt(2) + 0.5d));
            i--;
        }
        return true;
    }
}
