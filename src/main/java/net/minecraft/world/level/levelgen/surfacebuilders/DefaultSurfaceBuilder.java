package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/DefaultSurfaceBuilder.class */
public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        apply(random, chunkAccess, biome, i, i2, i3, d, blockState, blockState2, surfaceBuilderBaseConfiguration.getTopMaterial(), surfaceBuilderBaseConfiguration.getUnderMaterial(), surfaceBuilderBaseConfiguration.getUnderwaterMaterial(), i4);
    }

    protected void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4, BlockState blockState5, int i4) {
        BlockState blockState6 = blockState3;
        BlockState blockState7 = blockState4;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i5 = -1;
        int nextDouble = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        int i6 = i & 15;
        int i7 = i2 & 15;
        for (int i8 = i3; i8 >= 0; i8--) {
            mutableBlockPos.set(i6, i8, i7);
            BlockState blockState8 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState8.isAir()) {
                i5 = -1;
            } else if (blockState8.is(blockState.getBlock())) {
                if (i5 == -1) {
                    if (nextDouble <= 0) {
                        blockState6 = Blocks.AIR.defaultBlockState();
                        blockState7 = blockState;
                    } else if (i8 >= i4 - 4 && i8 <= i4 + 1) {
                        blockState6 = blockState3;
                        blockState7 = blockState4;
                    }
                    if (i8 < i4 && (blockState6 == null || blockState6.isAir())) {
                        if (biome.getTemperature(mutableBlockPos.set(i, i8, i2)) < 0.15f) {
                            blockState6 = Blocks.ICE.defaultBlockState();
                        } else {
                            blockState6 = blockState2;
                        }
                        mutableBlockPos.set(i6, i8, i7);
                    }
                    i5 = nextDouble;
                    if (i8 >= i4 - 1) {
                        chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
                    } else if (i8 < (i4 - 7) - nextDouble) {
                        blockState6 = Blocks.AIR.defaultBlockState();
                        blockState7 = blockState;
                        chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
                    }
                } else if (i5 > 0) {
                    i5--;
                    chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
                    if (i5 == 0 && blockState7.is(Blocks.SAND) && nextDouble > 1) {
                        i5 = random.nextInt(4) + Math.max(0, i8 - 63);
                        blockState7 = blockState7.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                    }
                }
            }
        }
    }
}
