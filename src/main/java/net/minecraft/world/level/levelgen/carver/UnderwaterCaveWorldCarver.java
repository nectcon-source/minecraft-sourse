package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import org.apache.commons.lang3.mutable.MutableBoolean;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/UnderwaterCaveWorldCarver.class */
public class UnderwaterCaveWorldCarver extends CaveWorldCarver {
    public UnderwaterCaveWorldCarver(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec, 256);
        this.replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.SAND, Blocks.GRAVEL, Blocks.WATER, Blocks.LAVA, Blocks.OBSIDIAN, Blocks.AIR, Blocks.CAVE_AIR, Blocks.PACKED_ICE});
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    protected boolean hasWater(ChunkAccess chunkAccess, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        return false;
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    protected boolean carveBlock(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, BlockPos.MutableBlockPos mutableBlockPos3, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, MutableBoolean mutableBoolean) {
        return carveBlock(this, chunkAccess, bitSet, random, mutableBlockPos, i, i2, i3, i4, i5, i6, i7, i8);
    }

    protected static boolean carveBlock(WorldCarver<?> worldCarver, ChunkAccess chunkAccess, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i7 >= i) {
            return false;
        }
        int i9 = i6 | (i8 << 4) | (i7 << 8);
        if (bitSet.get(i9)) {
            return false;
        }
        bitSet.set(i9);
        mutableBlockPos.set(i4, i7, i5);
        if (!worldCarver.canReplaceBlock(chunkAccess.getBlockState(mutableBlockPos))) {
            return false;
        }
        if (i7 == 10) {
            if (random.nextFloat() < 0.25d) {
                chunkAccess.setBlockState(mutableBlockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
                chunkAccess.getBlockTicks().scheduleTick(mutableBlockPos, Blocks.MAGMA_BLOCK, 0);
                return true;
            }
            chunkAccess.setBlockState(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), false);
            return true;
        }
        if (i7 < 10) {
            chunkAccess.setBlockState(mutableBlockPos, Blocks.LAVA.defaultBlockState(), false);
            return false;
        }
        boolean z = false;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            int stepX = i4 + next.getStepX();
            int stepZ = i5 + next.getStepZ();
            if ((stepX >> 4) != i2 || (stepZ >> 4) != i3 || chunkAccess.getBlockState(mutableBlockPos.set(stepX, i7, stepZ)).isAir()) {
                chunkAccess.setBlockState(mutableBlockPos, WATER.createLegacyBlock(), false);
                chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, WATER.getType(), 0);
                z = true;
                break;
            }
        }
        mutableBlockPos.set(i4, i7, i5);
        if (!z) {
            chunkAccess.setBlockState(mutableBlockPos, WATER.createLegacyBlock(), false);
            return true;
        }
        return true;
    }
}
