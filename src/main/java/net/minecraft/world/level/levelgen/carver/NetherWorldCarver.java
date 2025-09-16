package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/NetherWorldCarver.class */
public class NetherWorldCarver extends CaveWorldCarver {
    public NetherWorldCarver(Codec<ProbabilityFeatureConfiguration> codec) {
        super(codec, 128);
        this.replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM, Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.BASALT, Blocks.BLACKSTONE});
        this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
    }

    @Override // net.minecraft.world.level.levelgen.carver.CaveWorldCarver
    protected int getCaveBound() {
        return 10;
    }

    @Override // net.minecraft.world.level.levelgen.carver.CaveWorldCarver
    protected float getThickness(Random random) {
        return ((random.nextFloat() * 2.0f) + random.nextFloat()) * 2.0f;
    }

    @Override // net.minecraft.world.level.levelgen.carver.CaveWorldCarver
    protected double getYScale() {
        return 5.0d;
    }

    @Override // net.minecraft.world.level.levelgen.carver.CaveWorldCarver
    protected int getCaveY(Random random) {
        return random.nextInt(this.genHeight);
    }

    @Override // net.minecraft.world.level.levelgen.carver.WorldCarver
    protected boolean carveBlock(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, BlockPos.MutableBlockPos mutableBlockPos3, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, MutableBoolean mutableBoolean) {
        BlockState blockState;
        int i9 = i6 | (i8 << 4) | (i7 << 8);
        if (bitSet.get(i9)) {
            return false;
        }
        bitSet.set(i9);
        mutableBlockPos.set(i4, i7, i5);
        if (canReplaceBlock(chunkAccess.getBlockState(mutableBlockPos))) {
            if (i7 <= 31) {
                blockState = LAVA.createLegacyBlock();
            } else {
                blockState = CAVE_AIR;
            }
            chunkAccess.setBlockState(mutableBlockPos, blockState, false);
            return true;
        }
        return false;
    }
}
