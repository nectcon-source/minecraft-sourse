package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/NetherCappedSurfaceBuilder.class */
public abstract class NetherCappedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private long seed;
    private ImmutableMap<BlockState, PerlinNoise> floorNoises;
    private ImmutableMap<BlockState, PerlinNoise> ceilingNoises;
    private PerlinNoise patchNoise;

    protected abstract ImmutableList<BlockState> getFloorBlockStates();

    protected abstract ImmutableList<BlockState> getCeilingBlockStates();

    protected abstract BlockState getPatchBlockState();

    public NetherCappedSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
        this.floorNoises = ImmutableMap.of();
        this.ceilingNoises = ImmutableMap.of();
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int i5 = i4 + 1;
        int i6 = i & 15;
        int i7 = i2 & 15;
        int nextDouble = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        int nextDouble2 = (int) ((d / 3.0d) + 3.0d + (random.nextDouble() * 0.25d));
        boolean z = (this.patchNoise.getValue(((double) i) * 0.03125d, 109.0d, ((double) i2) * 0.03125d) * 75.0d) + random.nextDouble() > 0.0d;
        BlockState blockState3 = (BlockState) ((Map.Entry) this.ceilingNoises.entrySet().stream().max(Comparator.comparing(entry -> {
            return Double.valueOf(((PerlinNoise) entry.getValue()).getValue(i, i4, i2));
        })).get()).getKey();
        BlockState blockState4 = (BlockState) ((Map.Entry) this.floorNoises.entrySet().stream().max(Comparator.comparing(entry2 -> {
            return Double.valueOf(((PerlinNoise) entry2.getValue()).getValue(i, i4, i2));
        })).get()).getKey();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos.set(i6, 128, i7));
        for (int i8 = 127; i8 >= 0; i8--) {
            mutableBlockPos.set(i6, i8, i7);
            BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState5.is(blockState.getBlock()) && (blockState6.isAir() || blockState6 == blockState2)) {
                for (int i9 = 0; i9 < nextDouble; i9++) {
                    mutableBlockPos.move(Direction.UP);
                    if (!chunkAccess.getBlockState(mutableBlockPos).is(blockState.getBlock())) {
                        break;
                    }
                    chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                }
                mutableBlockPos.set(i6, i8, i7);
            }
            if ((blockState5.isAir() || blockState5 == blockState2) && blockState6.is(blockState.getBlock())) {
                for (int i10 = 0; i10 < nextDouble2 && chunkAccess.getBlockState(mutableBlockPos).is(blockState.getBlock()); i10++) {
                    if (z && i8 >= i5 - 4 && i8 <= i5 + 1) {
                        chunkAccess.setBlockState(mutableBlockPos, getPatchBlockState(), false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
                    }
                    mutableBlockPos.move(Direction.DOWN);
                }
            }
            blockState5 = blockState6;
        }
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder
    public void initNoise(long j) {
        if (this.seed != j || this.patchNoise == null || this.floorNoises.isEmpty() || this.ceilingNoises.isEmpty()) {
            this.floorNoises = initPerlinNoises(getFloorBlockStates(), j);
            this.ceilingNoises = initPerlinNoises(getCeilingBlockStates(), j + this.floorNoises.size());
            this.patchNoise = new PerlinNoise(new WorldgenRandom(j + this.floorNoises.size() + this.ceilingNoises.size()), (List<Integer>) ImmutableList.of(0));
        }
        this.seed = j;
    }

    private static ImmutableMap<BlockState, PerlinNoise> initPerlinNoises(ImmutableList<BlockState> immutableList, long j) {
        ImmutableMap.Builder<BlockState, PerlinNoise> builder = new ImmutableMap.Builder<>();
        UnmodifiableIterator it = immutableList.iterator();
        while (it.hasNext()) {
            builder.put((BlockState) it.next(), new PerlinNoise(new WorldgenRandom(j), (List<Integer>) ImmutableList.of(-4)));
            j++;
        }
        return builder.build();
    }
}
