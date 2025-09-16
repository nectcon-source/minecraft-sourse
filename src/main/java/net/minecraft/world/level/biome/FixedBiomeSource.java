package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/FixedBiomeSource.class */
public class FixedBiomeSource extends BiomeSource {
    public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, fixedBiomeSource -> {
        return fixedBiomeSource.biome;
    }).stable().codec();
    private final Supplier<Biome> biome;

    public FixedBiomeSource(Biome biome) {
        this((Supplier<Biome>) () -> {
            return biome;
        });
    }

    public FixedBiomeSource(Supplier<Biome> supplier) {
        super((List<Biome>) ImmutableList.of(supplier.get()));
        this.biome = supplier;
    }

    @Override // net.minecraft.world.level.biome.BiomeSource
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override // net.minecraft.world.level.biome.BiomeSource
    public BiomeSource withSeed(long j) {
        return this;
    }

    @Override // net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource
    public Biome getNoiseBiome(int i, int i2, int i3) {
        return this.biome.get();
    }

    @Override // net.minecraft.world.level.biome.BiomeSource
    @Nullable
    public BlockPos findBiomeHorizontal(int i, int i2, int i3, int i4, int i5, Predicate<Biome> predicate, Random random, boolean z) {
        if (predicate.test(this.biome.get())) {
            if (z) {
                return new BlockPos(i, i2, i3);
            }
            return new BlockPos((i - i4) + random.nextInt((i4 * 2) + 1), i2, (i3 - i4) + random.nextInt((i4 * 2) + 1));
        }
        return null;
    }

    @Override // net.minecraft.world.level.biome.BiomeSource
    public Set<Biome> getBiomesWithin(int i, int i2, int i3, int i4) {
        return Sets.newHashSet(new Biome[]{this.biome.get()});
    }
}
