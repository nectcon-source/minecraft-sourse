//package net.minecraft.world.level.biome;
//
//import com.google.common.collect.ImmutableList;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.Lifecycle;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import java.util.List;
//import java.util.function.Supplier;
//import java.util.stream.Stream;
//import net.minecraft.core.Registry;
//import net.minecraft.resources.RegistryLookupCodec;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.world.level.newbiome.layer.Layer;
//import net.minecraft.world.level.newbiome.layer.Layers;
//
///* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/OverworldBiomeSource.class */
//public class OverworldBiomeSource extends BiomeSource {
//    private final Layer noiseBiomeLayer;
//    private final long seed;
//    private final boolean legacyBiomeInitLayer;
//    private final boolean largeBiomes;
//    private final Registry<Biome> biomes;
//    public static final Codec<OverworldBiomeSource> CODEC = RecordCodecBuilder.create(instance -> {
//        return instance.group(Codec.LONG.fieldOf("seed").stable().forGetter(overworldBiomeSource -> {
//            return Long.valueOf(overworldBiomeSource.seed);
//        }), Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", false, Lifecycle.stable()).forGetter(overworldBiomeSource2 -> {
//            return Boolean.valueOf(overworldBiomeSource2.legacyBiomeInitLayer);
//        }), Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter(overworldBiomeSource3 -> {
//            return Boolean.valueOf(overworldBiomeSource3.largeBiomes);
//        }), RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(overworldBiomeSource4 -> {
//            return overworldBiomeSource4.biomes;
//        })).apply(instance, instance.stable((v1, v2, v3, v4) -> {
//            return new OverworldBiomeSource(v1, v2, v3, v4);
//        }));
//    });
//    private static final List<ResourceKey<Biome>> POSSIBLE_BIOMES = ImmutableList.<ResourceKey<Biome>>of(Biomes.OCEAN, Biomes.PLAINS, Biomes.DESERT, Biomes.MOUNTAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMP, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_MOUNTAINS, new ResourceKey[]{Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE, Biomes.BEACH, Biomes.DESERT_HILLS, Biomes.WOODED_HILLS, Biomes.TAIGA_HILLS, Biomes.MOUNTAIN_EDGE, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.DEEP_OCEAN, Biomes.STONE_SHORE, Biomes.SNOWY_BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA_HILLS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.WOODED_MOUNTAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.BADLANDS, Biomes.WOODED_BADLANDS_PLATEAU, Biomes.BADLANDS_PLATEAU, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.SUNFLOWER_PLAINS, Biomes.DESERT_LAKES, Biomes.GRAVELLY_MOUNTAINS, Biomes.FLOWER_FOREST, Biomes.TAIGA_MOUNTAINS, Biomes.SWAMP_HILLS, Biomes.ICE_SPIKES, Biomes.MODIFIED_JUNGLE, Biomes.MODIFIED_JUNGLE_EDGE, Biomes.TALL_BIRCH_FOREST, Biomes.TALL_BIRCH_HILLS, Biomes.DARK_FOREST_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS, Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA_HILLS, Biomes.MODIFIED_GRAVELLY_MOUNTAINS, Biomes.SHATTERED_SAVANNA, Biomes.SHATTERED_SAVANNA_PLATEAU, Biomes.ERODED_BADLANDS, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, Biomes.MODIFIED_BADLANDS_PLATEAU});
//
//    public OverworldBiomeSource(long j, boolean z, boolean z2, Registry<Biome> registry) {
//        super(POSSIBLE_BIOMES.stream().map((var1x) -> () -> registry.getOrThrow(var1x)));
//        this.seed = j;
//        this.legacyBiomeInitLayer = z;
//        this.largeBiomes = z2;
//        this.biomes = registry;
//        this.noiseBiomeLayer = Layers.getDefaultLayer(j, z, z2 ? 6 : 4, 4);
//    }
//
//    @Override // net.minecraft.world.level.biome.BiomeSource
//    protected Codec<? extends BiomeSource> codec() {
//        return CODEC;
//    }
//
//    @Override // net.minecraft.world.level.biome.BiomeSource
//    public BiomeSource withSeed(long j) {
//        return new OverworldBiomeSource(j, this.legacyBiomeInitLayer, this.largeBiomes, this.biomes);
//    }
//
//    @Override // net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource
//    public Biome getNoiseBiome(int i, int i2, int i3) {
//        return this.noiseBiomeLayer.get(this.biomes, i, i3);
//    }
//}
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;

public class OverworldBiomeSource extends BiomeSource {
    private final Layer noiseBiomeLayer;
    private final long seed;
    private final boolean legacyBiomeInitLayer;
    private final boolean largeBiomes;
    private final Registry<Biome> biomes;

