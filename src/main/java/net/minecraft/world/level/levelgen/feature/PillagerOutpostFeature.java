package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/PillagerOutpostFeature.class */
public class PillagerOutpostFeature extends JigsawFeature {
    private static final List<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 0, true, true);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return OUTPOST_ENEMIES;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, JigsawConfiguration jigsawConfiguration) {
        worldgenRandom.setSeed(((i >> 4) ^ ((i2 >> 4) << 4)) ^ j);
        worldgenRandom.nextInt();
        return worldgenRandom.nextInt(5) == 0 && !isNearVillage(chunkGenerator, j, worldgenRandom, i, i2);
    }

    private boolean isNearVillage(ChunkGenerator chunkGenerator, long j, WorldgenRandom worldgenRandom, int i, int i2) {
        StructureFeatureConfiguration config = chunkGenerator.getSettings().getConfig(StructureFeature.VILLAGE);
        if (config == null) {
            return false;
        }
        for (int i3 = i - 10; i3 <= i + 10; i3++) {
            for (int i4 = i2 - 10; i4 <= i2 + 10; i4++) {
                ChunkPos potentialFeatureChunk = StructureFeature.VILLAGE.getPotentialFeatureChunk(config, j, worldgenRandom, i3, i4);
                if (i3 == potentialFeatureChunk.x && i4 == potentialFeatureChunk.z) {
                    return true;
                }
            }
        }
        return false;
    }
}
