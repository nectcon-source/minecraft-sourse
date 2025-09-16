package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/GravellyMountainSurfaceBuilder.class */
public class GravellyMountainSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public GravellyMountainSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        if (d < -1.0d || d > 2.0d) {
            SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, i2, i3, d, blockState, blockState2, i4, j, SurfaceBuilder.CONFIG_GRAVEL);
        } else if (d > 1.0d) {
            SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, i2, i3, d, blockState, blockState2, i4, j, SurfaceBuilder.CONFIG_STONE);
        } else {
            SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, i2, i3, d, blockState, blockState2, i4, j, SurfaceBuilder.CONFIG_GRASS);
        }
    }
}
