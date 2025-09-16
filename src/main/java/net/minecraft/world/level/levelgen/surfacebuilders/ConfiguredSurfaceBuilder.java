package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/ConfiguredSurfaceBuilder.class */
public class ConfiguredSurfaceBuilder<SC extends SurfaceBuilderConfiguration> {
    public static final Codec<ConfiguredSurfaceBuilder<?>> DIRECT_CODEC = Registry.SURFACE_BUILDER.dispatch(configuredSurfaceBuilder -> {
        return configuredSurfaceBuilder.surfaceBuilder;
    }, (v0) -> {
        return v0.configuredCodec();
    });
    public static final Codec<Supplier<ConfiguredSurfaceBuilder<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, DIRECT_CODEC);
    public final SurfaceBuilder<SC> surfaceBuilder;
    public final SC config;

    public ConfiguredSurfaceBuilder(SurfaceBuilder<SC> surfaceBuilder, SC sc) {
        this.surfaceBuilder = surfaceBuilder;
        this.config = sc;
    }

    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j) {
        this.surfaceBuilder.apply(random, chunkAccess, biome, i, i2, i3, d, blockState, blockState2, i4, j, this.config);
    }

    public void initNoise(long j) {
        this.surfaceBuilder.initNoise(j);
    }

    public SC config() {
        return this.config;
    }
}
