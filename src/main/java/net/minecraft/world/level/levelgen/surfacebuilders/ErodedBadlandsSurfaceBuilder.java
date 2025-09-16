package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/ErodedBadlandsSurfaceBuilder.class */
public class ErodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

    public ErodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // net.minecraft.world.level.levelgen.surfacebuilders.BadlandsSurfaceBuilder, net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        BlockState blockState3;
        double d2 = 0.0d;
        double min = Math.min(Math.abs(d), this.pillarNoise.getValue(i * 0.25d, i2 * 0.25d, false) * 15.0d);
        if (min > 0.0d) {
            double d3 = min * min * 2.5d;
            double ceil = Math.ceil(Math.abs(this.pillarRoofNoise.getValue(i * 0.001953125d, i2 * 0.001953125d, false)) * 50.0d) + 14.0d;
            if (d3 > ceil) {
                d3 = ceil;
            }
            d2 = d3 + 64.0d;
        }
        int i5 = i & 15;
        int i6 = i2 & 15;
        BlockState blockState4 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration surfaceBuilderConfig = biome.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState underMaterial = surfaceBuilderConfig.getUnderMaterial();
        BlockState topMaterial = surfaceBuilderConfig.getTopMaterial();
        BlockState blockState5 = underMaterial;
        int nextDouble = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        boolean z = Math.cos((d / 3.0d) * 3.141592653589793d) > 0.0d;
        int i7 = -1;
        boolean z2 = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int max = Math.max(i3, ((int) d2) + 1); max >= 0; max--) {
            mutableBlockPos.set(i5, max, i6);
            if (chunkAccess.getBlockState(mutableBlockPos).isAir() && max < ((int) d2)) {
                chunkAccess.setBlockState(mutableBlockPos, blockState, false);
            }
            BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState6.isAir()) {
                i7 = -1;
            } else if (blockState6.is(blockState.getBlock())) {
                if (i7 == -1) {
                    z2 = false;
                    if (nextDouble <= 0) {
                        blockState4 = Blocks.AIR.defaultBlockState();
                        blockState5 = blockState;
                    } else if (max >= i4 - 4 && max <= i4 + 1) {
                        blockState4 = WHITE_TERRACOTTA;
                        blockState5 = underMaterial;
                    }
                    if (max < i4 && (blockState4 == null || blockState4.isAir())) {
                        blockState4 = blockState2;
                    }
                    i7 = nextDouble + Math.max(0, max - i4);
                    if (max >= i4 - 1) {
                        if (max > i4 + 3 + nextDouble) {
                            if (max < 64 || max > 127) {
                                blockState3 = ORANGE_TERRACOTTA;
                            } else if (z) {
                                blockState3 = TERRACOTTA;
                            } else {
                                blockState3 = getBand(i, max, i2);
                            }
                            chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                        } else {
                            chunkAccess.setBlockState(mutableBlockPos, topMaterial, false);
                            z2 = true;
                        }
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
                        Block block = blockState5.getBlock();
                        if (block == Blocks.WHITE_TERRACOTTA || block == Blocks.ORANGE_TERRACOTTA || block == Blocks.MAGENTA_TERRACOTTA || block == Blocks.LIGHT_BLUE_TERRACOTTA || block == Blocks.YELLOW_TERRACOTTA || block == Blocks.LIME_TERRACOTTA || block == Blocks.PINK_TERRACOTTA || block == Blocks.GRAY_TERRACOTTA || block == Blocks.LIGHT_GRAY_TERRACOTTA || block == Blocks.CYAN_TERRACOTTA || block == Blocks.PURPLE_TERRACOTTA || block == Blocks.BLUE_TERRACOTTA || block == Blocks.BROWN_TERRACOTTA || block == Blocks.GREEN_TERRACOTTA || block == Blocks.RED_TERRACOTTA || block == Blocks.BLACK_TERRACOTTA) {
                            chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                        }
                    }
                } else if (i7 > 0) {
                    i7--;
                    if (z2) {
                        chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, getBand(i, max, i2), false);
                    }
                }
            }
        }
    }
}
