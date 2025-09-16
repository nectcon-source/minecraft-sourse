package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/WorldCarver.class */
public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<ProbabilityFeatureConfiguration> CAVE = register("cave", new CaveWorldCarver(ProbabilityFeatureConfiguration.CODEC, 256));
    public static final WorldCarver<ProbabilityFeatureConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(ProbabilityFeatureConfiguration.CODEC));
    public static final WorldCarver<ProbabilityFeatureConfiguration> CANYON = register("canyon", new CanyonWorldCarver(ProbabilityFeatureConfiguration.CODEC));
    public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CANYON = register("underwater_canyon", new UnderwaterCanyonWorldCarver(ProbabilityFeatureConfiguration.CODEC));
    public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CAVE = register("underwater_cave", new UnderwaterCaveWorldCarver(ProbabilityFeatureConfiguration.CODEC));
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Block> replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE});
    protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
    private final Codec<ConfiguredWorldCarver<C>> configuredCodec;
    protected final int genHeight;

    public abstract boolean carve(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int i2, int i3, int i4, int i5, BitSet bitSet, C c);

    public abstract boolean isStartChunk(Random random, int i, int i2, C c);

    protected abstract boolean skip(double d, double d2, double d3, int i);

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String str, F f) {
        return (F) Registry.register(Registry.CARVER, str, f);
    }

    public WorldCarver(Codec<C> codec, int i) {
        this.genHeight = i;
        this.configuredCodec = codec.fieldOf("config").xmap(this::configured, (v0) -> {
            return v0.config();
        }).codec();
    }

    public ConfiguredWorldCarver<C> configured(C c) {
        return new ConfiguredWorldCarver<>(this, c);
    }

    public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
        return this.configuredCodec;
    }

    public int getRange() {
        return 4;
    }

    protected boolean carveSphere(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long j, int i, int i2, int i3, double d, double d2, double d3, double d4, double d5, BitSet bitSet) {
        Random random = new Random(j + i2 + i3);
        double d6 = (i2 * 16) + 8;
        double d7 = (i3 * 16) + 8;
        if (d < (d6 - 16.0d) - (d4 * 2.0d) || d3 < (d7 - 16.0d) - (d4 * 2.0d) || d > d6 + 16.0d + (d4 * 2.0d) || d3 > d7 + 16.0d + (d4 * 2.0d)) {
            return false;
        }
        int max = Math.max((Mth.floor(d - d4) - (i2 * 16)) - 1, 0);
        int min = Math.min((Mth.floor(d + d4) - (i2 * 16)) + 1, 16);
        int max2 = Math.max(Mth.floor(d2 - d5) - 1, 1);
        int min2 = Math.min(Mth.floor(d2 + d5) + 1, this.genHeight - 8);
        int max3 = Math.max((Mth.floor(d3 - d4) - (i3 * 16)) - 1, 0);
        int min3 = Math.min((Mth.floor(d3 + d4) - (i3 * 16)) + 1, 16);
        if (hasWater(chunkAccess, i2, i3, max, min, max2, min2, max3, min3)) {
            return false;
        }
        boolean z = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos3 = new BlockPos.MutableBlockPos();
        for (int i4 = max; i4 < min; i4++) {
            int i5 = i4 + (i2 * 16);
            double d8 = ((i5 + 0.5d) - d) / d4;
            for (int i6 = max3; i6 < min3; i6++) {
                int i7 = i6 + (i3 * 16);
                double d9 = ((i7 + 0.5d) - d3) / d4;
                if ((d8 * d8) + (d9 * d9) < 1.0d) {
                    MutableBoolean mutableBoolean = new MutableBoolean(false);
                    for (int i8 = min2; i8 > max2; i8--) {
                        if (!skip(d8, ((i8 - 0.5d) - d2) / d5, d9, i8)) {
                            z |= carveBlock(chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, mutableBlockPos3, i, i2, i3, i5, i7, i4, i8, i6, mutableBoolean);
                        }
                    }
                }
            }
        }
        return z;
    }

    protected boolean carveBlock(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, BlockPos.MutableBlockPos mutableBlockPos3, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, MutableBoolean mutableBoolean) {
        int i9 = i6 | (i8 << 4) | (i7 << 8);
        if (bitSet.get(i9)) {
            return false;
        }
        bitSet.set(i9);
        mutableBlockPos.set(i4, i7, i5);
        BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
        BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.UP));
        if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
            mutableBoolean.setTrue();
        }
        if (!canReplaceBlock(blockState, blockState2)) {
            return false;
        }
        if (i7 < 11) {
            chunkAccess.setBlockState(mutableBlockPos, LAVA.createLegacyBlock(), false);
            return true;
        }
        chunkAccess.setBlockState(mutableBlockPos, CAVE_AIR, false);
        if (mutableBoolean.isTrue()) {
            mutableBlockPos3.setWithOffset(mutableBlockPos, Direction.DOWN);
            if (chunkAccess.getBlockState(mutableBlockPos3).is(Blocks.DIRT)) {
                chunkAccess.setBlockState(mutableBlockPos3, function.apply(mutableBlockPos).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false);
                return true;
            }
            return true;
        }
        return true;
    }

    protected boolean canReplaceBlock(BlockState blockState) {
        return this.replaceableBlocks.contains(blockState.getBlock());
    }

    protected boolean canReplaceBlock(BlockState blockState, BlockState blockState2) {
        return canReplaceBlock(blockState) || ((blockState.is(Blocks.SAND) || blockState.is(Blocks.GRAVEL)) && !blockState2.getFluidState().is(FluidTags.WATER));
    }

    protected boolean hasWater(ChunkAccess chunkAccess, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i9 = i3; i9 < i4; i9++) {
            for (int i10 = i7; i10 < i8; i10++) {
                int i11 = i5 - 1;
                while (i11 <= i6 + 1) {
                    if (this.liquids.contains(chunkAccess.getFluidState(mutableBlockPos.set(i9 + (i * 16), i11, i10 + (i2 * 16))).getType())) {
                        return true;
                    }
                    if (i11 != i6 + 1 && !isEdge(i3, i4, i7, i8, i9, i10)) {
                        i11 = i6;
                    }
                    i11++;
                }
            }
        }
        return false;
    }

    private boolean isEdge(int i, int i2, int i3, int i4, int i5, int i6) {
        return i5 == i || i5 == i2 - 1 || i6 == i3 || i6 == i4 - 1;
    }

    protected boolean canReach(int i, int i2, double d, double d2, int i3, int i4, float f) {
        double d3 = d - ((i * 16) + 8);
        double d4 = d2 - ((i2 * 16) + 8);
        double d5 = i4 - i3;
        double d6 = f + 2.0f + 16.0f;
        return ((d3 * d3) + (d4 * d4)) - (d5 * d5) <= d6 * d6;
    }
}
