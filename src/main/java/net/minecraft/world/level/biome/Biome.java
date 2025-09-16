package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome.class */
public final class Biome {
    private final Map<Integer, List<StructureFeature<?>>> structuresByStep;
    private final ClimateSettings climateSettings;
    private final BiomeGenerationSettings generationSettings;
    private final MobSpawnSettings mobSettings;
    private final float depth;
    private final float scale;
    private final BiomeCategory biomeCategory;
    private final BiomeSpecialEffects specialEffects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ClimateSettings.CODEC.forGetter(biome -> {
            return biome.climateSettings;
        }), BiomeCategory.CODEC.fieldOf("category").forGetter(biome2 -> {
            return biome2.biomeCategory;
        }), Codec.FLOAT.fieldOf("depth").forGetter(biome3 -> {
            return Float.valueOf(biome3.depth);
        }), Codec.FLOAT.fieldOf("scale").forGetter(biome4 -> {
            return Float.valueOf(biome4.scale);
        }), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome5 -> {
            return biome5.specialEffects;
        }), BiomeGenerationSettings.CODEC.forGetter(biome6 -> {
            return biome6.generationSettings;
        }), MobSpawnSettings.CODEC.forGetter(biome7 -> {
            return biome7.mobSettings;
        })).apply(instance, (v1, v2, v3, v4, v5, v6, v7) -> {
            return new Biome(v1, v2, v3, v4, v5, v6, v7);
        });
    });
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ClimateSettings.CODEC.forGetter(biome -> {
            return biome.climateSettings;
        }), BiomeCategory.CODEC.fieldOf("category").forGetter(biome2 -> {
            return biome2.biomeCategory;
        }), Codec.FLOAT.fieldOf("depth").forGetter(biome3 -> {
            return Float.valueOf(biome3.depth);
        }), Codec.FLOAT.fieldOf("scale").forGetter(biome4 -> {
            return Float.valueOf(biome4.scale);
        }), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome5 -> {
            return biome5.specialEffects;
        })).apply(instance, (climateSettings, biomeCategory, f, f2, biomeSpecialEffects) -> {
            return new Biome(climateSettings, biomeCategory, f.floatValue(), f2.floatValue(), biomeSpecialEffects, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY);
        });
    });
    public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<Biome>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234), (List<Integer>) ImmutableList.of(0));
    private static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456), (List<Integer>) ImmutableList.of(-2, -1, 0));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345), (List<Integer>) ImmutableList.of(0));

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome$BiomeCategory.class */
    public enum BiomeCategory implements StringRepresentable {
        NONE("none"),
        TAIGA("taiga"),
        EXTREME_HILLS("extreme_hills"),
        JUNGLE("jungle"),
        MESA("mesa"),
        PLAINS("plains"),
        SAVANNA("savanna"),
        ICY("icy"),
        THEEND("the_end"),
        BEACH("beach"),
        FOREST("forest"),
        OCEAN("ocean"),
        DESERT("desert"),
        RIVER("river"),
        SWAMP("swamp"),
        MUSHROOM("mushroom"),
        NETHER("nether");

        public static final Codec<BiomeCategory> CODEC = StringRepresentable.fromEnum(BiomeCategory::values, BiomeCategory::byName);
        private static final Map<String, BiomeCategory> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, biomeCategory -> {
            return biomeCategory;
        }));
        private final String name;

        BiomeCategory(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        public static BiomeCategory byName(String str) {
            return BY_NAME.get(str);
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome$Precipitation.class */
    public enum Precipitation implements StringRepresentable {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Precipitation> CODEC = StringRepresentable.fromEnum(Precipitation::values, Precipitation::byName);
        private static final Map<String, Precipitation> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, precipitation -> {
            return precipitation;
        }));
        private final String name;

        Precipitation(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        public static Precipitation byName(String str) {
            return BY_NAME.get(str);
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome$TemperatureModifier.class */
    public enum TemperatureModifier implements StringRepresentable {
        NONE("none") { // from class: net.minecraft.world.level.biome.Biome.TemperatureModifier.1
            @Override // net.minecraft.world.level.biome.Biome.TemperatureModifier
            public float modifyTemperature(BlockPos blockPos, float f) {
                return f;
            }
        },
        FROZEN("frozen") { // from class: net.minecraft.world.level.biome.Biome.TemperatureModifier.2
            @Override // net.minecraft.world.level.biome.Biome.TemperatureModifier
            public float modifyTemperature(BlockPos blockPos, float f) {
                if ((Biome.FROZEN_TEMPERATURE_NOISE.getValue(blockPos.getX() * 0.05d, blockPos.getZ() * 0.05d, false) * 7.0d) + Biome.BIOME_INFO_NOISE.getValue(blockPos.getX() * 0.2d, blockPos.getZ() * 0.2d, false) < 0.3d && Biome.BIOME_INFO_NOISE.getValue(blockPos.getX() * 0.09d, blockPos.getZ() * 0.09d, false) < 0.8d) {
                    return 0.2f;
                }
                return f;
            }
        };

        private final String name;
        public static final Codec<TemperatureModifier> CODEC = StringRepresentable.fromEnum(TemperatureModifier::values, TemperatureModifier::byName);
        private static final Map<String, TemperatureModifier> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, temperatureModifier -> {
            return temperatureModifier;
        }));

        public abstract float modifyTemperature(BlockPos blockPos, float f);

        TemperatureModifier(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }

        public static TemperatureModifier byName(String str) {
            return BY_NAME.get(str);
        }
    }

    private Biome(ClimateSettings climateSettings, BiomeCategory biomeCategory, float f, float f2, BiomeSpecialEffects biomeSpecialEffects, BiomeGenerationSettings biomeGenerationSettings, MobSpawnSettings mobSpawnSettings) {
        this.structuresByStep = (Map) Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy(structureFeature -> {
            return Integer.valueOf(structureFeature.step().ordinal());
        }));
        this.temperatureCache = ThreadLocal.withInitial(() -> {
            return (Long2FloatLinkedOpenHashMap) Util.make(() -> {
                Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f) { // from class: net.minecraft.world.level.biome.Biome.1
                    protected void rehash(int i) {
                    }
                };
                long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
                return long2FloatLinkedOpenHashMap;
            });
        });
        this.climateSettings = climateSettings;
        this.generationSettings = biomeGenerationSettings;
        this.mobSettings = mobSpawnSettings;
        this.biomeCategory = biomeCategory;
        this.depth = f;
        this.scale = f2;
        this.specialEffects = biomeSpecialEffects;
    }

    public int getSkyColor() {
        return this.specialEffects.getSkyColor();
    }

    public MobSpawnSettings getMobSettings() {
        return this.mobSettings;
    }

    public Precipitation getPrecipitation() {
        return this.climateSettings.precipitation;
    }

    public boolean isHumid() {
        return getDownfall() > 0.85f;
    }

    private float getHeightAdjustedTemperature(BlockPos blockPos) {
        float modifyTemperature = this.climateSettings.temperatureModifier.modifyTemperature(blockPos, getBaseTemperature());
        if (blockPos.getY() > 64) {
            return modifyTemperature - ((((((float) (TEMPERATURE_NOISE.getValue(blockPos.getX() / 8.0f, blockPos.getZ() / 8.0f, false) * 4.0d)) + blockPos.getY()) - 64.0f) * 0.05f) / 30.0f);
        }
        return modifyTemperature;
    }

    public final float getTemperature(BlockPos blockPos) {
        long asLong = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = this.temperatureCache.get();
        float f = long2FloatLinkedOpenHashMap.get(asLong);
        if (!Float.isNaN(f)) {
            return f;
        }
        float heightAdjustedTemperature = getHeightAdjustedTemperature(blockPos);
        if (long2FloatLinkedOpenHashMap.size() == 1024) {
            long2FloatLinkedOpenHashMap.removeFirstFloat();
        }
        long2FloatLinkedOpenHashMap.put(asLong, heightAdjustedTemperature);
        return heightAdjustedTemperature;
    }

    public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos) {
        return shouldFreeze(levelReader, blockPos, true);
    }

    public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos, boolean z) {
        if (getTemperature(blockPos) < 0.15f && blockPos.getY() >= 0 && blockPos.getY() < 256 && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            if (levelReader.getFluidState(blockPos).getType() == Fluids.WATER && (blockState.getBlock() instanceof LiquidBlock)) {
                if (!z) {
                    return true;
                }
                if (!(levelReader.isWaterAt(blockPos.west()) && levelReader.isWaterAt(blockPos.east()) && levelReader.isWaterAt(blockPos.north()) && levelReader.isWaterAt(blockPos.south()))) {
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public boolean shouldSnow(LevelReader levelReader, BlockPos blockPos) {
        if (getTemperature(blockPos) < 0.15f && blockPos.getY() >= 0 && blockPos.getY() < 256 && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10 && levelReader.getBlockState(blockPos).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(levelReader, blockPos)) {
            return true;
        }
        return false;
    }

    public BiomeGenerationSettings getGenerationSettings() {
        return this.generationSettings;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void generate(StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, WorldGenRegion worldGenRegion, long j, WorldgenRandom worldgenRandom, BlockPos blockPos) {
        List<List<Supplier<ConfiguredFeature<?, ?>>>> features = this.generationSettings.features();
        int length = GenerationStep.Decoration.values().length;
        for (int i = 0; i < length; i++) {
            int i2 = 0;
            if (structureFeatureManager.shouldGenerateFeatures()) {
                for (StructureFeature<?> structureFeature : this.structuresByStep.getOrDefault(Integer.valueOf(i), Collections.emptyList())) {
                    worldgenRandom.setFeatureSeed(j, i2, i);
                    int x = blockPos.getX() >> 4;
                    int z = blockPos.getZ() >> 4;
                    int i3 = x << 4;
                    int i4 = z << 4;
                    try {
                        structureFeatureManager.startsForFeature(SectionPos.of(blockPos), structureFeature).forEach(structureStart -> {
                            structureStart.placeInChunk(worldGenRegion, structureFeatureManager, chunkGenerator, worldgenRandom, new BoundingBox(i3, i4, i3 + 15, i4 + 15), new ChunkPos(x, z));
                        });
                        i2++;
                    } catch (Exception e) {
                        CrashReport forThrowable = CrashReport.forThrowable(e, "Feature placement");
                        forThrowable.addCategory("Feature").setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structureFeature)).setDetail("Description", () -> {
                            return structureFeature.toString();
                        });
                        throw new ReportedException(forThrowable);
                    }
                }
            }
            if (features.size() > i) {
                Iterator<Supplier<ConfiguredFeature<?, ?>>> it = features.get(i).iterator();
                while (it.hasNext()) {
                    ConfiguredFeature<?, ?> configuredFeature = it.next().get();
                    worldgenRandom.setFeatureSeed(j, i2, i);
                    try {
                        configuredFeature.place(worldGenRegion, chunkGenerator, worldgenRandom, blockPos);
                        i2++;
                    } catch (Exception e2) {
                        CrashReport forThrowable2 = CrashReport.forThrowable(e2, "Feature placement");
                        forThrowable2.addCategory("Feature").setDetail("Id", Registry.FEATURE.getKey(configuredFeature.feature)).setDetail("Config", configuredFeature.config).setDetail("Description", () -> {
                            return configuredFeature.feature.toString();
                        });
                        throw new ReportedException(forThrowable2);
                    }
                }
            }
        }
    }

    public int getFogColor() {
        return this.specialEffects.getFogColor();
    }

    public int getGrassColor(double d, double d2) {
        return this.specialEffects.getGrassColorModifier().modifyColor(d, d2, this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture).intValue());
    }

    private int getGrassColorFromTexture() {
        return GrassColor.get(Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f), Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f));
    }

    public int getFoliageColor() {
        return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture).intValue();
    }

    private int getFoliageColorFromTexture() {
        return FoliageColor.get(Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f), Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f));
    }

    public void buildSurfaceAt(Random random, ChunkAccess chunkAccess, int i, int i2, int i3, double d, BlockState blockState, BlockState blockState2, int i4, long j) {
        ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder = this.generationSettings.getSurfaceBuilder().get();
        configuredSurfaceBuilder.initNoise(j);
        configuredSurfaceBuilder.apply(random, chunkAccess, this, i, i2, i3, d, blockState, blockState2, i4, j);
    }

    public final float getDepth() {
        return this.depth;
    }

    public final float getDownfall() {
        return this.climateSettings.downfall;
    }

    public final float getScale() {
        return this.scale;
    }

    public final float getBaseTemperature() {
        return this.climateSettings.temperature;
    }

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    public final int getWaterColor() {
        return this.specialEffects.getWaterColor();
    }

    public final int getWaterFogColor() {
        return this.specialEffects.getWaterFogColor();
    }

    public Optional<AmbientParticleSettings> getAmbientParticle() {
        return this.specialEffects.getAmbientParticleSettings();
    }

    public Optional<SoundEvent> getAmbientLoop() {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    public Optional<AmbientMoodSettings> getAmbientMood() {
        return this.specialEffects.getAmbientMoodSettings();
    }

    public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
        return this.specialEffects.getAmbientAdditionsSettings();
    }

    public Optional<Music> getBackgroundMusic() {
        return this.specialEffects.getBackgroundMusic();
    }

    public final BiomeCategory getBiomeCategory() {
        return this.biomeCategory;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome$BiomeBuilder.class */
    public static class BiomeBuilder {

        @Nullable
        private Precipitation precipitation;

        @Nullable
        private BiomeCategory biomeCategory;

        @Nullable
        private Float depth;

        @Nullable
        private Float scale;

        @Nullable
        private Float temperature;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;

        @Nullable
        private Float downfall;

        @Nullable
        private BiomeSpecialEffects specialEffects;

        @Nullable
        private MobSpawnSettings mobSpawnSettings;

        @Nullable
        private BiomeGenerationSettings generationSettings;

        public BiomeBuilder precipitation(Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public BiomeBuilder biomeCategory(BiomeCategory biomeCategory) {
            this.biomeCategory = biomeCategory;
            return this;
        }

        public BiomeBuilder depth(float f) {
            this.depth = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder scale(float f) {
            this.scale = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder temperature(float f) {
            this.temperature = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder downfall(float f) {
            this.downfall = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
            this.specialEffects = biomeSpecialEffects;
            return this;
        }

        public BiomeBuilder mobSpawnSettings(MobSpawnSettings mobSpawnSettings) {
            this.mobSpawnSettings = mobSpawnSettings;
            return this;
        }

        public BiomeBuilder generationSettings(BiomeGenerationSettings biomeGenerationSettings) {
            this.generationSettings = biomeGenerationSettings;
            return this;
        }

        public BiomeBuilder temperatureAdjustment(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            if (this.precipitation == null || this.biomeCategory == null || this.depth == null || this.scale == null || this.temperature == null || this.downfall == null || this.specialEffects == null || this.mobSpawnSettings == null || this.generationSettings == null) {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
            }
            return new Biome(new ClimateSettings(this.precipitation, this.temperature.floatValue(), this.temperatureModifier, this.downfall.floatValue()), this.biomeCategory, this.depth.floatValue(), this.scale.floatValue(), this.specialEffects, this.generationSettings, this.mobSpawnSettings);
        }

        public String toString() {
            return "BiomeBuilder{\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.biomeCategory + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.mobSpawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n}";
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome$ClimateParameters.class */
    public static class ClimateParameters {
        public static final Codec<ClimateParameters> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(Codec.floatRange(-2.0f, 2.0f).fieldOf("temperature").forGetter(climateParameters -> {
                return Float.valueOf(climateParameters.temperature);
            }), Codec.floatRange(-2.0f, 2.0f).fieldOf("humidity").forGetter(climateParameters2 -> {
                return Float.valueOf(climateParameters2.humidity);
            }), Codec.floatRange(-2.0f, 2.0f).fieldOf("altitude").forGetter(climateParameters3 -> {
                return Float.valueOf(climateParameters3.altitude);
            }), Codec.floatRange(-2.0f, 2.0f).fieldOf("weirdness").forGetter(climateParameters4 -> {
                return Float.valueOf(climateParameters4.weirdness);
            }), Codec.floatRange(0.0f, 1.0f).fieldOf("offset").forGetter(climateParameters5 -> {
                return Float.valueOf(climateParameters5.offset);
            })).apply(instance, (v1, v2, v3, v4, v5) -> {
                return new ClimateParameters(v1, v2, v3, v4, v5);
            });
        });
        private final float temperature;
        private final float humidity;
        private final float altitude;
        private final float weirdness;
        private final float offset;

        public ClimateParameters(float f, float f2, float f3, float f4, float f5) {
            this.temperature = f;
            this.humidity = f2;
            this.altitude = f3;
            this.weirdness = f4;
            this.offset = f5;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ClimateParameters climateParameters = (ClimateParameters) obj;
            return Float.compare(climateParameters.temperature, this.temperature) == 0 && Float.compare(climateParameters.humidity, this.humidity) == 0 && Float.compare(climateParameters.altitude, this.altitude) == 0 && Float.compare(climateParameters.weirdness, this.weirdness) == 0;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * (this.temperature != 0.0f ? Float.floatToIntBits(this.temperature) : 0)) + (this.humidity != 0.0f ? Float.floatToIntBits(this.humidity) : 0))) + (this.altitude != 0.0f ? Float.floatToIntBits(this.altitude) : 0))) + (this.weirdness != 0.0f ? Float.floatToIntBits(this.weirdness) : 0);
        }

        public float fitness(ClimateParameters climateParameters) {
            return ((this.temperature - climateParameters.temperature) * (this.temperature - climateParameters.temperature)) + ((this.humidity - climateParameters.humidity) * (this.humidity - climateParameters.humidity)) + ((this.altitude - climateParameters.altitude) * (this.altitude - climateParameters.altitude)) + ((this.weirdness - climateParameters.weirdness) * (this.weirdness - climateParameters.weirdness)) + ((this.offset - climateParameters.offset) * (this.offset - climateParameters.offset));
        }
    }

    public String toString() {
        ResourceLocation key = BuiltinRegistries.BIOME.getKey(this);
        return key == null ? super.toString() : key.toString();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/Biome$ClimateSettings.class */
    static class ClimateSettings {
        public static final MapCodec<ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(Precipitation.CODEC.fieldOf("precipitation").forGetter(climateSettings -> {
                return climateSettings.precipitation;
            }), Codec.FLOAT.fieldOf("temperature").forGetter(climateSettings2 -> {
                return Float.valueOf(climateSettings2.temperature);
            }), TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", TemperatureModifier.NONE).forGetter(climateSettings3 -> {
                return climateSettings3.temperatureModifier;
            }), Codec.FLOAT.fieldOf("downfall").forGetter(climateSettings4 -> {
                return Float.valueOf(climateSettings4.downfall);
            })).apply(instance, (v1, v2, v3, v4) -> {
                return new ClimateSettings(v1, v2, v3, v4);
            });
        });
        private final Precipitation precipitation;
        private final float temperature;
        private final TemperatureModifier temperatureModifier;
        private final float downfall;

        private ClimateSettings(Precipitation precipitation, float f, TemperatureModifier temperatureModifier, float f2) {
            this.precipitation = precipitation;
            this.temperature = f;
            this.temperatureModifier = temperatureModifier;
            this.downfall = f2;
        }
    }
}
