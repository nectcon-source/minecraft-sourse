package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/NetherSurfaceBuilder.class */
public class NetherSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    protected long seed;
    protected PerlinNoise decorationNoise;

    public NetherSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int i5 = i & 15;
        int i6 = i2 & 15;
        boolean z = (this.decorationNoise.getValue(((double) i) * 0.03125d, ((double) i2) * 0.03125d, 0.0d) * 75.0d) + random.nextDouble() > 0.0d;
        boolean z2 = (this.decorationNoise.getValue(((double) i) * 0.03125d, 109.0d, ((double) i2) * 0.03125d) * 75.0d) + random.nextDouble() > 0.0d;
        int nextDouble = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i7 = -1;
        BlockState topMaterial = surfaceBuilderBaseConfiguration.getTopMaterial();
        BlockState underMaterial = surfaceBuilderBaseConfiguration.getUnderMaterial();
        for (int i8 = 127; i8 >= 0; i8--) {
            mutableBlockPos.set(i5, i8, i6);
            BlockState blockState3 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState3.isAir()) {
                i7 = -1;
            } else if (blockState3.is(blockState.getBlock())) {
                if (i7 == -1) {
                    boolean z3 = false;
                    if (nextDouble > 0) {
                        if (i8 >= i4 - 4 && i8 <= i4 + 1) {
                            topMaterial = surfaceBuilderBaseConfiguration.getTopMaterial();
                            underMaterial = surfaceBuilderBaseConfiguration.getUnderMaterial();
                            if (z2) {
                                topMaterial = GRAVEL;
                                underMaterial = surfaceBuilderBaseConfiguration.getUnderMaterial();
                            }
                            if (z) {
                                topMaterial = SOUL_SAND;
                                underMaterial = SOUL_SAND;
                            }
                        }
                    } else {
                        z3 = true;
                        underMaterial = surfaceBuilderBaseConfiguration.getUnderMaterial();
                    }
                    if (i8 < i4 && z3) {
                        topMaterial = blockState2;
                    }
                    i7 = nextDouble;
                    if (i8 >= i4 - 1) {
                        chunkAccess.setBlockState(mutableBlockPos, topMaterial, false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, underMaterial, false);
                    }
                } else if (i7 > 0) {
                    i7--;
                    chunkAccess.setBlockState(mutableBlockPos, underMaterial, false);
                }
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void initNoise(long j) {
        if (this.seed != j || this.decorationNoise == null) {
            this.decorationNoise = new PerlinNoise(new WorldgenRandom(j), IntStream.rangeClosed(-3, 0));
        }
        this.seed = j;
    }
}
