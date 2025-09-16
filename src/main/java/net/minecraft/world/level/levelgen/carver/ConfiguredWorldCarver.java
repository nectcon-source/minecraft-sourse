package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver.class */
public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
    public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER.dispatch(configuredWorldCarver -> {
        return configuredWorldCarver.worldCarver;
    }, (v0) -> {
        return v0.configuredCodec();
    });
    public static final Codec<Supplier<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<ConfiguredWorldCarver<?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
    private final WorldCarver<WC> worldCarver;
    private final WC config;

    public ConfiguredWorldCarver(WorldCarver<WC> worldCarver, WC wc) {
        this.worldCarver = worldCarver;
        this.config = wc;
    }

    public WC config() {
        return this.config;
    }

    public boolean isStartChunk(Random random, int i, int i2) {
        return this.worldCarver.isStartChunk(random, i, i2, this.config);
    }

    public boolean carve(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int i2, int i3, int i4, int i5, BitSet bitSet) {
        return this.worldCarver.carve(chunkAccess, function, random, i, i2, i3, i4, i5, bitSet, this.config);
    }
}
