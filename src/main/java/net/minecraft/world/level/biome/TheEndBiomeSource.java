package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/TheEndBiomeSource.class */
public class TheEndBiomeSource extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(theEndBiomeSource -> {
            return theEndBiomeSource.biomes;
        }), Codec.LONG.fieldOf("seed").stable().forGetter(theEndBiomeSource2 -> {
            return Long.valueOf(theEndBiomeSource2.seed);
        })).apply(instance, instance.stable((v1, v2) -> {
            return new TheEndBiomeSource(v1, v2);
        }));
    });
    private final SimplexNoise islandNoise;
    private final Registry<Biome> biomes;
    private final long seed;
    private final Biome end;
    private final Biome highlands;
    private final Biome midlands;
    private final Biome islands;
    private final Biome barrens;

    public TheEndBiomeSource(Registry<Biome> registry, long j) {
        this(registry, j, registry.getOrThrow(Biomes.THE_END), registry.getOrThrow(Biomes.END_HIGHLANDS), registry.getOrThrow(Biomes.END_MIDLANDS), registry.getOrThrow(Biomes.SMALL_END_ISLANDS), registry.getOrThrow(Biomes.END_BARRENS));
    }

    private TheEndBiomeSource(Registry<Biome> registry, long j, Biome biome, Biome biome2, Biome biome3, Biome biome4, Biome biome5) {
        super((List<Biome>) ImmutableList.of(biome, biome2, biome3, biome4, biome5));
        this.biomes = registry;
        this.seed = j;
        this.end = biome;
        this.highlands = biome2;
        this.midlands = biome3;
        this.islands = biome4;
        this.barrens = biome5;
        WorldgenRandom worldgenRandom = new WorldgenRandom(j);
        worldgenRandom.consumeCount(17292);
        this.islandNoise = new SimplexNoise(worldgenRandom);
    }

    @Override // net.minecraft.world.level.biome.BiomeSource
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override // net.minecraft.world.level.biome.BiomeSource
    public BiomeSource withSeed(long j) {
        return new TheEndBiomeSource(this.biomes, j, this.end, this.highlands, this.midlands, this.islands, this.barrens);
    }

    @Override // net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource
    public Biome getNoiseBiome(int i, int i2, int i3) {
        int i4 = i >> 2;
        int i5 = i3 >> 2;
        if ((i4 * i4) + (i5 * i5) <= 4096) {
            return this.end;
        }
        float heightValue = getHeightValue(this.islandNoise, (i4 * 2) + 1, (i5 * 2) + 1);
        if (heightValue > 40.0f) {
            return this.highlands;
        }
        if (heightValue >= 0.0f) {
            return this.midlands;
        }
        if (heightValue < -20.0f) {
            return this.islands;
        }
        return this.barrens;
    }

    public boolean stable(long j) {
        return this.seed == j;
    }

    public static float getHeightValue(SimplexNoise simplexNoise, int i, int i2) {
        int i3 = i / 2;
        int i4 = i2 / 2;
        int i5 = i % 2;
        int i6 = i2 % 2;
        float clamp = Mth.clamp(100.0f - (Mth.sqrt((i * i) + (i2 * i2)) * 8.0f), -100.0f, 80.0f);
        for (int i7 = -12; i7 <= 12; i7++) {
            for (int i8 = -12; i8 <= 12; i8++) {
                long j = i3 + i7;
                long j2 = i4 + i8;
                if ((j * j) + (j2 * j2) > 4096 && simplexNoise.getValue(j, j2) < -0.8999999761581421d) {
                    float abs = (((Mth.abs(j) * 3439.0f) + (Mth.abs(j2) * 147.0f)) % 13.0f) + 9.0f;
                    float f = i5 - (i7 * 2);
                    float f2 = i6 - (i8 * 2);
                    clamp = Math.max(clamp, Mth.clamp(100.0f - (Mth.sqrt((f * f) + (f2 * f2)) * abs), -100.0f, 80.0f));
                }
            }
        }
        return clamp;
    }
}
