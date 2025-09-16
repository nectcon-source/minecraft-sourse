package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/flat/FlatLevelGeneratorSettings.class */
public class FlatLevelGeneratorSettings {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create(
                    var0 -> var0.group(
                                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(var0x -> var0x.biomes),
                                    StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
                                    FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
                                    Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(var0x -> var0x.addLakes),
                                    Codec.BOOL.fieldOf("features").orElse(false).forGetter(var0x -> var0x.decoration),
                                    Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(var0x -> Optional.of(var0x.biome))
                            )
                            .apply(var0, FlatLevelGeneratorSettings::new)
            )
            .stable();

    private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES =  Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(StructureFeature.MINESHAFT, StructureFeatures.MINESHAFT);
        hashMap.put(StructureFeature.VILLAGE, StructureFeatures.VILLAGE_PLAINS);
        hashMap.put(StructureFeature.STRONGHOLD, StructureFeatures.STRONGHOLD);
        hashMap.put(StructureFeature.SWAMP_HUT, StructureFeatures.SWAMP_HUT);
        hashMap.put(StructureFeature.DESERT_PYRAMID, StructureFeatures.DESERT_PYRAMID);
        hashMap.put(StructureFeature.JUNGLE_TEMPLE, StructureFeatures.JUNGLE_TEMPLE);
        hashMap.put(StructureFeature.IGLOO, StructureFeatures.IGLOO);
        hashMap.put(StructureFeature.OCEAN_RUIN, StructureFeatures.OCEAN_RUIN_COLD);
        hashMap.put(StructureFeature.SHIPWRECK, StructureFeatures.SHIPWRECK);
        hashMap.put(StructureFeature.OCEAN_MONUMENT, StructureFeatures.OCEAN_MONUMENT);
        hashMap.put(StructureFeature.END_CITY, StructureFeatures.END_CITY);
        hashMap.put(StructureFeature.WOODLAND_MANSION, StructureFeatures.WOODLAND_MANSION);
        hashMap.put(StructureFeature.NETHER_BRIDGE, StructureFeatures.NETHER_BRIDGE);
        hashMap.put(StructureFeature.PILLAGER_OUTPOST, StructureFeatures.PILLAGER_OUTPOST);
        hashMap.put(StructureFeature.RUINED_PORTAL, StructureFeatures.RUINED_PORTAL_STANDARD);
        hashMap.put(StructureFeature.BASTION_REMNANT, StructureFeatures.BASTION_REMNANT);
    });
    private final Registry<Biome> biomes;
    private final StructureSettings structureSettings;
    private final List<FlatLayerInfo> layersInfo;
    private Supplier<Biome> biome;
    private final BlockState[] layers;
    private boolean voidGen;
    private boolean decoration;
    private boolean addLakes;

    public FlatLevelGeneratorSettings(Registry<Biome> registry, StructureSettings structureSettings, List<FlatLayerInfo> list, boolean z, boolean z2, Optional<Supplier<Biome>> optional) {
        this(structureSettings, registry);
        if (z) {
            setAddLakes();
        }
        if (z2) {
            setDecoration();
        }
        this.layersInfo.addAll(list);
        updateLayers();
        if (!optional.isPresent()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            this.biome = () -> {
                return (Biome) registry.getOrThrow(Biomes.PLAINS);
            };
        } else {
            this.biome = optional.get();
        }
    }

    public FlatLevelGeneratorSettings(StructureSettings structureSettings, Registry<Biome> registry) {
        this.layersInfo = Lists.newArrayList();
        this.layers = new BlockState[256];
        this.decoration = false;
        this.addLakes = false;
        this.biomes = registry;
        this.structureSettings = structureSettings;
        this.biome = () -> {
            return (Biome) registry.getOrThrow(Biomes.PLAINS);
        };
    }

    public FlatLevelGeneratorSettings withStructureSettings(StructureSettings structureSettings) {
        return withLayers(this.layersInfo, structureSettings);
    }

    public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> list, StructureSettings structureSettings) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, this.biomes);
        for (FlatLayerInfo flatLayerInfo : list) {
            flatLevelGeneratorSettings.layersInfo.add(new FlatLayerInfo(flatLayerInfo.getHeight(), flatLayerInfo.getBlockState().getBlock()));
            flatLevelGeneratorSettings.updateLayers();
        }
        flatLevelGeneratorSettings.setBiome(this.biome);
        if (this.decoration) {
            flatLevelGeneratorSettings.setDecoration();
        }
        if (this.addLakes) {
            flatLevelGeneratorSettings.setAddLakes();
        }
        return flatLevelGeneratorSettings;
    }

    public void setDecoration() {
        this.decoration = true;
    }

    public void setAddLakes() {
        this.addLakes = true;
    }

    public Biome getBiomeFromSettings() {
        Biome biome = getBiome();
        BiomeGenerationSettings generationSettings = biome.getGenerationSettings();
        BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(generationSettings.getSurfaceBuilder());
        if (this.addLakes) {
            surfaceBuilder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
            surfaceBuilder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
        }
        Iterator<Map.Entry<StructureFeature<?>, StructureFeatureConfiguration>> it = this.structureSettings.structureConfig().entrySet().iterator();
        while (it.hasNext()) {
            surfaceBuilder.addStructureStart(generationSettings.withBiomeConfig(STRUCTURE_FEATURES.get(it.next().getKey())));
        }
        if ((!this.voidGen || this.biomes.getResourceKey(biome).equals(Optional.of(Biomes.THE_VOID))) && this.decoration) {
            List<List<Supplier<ConfiguredFeature<?, ?>>>> features = generationSettings.features();
            for (int i = 0; i < features.size(); i++) {
                if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
                    Iterator<Supplier<ConfiguredFeature<?, ?>>> it2 = features.get(i).iterator();
                    while (it2.hasNext()) {
                        surfaceBuilder.addFeature(i, it2.next());
                    }
                }
            }
        }
        BlockState[] layers = getLayers();
        for (int i2 = 0; i2 < layers.length; i2++) {
            BlockState blockState = layers[i2];
            if (blockState != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
                this.layers[i2] = null;
                surfaceBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(i2, blockState)));
            }
        }
        return new Biome.BiomeBuilder().precipitation(biome.getPrecipitation()).biomeCategory(biome.getBiomeCategory()).depth(biome.getDepth()).scale(biome.getScale()).temperature(biome.getBaseTemperature()).downfall(biome.getDownfall()).specialEffects(biome.getSpecialEffects()).generationSettings(surfaceBuilder.build()).mobSpawnSettings(biome.getMobSettings()).build();
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public Biome getBiome() {
        return this.biome.get();
    }

    public void setBiome(Supplier<Biome> supplier) {
        this.biome = supplier;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public BlockState[] getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        Arrays.fill(this.layers, 0, this.layers.length,  null);
        int i = 0;
        for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
            flatLayerInfo.setStart(i);
            i += flatLayerInfo.getHeight();
        }
        this.voidGen = true;
        for (FlatLayerInfo flatLayerInfo2 : this.layersInfo) {
            for (int start = flatLayerInfo2.getStart(); start < flatLayerInfo2.getStart() + flatLayerInfo2.getHeight(); start++) {
                BlockState blockState = flatLayerInfo2.getBlockState();
                if (!blockState.is(Blocks.AIR)) {
                    this.voidGen = false;
                    this.layers[start] = blockState;
                }
            }
        }
    }

    public static FlatLevelGeneratorSettings getDefault(Registry<Biome> registry) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(new StructureSettings(Optional.of(StructureSettings.DEFAULT_STRONGHOLD), Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE)))), registry);
        flatLevelGeneratorSettings.biome = () -> {
            return (Biome) registry.getOrThrow(Biomes.PLAINS);
        };
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        flatLevelGeneratorSettings.updateLayers();
        return flatLevelGeneratorSettings;
    }
}
