package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.BiomeManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/FuzzyOffsetConstantColumnBiomeZoomer.class */
public enum FuzzyOffsetConstantColumnBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override // net.minecraft.world.level.biome.BiomeZoomer
    public Biome getBiome(long j, int i, int i2, int i3, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        return FuzzyOffsetBiomeZoomer.INSTANCE.getBiome(j, i, 0, i3, noiseBiomeSource);
    }
}
