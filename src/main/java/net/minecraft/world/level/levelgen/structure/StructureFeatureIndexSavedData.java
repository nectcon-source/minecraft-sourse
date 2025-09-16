package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StructureFeatureIndexSavedData.class */
public class StructureFeatureIndexSavedData extends SavedData {
    private LongSet all;
    private LongSet remaining;

    public StructureFeatureIndexSavedData(String str) {
        super(str);
        this.all = new LongOpenHashSet();
        this.remaining = new LongOpenHashSet();
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public void load(CompoundTag compoundTag) {
        this.all = new LongOpenHashSet(compoundTag.getLongArray("All"));
        this.remaining = new LongOpenHashSet(compoundTag.getLongArray("Remaining"));
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLongArray("All", this.all.toLongArray());
        compoundTag.putLongArray("Remaining", this.remaining.toLongArray());
        return compoundTag;
    }

    public void addIndex(long j) {
        this.all.add(j);
        this.remaining.add(j);
    }

    public boolean hasStartIndex(long j) {
        return this.all.contains(j);
    }

    public boolean hasUnhandledIndex(long j) {
        return this.remaining.contains(j);
    }

    public void removeIndex(long j) {
        this.remaining.remove(j);
    }

    public LongSet getAll() {
        return this.all;
    }
}
