package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/AbstractFlowerFeature.class */
public abstract class AbstractFlowerFeature<U extends FeatureConfiguration> extends Feature<U> {
    public abstract boolean isValid(LevelAccessor levelAccessor, BlockPos blockPos, U u);

    public abstract int getCount(U u);

    public abstract BlockPos getPos(Random random, BlockPos blockPos, U u);

    public abstract BlockState getRandomFlower(Random random, BlockPos blockPos, U u);

    public AbstractFlowerFeature(Codec<U> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, U u) {
        BlockState randomFlower = getRandomFlower(random, blockPos, u);
        int i = 0;
        for (int i2 = 0; i2 < getCount(u); i2++) {
            BlockPos pos = getPos(random, blockPos, u);
            if (worldGenLevel.isEmptyBlock(pos) && pos.getY() < 255 && randomFlower.canSurvive(worldGenLevel, pos) && isValid(worldGenLevel, pos, u)) {
                worldGenLevel.setBlock(pos, randomFlower, 2);
                i++;
            }
        }
        return i > 0;
    }
}
