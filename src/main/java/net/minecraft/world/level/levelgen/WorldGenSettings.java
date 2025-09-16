//package net.minecraft.world.level.levelgen;
//
//import com.google.common.base.MoreObjects;
//import com.google.common.collect.ImmutableSet;
//import com.google.gson.JsonObject;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.DataResult;
//import com.mojang.serialization.Dynamic;
//import com.mojang.serialization.JsonOps;
//import com.mojang.serialization.Lifecycle;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.OptionalLong;
//import java.util.Properties;
//import java.util.Random;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import net.minecraft.core.MappedRegistry;
//import net.minecraft.core.Registry;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.biome.OverworldBiomeSource;
//import net.minecraft.world.level.chunk.ChunkGenerator;
//import net.minecraft.world.level.dimension.DimensionType;
//import net.minecraft.world.level.dimension.LevelStem;
//import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/WorldGenSettings.class */
//public class WorldGenSettings {
//    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.<WorldGenSettings>create(
//            (var0) -> var0.group(Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
//                    Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateFeatures),
//                    Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest),
//                    MappedRegistry.<LevelStem>dataPackCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(),
//                            LevelStem.CODEC).xmap(LevelStem::sortMap, Function.identity()).fieldOf("dimensions").forGetter(WorldGenSettings::dimensions),
//                    Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter((var0x) -> var0x.legacyCustomOptions)).apply(var0, var0.stable(WorldGenSettings::new))).comapFlatMap(WorldGenSettings::guardExperimental,
//            Function.identity());
//
//    private static final Logger LOGGER = LogManager.getLogger();
//    private final long seed;
//    private final boolean generateFeatures;
//    private final boolean generateBonusChest;
//    private final MappedRegistry<LevelStem> dimensions;
//    private final Optional<String> legacyCustomOptions;
//
//    private DataResult<WorldGenSettings> guardExperimental() {
//        if (this.dimensions.get(LevelStem.OVERWORLD) == null) {
//            return DataResult.error("Overworld settings missing");
//        }
//        if (stable()) {
//            return DataResult.success(this, Lifecycle.stable());
//        }
//        return DataResult.success(this);
//    }
//
//    private boolean stable() {
//        return LevelStem.stable(this.seed, this.dimensions);
//    }
//
//    public WorldGenSettings(long j, boolean z, boolean z2, MappedRegistry<LevelStem> mappedRegistry) {
//        this(j, z, z2, mappedRegistry, Optional.empty());
//        if (mappedRegistry.get(LevelStem.OVERWORLD) == null) {
//            throw new IllegalStateException("Overworld settings missing");
//        }
//    }
//
//    private WorldGenSettings(long j, boolean z, boolean z2, MappedRegistry<LevelStem> mappedRegistry, Optional<String> optional) {
//        this.seed = j;
//        this.generateFeatures = z;
//        this.generateBonusChest = z2;
//        this.dimensions = mappedRegistry;
//        this.legacyCustomOptions = optional;
//    }
//
//    public static WorldGenSettings demoSettings(RegistryAccess registryAccess) {
//        Registry<Biome> registryOrThrow = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
//        int hashCode = "North Carolina".hashCode();
//        Registry<DimensionType> registryOrThrow2 = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
//        Registry<NoiseGeneratorSettings> registryOrThrow3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
//        return new WorldGenSettings(hashCode, true, true, withOverworld(registryOrThrow2, DimensionType.defaultDimensions(registryOrThrow2, registryOrThrow, registryOrThrow3, hashCode), makeDefaultOverworld(registryOrThrow, registryOrThrow3, hashCode)));
//    }
//
//    public static WorldGenSettings makeDefault(Registry<DimensionType> registry, Registry<Biome> registry2, Registry<NoiseGeneratorSettings> registry3) {
//        long nextLong = new Random().nextLong();
//        return new WorldGenSettings(nextLong, true, false, withOverworld(registry, DimensionType.defaultDimensions(registry, registry2, registry3, nextLong), makeDefaultOverworld(registry2, registry3, nextLong)));
//    }
//
//    public static NoiseBasedChunkGenerator makeDefaultOverworld(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long j) {
//        return new NoiseBasedChunkGenerator(new OverworldBiomeSource(j, false, false, registry), j, () -> {
//            return  registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
//        });
//    }
//
//    public long seed() {
//        return this.seed;
//    }
//
//    public boolean generateFeatures() {
//        return this.generateFeatures;
//    }
//
//    public boolean generateBonusChest() {
//        return this.generateBonusChest;
//    }
//
//    public static MappedRegistry<LevelStem> withOverworld(Registry<DimensionType> registry, MappedRegistry<LevelStem> mappedRegistry, ChunkGenerator chunkGenerator) {
//        LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
//        return withOverworld(mappedRegistry, () -> {
//            return levelStem == null ? (DimensionType) registry.getOrThrow(DimensionType.OVERWORLD_LOCATION) : levelStem.type();
//        }, chunkGenerator);
//    }
//
//    public static MappedRegistry<LevelStem> withOverworld(MappedRegistry<LevelStem> mappedRegistry, Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
//        MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
//        mappedRegistry2.register(LevelStem.OVERWORLD,  new LevelStem(supplier, chunkGenerator), Lifecycle.stable());
//        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
//            ResourceKey<LevelStem> key = entry.getKey();
//            if (key != LevelStem.OVERWORLD) {
//                mappedRegistry2.register(key,  entry.getValue(), mappedRegistry.lifecycle(entry.getValue()));
//            }
//        }
//        return mappedRegistry2;
//    }
//
//    public MappedRegistry<LevelStem> dimensions() {
//        return this.dimensions;
//    }
//
//    public ChunkGenerator overworld() {
//        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
//        if (levelStem == null) {
//            throw new IllegalStateException("Overworld settings missing");
//        }
//        return levelStem.generator();
//    }
//
//    public ImmutableSet<ResourceKey<Level>> levels() {
//        return  dimensions().entrySet().stream().map(entry -> {
//            return ResourceKey.create(Registry.DIMENSION_REGISTRY, ( entry.getKey()).location());
//        }).collect(ImmutableSet.toImmutableSet());
//    }
//
//    public boolean isDebug() {
//        return overworld() instanceof DebugLevelSource;
//    }
//
//    public boolean isFlatWorld() {
//        return overworld() instanceof FlatLevelSource;
//    }
//
//    public boolean isOldCustomizedWorld() {
//        return this.legacyCustomOptions.isPresent();
//    }
//
//    public WorldGenSettings withBonusChest() {
//        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
//    }
//
//    public WorldGenSettings withFeaturesToggled() {
//        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
//    }
//
//    public WorldGenSettings withBonusChestToggled() {
//        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
//    }
//
//    public static WorldGenSettings create(RegistryAccess registryAccess, Properties properties) {
//        String str;
//        boolean z;
//        long nextLong;
//        Registry<DimensionType> registryOrThrow;
//        Registry<Biome> registryOrThrow2;
//        Registry<NoiseGeneratorSettings> registryOrThrow3;
//        MappedRegistry<LevelStem> defaultDimensions;
//        str =  MoreObjects.firstNonNull((String) properties.get("generator-settings"), "");
//        properties.put("generator-settings", str);
//        String str2 =  MoreObjects.firstNonNull((String) properties.get("level-seed"), "");
//        properties.put("level-seed", str2);
//        String str3 = (String) properties.get("generate-structures");
//        z = str3 == null || Boolean.parseBoolean(str3);
//        properties.put("generate-structures", Objects.toString(Boolean.valueOf(z)));
//        String str4 =  Optional.ofNullable((String) properties.get("level-type")).map(str5 -> {
//            return str5.toLowerCase(Locale.ROOT);
//        }).orElse("default");
//        properties.put("level-type", str4);
//        nextLong = new Random().nextLong();
//        if (!str2.isEmpty()) {
//            try {
//                long parseLong = Long.parseLong(str2);
//                if (parseLong != 0) {
//                    nextLong = parseLong;
//                }
//            } catch (NumberFormatException e) {
//                nextLong = str2.hashCode();
//            }
//        }
//        registryOrThrow = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
//        registryOrThrow2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
//        registryOrThrow3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
//        defaultDimensions = DimensionType.defaultDimensions(registryOrThrow, registryOrThrow2, registryOrThrow3, nextLong);
//        switch (str4) {
//            case "flat":
//                DataResult parse = FlatLevelGeneratorSettings.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, !str.isEmpty() ? GsonHelper.parse(str) : new JsonObject()));
//                Logger logger = LOGGER;
//                logger.getClass();
//                return new WorldGenSettings(nextLong, z, false, withOverworld(registryOrThrow, defaultDimensions, new FlatLevelSource((FlatLevelGeneratorSettings) parse.resultOrPartial(logger::error).orElseGet(() -> {
//                    return FlatLevelGeneratorSettings.getDefault(registryOrThrow2);
//                }))));
//            case "debug_all_block_states":
//                return new WorldGenSettings(nextLong, z, false, withOverworld(registryOrThrow, defaultDimensions, new DebugLevelSource(registryOrThrow2)));
//            case "amplified":
//                return new WorldGenSettings(nextLong, z, false, withOverworld(registryOrThrow, defaultDimensions, new NoiseBasedChunkGenerator(new OverworldBiomeSource(nextLong, false, false, registryOrThrow2), nextLong, () -> {
//                    return  registryOrThrow3.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
//                })));
//            case "largebiomes":
//                return new WorldGenSettings(nextLong, z, false, withOverworld(registryOrThrow, defaultDimensions, new NoiseBasedChunkGenerator(new OverworldBiomeSource(nextLong, false, true, registryOrThrow2), nextLong, () -> {
//                    return  registryOrThrow3.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
//                })));
//            default:
//                return new WorldGenSettings(nextLong, z, false, withOverworld(registryOrThrow, defaultDimensions, makeDefaultOverworld(registryOrThrow2, registryOrThrow3, nextLong)));
//        }
//    }
//
//    public WorldGenSettings withSeed(boolean z, OptionalLong optionalLong) {
//        MappedRegistry<LevelStem> mappedRegistry;
//        WorldGenSettings worldGenSettings;
//        long orElse = optionalLong.orElse(this.seed);
//        if (optionalLong.isPresent()) {
//            mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
//            long asLong = optionalLong.getAsLong();
//            for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.entrySet()) {
//                mappedRegistry.register(entry.getKey(), new LevelStem(entry.getValue().typeSupplier(), entry.getValue().generator().withSeed(asLong)), this.dimensions.lifecycle(entry.getValue()));
//            }
//        } else {
//            mappedRegistry = this.dimensions;
//        }
//        if (isDebug()) {
//            worldGenSettings = new WorldGenSettings(orElse, false, false, mappedRegistry);
//        } else {
//            worldGenSettings = new WorldGenSettings(orElse, generateFeatures(), generateBonusChest() && !z, mappedRegistry);
//        }
//        return worldGenSettings;
//    }
//}
package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* fixed generics version */
public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.<WorldGenSettings>create(
                    (var0) -> var0.group(
                            Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
                            Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateFeatures),
                            Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest),
                            MappedRegistry.<LevelStem>dataPackCodec(
                                            Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC
                                    ).xmap(LevelStem::sortMap, Function.identity())
                                    .fieldOf("dimensions").forGetter(WorldGenSettings::dimensions),
                            Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter((var0x) -> var0x.legacyCustomOptions)
                    ).apply(var0, var0.stable(WorldGenSettings::new)))
            .comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());

    private static final Logger LOGGER = LogManager.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final MappedRegistry<LevelStem> dimensions;
    private final Optional<String> legacyCustomOptions;

    private DataResult<WorldGenSettings> guardExperimental() {
        if (this.dimensions.get(LevelStem.OVERWORLD) == null) {
            return DataResult.error("Overworld settings missing");
        }
        if (stable()) {
            return DataResult.success(this, Lifecycle.stable());
        }
        return DataResult.success(this);
    }

    private boolean stable() {
        return LevelStem.stable(this.seed, this.dimensions);
    }

    public WorldGenSettings(long j, boolean z, boolean z2, MappedRegistry<LevelStem> mappedRegistry) {
        this(j, z, z2, mappedRegistry, Optional.empty());
        if (mappedRegistry.get(LevelStem.OVERWORLD) == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    private WorldGenSettings(long j, boolean z, boolean z2, MappedRegistry<LevelStem> mappedRegistry, Optional<String> optional) {
        this.seed = j;
        this.generateFeatures = z;
        this.generateBonusChest = z2;
        this.dimensions = mappedRegistry;
        this.legacyCustomOptions = optional;
    }

    public static WorldGenSettings demoSettings(RegistryAccess registryAccess) {
        Registry<Biome> registryOrThrow = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        int hashCode = "North Carolina".hashCode();
        Registry<DimensionType> registryOrThrow2 = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<NoiseGeneratorSettings> registryOrThrow3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        return new WorldGenSettings(hashCode, true, true,
                withOverworld(registryOrThrow2,
                        DimensionType.defaultDimensions(registryOrThrow2, registryOrThrow, registryOrThrow3, hashCode),
                        makeDefaultOverworld(registryOrThrow, registryOrThrow3, hashCode)));
    }

    public static WorldGenSettings makeDefault(Registry<DimensionType> registry, Registry<Biome> registry2, Registry<NoiseGeneratorSettings> registry3) {
        long nextLong = new Random().nextLong();
        return new WorldGenSettings(nextLong, true, false,
                withOverworld(registry,
                        DimensionType.defaultDimensions(registry, registry2, registry3, nextLong),
                        makeDefaultOverworld(registry2, registry3, nextLong)));
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long j) {
        return new NoiseBasedChunkGenerator(new OverworldBiomeSource(j, false, false, registry), j,
                () -> registry2.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateFeatures() {
        return this.generateFeatures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public static MappedRegistry<LevelStem> withOverworld(Registry<DimensionType> registry, MappedRegistry<LevelStem> mappedRegistry, ChunkGenerator chunkGenerator) {
        LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
        return withOverworld(mappedRegistry, () ->
                levelStem == null ? registry.getOrThrow(DimensionType.OVERWORLD_LOCATION) : levelStem.type(), chunkGenerator);
    }

    public static MappedRegistry<LevelStem> withOverworld(MappedRegistry<LevelStem> mappedRegistry, Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
        MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        mappedRegistry2.register(LevelStem.OVERWORLD, new LevelStem(supplier, chunkGenerator), Lifecycle.stable());
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.<ResourceKey<LevelStem>, LevelStem>entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            if (key != LevelStem.OVERWORLD) {
                mappedRegistry2.register(key, entry.getValue(), mappedRegistry.lifecycle(entry.getValue()));
            }
        }
        return mappedRegistry2;
    }

    public MappedRegistry<LevelStem> dimensions() {
        return this.dimensions;
    }

    public ChunkGenerator overworld() {
        LevelStem levelStem = this.dimensions.get(LevelStem.OVERWORLD);
        if (levelStem == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
        return levelStem.generator();
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return dimensions().entrySet().stream()
                .map(entry -> ResourceKey.create(Registry.DIMENSION_REGISTRY, entry.getKey().location()))
                .collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return overworld() instanceof DebugLevelSource;
    }

    public boolean isFlatWorld() {
        return overworld() instanceof FlatLevelSource;
    }

    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
    }

    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
    }

    public static WorldGenSettings create(RegistryAccess registryAccess, Properties properties) {
        String str = MoreObjects.firstNonNull((String) properties.get("generator-settings"), "");
        properties.put("generator-settings", str);
        String str2 = MoreObjects.firstNonNull((String) properties.get("level-seed"), "");
        properties.put("level-seed", str2);
        String str3 = (String) properties.get("generate-structures");
        boolean z = str3 == null || Boolean.parseBoolean(str3);
        properties.put("generate-structures", Objects.toString(z));
        String str4 = Optional.ofNullable((String) properties.get("level-type"))
                .map(s -> s.toLowerCase(Locale.ROOT))
                .orElse("default");
        properties.put("level-type", str4);

        long nextLong = new Random().nextLong();
        if (!str2.isEmpty()) {
            try {
                long parseLong = Long.parseLong(str2);
                if (parseLong != 0) {
                    nextLong = parseLong;
                }
            } catch (NumberFormatException e) {
                nextLong = str2.hashCode();
            }
        }

        Registry<DimensionType> registryOrThrow = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> registryOrThrow2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> registryOrThrow3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        MappedRegistry<LevelStem> defaultDimensions = DimensionType.defaultDimensions(registryOrThrow, registryOrThrow2, registryOrThrow3, nextLong);

        switch (str4) {
            case "flat": {
                DataResult<FlatLevelGeneratorSettings> parse = FlatLevelGeneratorSettings.CODEC.parse(
                        new Dynamic<>(JsonOps.INSTANCE, !str.isEmpty() ? GsonHelper.parse(str) : new JsonObject()));
                return new WorldGenSettings(nextLong, z, false,
                        withOverworld(registryOrThrow, defaultDimensions,
                                new FlatLevelSource(parse.resultOrPartial(LOGGER::error)
                                        .orElseGet(() -> FlatLevelGeneratorSettings.getDefault(registryOrThrow2)))));
            }
            case "debug_all_block_states":
                return new WorldGenSettings(nextLong, z, false,
                        withOverworld(registryOrThrow, defaultDimensions, new DebugLevelSource(registryOrThrow2)));
            case "amplified":
                return new WorldGenSettings(nextLong, z, false,
                        withOverworld(registryOrThrow, defaultDimensions,
                                new NoiseBasedChunkGenerator(new OverworldBiomeSource(nextLong, false, false, registryOrThrow2), nextLong,
                                        () -> registryOrThrow3.getOrThrow(NoiseGeneratorSettings.AMPLIFIED))));
            case "largebiomes":
                return new WorldGenSettings(nextLong, z, false,
                        withOverworld(registryOrThrow, defaultDimensions,
                                new NoiseBasedChunkGenerator(new OverworldBiomeSource(nextLong, false, true, registryOrThrow2), nextLong,
                                        () -> registryOrThrow3.getOrThrow(NoiseGeneratorSettings.OVERWORLD))));
            default:
                return new WorldGenSettings(nextLong, z, false,
                        withOverworld(registryOrThrow, defaultDimensions,
                                makeDefaultOverworld(registryOrThrow2, registryOrThrow3, nextLong)));
        }
    }

    public WorldGenSettings withSeed(boolean z, OptionalLong optionalLong) {
        MappedRegistry<LevelStem> mappedRegistry;
        long orElse = optionalLong.orElse(this.seed);
        if (optionalLong.isPresent()) {
            mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
            long asLong = optionalLong.getAsLong();
            for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : this.dimensions.<ResourceKey<LevelStem>, LevelStem>entrySet()) {
                mappedRegistry.register(entry.getKey(),
                        new LevelStem(entry.getValue().typeSupplier(), entry.getValue().generator().withSeed(asLong)),
                        this.dimensions.lifecycle(entry.getValue()));
            }
        } else {
            mappedRegistry = this.dimensions;
        }
        if (isDebug()) {
            return new WorldGenSettings(orElse, false, false, mappedRegistry);
        } else {
            return new WorldGenSettings(orElse, generateFeatures(), generateBonusChest() && !z, mappedRegistry);
        }
    }
}
