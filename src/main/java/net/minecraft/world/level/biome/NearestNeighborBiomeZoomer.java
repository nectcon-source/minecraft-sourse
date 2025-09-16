package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.BiomeManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/NearestNeighborBiomeZoomer.class */
public enum NearestNeighborBiomeZoomer implements BiomeZoomer {
    INSTANCE;

    @Override // net.minecraft.world.level.biome.BiomeZoomer
    public Biome getBiome(long j, int i, int i2, int i3, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        return noiseBiomeSource.getNoiseBiome(i >> 2, i2 >> 2, i3 >> 2);
    }
}
