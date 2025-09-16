package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BambooFeature.class */
public class BambooFeature extends Feature<ProbabilityFeatureConfiguration> {
    private static final BlockState BAMBOO_TRUNK = (BlockState) ((BlockState) ((BlockState) Blocks.BAMBOO.defaultBlockState().setValue(BambooBlock.AGE, 1)).setValue(BambooBlock.LEAVES, BambooLeaves.NONE)).setValue(BambooBlock.STAGE, 0);
    private static final BlockState BAMBOO_FINAL_LARGE = (BlockState) ((BlockState) BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE)).setValue(BambooBlock.STAGE, 1);
    private static final BlockState BAMBOO_TOP_LARGE = (BlockState) BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE);
    private static final BlockState BAMBOO_TOP_SMALL = (BlockState) BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL);

    public BambooFeature(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        int i = 0;
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        BlockPos.MutableBlockPos mutable2 = blockPos.mutable();
        if (worldGenLevel.isEmptyBlock(mutable)) {
            if (Blocks.BAMBOO.defaultBlockState().canSurvive(worldGenLevel, mutable)) {
                int nextInt = random.nextInt(12) + 5;
                if (random.nextFloat() < probabilityFeatureConfiguration.probability) {
                    int nextInt2 = random.nextInt(4) + 1;
                    for (int x = blockPos.getX() - nextInt2; x <= blockPos.getX() + nextInt2; x++) {
                        for (int z = blockPos.getZ() - nextInt2; z <= blockPos.getZ() + nextInt2; z++) {
                            int x2 = x - blockPos.getX();
                            int z2 = z - blockPos.getZ();
                            if ((x2 * x2) + (z2 * z2) <= nextInt2 * nextInt2) {
                                mutable2.set(x, worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
                                if (isDirt(worldGenLevel.getBlockState(mutable2).getBlock())) {
                                    worldGenLevel.setBlock(mutable2, Blocks.PODZOL.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
                for (int i2 = 0; i2 < nextInt && worldGenLevel.isEmptyBlock(mutable); i2++) {
                    worldGenLevel.setBlock(mutable, BAMBOO_TRUNK, 2);
                    mutable.move(Direction.UP, 1);
                }
                if (mutable.getY() - blockPos.getY() >= 3) {
                    worldGenLevel.setBlock(mutable, BAMBOO_FINAL_LARGE, 2);
                    worldGenLevel.setBlock(mutable.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
                    worldGenLevel.setBlock(mutable.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
                }
            }
            i = 0 + 1;
        }
        return i > 0;
    }
}
