package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/ChunkBiomeContainer.class */
public class ChunkBiomeContainer implements BiomeManager.NoiseBiomeSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int WIDTH_BITS = ((int) Math.round(Math.log(16.0d) / Math.log(2.0d))) - 2;
    private static final int HEIGHT_BITS = ((int) Math.round(Math.log(256.0d) / Math.log(2.0d))) - 2;
    public static final int BIOMES_SIZE = 1 << ((WIDTH_BITS + WIDTH_BITS) + HEIGHT_BITS);
    public static final int HORIZONTAL_MASK = (1 << WIDTH_BITS) - 1;
    public static final int VERTICAL_MASK = (1 << HEIGHT_BITS) - 1;
    private final IdMap<Biome> biomeRegistry;
    private final Biome[] biomes;

    public ChunkBiomeContainer(IdMap<Biome> idMap, Biome[] biomeArr) {
        this.biomeRegistry = idMap;
        this.biomes = biomeArr;
    }

    private ChunkBiomeContainer(IdMap<Biome> idMap) {
        this(idMap, new Biome[BIOMES_SIZE]);
    }

    public ChunkBiomeContainer(IdMap<Biome> idMap, int[] iArr) {
        this(idMap);
        for (int i = 0; i < this.biomes.length; i++) {
            int i2 = iArr[i];
            Biome byId = idMap.byId(i2);
            if (byId == null) {
                LOGGER.warn("Received invalid biome id: " + i2);
                this.biomes[i] = idMap.byId(0);
            } else {
                this.biomes[i] = byId;
            }
        }
    }

    public ChunkBiomeContainer(IdMap<Biome> idMap, ChunkPos chunkPos, BiomeSource biomeSource) {
        this(idMap);
        int minBlockX = chunkPos.getMinBlockX() >> 2;
        int minBlockZ = chunkPos.getMinBlockZ() >> 2;
        for (int i = 0; i < this.biomes.length; i++) {
            this.biomes[i] = biomeSource.getNoiseBiome(minBlockX + (i & HORIZONTAL_MASK), (i >> (WIDTH_BITS + WIDTH_BITS)) & VERTICAL_MASK, minBlockZ + ((i >> WIDTH_BITS) & HORIZONTAL_MASK));
        }
    }

    public ChunkBiomeContainer(IdMap<Biome> idMap, ChunkPos chunkPos, BiomeSource biomeSource, @Nullable int[] iArr) {
        this(idMap);
        int minBlockX = chunkPos.getMinBlockX() >> 2;
        int minBlockZ = chunkPos.getMinBlockZ() >> 2;
        if (iArr != null) {
            for (int i = 0; i < iArr.length; i++) {
                this.biomes[i] = idMap.byId(iArr[i]);
                if (this.biomes[i] == null) {
                    this.biomes[i] = biomeSource.getNoiseBiome(minBlockX + (i & HORIZONTAL_MASK), (i >> (WIDTH_BITS + WIDTH_BITS)) & VERTICAL_MASK, minBlockZ + ((i >> WIDTH_BITS) & HORIZONTAL_MASK));
                }
            }
            return;
        }
        for (int i2 = 0; i2 < this.biomes.length; i2++) {
            this.biomes[i2] = biomeSource.getNoiseBiome(minBlockX + (i2 & HORIZONTAL_MASK), (i2 >> (WIDTH_BITS + WIDTH_BITS)) & VERTICAL_MASK, minBlockZ + ((i2 >> WIDTH_BITS) & HORIZONTAL_MASK));
        }
    }

    public int[] writeBiomes() {
        int[] iArr = new int[this.biomes.length];
        for (int i = 0; i < this.biomes.length; i++) {
            iArr[i] = this.biomeRegistry.getId(this.biomes[i]);
        }
        return iArr;
    }

    @Override // net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource
    public Biome getNoiseBiome(int i, int i2, int i3) {
        return this.biomes[(Mth.clamp(i2, 0, VERTICAL_MASK) << (WIDTH_BITS + WIDTH_BITS)) | ((i3 & HORIZONTAL_MASK) << WIDTH_BITS) | (i & HORIZONTAL_MASK)];
    }
}
