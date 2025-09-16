package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/SwampSurfaceBuilder.class */
public class SwampSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public SwampSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        if (Biome.BIOME_INFO_NOISE.getValue(i * 0.25d, i2 * 0.25d, false) > 0.0d) {
            int i5 = i & 15;
            int i6 = i2 & 15;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            int i7 = i3;
            while (true) {
                if (i7 < 0) {
                    break;
                }
                mutableBlockPos.set(i5, i7, i6);
                if (chunkAccess.getBlockState(mutableBlockPos).isAir()) {
                    i7--;
                } else if (i7 == 62 && !chunkAccess.getBlockState(mutableBlockPos).is(blockState2.getBlock())) {
                    chunkAccess.setBlockState(mutableBlockPos, blockState2, false);
                }
            }
        }
        SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, i2, i3, d, blockState, blockState2, i4, j, surfaceBuilderBaseConfiguration);
    }
}
