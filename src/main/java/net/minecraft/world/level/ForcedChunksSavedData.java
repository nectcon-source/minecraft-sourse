package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ForcedChunksSavedData.class */
public class ForcedChunksSavedData extends SavedData {
    private LongSet chunks;

    public ForcedChunksSavedData() {
        super("chunks");
        this.chunks = new LongOpenHashSet();
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public void load(CompoundTag compoundTag) {
        this.chunks = new LongOpenHashSet(compoundTag.getLongArray("Forced"));
    }

    @Override // net.minecraft.world.level.saveddata.SavedData
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLongArray("Forced", this.chunks.toLongArray());
        return compoundTag;
    }

    public LongSet getChunks() {
        return this.chunks;
    }
}
