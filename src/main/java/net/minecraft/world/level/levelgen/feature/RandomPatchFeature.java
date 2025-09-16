package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/RandomPatchFeature.class */
public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomPatchConfiguration randomPatchConfiguration) {
        BlockPos blockPos2;
        BlockState state = randomPatchConfiguration.stateProvider.getState(random, blockPos);
        if (randomPatchConfiguration.project) {
            blockPos2 = worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, blockPos);
        } else {
            blockPos2 = blockPos;
        }
        int i = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i2 = 0; i2 < randomPatchConfiguration.tries; i2++) {
            mutableBlockPos.setWithOffset(blockPos2, random.nextInt(randomPatchConfiguration.xspread + 1) - random.nextInt(randomPatchConfiguration.xspread + 1), random.nextInt(randomPatchConfiguration.yspread + 1) - random.nextInt(randomPatchConfiguration.yspread + 1), random.nextInt(randomPatchConfiguration.zspread + 1) - random.nextInt(randomPatchConfiguration.zspread + 1));
            BlockPos below = mutableBlockPos.below();
            BlockState blockState = worldGenLevel.getBlockState(below);
            if ((worldGenLevel.isEmptyBlock(mutableBlockPos) || (randomPatchConfiguration.canReplace && worldGenLevel.getBlockState(mutableBlockPos).getMaterial().isReplaceable())) && state.canSurvive(worldGenLevel, mutableBlockPos) && ((randomPatchConfiguration.whitelist.isEmpty() || randomPatchConfiguration.whitelist.contains(blockState.getBlock())) && !randomPatchConfiguration.blacklist.contains(blockState) && (!randomPatchConfiguration.needWater || worldGenLevel.getFluidState(below.west()).is(FluidTags.WATER) || worldGenLevel.getFluidState(below.east()).is(FluidTags.WATER) || worldGenLevel.getFluidState(below.north()).is(FluidTags.WATER) || worldGenLevel.getFluidState(below.south()).is(FluidTags.WATER)))) {
                randomPatchConfiguration.blockPlacer.place(worldGenLevel, mutableBlockPos, state, random);
                i++;
            }
        }
        return i > 0;
    }
}
