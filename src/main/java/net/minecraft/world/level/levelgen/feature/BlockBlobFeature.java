package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BlockBlobFeature.class */
public class BlockBlobFeature extends Feature<BlockStateConfiguration> {
    public BlockBlobFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateConfiguration blockStateConfiguration) {
        while (blockPos.getY() > 3) {
            if (!worldGenLevel.isEmptyBlock(blockPos.below())) {
                Block block = worldGenLevel.getBlockState(blockPos.below()).getBlock();
                if (isDirt(block) || isStone(block)) {
                    break;
                }
            }
            blockPos = blockPos.below();
        }
        if (blockPos.getY() <= 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            int nextInt = random.nextInt(2);
            int nextInt2 = random.nextInt(2);
            int nextInt3 = random.nextInt(2);
            float f = ((nextInt + nextInt2 + nextInt3) * 0.333f) + 0.5f;
            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-nextInt, -nextInt2, -nextInt3), blockPos.offset(nextInt, nextInt2, nextInt3))) {
                if (blockPos2.distSqr(blockPos) <= f * f) {
                    worldGenLevel.setBlock(blockPos2, blockStateConfiguration.state, 4);
                }
            }
            blockPos = blockPos.offset((-1) + random.nextInt(2), -random.nextInt(2), (-1) + random.nextInt(2));
        }
        return true;
    }
}
