package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/DecoratedFeature.class */
public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
    public DecoratedFeature(Codec<DecoratedFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DecoratedFeatureConfiguration decoratedFeatureConfiguration) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        decoratedFeatureConfiguration.decorator.getPositions(new DecorationContext(worldGenLevel, chunkGenerator), random, blockPos).forEach(blockPos2 -> {
            if (decoratedFeatureConfiguration.feature.get().place(worldGenLevel, chunkGenerator, random, blockPos2)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.isTrue();
    }

    public String toString() {
        return String.format("< %s [%s] >", getClass().getSimpleName(), Registry.FEATURE.getKey(this));
    }
}
