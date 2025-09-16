package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BaseDiskFeature.class */
public class BaseDiskFeature extends Feature<DiskConfiguration> {
    public BaseDiskFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DiskConfiguration diskConfiguration) {
        boolean z = false;
        int sample = diskConfiguration.radius.sample(random);
        for (int x = blockPos.getX() - sample; x <= blockPos.getX() + sample; x++) {
            for (int z2 = blockPos.getZ() - sample; z2 <= blockPos.getZ() + sample; z2++) {
                int x2 = x - blockPos.getX();
                int z3 = z2 - blockPos.getZ();
                if ((x2 * x2) + (z3 * z3) <= sample * sample) {
                    for (int y = blockPos.getY() - diskConfiguration.halfHeight; y <= blockPos.getY() + diskConfiguration.halfHeight; y++) {
                        BlockPos blockPos2 = new BlockPos(x, y, z2);
                        Block block = worldGenLevel.getBlockState(blockPos2).getBlock();
                        Iterator<BlockState> it = diskConfiguration.targets.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            if (it.next().is(block)) {
                                worldGenLevel.setBlock(blockPos2, diskConfiguration.state, 2);
                                z = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return z;
    }
}
