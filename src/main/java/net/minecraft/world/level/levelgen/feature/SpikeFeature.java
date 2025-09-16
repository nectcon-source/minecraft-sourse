package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SpikeFeature.class */
public class SpikeFeature extends Feature<SpikeConfiguration> {
    private static final LoadingCache<Long, List<EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new SpikeCacheLoader());

    public SpikeFeature(Codec<SpikeConfiguration> codec) {
        super(codec);
    }

    public static List<EndSpike> getSpikesForLevel(WorldGenLevel worldGenLevel) {
        return (List) SPIKE_CACHE.getUnchecked(Long.valueOf(new Random(worldGenLevel.getSeed()).nextLong() & 65535));
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SpikeConfiguration spikeConfiguration) {
        List<EndSpike> spikes = spikeConfiguration.getSpikes();
        if (spikes.isEmpty()) {
            spikes = getSpikesForLevel(worldGenLevel);
        }
        for (EndSpike endSpike : spikes) {
            if (endSpike.isCenterWithinChunk(blockPos)) {
                placeSpike(worldGenLevel, random, spikeConfiguration, endSpike);
            }
        }
        return true;
    }

    private void placeSpike(ServerLevelAccessor serverLevelAccessor, Random random, SpikeConfiguration spikeConfiguration, EndSpike endSpike) {
        int radius = endSpike.getRadius();
        for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(endSpike.getCenterX() - radius, 0, endSpike.getCenterZ() - radius), new BlockPos(endSpike.getCenterX() + radius, endSpike.getHeight() + 10, endSpike.getCenterZ() + radius))) {
            if (blockPos.distSqr(endSpike.getCenterX(), blockPos.getY(), endSpike.getCenterZ(), false) <= (radius * radius) + 1 && blockPos.getY() < endSpike.getHeight()) {
                setBlock(serverLevelAccessor, blockPos, Blocks.OBSIDIAN.defaultBlockState());
            } else if (blockPos.getY() > 65) {
                setBlock(serverLevelAccessor, blockPos, Blocks.AIR.defaultBlockState());
            }
        }
        if (endSpike.isGuarded()) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            int i = -2;
            while (i <= 2) {
                int i2 = -2;
                while (i2 <= 2) {
                    int i3 = 0;
                    while (i3 <= 3) {
                        boolean z = Mth.abs(i) == 2;
                        boolean z2 = Mth.abs(i2) == 2;
                        boolean z3 = i3 == 3;
                        if (z || z2 || z3) {
                            boolean z4 = i == -2 || i == 2 || z3;
                            boolean z5 = i2 == -2 || i2 == 2 || z3;
                            setBlock(serverLevelAccessor, mutableBlockPos.set(endSpike.getCenterX() + i, endSpike.getHeight() + i3, endSpike.getCenterZ() + i2), (BlockState) ((BlockState) ((BlockState) ((BlockState) Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(z4 && i2 != -2))).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(z4 && i2 != 2))).setValue(IronBarsBlock.WEST, Boolean.valueOf(z5 && i != -2))).setValue(IronBarsBlock.EAST, Boolean.valueOf(z5 && i != 2)));
                        }
                        i3++;
                    }
                    i2++;
                }
                i++;
            }
        }
        EndCrystal create = EntityType.END_CRYSTAL.create(serverLevelAccessor.getLevel());
        create.setBeamTarget(spikeConfiguration.getCrystalBeamTarget());
        create.setInvulnerable(spikeConfiguration.isCrystalInvulnerable());
        create.moveTo(endSpike.getCenterX() + 0.5d, endSpike.getHeight() + 1, endSpike.getCenterZ() + 0.5d, random.nextFloat() * 360.0f, 0.0f);
        serverLevelAccessor.addFreshEntity(create);
        setBlock(serverLevelAccessor, new BlockPos(endSpike.getCenterX(), endSpike.getHeight(), endSpike.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SpikeFeature$EndSpike.class */
    public static class EndSpike {
        public static final Codec<EndSpike> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(Codec.INT.fieldOf("centerX").orElse(0).forGetter(endSpike -> {
                return Integer.valueOf(endSpike.centerX);
            }), Codec.INT.fieldOf("centerZ").orElse(0).forGetter(endSpike2 -> {
                return Integer.valueOf(endSpike2.centerZ);
            }), Codec.INT.fieldOf("radius").orElse(0).forGetter(endSpike3 -> {
                return Integer.valueOf(endSpike3.radius);
            }), Codec.INT.fieldOf("height").orElse(0).forGetter(endSpike4 -> {
                return Integer.valueOf(endSpike4.height);
            }), Codec.BOOL.fieldOf("guarded").orElse(false).forGetter(endSpike5 -> {
                return Boolean.valueOf(endSpike5.guarded);
            })).apply(instance, (v1, v2, v3, v4, v5) -> {
                return new EndSpike(v1, v2, v3, v4, v5);
            });
        });
        private final int centerX;
        private final int centerZ;
        private final int radius;
        private final int height;
        private final boolean guarded;
        private final AABB topBoundingBox;

        public EndSpike(int i, int i2, int i3, int i4, boolean z) {
            this.centerX = i;
            this.centerZ = i2;
            this.radius = i3;
            this.height = i4;
            this.guarded = z;
            this.topBoundingBox = new AABB(i - i3, 0.0d, i2 - i3, i + i3, 256.0d, i2 + i3);
        }

        public boolean isCenterWithinChunk(BlockPos blockPos) {
            return (blockPos.getX() >> 4) == (this.centerX >> 4) && (blockPos.getZ() >> 4) == (this.centerZ >> 4);
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean isGuarded() {
            return this.guarded;
        }

        public AABB getTopBoundingBox() {
            return this.topBoundingBox;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/SpikeFeature$SpikeCacheLoader.class */
    static class SpikeCacheLoader extends CacheLoader<Long, List<EndSpike>> {
        private SpikeCacheLoader() {
        }

        public List<EndSpike> load(Long l) {
            List<Integer> list = (List) IntStream.range(0, 10).boxed().collect(Collectors.toList());
            Collections.shuffle(list, new Random(l.longValue()));
            List<EndSpike> newArrayList = Lists.newArrayList();
            for (int i = 0; i < 10; i++) {
                int floor = Mth.floor(42.0d * Math.cos(2.0d * ((-3.141592653589793d) + (0.3141592653589793d * i))));
                int floor2 = Mth.floor(42.0d * Math.sin(2.0d * ((-3.141592653589793d) + (0.3141592653589793d * i))));
                int intValue = list.get(i).intValue();
                newArrayList.add(new EndSpike(floor, floor2, 2 + (intValue / 3), 76 + (intValue * 3), intValue == 1 || intValue == 2));
            }
            return newArrayList;
        }
    }
}
