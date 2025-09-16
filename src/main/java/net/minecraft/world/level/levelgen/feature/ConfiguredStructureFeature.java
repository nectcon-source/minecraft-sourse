package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/ConfiguredStructureFeature.class */
public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
    public static final Codec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE.dispatch(configuredStructureFeature -> {
        return configuredStructureFeature.feature;
    }, (v0) -> {
        return v0.configuredStructureCodec();
    });
    public static final Codec<Supplier<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<ConfiguredStructureFeature<?, ?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
    public final F feature;
    public final FC config;

    public ConfiguredStructureFeature(F f, FC fc) {
        this.feature = f;
        this.config = fc;
    }

    public StructureStart<?> generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long j, ChunkPos chunkPos, Biome biome, int i, StructureFeatureConfiguration structureFeatureConfiguration) {
        return this.feature.generate(registryAccess, chunkGenerator, biomeSource, structureManager, j, chunkPos, biome, i, new WorldgenRandom(), structureFeatureConfiguration, this.config);
    }
}
