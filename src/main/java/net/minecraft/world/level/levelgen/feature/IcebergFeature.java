package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/IcebergFeature.class */
public class IcebergFeature extends Feature<BlockStateConfiguration> {
    public IcebergFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateConfiguration blockStateConfiguration) {
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), chunkGenerator.getSeaLevel(), blockPos.getZ());
        boolean z = random.nextDouble() > 0.7d;
        BlockState blockState = blockStateConfiguration.state;
        double nextDouble = random.nextDouble() * 2.0d * 3.141592653589793d;
        int nextInt = 11 - random.nextInt(5);
        int nextInt2 = 3 + random.nextInt(3);
        boolean z2 = random.nextDouble() > 0.7d;
        int nextInt3 = z2 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
        if (!z2 && random.nextDouble() > 0.9d) {
            nextInt3 += random.nextInt(19) + 7;
        }
        int min = Math.min(nextInt3 + random.nextInt(11), 18);
        int min2 = Math.min((nextInt3 + random.nextInt(7)) - random.nextInt(5), 11);
        int i = z2 ? nextInt : 11;
        for (int i2 = -i; i2 < i; i2++) {
            for (int i3 = -i; i3 < i; i3++) {
                for (int i4 = 0; i4 < nextInt3; i4++) {
                    int heightDependentRadiusEllipse = z2 ? heightDependentRadiusEllipse(i4, nextInt3, min2) : heightDependentRadiusRound(random, i4, nextInt3, min2);
                    if (z2 || i2 < heightDependentRadiusEllipse) {
                        generateIcebergBlock(worldGenLevel, random, blockPos2, nextInt3, i2, i4, i3, heightDependentRadiusEllipse, i, z2, nextInt2, nextDouble, z, blockState);
                    }
                }
            }
        }
        smooth(worldGenLevel, blockPos2, min2, nextInt3, z2, nextInt);
        for (int i5 = -i; i5 < i; i5++) {
            for (int i6 = -i; i6 < i; i6++) {
                for (int i7 = -1; i7 > (-min); i7--) {
                    int ceil = z2 ? Mth.ceil(i * (1.0f - (((float) Math.pow(i7, 2.0d)) / (min * 8.0f)))) : i;
                    int heightDependentRadiusSteep = heightDependentRadiusSteep(random, -i7, min, min2);
                    if (i5 < heightDependentRadiusSteep) {
                        generateIcebergBlock(worldGenLevel, random, blockPos2, min, i5, i7, i6, heightDependentRadiusSteep, ceil, z2, nextInt2, nextDouble, z, blockState);
                    }
                }
            }
        }
        if (z2 ? random.nextDouble() > 0.1d : random.nextDouble() > 0.7d) {
            generateCutOut(random, worldGenLevel, min2, nextInt3, blockPos2, z2, nextInt, nextDouble, nextInt2);
            return true;
        }
        return true;
    }

    private void generateCutOut(Random random, LevelAccessor levelAccessor, int i, int i2, BlockPos blockPos, boolean z, int i3, double d, int i4) {
        int i5 = random.nextBoolean() ? -1 : 1;
        int i6 = random.nextBoolean() ? -1 : 1;
        int nextInt = random.nextInt(Math.max((i / 2) - 2, 1));
        if (random.nextBoolean()) {
            nextInt = ((i / 2) + 1) - random.nextInt(Math.max((i - (i / 2)) - 1, 1));
        }
        int nextInt2 = random.nextInt(Math.max((i / 2) - 2, 1));
        if (random.nextBoolean()) {
            nextInt2 = ((i / 2) + 1) - random.nextInt(Math.max((i - (i / 2)) - 1, 1));
        }
        if (z) {
            int nextInt3 = random.nextInt(Math.max(i3 - 5, 1));
            nextInt2 = nextInt3;
            nextInt = nextInt3;
        }
        BlockPos blockPos2 = new BlockPos(i5 * nextInt, 0, i6 * nextInt2);
        double nextDouble = z ? d + 1.5707963267948966d : random.nextDouble() * 2.0d * 3.141592653589793d;
        for (int i7 = 0; i7 < i2 - 3; i7++) {
            carve(heightDependentRadiusRound(random, i7, i2, i), i7, blockPos, levelAccessor, false, nextDouble, blockPos2, i3, i4);
        }
        for (int i8 = -1; i8 > (-i2) + random.nextInt(5); i8--) {
            carve(heightDependentRadiusSteep(random, -i8, i2, i), i8, blockPos, levelAccessor, true, nextDouble, blockPos2, i3, i4);
        }
    }

    private void carve(int i, int i2, BlockPos blockPos, LevelAccessor levelAccessor, boolean z, double d, BlockPos blockPos2, int i3, int i4) {
        int i5 = i + 1 + (i3 / 3);
        int min = (Math.min(i - 3, 3) + (i4 / 2)) - 1;
        for (int i6 = -i5; i6 < i5; i6++) {
            for (int i7 = -i5; i7 < i5; i7++) {
                if (signedDistanceEllipse(i6, i7, blockPos2, i5, min, d) < 0.0d) {
                    BlockPos offset = blockPos.offset(i6, i2, i7);
                    Block block = levelAccessor.getBlockState(offset).getBlock();
                    if (isIcebergBlock(block) || block == Blocks.SNOW_BLOCK) {
                        if (z) {
                            setBlock(levelAccessor, offset, Blocks.WATER.defaultBlockState());
                        } else {
                            setBlock(levelAccessor, offset, Blocks.AIR.defaultBlockState());
                            removeFloatingSnowLayer(levelAccessor, offset);
                        }
                    }
                }
            }
        }
    }

    private void removeFloatingSnowLayer(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (levelAccessor.getBlockState(blockPos.above()).is(Blocks.SNOW)) {
            setBlock(levelAccessor, blockPos.above(), Blocks.AIR.defaultBlockState());
        }
    }

    private void generateIcebergBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, int i2, int i3, int i4, int i5, int i6, boolean z, int i7, double d, boolean z2, BlockState blockState) {
        double signedDistanceEllipse = z ? signedDistanceEllipse(i2, i4, BlockPos.ZERO, i6, getEllipseC(i3, i, i7), d) : signedDistanceCircle(i2, i4, BlockPos.ZERO, i5, random);
        if (signedDistanceEllipse < 0.0d) {
            BlockPos offset = blockPos.offset(i2, i3, i4);
            if (signedDistanceEllipse > (z ? -0.5d : (-6) - random.nextInt(3)) && random.nextDouble() > 0.9d) {
                return;
            }
            setIcebergBlock(offset, levelAccessor, random, i - i3, i, z, z2, blockState);
        }
    }

    private void setIcebergBlock(BlockPos blockPos, LevelAccessor levelAccessor, Random random, int i, int i2, boolean z, boolean z2, BlockState blockState) {
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if (blockState2.getMaterial() == Material.AIR || blockState2.is(Blocks.SNOW_BLOCK) || blockState2.is(Blocks.ICE) || blockState2.is(Blocks.WATER)) {
            boolean z3 = !z || random.nextDouble() > 0.05d;
            int i3 = z ? 3 : 2;
            if (z2 && !blockState2.is(Blocks.WATER) && i <= random.nextInt(Math.max(1, i2 / i3)) + (i2 * 0.6d) && z3) {
                setBlock(levelAccessor, blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
            } else {
                setBlock(levelAccessor, blockPos, blockState);
            }
        }
    }

    private int getEllipseC(int i, int i2, int i3) {
        int i4 = i3;
        if (i > 0 && i2 - i <= 3) {
            i4 -= 4 - (i2 - i);
        }
        return i4;
    }

    private double signedDistanceCircle(int i, int i2, BlockPos blockPos, int i3, Random random) {
        return ((((10.0f * Mth.clamp(random.nextFloat(), 0.2f, 0.8f)) / i3) + Math.pow(i - blockPos.getX(), 2.0d)) + Math.pow(i2 - blockPos.getZ(), 2.0d)) - Math.pow(i3, 2.0d);
    }

    private double signedDistanceEllipse(int i, int i2, BlockPos blockPos, int i3, int i4, double d) {
        return (Math.pow((((i - blockPos.getX()) * Math.cos(d)) - ((i2 - blockPos.getZ()) * Math.sin(d))) / i3, 2.0d) + Math.pow((((i - blockPos.getX()) * Math.sin(d)) + ((i2 - blockPos.getZ()) * Math.cos(d))) / i4, 2.0d)) - 1.0d;
    }

    private int heightDependentRadiusRound(Random random, int i, int i2, int i3) {
        float nextFloat = 3.5f - random.nextFloat();
        float pow = (1.0f - (((float) Math.pow(i, 2.0d)) / (i2 * nextFloat))) * i3;
        if (i2 > 15 + random.nextInt(5)) {
            pow = (1.0f - ((i < 3 + random.nextInt(6) ? i / 2 : i) / ((i2 * nextFloat) * 0.4f))) * i3;
        }
        return Mth.ceil(pow / 2.0f);
    }

    private int heightDependentRadiusEllipse(int i, int i2, int i3) {
        return Mth.ceil(((1.0f - (((float) Math.pow(i, 2.0d)) / (i2 * 1.0f))) * i3) / 2.0f);
    }

    private int heightDependentRadiusSteep(Random random, int i, int i2, int i3) {
        return Mth.ceil(((1.0f - (i / (i2 * (1.0f + (random.nextFloat() / 2.0f))))) * i3) / 2.0f);
    }

    private boolean isIcebergBlock(Block block) {
        return block == Blocks.PACKED_ICE || block == Blocks.SNOW_BLOCK || block == Blocks.BLUE_ICE;
    }

    private boolean belowIsAir(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos.below()).getMaterial() == Material.AIR;
    }

    private void smooth(LevelAccessor levelAccessor, BlockPos blockPos, int i, int i2, boolean z, int i3) {
        int i4 = z ? i3 : i / 2;
        for (int i5 = -i4; i5 <= i4; i5++) {
            for (int i6 = -i4; i6 <= i4; i6++) {
                for (int i7 = 0; i7 <= i2; i7++) {
                    BlockPos offset = blockPos.offset(i5, i7, i6);
                    Block block = levelAccessor.getBlockState(offset).getBlock();
                    if (isIcebergBlock(block) || block == Blocks.SNOW) {
                        if (belowIsAir(levelAccessor, offset)) {
                            setBlock(levelAccessor, offset, Blocks.AIR.defaultBlockState());
                            setBlock(levelAccessor, offset.above(), Blocks.AIR.defaultBlockState());
                        } else if (isIcebergBlock(block)) {
                            int i8 = 0;
                            for (Block block2 : new Block[]{levelAccessor.getBlockState(offset.west()).getBlock(), levelAccessor.getBlockState(offset.east()).getBlock(), levelAccessor.getBlockState(offset.north()).getBlock(), levelAccessor.getBlockState(offset.south()).getBlock()}) {
                                if (!isIcebergBlock(block2)) {
                                    i8++;
                                }
                            }
                            if (i8 >= 3) {
                                setBlock(levelAccessor, offset, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
    }
}
