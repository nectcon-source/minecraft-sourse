package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/NoiseGeneratorSettings.class */
public final class NoiseGeneratorSettings {
    private final StructureSettings structureSettings;
    private final NoiseSettings noiseSettings;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final int bedrockRoofPosition;
    private final int bedrockFloorPosition;
    private final int seaLevel;
    private final boolean disableMobGeneration;
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(StructureSettings.CODEC.fieldOf("structures").forGetter((v0) -> {
            return v0.structureSettings();
        }), NoiseSettings.CODEC.fieldOf("noise").forGetter((v0) -> {
            return v0.noiseSettings();
        }), BlockState.CODEC.fieldOf("default_block").forGetter((v0) -> {
            return v0.getDefaultBlock();
        }), BlockState.CODEC.fieldOf("default_fluid").forGetter((v0) -> {
            return v0.getDefaultFluid();
        }), Codec.intRange(-20, 276).fieldOf("bedrock_roof_position").forGetter((v0) -> {
            return v0.getBedrockRoofPosition();
        }), Codec.intRange(-20, 276).fieldOf("bedrock_floor_position").forGetter((v0) -> {
            return v0.getBedrockFloorPosition();
        }), Codec.intRange(0, 255).fieldOf("sea_level").forGetter((v0) -> {
            return v0.seaLevel();
        }), Codec.BOOL.fieldOf("disable_mob_generation").forGetter((v0) -> {
            return v0.disableMobGeneration();
        })).apply(instance, (v1, v2, v3, v4, v5, v6, v7, v8) -> {
            return new NoiseGeneratorSettings(v1, v2, v3, v4, v5, v6, v7, v8);
        });
    });
    public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));
    private static final NoiseGeneratorSettings BUILTIN_OVERWORLD = register(OVERWORLD, overworld(new StructureSettings(true), false, OVERWORLD.location()));

    static {
        register(AMPLIFIED, overworld(new StructureSettings(true), true, AMPLIFIED.location()));
        register(NETHER, nether(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), NETHER.location()));
        register(END, end(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), END.location(), true, true));
        register(CAVES, nether(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), CAVES.location()));
        register(FLOATING_ISLANDS, end(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), FLOATING_ISLANDS.location(), false, false));
    }

    private NoiseGeneratorSettings(StructureSettings structureSettings, NoiseSettings noiseSettings, BlockState blockState, BlockState blockState2, int i, int i2, int i3, boolean z) {
        this.structureSettings = structureSettings;
        this.noiseSettings = noiseSettings;
        this.defaultBlock = blockState;
        this.defaultFluid = blockState2;
        this.bedrockRoofPosition = i;
        this.bedrockFloorPosition = i2;
        this.seaLevel = i3;
        this.disableMobGeneration = z;
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public NoiseSettings noiseSettings() {
        return this.noiseSettings;
    }

    public BlockState getDefaultBlock() {
        return this.defaultBlock;
    }

    public BlockState getDefaultFluid() {
        return this.defaultFluid;
    }

    public int getBedrockRoofPosition() {
        return this.bedrockRoofPosition;
    }

    public int getBedrockFloorPosition() {
        return this.bedrockFloorPosition;
    }

    public int seaLevel() {
        return this.seaLevel;
    }

    @Deprecated
    protected boolean disableMobGeneration() {
        return this.disableMobGeneration;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(resourceKey));
    }

    private static NoiseGeneratorSettings register(ResourceKey<NoiseGeneratorSettings> resourceKey, NoiseGeneratorSettings noiseGeneratorSettings) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, resourceKey.location(), noiseGeneratorSettings);
        return noiseGeneratorSettings;
    }

    public static NoiseGeneratorSettings bootstrap() {
        return BUILTIN_OVERWORLD;
    }

    private static NoiseGeneratorSettings end(StructureSettings structureSettings, BlockState blockState, BlockState blockState2, ResourceLocation resourceLocation, boolean z, boolean z2) {
        return new NoiseGeneratorSettings(structureSettings, new NoiseSettings(128, new NoiseSamplingSettings(2.0d, 1.0d, 80.0d, 160.0d), new NoiseSlideSettings(-3000, 64, -46), new NoiseSlideSettings(-30, 7, 1), 2, 1, 0.0d, 0.0d, true, false, z2, false), blockState, blockState2, -10, -10, 0, z);
    }

    private static NoiseGeneratorSettings nether(StructureSettings structureSettings, BlockState blockState, BlockState blockState2, ResourceLocation resourceLocation) {
        Map<StructureFeature<?>, StructureFeatureConfiguration> newHashMap = Maps.newHashMap(StructureSettings.DEFAULTS);
        newHashMap.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
        return new NoiseGeneratorSettings(new StructureSettings(Optional.ofNullable(structureSettings.stronghold()), newHashMap), new NoiseSettings(128, new NoiseSamplingSettings(1.0d, 3.0d, 80.0d, 60.0d), new NoiseSlideSettings(120, 3, 0), new NoiseSlideSettings(320, 4, -1), 1, 2, 0.0d, 0.019921875d, false, false, false, false), blockState, blockState2, 0, 0, 32, false);
    }

    private static NoiseGeneratorSettings overworld(StructureSettings structureSettings, boolean z, ResourceLocation resourceLocation) {
        return new NoiseGeneratorSettings(structureSettings, new NoiseSettings(256, new NoiseSamplingSettings(0.9999999814507745d, 0.9999999814507745d, 80.0d, 160.0d), new NoiseSlideSettings(-10, 3, 0), new NoiseSlideSettings(-30, 0, 0), 1, 2, 1.0d, -0.46875d, true, true, false, z), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), -10, 0, 63, false);
    }
}
