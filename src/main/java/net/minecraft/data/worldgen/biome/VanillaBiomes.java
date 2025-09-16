package net.minecraft.data.worldgen.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/worldgen/biome/VanillaBiomes.class */
public class VanillaBiomes {
   private static int calculateSkyColor(float f) {
      float clamp = Mth.clamp(f / 3.0f, -1.0f, 1.0f);
      return Mth.hsvToRgb(0.62222224f - (clamp * 0.05f), 0.5f + (clamp * 0.1f), 1.0f);
   }

   public static Biome giantTreeTaiga(float f, float f2, float f3, boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      if (z) {
         BiomeDefaultFeatures.commonSpawns(builder);
      } else {
         BiomeDefaultFeatures.ambientSpawns(builder);
         BiomeDefaultFeatures.monsters(builder, 100, 25, 100);
      }
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GIANT_TREE_TAIGA);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addMossyStoneBlock(surfaceBuilder);
      BiomeDefaultFeatures.addFerns(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, z ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addGiantTaigaVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSparseBerryBushes(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).depth(f).scale(f2).temperature(f3).downfall(0.8f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f3)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome birchForestBiome(float f, float f2, boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      BiomeDefaultFeatures.commonSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addForestFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      if (z) {
         BiomeDefaultFeatures.addTallBirchTrees(surfaceBuilder);
      } else {
         BiomeDefaultFeatures.addBirchTrees(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addForestGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(f).scale(f2).temperature(0.6f).downfall(0.6f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.6f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome jungleBiome() {
      return jungleBiome(0.1f, 0.2f, 40, 2, 3);
   }

   public static Biome jungleEdgeBiome() {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(builder);
      return baseJungleBiome(0.1f, 0.2f, 0.8f, false, true, false, builder);
   }

   public static Biome modifiedJungleEdgeBiome() {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(builder);
      return baseJungleBiome(0.2f, 0.4f, 0.8f, false, true, true, builder);
   }

   public static Biome modifiedJungleBiome() {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 10, 1, 1)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
      return baseJungleBiome(0.2f, 0.4f, 0.9f, false, false, true, builder);
   }

   public static Biome jungleHillsBiome() {
      return jungleBiome(0.45f, 0.3f, 10, 1, 1);
   }

   public static Biome bambooJungleBiome() {
      return bambooJungleBiome(0.1f, 0.2f, 40, 2);
   }

   public static Biome bambooJungleHillsBiome() {
      return bambooJungleBiome(0.45f, 0.3f, 10, 1);
   }

   private static Biome jungleBiome(float f, float f2, int i, int i2, int i3) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, i, 1, i2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, i3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
      builder.setPlayerCanSpawn();
      return baseJungleBiome(f, f2, 0.9f, false, false, false, builder);
   }

   private static Biome bambooJungleBiome(float f, float f2, int i, int i2) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, i, 1, i2)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
      return baseJungleBiome(f, f2, 0.9f, true, false, false, builder);
   }

   private static Biome baseJungleBiome(float f, float f2, float f3, boolean z, boolean z2, boolean z3, MobSpawnSettings.Builder builder) {
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      if (!z2 && !z3) {
         surfaceBuilder.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
      }
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      if (z) {
         BiomeDefaultFeatures.addBambooVegetation(surfaceBuilder);
      } else {
         if (!z2 && !z3) {
            BiomeDefaultFeatures.addLightBambooVegetation(surfaceBuilder);
         }
         if (z2) {
            BiomeDefaultFeatures.addJungleEdgeTrees(surfaceBuilder);
         } else {
            BiomeDefaultFeatures.addJungleTrees(surfaceBuilder);
         }
      }
      BiomeDefaultFeatures.addWarmFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addJungleGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addJungleExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.JUNGLE).depth(f).scale(f2).temperature(0.95f).downfall(f3).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.95f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome mountainBiome(float f, float f2, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
      BiomeDefaultFeatures.commonSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(configuredSurfaceBuilder);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      if (z) {
         BiomeDefaultFeatures.addMountainEdgeTrees(surfaceBuilder);
      } else {
         BiomeDefaultFeatures.addMountainTrees(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addExtraEmeralds(surfaceBuilder);
      BiomeDefaultFeatures.addInfestedStone(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.EXTREME_HILLS).depth(f).scale(f2).temperature(0.2f).downfall(0.3f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.2f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome desertBiome(float f, float f2, boolean z, boolean z2, boolean z3) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.desertSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.DESERT);
      if (z) {
         surfaceBuilder.addStructureStart(StructureFeatures.VILLAGE_DESERT);
         surfaceBuilder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }
      if (z2) {
         surfaceBuilder.addStructureStart(StructureFeatures.DESERT_PYRAMID);
      }
      if (z3) {
         BiomeDefaultFeatures.addFossilDecoration(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDesertLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDesertVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDesertExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addDesertExtraDecoration(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.DESERT).depth(f).scale(f2).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome plainsBiome(boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.plainsSpawns(builder);
      if (!z) {
         builder.setPlayerCanSpawn();
      }
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      if (!z) {
         surfaceBuilder.addStructureStart(StructureFeatures.VILLAGE_PLAINS).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addPlainGrass(surfaceBuilder);
      if (z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
      }
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addPlainVegetation(surfaceBuilder);
      if (z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
      }
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      if (z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      } else {
         BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.PLAINS).depth(0.125f).scale(0.05f).temperature(0.8f).downfall(0.4f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.8f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   private static Biome baseEndBiome(BiomeGenerationSettings.Builder builder) {
      MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.endSpawns(builder2);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.THEEND).depth(0.1f).scale(0.2f).temperature(0.5f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder2.build()).generationSettings(builder.build()).build();
   }

   public static Biome endBarrensBiome() {
      return baseEndBiome(new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END));
   }

   public static Biome theEndBiome() {
      return baseEndBiome(new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE));
   }

   public static Biome endMidlandsBiome() {
      return baseEndBiome(new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY));
   }

   public static Biome endHighlandsBiome() {
      return baseEndBiome(new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT));
   }

   public static Biome smallEndIslandsBiome() {
      return baseEndBiome(new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END).addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED));
   }

   public static Biome mushroomFieldsBiome(float f, float f2) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.mooshroomSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.MYCELIUM);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addMushroomFieldVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.MUSHROOM).depth(f).scale(f2).temperature(0.9f).downfall(1.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.9f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   private static Biome baseSavannaBiome(float f, float f2, float f3, boolean z, boolean z2, MobSpawnSettings.Builder builder) {
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(z2 ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS);
      if (!z && !z2) {
         surfaceBuilder.addStructureStart(StructureFeatures.VILLAGE_SAVANNA).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(z ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      if (!z2) {
         BiomeDefaultFeatures.addSavannaGrass(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      if (z2) {
         BiomeDefaultFeatures.addShatteredSavannaTrees(surfaceBuilder);
         BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
         BiomeDefaultFeatures.addShatteredSavannaGrass(surfaceBuilder);
      } else {
         BiomeDefaultFeatures.addSavannaTrees(surfaceBuilder);
         BiomeDefaultFeatures.addWarmFlowers(surfaceBuilder);
         BiomeDefaultFeatures.addSavannaExtraGrass(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.SAVANNA).depth(f).scale(f2).temperature(f3).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f3)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome savannaBiome(float f, float f2, float f3, boolean z, boolean z2) {
      return baseSavannaBiome(f, f2, f3, z, z2, savannaMobs());
   }

   private static MobSpawnSettings.Builder savannaMobs() {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
      BiomeDefaultFeatures.commonSpawns(builder);
      return builder;
   }

   public static Biome savanaPlateauBiome() {
      MobSpawnSettings.Builder savannaMobs = savannaMobs();
      savannaMobs.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
      return baseSavannaBiome(1.5f, 0.025f, 1.0f, true, false, savannaMobs);
   }

   private static Biome baseBadlandsBiome(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, float f, float f2, boolean z, boolean z2) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(configuredSurfaceBuilder);
      BiomeDefaultFeatures.addDefaultOverworldLandMesaStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(z ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addExtraGold(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      if (z2) {
         BiomeDefaultFeatures.addBadlandsTrees(surfaceBuilder);
      }
      BiomeDefaultFeatures.addBadlandGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addBadlandExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.MESA).depth(f).scale(f2).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0f)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome badlandsBiome(float f, float f2, boolean z) {
      return baseBadlandsBiome(SurfaceBuilders.BADLANDS, f, f2, z, false);
   }

   public static Biome woodedBadlandsPlateauBiome(float f, float f2) {
      return baseBadlandsBiome(SurfaceBuilders.WOODED_BADLANDS, f, f2, true, true);
   }

   public static Biome erodedBadlandsBiome() {
      return baseBadlandsBiome(SurfaceBuilders.ERODED_BADLANDS, 0.1f, 0.2f, true, false);
   }

   private static Biome baseOceanBiome(MobSpawnSettings.Builder builder, int i, int i2, boolean z, BiomeGenerationSettings.Builder builder2) {
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.OCEAN).depth(z ? -1.8f : -1.0f).scale(0.1f).temperature(0.5f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(i).waterFogColor(i2).fogColor(12638463).skyColor(calculateSkyColor(0.5f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
   }

   private static BiomeGenerationSettings.Builder baseOceanGeneration(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, boolean z, boolean z2, boolean z3) {
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(configuredSurfaceBuilder);
      ConfiguredStructureFeature<?, ?> configuredStructureFeature = z2 ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;
      if (z3) {
         if (z) {
            surfaceBuilder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
         }
         BiomeDefaultFeatures.addDefaultOverworldOceanStructures(surfaceBuilder);
         surfaceBuilder.addStructureStart(configuredStructureFeature);
      } else {
         surfaceBuilder.addStructureStart(configuredStructureFeature);
         if (z) {
            surfaceBuilder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
         }
         BiomeDefaultFeatures.addDefaultOverworldOceanStructures(surfaceBuilder);
      }
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
      BiomeDefaultFeatures.addOceanCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addWaterTrees(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      return surfaceBuilder;
   }

   public static Biome coldOceanBiome(boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(builder, 3, 4, 15);
      builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
      BiomeGenerationSettings.Builder baseOceanGeneration = baseOceanGeneration(SurfaceBuilders.GRASS, z, false, !z);
      baseOceanGeneration.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, z ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
      BiomeDefaultFeatures.addDefaultSeagrass(baseOceanGeneration);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(baseOceanGeneration);
      BiomeDefaultFeatures.addSurfaceFreezing(baseOceanGeneration);
      return baseOceanBiome(builder, 4020182, 329011, z, baseOceanGeneration);
   }

   public static Biome oceanBiome(boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(builder, 1, 4, 10);
      builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
      BiomeGenerationSettings.Builder baseOceanGeneration = baseOceanGeneration(SurfaceBuilders.GRASS, z, false, true);
      baseOceanGeneration.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, z ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
      BiomeDefaultFeatures.addDefaultSeagrass(baseOceanGeneration);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(baseOceanGeneration);
      BiomeDefaultFeatures.addSurfaceFreezing(baseOceanGeneration);
      return baseOceanBiome(builder, 4159204, 329011, z, baseOceanGeneration);
   }

   public static Biome lukeWarmOceanBiome(boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      if (z) {
         BiomeDefaultFeatures.oceanSpawns(builder, 8, 4, 8);
      } else {
         BiomeDefaultFeatures.oceanSpawns(builder, 10, 2, 15);
      }
      builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8)).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      BiomeGenerationSettings.Builder baseOceanGeneration = baseOceanGeneration(SurfaceBuilders.OCEAN_SAND, z, true, false);
      baseOceanGeneration.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, z ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
      if (z) {
         BiomeDefaultFeatures.addDefaultSeagrass(baseOceanGeneration);
      }
      BiomeDefaultFeatures.addLukeWarmKelp(baseOceanGeneration);
      BiomeDefaultFeatures.addSurfaceFreezing(baseOceanGeneration);
      return baseOceanBiome(builder, 4566514, 267827, z, baseOceanGeneration);
   }

   public static Biome warmOceanBiome() {
      MobSpawnSettings.Builder addSpawn = new MobSpawnSettings.Builder().addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
      BiomeDefaultFeatures.warmOceanSpawns(addSpawn, 10, 4);
      BiomeGenerationSettings.Builder addFeature = baseOceanGeneration(SurfaceBuilders.FULL_SAND, false, true, false).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
      BiomeDefaultFeatures.addSurfaceFreezing(addFeature);
      return baseOceanBiome(addSpawn, 4445678, 270131, false, addFeature);
   }

   public static Biome deepWarmOceanBiome() {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.warmOceanSpawns(builder, 5, 1);
      builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      BiomeGenerationSettings.Builder addFeature = baseOceanGeneration(SurfaceBuilders.FULL_SAND, true, true, false).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
      BiomeDefaultFeatures.addDefaultSeagrass(addFeature);
      BiomeDefaultFeatures.addSurfaceFreezing(addFeature);
      return baseOceanBiome(builder, 4445678, 270131, true, addFeature);
   }

   public static Biome frozenOceanBiome(boolean z) {
      MobSpawnSettings.Builder addSpawn = new MobSpawnSettings.Builder().addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      BiomeDefaultFeatures.commonSpawns(addSpawn);
      addSpawn.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      float f = z ? 0.5f : 0.0f;
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN);
      surfaceBuilder.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
      if (z) {
         surfaceBuilder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
      }
      BiomeDefaultFeatures.addDefaultOverworldOceanStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
      BiomeDefaultFeatures.addOceanCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addIcebergs(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addBlueIce(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addWaterTrees(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(z ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW).biomeCategory(Biome.BiomeCategory.OCEAN).depth(z ? -1.8f : -1.0f).scale(0.1f).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(addSpawn.build()).generationSettings(surfaceBuilder.build()).build();
   }

   private static Biome baseForestBiome(float f, float f2, boolean z, MobSpawnSettings.Builder builder) {
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      if (z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
      } else {
         BiomeDefaultFeatures.addForestFlowers(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      if (z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
         BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      } else {
         BiomeDefaultFeatures.addOtherBirchTrees(surfaceBuilder);
         BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
         BiomeDefaultFeatures.addForestGrass(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(f).scale(f2).temperature(0.7f).downfall(0.8f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   private static MobSpawnSettings.Builder defaultSpawns() {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      BiomeDefaultFeatures.commonSpawns(builder);
      return builder;
   }

   public static Biome forestBiome(float f, float f2) {
      return baseForestBiome(f, f2, false, defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4)).setPlayerCanSpawn());
   }

   public static Biome flowerForestBiome() {
      return baseForestBiome(0.1f, 0.4f, true, defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)));
   }

   public static Biome taigaBiome(float f, float f2, boolean z, boolean z2, boolean z3, boolean z4) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      if (!z && !z2) {
         builder.setPlayerCanSpawn();
      }
      BiomeDefaultFeatures.commonSpawns(builder);
      float f3 = z ? -0.5f : 0.25f;
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      if (z3) {
         surfaceBuilder.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
         surfaceBuilder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }
      if (z4) {
         surfaceBuilder.addStructureStart(StructureFeatures.IGLOO);
      }
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(z2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addFerns(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addTaigaTrees(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addTaigaGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      if (z) {
         BiomeDefaultFeatures.addBerryBushes(surfaceBuilder);
      } else {
         BiomeDefaultFeatures.addSparseBerryBushes(surfaceBuilder);
      }
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(z ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).depth(f).scale(f2).temperature(f3).downfall(z ? 0.4f : 0.8f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(z ? 4020182 : 4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f3)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome darkForestBiome(float f, float f2, boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      BiomeDefaultFeatures.commonSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      surfaceBuilder.addStructureStart(StructureFeatures.WOODLAND_MANSION);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, z ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
      BiomeDefaultFeatures.addForestFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addForestGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(f).scale(f2).temperature(0.7f).downfall(0.8f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7f)).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome swampBiome(float f, float f2, boolean z) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(builder);
      BiomeDefaultFeatures.commonSpawns(builder);
      builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.SWAMP);
      if (!z) {
         surfaceBuilder.addStructureStart(StructureFeatures.SWAMP_HUT);
      }
      surfaceBuilder.addStructureStart(StructureFeatures.MINESHAFT);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      if (!z) {
         BiomeDefaultFeatures.addFossilDecoration(surfaceBuilder);
      }
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addSwampClayDisk(surfaceBuilder);
      BiomeDefaultFeatures.addSwampVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addSwampExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      if (z) {
         BiomeDefaultFeatures.addFossilDecoration(surfaceBuilder);
      } else {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
      }
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.SWAMP).depth(f).scale(f2).temperature(0.8f).downfall(0.9f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8f)).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome tundraBiome(float f, float f2, boolean z, boolean z2) {
      MobSpawnSettings.Builder creatureGenerationProbability = new MobSpawnSettings.Builder().creatureGenerationProbability(0.07f);
      BiomeDefaultFeatures.snowySpawns(creatureGenerationProbability);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(z ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS);
      if (!z && !z2) {
         surfaceBuilder.addStructureStart(StructureFeatures.VILLAGE_SNOWY).addStructureStart(StructureFeatures.IGLOO);
      }
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      if (!z && !z2) {
         surfaceBuilder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }
      surfaceBuilder.addStructureStart(z2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      if (z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
         surfaceBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
      }
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addSnowyTrees(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.SNOW).biomeCategory(Biome.BiomeCategory.ICY).depth(f).scale(f2).temperature(0.0f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.0f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(creatureGenerationProbability.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome riverBiome(float f, float f2, float f3, int i, boolean z) {
      MobSpawnSettings.Builder addSpawn = new MobSpawnSettings.Builder().addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
      BiomeDefaultFeatures.commonSpawns(addSpawn);
      addSpawn.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, z ? 1 : 100, 1, 1));
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
      surfaceBuilder.addStructureStart(StructureFeatures.MINESHAFT);
      surfaceBuilder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addWaterTrees(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      if (!z) {
         surfaceBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
      }
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(z ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.RIVER).depth(f).scale(f2).temperature(f3).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(i).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f3)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(addSpawn.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome beachBiome(float f, float f2, float f3, float f4, int i, boolean z, boolean z2) {
      MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
      if (!z2 && !z) {
         builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
      }
      BiomeDefaultFeatures.commonSpawns(builder);
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(z2 ? SurfaceBuilders.STONE : SurfaceBuilders.DESERT);
      if (z2) {
         BiomeDefaultFeatures.addDefaultOverworldLandStructures(surfaceBuilder);
      } else {
         surfaceBuilder.addStructureStart(StructureFeatures.MINESHAFT);
         surfaceBuilder.addStructureStart(StructureFeatures.BURIED_TREASURE);
         surfaceBuilder.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
      }
      surfaceBuilder.addStructureStart(z2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultLakes(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultOres(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSoftDisks(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultFlowers(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultGrass(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultMushrooms(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(surfaceBuilder);
      BiomeDefaultFeatures.addDefaultSprings(surfaceBuilder);
      BiomeDefaultFeatures.addSurfaceFreezing(surfaceBuilder);
      return new Biome.BiomeBuilder().precipitation(z ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(z2 ? Biome.BiomeCategory.NONE : Biome.BiomeCategory.BEACH).depth(f).scale(f2).temperature(f3).downfall(f4).specialEffects(new BiomeSpecialEffects.Builder().waterColor(i).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f3)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome theVoidBiome() {
      BiomeGenerationSettings.Builder surfaceBuilder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.NOPE);
      surfaceBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NONE).depth(0.1f).scale(0.2f).temperature(0.5f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(surfaceBuilder.build()).build();
   }

   public static Biome netherWastesBiome() {
      MobSpawnSettings build = new MobSpawnSettings.Builder().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder addFeature = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.NETHER).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms(addFeature);
      addFeature.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
      BiomeDefaultFeatures.addNetherDefaultOres(addFeature);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1f).scale(0.2f).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(3344392).skyColor(calculateSkyColor(2.0f)).ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0d)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111d)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES)).build()).mobSpawnSettings(build).generationSettings(addFeature.build()).build();
   }

   public static Biome soulSandValleyBiome() {
      MobSpawnSettings build = new MobSpawnSettings.Builder().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 20, 5, 5)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.SKELETON, 0.7d, 0.15d).addMobCharge(EntityType.GHAST, 0.7d, 0.15d).addMobCharge(EntityType.ENDERMAN, 0.7d, 0.15d).addMobCharge(EntityType.STRIDER, 0.7d, 0.15d).build();
      BiomeGenerationSettings.Builder addFeature = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.NETHER_FOSSIL).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA).addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
      BiomeDefaultFeatures.addNetherDefaultOres(addFeature);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1f).scale(0.2f).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(1787717).skyColor(calculateSkyColor(2.0f)).ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625f)).ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0d)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111d)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY)).build()).mobSpawnSettings(build).generationSettings(addFeature.build()).build();
   }

   public static Biome basaltDeltasBiome() {
      MobSpawnSettings build = new MobSpawnSettings.Builder().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 40, 1, 1)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder addFeature = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.BASALT_DELTAS).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
      BiomeDefaultFeatures.addAncientDebris(addFeature);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1f).scale(0.2f).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(4341314).fogColor(6840176).skyColor(calculateSkyColor(2.0f)).ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334f)).ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0d)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111d)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS)).build()).mobSpawnSettings(build).generationSettings(addFeature.build()).build();
   }

   public static Biome crimsonForestBiome() {
      MobSpawnSettings build = new MobSpawnSettings.Builder().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HOGLIN, 9, 3, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 5, 3, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder addFeature = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms(addFeature);
      addFeature.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
      BiomeDefaultFeatures.addNetherDefaultOres(addFeature);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1f).scale(0.2f).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(3343107).skyColor(calculateSkyColor(2.0f)).ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025f)).ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0d)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111d)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST)).build()).mobSpawnSettings(build).generationSettings(addFeature.build()).build();
   }

   public static Biome warpedForestBiome() {
      MobSpawnSettings build = new MobSpawnSettings.Builder().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.ENDERMAN, 1.0d, 0.12d).build();
      BiomeGenerationSettings.Builder addFeature = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.WARPED_FOREST).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms(addFeature);
      addFeature.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
      BiomeDefaultFeatures.addNetherDefaultOres(addFeature);
      return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1f).scale(0.2f).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(1705242).skyColor(calculateSkyColor(2.0f)).ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428f)).ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0d)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111d)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST)).build()).mobSpawnSettings(build).generationSettings(addFeature.build()).build();
   }
}
