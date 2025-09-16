package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/StructureFeature.class */
public abstract class StructureFeature<C extends FeatureConfiguration> {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = register("Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register("Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register("Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register("Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register("Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register("Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register("Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register("Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final SwamplandHutFeature SWAMP_HUT = (SwamplandHutFeature) register("Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register("Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS);
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register("Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register("Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register("Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register("EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = register("Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<JigsawConfiguration> VILLAGE = register("Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_FOSSIL = register("Nether_Fossil", new NetherFossilFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register("Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL);
    private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
    private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder().put(new ResourceLocation("nvi"), JIGSAW_RENAME).put(new ResourceLocation("pcp"), JIGSAW_RENAME).put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME).put(new ResourceLocation("runtime"), JIGSAW_RENAME).build();
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/StructureFeature$StructureStartFactory.class */
    public interface StructureStartFactory<C extends FeatureConfiguration> {
        StructureStart<C> create(StructureFeature<C> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j);
    }

    public abstract StructureStartFactory<C> getStartFactory();

    private static <F extends StructureFeature<?>> F register(String str, F f, GenerationStep.Decoration decoration) {
        STRUCTURES_REGISTRY.put(str.toLowerCase(Locale.ROOT), f);
        STEP.put(f, decoration);
        return (F) Registry.register(Registry.STRUCTURE_FEATURE, str.toLowerCase(Locale.ROOT), f);
    }

    public StructureFeature(Codec<C> codec) {
        this.configuredStructureCodec = codec.fieldOf("config").xmap(var1x -> new ConfiguredStructureFeature<>(this, (C)var1x), var0 -> var0.config).codec();
    }

    public GenerationStep.Decoration step() {
        return STEP.get(this);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart<?> loadStaticStart(StructureManager structureManager, CompoundTag compoundTag, long j) {
        String string = compoundTag.getString("id");
        if ("INVALID".equals(string)) {
            return StructureStart.INVALID_START;
        }
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(string.toLowerCase(Locale.ROOT)));
        if (structureFeature == null) {
            LOGGER.error("Unknown feature id: {}", string);
            return null;
        }
        int i = compoundTag.getInt("ChunkX");
        int i2 = compoundTag.getInt("ChunkZ");
        int i3 = compoundTag.getInt("references");
        BoundingBox boundingBox = compoundTag.contains("BB") ? new BoundingBox(compoundTag.getIntArray("BB")) : BoundingBox.getUnknownBox();
        ListTag list = compoundTag.getList("Children", 10);
        try {
            StructureStart<?> createStart = structureFeature.createStart(i, i2, boundingBox, i3, j);
            for (int i4 = 0; i4 < list.size(); i4++) {
                CompoundTag compound = list.getCompound(i4);
                ResourceLocation resourceLocation = new ResourceLocation(compound.getString("id").toLowerCase(Locale.ROOT));
                ResourceLocation orDefault = RENAMES.getOrDefault(resourceLocation, resourceLocation);
                StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(orDefault);
                if (structurePieceType == null) {
                    LOGGER.error("Unknown structure piece id: {}", orDefault);
                } else {
                    try {
                        createStart.getPieces().add(structurePieceType.load(structureManager, compound));
                    } catch (Exception e) {
                        LOGGER.error("Exception loading structure piece with id {}", orDefault, e);
                    }
                }
            }
            return createStart;
        } catch (Exception e2) {
            LOGGER.error("Failed Start with id {}", string, e2);
            return null;
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C c) {
        return new ConfiguredStructureFeature<>(this, c);
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(LevelReader levelReader, StructureFeatureManager structureFeatureManager, BlockPos blockPos, int i, boolean z, long j, StructureFeatureConfiguration structureFeatureConfiguration) {
        int spacing = structureFeatureConfiguration.spacing();
        int x = blockPos.getX() >> 4;
        int z2 = blockPos.getZ() >> 4;
        int i2 = 0;
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        while (i2 <= i) {
            int i3 = -i2;
            while (i3 <= i2) {
                boolean z3 = i3 == (-i2) || i3 == i2;
                int i4 = -i2;
                while (i4 <= i2) {
                    boolean z4 = i4 == (-i2) || i4 == i2;
                    if (z3 || z4) {
                        ChunkPos potentialFeatureChunk = getPotentialFeatureChunk(structureFeatureConfiguration, j, worldgenRandom, x + (spacing * i3), z2 + (spacing * i4));
                        ChunkAccess chunk = levelReader.getChunk(potentialFeatureChunk.x, potentialFeatureChunk.z, ChunkStatus.STRUCTURE_STARTS);
                        StructureStart<?> startForFeature = structureFeatureManager.getStartForFeature(SectionPos.of(chunk.getPos(), 0), this, chunk);
                        if (startForFeature != null && startForFeature.isValid()) {
                            if (z && startForFeature.canBeReferenced()) {
                                startForFeature.addReference();
                                return startForFeature.getLocatePos();
                            }
                            if (!z) {
                                return startForFeature.getLocatePos();
                            }
                        }
                        if (i2 == 0) {
                            break;
                        }
                    }
                    i4++;
                }
                if (i2 == 0) {
                    break;
                }
                i3++;
            }
            i2++;
        }
        return null;
    }

    protected boolean linearSeparation() {
        return true;
    }

    public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration structureFeatureConfiguration, long j, WorldgenRandom worldgenRandom, int i, int i2) {
        int nextInt;
        int nextInt2;
        int spacing = structureFeatureConfiguration.spacing();
        int separation = structureFeatureConfiguration.separation();
        int floorDiv = Math.floorDiv(i, spacing);
        int floorDiv2 = Math.floorDiv(i2, spacing);
        worldgenRandom.setLargeFeatureWithSalt(j, floorDiv, floorDiv2, structureFeatureConfiguration.salt());
        if (linearSeparation()) {
            nextInt = worldgenRandom.nextInt(spacing - separation);
            nextInt2 = worldgenRandom.nextInt(spacing - separation);
        } else {
            nextInt = (worldgenRandom.nextInt(spacing - separation) + worldgenRandom.nextInt(spacing - separation)) / 2;
            nextInt2 = (worldgenRandom.nextInt(spacing - separation) + worldgenRandom.nextInt(spacing - separation)) / 2;
        }
        return new ChunkPos((floorDiv * spacing) + nextInt, (floorDiv2 * spacing) + nextInt2);
    }

    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, C c) {
        return true;
    }

    private StructureStart<C> createStart(int i, int i2, BoundingBox boundingBox, int i3, long j) {
        return getStartFactory().create(this, i, i2, boundingBox, i3, j);
    }

    public StructureStart<?> generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long j, ChunkPos chunkPos, Biome biome, int i, WorldgenRandom worldgenRandom, StructureFeatureConfiguration structureFeatureConfiguration, C c) {
        ChunkPos potentialFeatureChunk = getPotentialFeatureChunk(structureFeatureConfiguration, j, worldgenRandom, chunkPos.x, chunkPos.z);
        if (chunkPos.x == potentialFeatureChunk.x && chunkPos.z == potentialFeatureChunk.z && isFeatureChunk(chunkGenerator, biomeSource, j, worldgenRandom, chunkPos.x, chunkPos.z, biome, potentialFeatureChunk, c)) {
            StructureStart<C> createStart = createStart(chunkPos.x, chunkPos.z, BoundingBox.getUnknownBox(), i, j);
            createStart.generatePieces(registryAccess, chunkGenerator, structureManager, chunkPos.x, chunkPos.z, biome, c);
            if (createStart.isValid()) {
                return createStart;
            }
        }
        return StructureStart.INVALID_START;
    }

    public String getFeatureName() {
        return (String) STRUCTURES_REGISTRY.inverse().get(this);
    }

    public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return ImmutableList.of();
    }

    public List<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
        return ImmutableList.of();
    }
}
