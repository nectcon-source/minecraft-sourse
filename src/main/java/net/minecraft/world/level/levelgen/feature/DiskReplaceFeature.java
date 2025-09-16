package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/DiskReplaceFeature.class */
public class DiskReplaceFeature extends BaseDiskFeature {
    public DiskReplaceFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.level.levelgen.feature.BaseDiskFeature, net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DiskConfiguration diskConfiguration) {
        if (!worldGenLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
            return false;
        }
        return super.place(worldGenLevel, chunkGenerator, random, blockPos, diskConfiguration);
    }
}
