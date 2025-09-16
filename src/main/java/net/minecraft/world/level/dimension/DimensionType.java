package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/dimension/DimensionType.class */
public class DimensionType {
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
    public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
    public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
    public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.LONG.optionalFieldOf("fixed_time").xmap(optional -> {
            return  optional.map((v0) -> {
                return OptionalLong.of(v0);
            }).orElseGet(OptionalLong::empty);
        }, optionalLong -> {
            return optionalLong.isPresent() ? Optional.of(Long.valueOf(optionalLong.getAsLong())) : Optional.empty();
        }).forGetter(dimensionType -> {
            return dimensionType.fixedTime;
        }), Codec.BOOL.fieldOf("has_skylight").forGetter((v0) -> {
            return v0.hasSkyLight();
        }), Codec.BOOL.fieldOf("has_ceiling").forGetter((v0) -> {
            return v0.hasCeiling();
        }), Codec.BOOL.fieldOf("ultrawarm").forGetter((v0) -> {
            return v0.ultraWarm();
        }), Codec.BOOL.fieldOf("natural").forGetter((v0) -> {
            return v0.natural();
        }), Codec.doubleRange(9.999999747378752E-6d, 3.0E7d).fieldOf("coordinate_scale").forGetter((v0) -> {
            return v0.coordinateScale();
        }), Codec.BOOL.fieldOf("piglin_safe").forGetter((v0) -> {
            return v0.piglinSafe();
        }), Codec.BOOL.fieldOf("bed_works").forGetter((v0) -> {
            return v0.bedWorks();
        }), Codec.BOOL.fieldOf("respawn_anchor_works").forGetter((v0) -> {
            return v0.respawnAnchorWorks();
        }), Codec.BOOL.fieldOf("has_raids").forGetter((v0) -> {
            return v0.hasRaids();
        }), Codec.intRange(0, 256).fieldOf("logical_height").forGetter((v0) -> {
            return v0.logicalHeight();
        }), ResourceLocation.CODEC.fieldOf("infiniburn").forGetter(dimensionType2 -> {
            return dimensionType2.infiniburn;
        }), ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter(dimensionType3 -> {
            return dimensionType3.effectsLocation;
        }), Codec.FLOAT.fieldOf("ambient_light").forGetter(dimensionType4 -> {
            return Float.valueOf(dimensionType4.ambientLight);
        })).apply(instance, (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> {
            return new DimensionType(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
        });
    });
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = {1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    protected static final DimensionType DEFAULT_OVERWORLD = new DimensionType(OptionalLong.empty(), true, false, false, true, 1.0d, false, false, true, false, true, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0f);
    protected static final DimensionType DEFAULT_NETHER = new DimensionType(OptionalLong.of(18000), false, true, true, false, 8.0d, false, true, false, true, false, 128, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_NETHER.getName(), NETHER_EFFECTS, 0.1f);
    protected static final DimensionType DEFAULT_END = new DimensionType(OptionalLong.of(6000), false, false, false, false, 1.0d, true, false, false, false, true, 256, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_END.getName(), END_EFFECTS, 0.0f);
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = new DimensionType(OptionalLong.empty(), true, true, false, true, 1.0d, false, false, true, false, true, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0f);
    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int logicalHeight;
    private final BiomeZoomer biomeZoomer;
    private final ResourceLocation infiniburn;
    private final ResourceLocation effectsLocation;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    protected DimensionType(OptionalLong optionalLong, boolean z, boolean z2, boolean z3, boolean z4, double d, boolean z5, boolean z6, boolean z7, boolean z8, int i, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, float f) {
        this(optionalLong, z, z2, z3, z4, d, false, z5, z6, z7, z8, i, FuzzyOffsetBiomeZoomer.INSTANCE, resourceLocation, resourceLocation2, f);
    }

    protected DimensionType(OptionalLong optionalLong, boolean z, boolean z2, boolean z3, boolean z4, double d, boolean z5, boolean z6, boolean z7, boolean z8, boolean z9, int i, BiomeZoomer biomeZoomer, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, float f) {
        this.fixedTime = optionalLong;
        this.hasSkylight = z;
        this.hasCeiling = z2;
        this.ultraWarm = z3;
        this.natural = z4;
        this.coordinateScale = d;
        this.createDragonFight = z5;
        this.piglinSafe = z6;
        this.bedWorks = z7;
        this.respawnAnchorWorks = z8;
        this.hasRaids = z9;
        this.logicalHeight = i;
        this.biomeZoomer = biomeZoomer;
        this.infiniburn = resourceLocation;
        this.effectsLocation = resourceLocation2;
        this.ambientLight = f;
        this.brightnessRamp = fillBrightnessRamp(f);
    }

    private static float[] fillBrightnessRamp(float f) {
        float[] fArr = new float[16];
        for (int i = 0; i <= 15; i++) {
            float f2 = i / 15.0f;
            fArr[i] = Mth.lerp(f, f2 / (4.0f - (3.0f * f2)), 1.0f);
        }
        return fArr;
    }

    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> dynamic) {
        Optional<Number> result = dynamic.asNumber().result();
        if (result.isPresent()) {
            int intValue = result.get().intValue();
            if (intValue == -1) {
                return DataResult.success(Level.NETHER);
            }
            if (intValue == 0) {
                return DataResult.success(Level.OVERWORLD);
            }
            if (intValue == 1) {
                return DataResult.success(Level.END);
            }
        }
        return Level.RESOURCE_KEY_CODEC.parse(dynamic);
    }

    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder registryHolder) {
        WritableRegistry<DimensionType> registryOrThrow = registryHolder.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        registryOrThrow.register(OVERWORLD_LOCATION,  DEFAULT_OVERWORLD, Lifecycle.stable());
        registryOrThrow.register(OVERWORLD_CAVES_LOCATION,  DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
        registryOrThrow.register(NETHER_LOCATION,  DEFAULT_NETHER, Lifecycle.stable());
        registryOrThrow.register(END_LOCATION,  DEFAULT_END, Lifecycle.stable());
        return registryHolder;
    }

    private static ChunkGenerator defaultEndGenerator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long j) {
        return new NoiseBasedChunkGenerator(new TheEndBiomeSource(registry, j), j, () -> {
            return (NoiseGeneratorSettings) registry2.getOrThrow(NoiseGeneratorSettings.END);
        });
    }

    private static ChunkGenerator defaultNetherGenerator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long j) {
        return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(registry, j), j, () -> {
            return (NoiseGeneratorSettings) registry2.getOrThrow(NoiseGeneratorSettings.NETHER);
        });
    }

    public static MappedRegistry<LevelStem> defaultDimensions(Registry<DimensionType> registry, Registry<Biome> registry2, Registry<NoiseGeneratorSettings> registry3, long j) {
        MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        mappedRegistry.register(LevelStem.NETHER,  new LevelStem(() -> {
            return  registry.getOrThrow(NETHER_LOCATION);
        }, defaultNetherGenerator(registry2, registry3, j)), Lifecycle.stable());
        mappedRegistry.register(LevelStem.END,  new LevelStem(() -> {
            return  registry.getOrThrow(END_LOCATION);
        }, defaultEndGenerator(registry2, registry3, j)), Lifecycle.stable());
        return mappedRegistry;
    }

    public static double getTeleportationScale(DimensionType dimensionType, DimensionType dimensionType2) {
        return dimensionType.coordinateScale() / dimensionType2.coordinateScale();
    }

    @Deprecated
    public String getFileSuffix() {
        if (equalTo(DEFAULT_END)) {
            return "_end";
        }
        return "";
    }

    public static File getStorageFolder(ResourceKey<Level> resourceKey, File file) {
        if (resourceKey == Level.OVERWORLD) {
            return file;
        }
        if (resourceKey == Level.END) {
            return new File(file, "DIM1");
        }
        if (resourceKey == Level.NETHER) {
            return new File(file, "DIM-1");
        }
        return new File(file, "dimensions/" + resourceKey.location().getNamespace() + "/" + resourceKey.location().getPath());
    }

    public boolean hasSkyLight() {
        return this.hasSkylight;
    }

    public boolean hasCeiling() {
        return this.hasCeiling;
    }

    public boolean ultraWarm() {
        return this.ultraWarm;
    }

    public boolean natural() {
        return this.natural;
    }

    public double coordinateScale() {
        return this.coordinateScale;
    }

    public boolean piglinSafe() {
        return this.piglinSafe;
    }

    public boolean bedWorks() {
        return this.bedWorks;
    }

    public boolean respawnAnchorWorks() {
        return this.respawnAnchorWorks;
    }

    public boolean hasRaids() {
        return this.hasRaids;
    }

    public int logicalHeight() {
        return this.logicalHeight;
    }

    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    public BiomeZoomer getBiomeZoomer() {
        return this.biomeZoomer;
    }

    public boolean hasFixedTime() {
        return this.fixedTime.isPresent();
    }

    public float timeOfDay(long j) {
        double frac = Mth.frac((this.fixedTime.orElse(j) / 24000.0d) - 0.25d);
        return ((float) ((frac * 2.0d) + (0.5d - (Math.cos(frac * 3.141592653589793d) / 2.0d)))) / 3.0f;
    }

    public int moonPhase(long j) {
        return ((int) (((j / 24000) % 8) + 8)) % 8;
    }

    public float brightness(int i) {
        return this.brightnessRamp[i];
    }

    public Tag<Block> infiniburn() {
        Tag<Block> tag = BlockTags.getAllTags().getTag(this.infiniburn);
        return tag != null ? tag : BlockTags.INFINIBURN_OVERWORLD;
    }

    public ResourceLocation effectsLocation() {
        return this.effectsLocation;
    }

    public boolean equalTo(DimensionType dimensionType) {
        if (this == dimensionType) {
            return true;
        }
        return this.hasSkylight == dimensionType.hasSkylight && this.hasCeiling == dimensionType.hasCeiling && this.ultraWarm == dimensionType.ultraWarm && this.natural == dimensionType.natural && this.coordinateScale == dimensionType.coordinateScale && this.createDragonFight == dimensionType.createDragonFight && this.piglinSafe == dimensionType.piglinSafe && this.bedWorks == dimensionType.bedWorks && this.respawnAnchorWorks == dimensionType.respawnAnchorWorks && this.hasRaids == dimensionType.hasRaids && this.logicalHeight == dimensionType.logicalHeight && Float.compare(dimensionType.ambientLight, this.ambientLight) == 0 && this.fixedTime.equals(dimensionType.fixedTime) && this.biomeZoomer.equals(dimensionType.biomeZoomer) && this.infiniburn.equals(dimensionType.infiniburn) && this.effectsLocation.equals(dimensionType.effectsLocation);
    }
}
