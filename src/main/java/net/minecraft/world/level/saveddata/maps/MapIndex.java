package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/saveddata/maps/MapIndex.class */
public class MapIndex extends SavedData {
    private final Object2IntMap<String> usedAuxIds;

    public MapIndex() {
        super("idcounts");
        this.usedAuxIds = new Object2IntOpenHashMap();
        this.usedAuxIds.defaultReturnValue(-1);
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public void load(CompoundTag compoundTag) {
        this.usedAuxIds.clear();
        for (String str : compoundTag.getAllKeys()) {
            if (compoundTag.contains(str, 99)) {
                this.usedAuxIds.put(str, compoundTag.getInt(str));
            }
        }
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public CompoundTag save(CompoundTag compoundTag) {
        ObjectIterator it = this.usedAuxIds.object2IntEntrySet().iterator();
        while (it.hasNext()) {
            Object2IntMap.Entry<String> entry = (Object2IntMap.Entry) it.next();
            compoundTag.putInt( entry.getKey(), entry.getIntValue());
        }
        return compoundTag;
    }

    public int getFreeAuxValueForMap() {
        int i = this.usedAuxIds.getInt("map") + 1;
        this.usedAuxIds.put("map", i);
        setDirty();
        return i;
    }
}
