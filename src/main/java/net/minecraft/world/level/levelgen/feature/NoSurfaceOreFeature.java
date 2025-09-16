package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/NoSurfaceOreFeature.class */
public class NoSurfaceOreFeature extends Feature<OreConfiguration> {
    NoSurfaceOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, OreConfiguration oreConfiguration) {
        int nextInt = random.nextInt(oreConfiguration.size + 1);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < nextInt; i++) {
            offsetTargetPos(mutableBlockPos, random, blockPos, Math.min(i, 7));
            if (oreConfiguration.target.test(worldGenLevel.getBlockState(mutableBlockPos), random) && !isFacingAir(worldGenLevel, mutableBlockPos)) {
                worldGenLevel.setBlock(mutableBlockPos, oreConfiguration.state, 2);
            }
        }
        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos mutableBlockPos, Random random, BlockPos blockPos, int i) {
        mutableBlockPos.setWithOffset(blockPos, getRandomPlacementInOneAxisRelativeToOrigin(random, i), getRandomPlacementInOneAxisRelativeToOrigin(random, i), getRandomPlacementInOneAxisRelativeToOrigin(random, i));
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(Random random, int i) {
        return Math.round((random.nextFloat() - random.nextFloat()) * i);
    }

    private boolean isFacingAir(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            if (levelAccessor.getBlockState(mutableBlockPos).isAir()) {
                return true;
            }
        }
        return false;
    }
}