    public static final Codec<OverworldBiomeSource> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter(src -> src.seed),
                    Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", false, Lifecycle.stable()).forGetter(src -> src.legacyBiomeInitLayer),
                    Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter(src -> src.largeBiomes),
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(src -> src.biomes)
            ).apply(instance, instance.stable(OverworldBiomeSource::new))
    );

    private static final List<ResourceKey<Biome>> POSSIBLE_BIOMES =
            ImmutableList.<ResourceKey<Biome>>builder()
                    .add(Biomes.OCEAN)
                    .add(Biomes.PLAINS)
                    .add(Biomes.DESERT)
                    .add(Biomes.MOUNTAINS)
                    .add(Biomes.FOREST)
                    .add(Biomes.TAIGA)
                    .add(Biomes.SWAMP)
                    .add(Biomes.RIVER)
                    .add(Biomes.FROZEN_OCEAN)
                    .add(Biomes.FROZEN_RIVER)
                    .add(Biomes.SNOWY_TUNDRA)
                    .add(Biomes.SNOWY_MOUNTAINS)
                    .add(Biomes.MUSHROOM_FIELDS)
                    .add(Biomes.MUSHROOM_FIELD_SHORE)
                    .add(Biomes.BEACH)
                    .add(Biomes.DESERT_HILLS)
                    .add(Biomes.WOODED_HILLS)
                    .add(Biomes.TAIGA_HILLS)
                    .add(Biomes.MOUNTAIN_EDGE)
                    .add(Biomes.JUNGLE)
                    .add(Biomes.JUNGLE_HILLS)
                    .add(Biomes.JUNGLE_EDGE)
                    .add(Biomes.DEEP_OCEAN)
                    .add(Biomes.STONE_SHORE)
                    .add(Biomes.SNOWY_BEACH)
                    .add(Biomes.BIRCH_FOREST)
                    .add(Biomes.BIRCH_FOREST_HILLS)
                    .add(Biomes.DARK_FOREST)
                    .add(Biomes.SNOWY_TAIGA)
                    .add(Biomes.SNOWY_TAIGA_HILLS)
                    .add(Biomes.GIANT_TREE_TAIGA)
                    .add(Biomes.GIANT_TREE_TAIGA_HILLS)
                    .add(Biomes.WOODED_MOUNTAINS)
                    .add(Biomes.SAVANNA)
                    .add(Biomes.SAVANNA_PLATEAU)
                    .add(Biomes.BADLANDS)
                    .add(Biomes.WOODED_BADLANDS_PLATEAU)
                    .add(Biomes.BADLANDS_PLATEAU)
                    .add(Biomes.WARM_OCEAN)
                    .add(Biomes.LUKEWARM_OCEAN)
                    .add(Biomes.COLD_OCEAN)
                    .add(Biomes.DEEP_WARM_OCEAN)
                    .add(Biomes.DEEP_LUKEWARM_OCEAN)
                    .add(Biomes.DEEP_COLD_OCEAN)
                    .add(Biomes.DEEP_FROZEN_OCEAN)
                    .add(Biomes.SUNFLOWER_PLAINS)
                    .add(Biomes.DESERT_LAKES)
                    .add(Biomes.GRAVELLY_MOUNTAINS)
                    .add(Biomes.FLOWER_FOREST)
                    .add(Biomes.TAIGA_MOUNTAINS)
                    .add(Biomes.SWAMP_HILLS)
                    .add(Biomes.ICE_SPIKES)
                    .add(Biomes.MODIFIED_JUNGLE)
                    .add(Biomes.MODIFIED_JUNGLE_EDGE)
                    .add(Biomes.TALL_BIRCH_FOREST)
                    .add(Biomes.TALL_BIRCH_HILLS)
                    .add(Biomes.DARK_FOREST_HILLS)
                    .add(Biomes.SNOWY_TAIGA_MOUNTAINS)
                    .add(Biomes.GIANT_SPRUCE_TAIGA)
                    .add(Biomes.GIANT_SPRUCE_TAIGA_HILLS)
                    .add(Biomes.MODIFIED_GRAVELLY_MOUNTAINS)
                    .add(Biomes.SHATTERED_SAVANNA)
                    .add(Biomes.SHATTERED_SAVANNA_PLATEAU)
                    .add(Biomes.ERODED_BADLANDS)
                    .add(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU)
                    .add(Biomes.MODIFIED_BADLANDS_PLATEAU)
                    .build();

    public OverworldBiomeSource(long seed, boolean legacy, boolean large, Registry<Biome> registry) {
        super(POSSIBLE_BIOMES.stream().<Supplier<Biome>>map(key -> () -> registry.getOrThrow(key)));
        this.seed = seed;
        this.legacyBiomeInitLayer = legacy;
        this.largeBiomes = large;
        this.biomes = registry;
        this.noiseBiomeLayer = Layers.getDefaultLayer(seed, legacy, large ? 6 : 4, 4);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long newSeed) {
        return new OverworldBiomeSource(newSeed, this.legacyBiomeInitLayer, this.largeBiomes, this.biomes);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return this.noiseBiomeLayer.get(this.biomes, x, z);
    }
}
