package net.minecraft.world.level.chunk.storage;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.OldDataLayer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/OldChunkStorage.class */
public class OldChunkStorage {
    public static OldLevelChunk load(CompoundTag compoundTag) {
        OldLevelChunk oldLevelChunk = new OldLevelChunk(compoundTag.getInt("xPos"), compoundTag.getInt("zPos"));
        oldLevelChunk.blocks = compoundTag.getByteArray("Blocks");
        oldLevelChunk.data = new OldDataLayer(compoundTag.getByteArray("Data"), 7);
        oldLevelChunk.skyLight = new OldDataLayer(compoundTag.getByteArray("SkyLight"), 7);
        oldLevelChunk.blockLight = new OldDataLayer(compoundTag.getByteArray("BlockLight"), 7);
        oldLevelChunk.heightmap = compoundTag.getByteArray("HeightMap");
        oldLevelChunk.terrainPopulated = compoundTag.getBoolean("TerrainPopulated");
        oldLevelChunk.entities = compoundTag.getList("Entities", 10);
        oldLevelChunk.blockEntities = compoundTag.getList("TileEntities", 10);
        oldLevelChunk.blockTicks = compoundTag.getList("TileTicks", 10);
        try {
            oldLevelChunk.lastUpdated = compoundTag.getLong("LastUpdate");
        } catch (ClassCastException e) {
            oldLevelChunk.lastUpdated = compoundTag.getInt("LastUpdate");
        }
        return oldLevelChunk;
    }

    public static void convertToAnvilFormat(RegistryAccess.RegistryHolder registryHolder, OldLevelChunk oldLevelChunk, CompoundTag compoundTag, BiomeSource biomeSource) {
        compoundTag.putInt("xPos", oldLevelChunk.x);
        compoundTag.putInt("zPos", oldLevelChunk.z);
        compoundTag.putLong("LastUpdate", oldLevelChunk.lastUpdated);
        int[] iArr = new int[oldLevelChunk.heightmap.length];
        for (int i = 0; i < oldLevelChunk.heightmap.length; i++) {
            iArr[i] = oldLevelChunk.heightmap[i];
        }
        compoundTag.putIntArray("HeightMap", iArr);
        compoundTag.putBoolean("TerrainPopulated", oldLevelChunk.terrainPopulated);
        ListTag listTag = new ListTag();
        for (int i2 = 0; i2 < 8; i2++) {
            boolean z = true;
            for (int i3 = 0; i3 < 16 && z; i3++) {
                for (int i4 = 0; i4 < 16 && z; i4++) {
                    int i5 = 0;
                    while (true) {
                        if (i5 < 16) {
                            if (oldLevelChunk.blocks[(i3 << 11) | (i5 << 7) | (i4 + (i2 << 4))] == 0) {
                                i5++;
                            } else {
                                z = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (!z) {
                byte[] bArr = new byte[4096];
                DataLayer dataLayer = new DataLayer();
                DataLayer dataLayer2 = new DataLayer();
                DataLayer dataLayer3 = new DataLayer();
                for (int i6 = 0; i6 < 16; i6++) {
                    for (int i7 = 0; i7 < 16; i7++) {
                        for (int i8 = 0; i8 < 16; i8++) {
                            bArr[(i7 << 8) | (i8 << 4) | i6] = (byte) (oldLevelChunk.blocks[(i6 << 11) | (i8 << 7) | (i7 + (i2 << 4))] & 255);
                            dataLayer.set(i6, i7, i8, oldLevelChunk.data.get(i6, i7 + (i2 << 4), i8));
                            dataLayer2.set(i6, i7, i8, oldLevelChunk.skyLight.get(i6, i7 + (i2 << 4), i8));
                            dataLayer3.set(i6, i7, i8, oldLevelChunk.blockLight.get(i6, i7 + (i2 << 4), i8));
                        }
                    }
                }
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putByte("Y", (byte) (i2 & 255));
                compoundTag2.putByteArray("Blocks", bArr);
                compoundTag2.putByteArray("Data", dataLayer.getData());
                compoundTag2.putByteArray("SkyLight", dataLayer2.getData());
                compoundTag2.putByteArray("BlockLight", dataLayer3.getData());
                listTag.add(compoundTag2);
            }
        }
        compoundTag.put("Sections", listTag);
        compoundTag.putIntArray("Biomes", new ChunkBiomeContainer(registryHolder.registryOrThrow(Registry.BIOME_REGISTRY), new ChunkPos(oldLevelChunk.x, oldLevelChunk.z), biomeSource).writeBiomes());
        compoundTag.put("Entities", oldLevelChunk.entities);
        compoundTag.put("TileEntities", oldLevelChunk.blockEntities);
        if (oldLevelChunk.blockTicks != null) {
            compoundTag.put("TileTicks", oldLevelChunk.blockTicks);
        }
        compoundTag.putBoolean("convertedFromAlphaFormat", true);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/OldChunkStorage$OldLevelChunk.class */
    public static class OldLevelChunk {
        public long lastUpdated;
        public boolean terrainPopulated;
        public byte[] heightmap;
        public OldDataLayer blockLight;
        public OldDataLayer skyLight;
        public OldDataLayer data;
        public byte[] blocks;
        public ListTag entities;
        public ListTag blockEntities;
        public ListTag blockTicks;

        /* renamed from: x */
        public final int x;

        /* renamed from: z */
        public final int z;

        public OldLevelChunk(int i, int i2) {
            this.x = i;
            this.z = i2;
        }
    }
}
