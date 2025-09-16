package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/CheckerboardColumnBiomeSource.class */
public class CheckerboardColumnBiomeSource extends BiomeSource {
    public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter(checkerboardColumnBiomeSource -> {
            return checkerboardColumnBiomeSource.allowedBiomes;
        }), Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter(checkerboardColumnBiomeSource2 -> {
            return Integer.valueOf(checkerboardColumnBiomeSource2.size);
        })).apply(instance, (v1, v2) -> {
            return new CheckerboardColumnBiomeSource(v1, v2);
        });
    });
    private final List<Supplier<Biome>> allowedBiomes;
    private final int bitShift;
    private final int size;

    public CheckerboardColumnBiomeSource(List<Supplier<Biome>> list, int i) {
        super(list.stream());
        this.allowedBiomes = list;
        this.bitShift = i + 2;
        this.size = i;
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
        return this.allowedBiomes.get(Math.floorMod((i >> this.bitShift) + (i3 >> this.bitShift), this.allowedBiomes.size())).get();
    }
}
