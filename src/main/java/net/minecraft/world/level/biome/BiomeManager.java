package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeManager.class */
public class BiomeManager {
    private final NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;
    private final BiomeZoomer zoomer;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeManager$NoiseBiomeSource.class */
    public interface NoiseBiomeSource {
        Biome getNoiseBiome(int i, int i2, int i3);
    }

    public BiomeManager(NoiseBiomeSource noiseBiomeSource, long j, BiomeZoomer biomeZoomer) {
        this.noiseBiomeSource = noiseBiomeSource;
        this.biomeZoomSeed = j;
        this.zoomer = biomeZoomer;
    }

    public static long obfuscateSeed(long j) {
        return Hashing.sha256().hashLong(j).asLong();
    }

    public BiomeManager withDifferentSource(BiomeSource biomeSource) {
        return new BiomeManager(biomeSource, this.biomeZoomSeed, this.zoomer);
    }

    public Biome getBiome(BlockPos blockPos) {
        return this.zoomer.getBiome(this.biomeZoomSeed, blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.noiseBiomeSource);
    }

    public Biome getNoiseBiomeAtPosition(double d, double d2, double d3) {
        return getNoiseBiomeAtQuart(Mth.floor(d) >> 2, Mth.floor(d2) >> 2, Mth.floor(d3) >> 2);
    }

    public Biome getNoiseBiomeAtPosition(BlockPos blockPos) {
        return getNoiseBiomeAtQuart(blockPos.getX() >> 2, blockPos.getY() >> 2, blockPos.getZ() >> 2);
    }

    public Biome getNoiseBiomeAtQuart(int i, int i2, int i3) {
        return this.noiseBiomeSource.getNoiseBiome(i, i2, i3);
    }
}
