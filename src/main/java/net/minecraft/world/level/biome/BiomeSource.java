package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeSource.class */
public abstract class BiomeSource implements BiomeManager.NoiseBiomeSource {
    public static final Codec<BiomeSource> CODEC;
    protected final Map<StructureFeature<?>, Boolean> supportedStructures;
    protected final Set<BlockState> surfaceBlocks;
    protected final List<Biome> possibleBiomes;

    protected abstract Codec<? extends BiomeSource> codec();

    public abstract BiomeSource withSeed(long j);

    static {
        Registry.register( Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register( Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register( Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register( Registry.BIOME_SOURCE, "vanilla_layered", OverworldBiomeSource.CODEC);
        Registry.register( Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
        CODEC = Registry.BIOME_SOURCE.dispatchStable((v0) -> {
            return v0.codec();
        }, Function.identity());
    }

    protected BiomeSource(Stream<Supplier<Biome>> stream) {
        this( stream.map((v0) -> {
            return v0.get();
        }).collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> list) {
        this.supportedStructures = Maps.newHashMap();
        this.surfaceBlocks = Sets.newHashSet();
        this.possibleBiomes = list;
    }

    public List<Biome> possibleBiomes() {
        return this.possibleBiomes;
    }

    public Set<Biome> getBiomesWithin(int i, int i2, int i3, int i4) {
        int i5 = (i - i4) >> 2;
        int i6 = (i2 - i4) >> 2;
        int i7 = (i3 - i4) >> 2;
        int i8 = (((i + i4) >> 2) - i5) + 1;
        int i9 = (((i2 + i4) >> 2) - i6) + 1;
        int i10 = (((i3 + i4) >> 2) - i7) + 1;
        Set<Biome> newHashSet = Sets.newHashSet();
        for (int i11 = 0; i11 < i10; i11++) {
            for (int i12 = 0; i12 < i8; i12++) {
                for (int i13 = 0; i13 < i9; i13++) {
                    newHashSet.add(getNoiseBiome(i5 + i12, i6 + i13, i7 + i11));
                }
            }
        }
        return newHashSet;
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int i2, int i3, int i4, Predicate<Biome> predicate, Random random) {
        return findBiomeHorizontal(i, i2, i3, i4, 1, predicate, random, false);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int i2, int i3, int i4, int i5, Predicate<Biome> predicate, Random random, boolean z) {
        int i6 = i >> 2;
        int i7 = i3 >> 2;
        int i8 = i4 >> 2;
        int i9 = i2 >> 2;
        BlockPos blockPos = null;
        int i10 = 0;
        int i11 = z ? 0 : i8;
        while (true) {
            int i12 = i11;
            if (i12 <= i8) {
                int i13 = -i12;
                while (true) {
                    int i14 = i13;
                    if (i14 <= i12) {
                        boolean z2 = Math.abs(i14) == i12;
                        int i15 = -i12;
                        while (true) {
                            int i16 = i15;
                            if (i16 <= i12) {
                                if (z) {
                                    if (!(Math.abs(i16) == i12) && !z2) {
                                        i15 = i16 + i5;
                                    }
                                }
                                int i17 = i6 + i16;
                                int i18 = i7 + i14;
                                if (predicate.test(getNoiseBiome(i17, i9, i18))) {
                                    if (blockPos == null || random.nextInt(i10 + 1) == 0) {
                                        blockPos = new BlockPos(i17 << 2, i2, i18 << 2);
                                        if (z) {
                                            return blockPos;
                                        }
                                    }
                                    i10++;
                                } else {
                                    continue;
                                }
                                i15 = i16 + i5;
                            }
                        }
                    }
                    i13 = i14 + i5;
                }
            } else {
                return blockPos;
            }
//            i11 = i12 + i5;
        }
    }

    public boolean canGenerateStructure(StructureFeature<?> structureFeature) {
        return this.supportedStructures.computeIfAbsent(structureFeature, structureFeature2 -> {
            return Boolean.valueOf(this.possibleBiomes.stream().anyMatch(biome -> {
                return biome.getGenerationSettings().isValidStart(structureFeature2);
            }));
        }).booleanValue();
    }

    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            Iterator<Biome> it = this.possibleBiomes.iterator();
            while (it.hasNext()) {
                this.surfaceBlocks.add(it.next().getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial());
            }
        }
        return this.surfaceBlocks;
    }
}
