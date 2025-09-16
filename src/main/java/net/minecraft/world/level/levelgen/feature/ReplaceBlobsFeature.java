package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/ReplaceBlobsFeature.class */
public class ReplaceBlobsFeature extends Feature<ReplaceSphereConfiguration> {
    public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ReplaceSphereConfiguration replaceSphereConfiguration) {
        Block block = replaceSphereConfiguration.targetState.getBlock();
        BlockPos findTarget = findTarget(worldGenLevel, blockPos.mutable().clamp(Direction.Axis.Y, 1, worldGenLevel.getMaxBuildHeight() - 1), block);
        if (findTarget == null) {
            return false;
        }
        int sample = replaceSphereConfiguration.radius().sample(random);
        boolean z = false;
        for (BlockPos blockPos2 : BlockPos.withinManhattan(findTarget, sample, sample, sample)) {
            if (blockPos2.distManhattan(findTarget) > sample) {
                break;
            }
            if (worldGenLevel.getBlockState(blockPos2).is(block)) {
                setBlock(worldGenLevel, blockPos2, replaceSphereConfiguration.replaceState);
                z = true;
            }
        }
        return z;
    }

    @Nullable
    private static BlockPos findTarget(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, Block block) {
        while (mutableBlockPos.getY() > 1) {
            if (levelAccessor.getBlockState(mutableBlockPos).is(block)) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return null;
    }
}
