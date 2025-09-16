package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.DimensionDataStorage;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/LegacyStructureDataHandler.class */
public class LegacyStructureDataHandler {
    private static final Map<String, String> CURRENT_TO_LEGACY_MAP = (Map) Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Village", "Village");
        hashMap.put("Mineshaft", "Mineshaft");
        hashMap.put("Mansion", "Mansion");
        hashMap.put("Igloo", "Temple");
        hashMap.put("Desert_Pyramid", "Temple");
        hashMap.put("Jungle_Pyramid", "Temple");
        hashMap.put("Swamp_Hut", "Temple");
        hashMap.put("Stronghold", "Stronghold");
        hashMap.put("Monument", "Monument");
        hashMap.put("Fortress", "Fortress");
        hashMap.put("EndCity", "EndCity");
    });
    private static final Map<String, String> LEGACY_TO_CURRENT_MAP = (Map) Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Iglu", "Igloo");
        hashMap.put("TeDP", "Desert_Pyramid");
        hashMap.put("TeJP", "Jungle_Pyramid");
        hashMap.put("TeSH", "Swamp_Hut");
    });
    private final boolean hasLegacyData;
    private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
    private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
    private final List<String> legacyKeys;
    private final List<String> currentKeys;

    public LegacyStructureDataHandler(@Nullable DimensionDataStorage dimensionDataStorage, List<String> list, List<String> list2) {
        this.legacyKeys = list;
        this.currentKeys = list2;
        populateCaches(dimensionDataStorage);
        boolean z = false;
        Iterator<String> it = this.currentKeys.iterator();
        while (it.hasNext()) {
            z |= this.dataMap.get(it.next()) != null;
        }
        this.hasLegacyData = z;
    }

    public void removeIndex(long j) {
        Iterator<String> it = this.legacyKeys.iterator();
        while (it.hasNext()) {
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = this.indexMap.get(it.next());
            if (structureFeatureIndexSavedData != null && structureFeatureIndexSavedData.hasUnhandledIndex(j)) {
                structureFeatureIndexSavedData.removeIndex(j);
                structureFeatureIndexSavedData.setDirty();
            }
        }
    }

    public CompoundTag updateFromLegacy(CompoundTag compoundTag) {
        CompoundTag compound = compoundTag.getCompound("Level");
        ChunkPos chunkPos = new ChunkPos(compound.getInt("xPos"), compound.getInt("zPos"));
        if (isUnhandledStructureStart(chunkPos.x, chunkPos.z)) {
            compoundTag = updateStructureStart(compoundTag, chunkPos);
        }
        CompoundTag compound2 = compound.getCompound("Structures");
        CompoundTag compound3 = compound2.getCompound("References");
        for (String str : this.currentKeys) {
            StructureFeature<?> structureFeature = (StructureFeature) StructureFeature.STRUCTURES_REGISTRY.get(str.toLowerCase(Locale.ROOT));
            if (!compound3.contains(str, 12) && structureFeature != null) {
                LongArrayList longArrayList = new LongArrayList();
                for (int i = chunkPos.x - 8; i <= chunkPos.x + 8; i++) {
                    for (int i2 = chunkPos.z - 8; i2 <= chunkPos.z + 8; i2++) {
                        if (hasLegacyStart(i, i2, str)) {
                            longArrayList.add(ChunkPos.asLong(i, i2));
                        }
                    }
                }
                compound3.putLongArray(str, (List<Long>) longArrayList);
            }
        }
        compound2.put("References", compound3);
        compound.put("Structures", compound2);
        compoundTag.put("Level", compound);
        return compoundTag;
    }

    private boolean hasLegacyStart(int i, int i2, String str) {
        if (this.hasLegacyData && this.dataMap.get(str) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(str)).hasStartIndex(ChunkPos.asLong(i, i2))) {
            return true;
        }
        return false;
    }

    private boolean isUnhandledStructureStart(int i, int i2) {
        if (!this.hasLegacyData) {
            return false;
        }
        for (String str : this.currentKeys) {
            if (this.dataMap.get(str) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(str)).hasUnhandledIndex(ChunkPos.asLong(i, i2))) {
                return true;
            }
        }
        return false;
    }

    private CompoundTag updateStructureStart(CompoundTag compoundTag, ChunkPos chunkPos) {
        CompoundTag compoundTag2;
        CompoundTag compound = compoundTag.getCompound("Level");
        CompoundTag compound2 = compound.getCompound("Structures");
        CompoundTag compound3 = compound2.getCompound("Starts");
        for (String str : this.currentKeys) {
            Long2ObjectMap<CompoundTag> long2ObjectMap = this.dataMap.get(str);
            if (long2ObjectMap != null) {
                long j = chunkPos.toLong();
                if (this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(str)).hasUnhandledIndex(j) && (compoundTag2 = (CompoundTag) long2ObjectMap.get(j)) != null) {
                    compound3.put(str, compoundTag2);
                }
            }
        }
        compound2.put("Starts", compound3);
        compound.put("Structures", compound2);
        compoundTag.put("Level", compound);
        return compoundTag;
    }

    private void populateCaches(@Nullable DimensionDataStorage dimensionDataStorage) {
        if (dimensionDataStorage == null) {
            return;
        }
        for (String str : this.legacyKeys) {
            CompoundTag compoundTag = new CompoundTag();
            try {
                compoundTag = dimensionDataStorage.readTagFromDisk(str, 1493).getCompound("data").getCompound("Features");
            } catch (IOException e) {
            }
            if (!compoundTag.isEmpty()) {
                Iterator<String> it = compoundTag.getAllKeys().iterator();
                while (it.hasNext()) {
                    CompoundTag compound = compoundTag.getCompound(it.next());
                    long asLong = ChunkPos.asLong(compound.getInt("ChunkX"), compound.getInt("ChunkZ"));
                    ListTag list = compound.getList("Children", 10);
                    if (!list.isEmpty()) {
                        String str2 = LEGACY_TO_CURRENT_MAP.get(list.getCompound(0).getString("id"));
                        if (str2 != null) {
                            compound.putString("id", str2);
                        }
                    }
                    this.dataMap.computeIfAbsent(compound.getString("id"), str3 -> {
                        return new Long2ObjectOpenHashMap();
                    }).put(asLong, compound);
                }
                String str4 = str + "_index";
                StructureFeatureIndexSavedData structureFeatureIndexSavedData = (StructureFeatureIndexSavedData) dimensionDataStorage.computeIfAbsent(() -> {
                    return new StructureFeatureIndexSavedData(str4);
                }, str4);
                if (structureFeatureIndexSavedData.getAll().isEmpty()) {
                    StructureFeatureIndexSavedData structureFeatureIndexSavedData2 = new StructureFeatureIndexSavedData(str4);
                    this.indexMap.put(str, structureFeatureIndexSavedData2);
                    Iterator<String> it2 = compoundTag.getAllKeys().iterator();
                    while (it2.hasNext()) {
                        CompoundTag compound2 = compoundTag.getCompound(it2.next());
                        structureFeatureIndexSavedData2.addIndex(ChunkPos.asLong(compound2.getInt("ChunkX"), compound2.getInt("ChunkZ")));
                    }
                    structureFeatureIndexSavedData2.setDirty();
                } else {
                    this.indexMap.put(str, structureFeatureIndexSavedData);
                }
            }
        }
    }

    public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourceKey, @Nullable DimensionDataStorage dimensionDataStorage) {
        if (resourceKey == Level.OVERWORLD) {
            return new LegacyStructureDataHandler(dimensionDataStorage, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
        }
        if (resourceKey == Level.NETHER) {
            ImmutableList of = ImmutableList.of("Fortress");
            return new LegacyStructureDataHandler(dimensionDataStorage, of, of);
        }
        if (resourceKey == Level.END) {
            ImmutableList of2 = ImmutableList.of("EndCity");
            return new LegacyStructureDataHandler(dimensionDataStorage, of2, of2);
        }
        throw new RuntimeException(String.format("Unknown dimension type : %s", resourceKey));
    }
}
