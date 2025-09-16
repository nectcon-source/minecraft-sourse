package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/ChunkStorage.class */
public class ChunkStorage implements AutoCloseable {
    private final IOWorker worker;
    protected final DataFixer fixerUpper;

    @Nullable
    private LegacyStructureDataHandler legacyStructureHandler;

    public ChunkStorage(File file, DataFixer dataFixer, boolean z) {
        this.fixerUpper = dataFixer;
        this.worker = new IOWorker(file, z, "chunk");
    }

    public CompoundTag upgradeChunkTag(ResourceKey<Level> resourceKey, Supplier<DimensionDataStorage> supplier, CompoundTag compoundTag) {
        int version = getVersion(compoundTag);
        if (version < 1493) {
            compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, version, 1493);
            if (compoundTag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
                if (this.legacyStructureHandler == null) {
                    this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(resourceKey, supplier.get());
                }
                compoundTag = this.legacyStructureHandler.updateFromLegacy(compoundTag);
            }
        }
        CompoundTag update = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, Math.max(1493, version));
        if (version < SharedConstants.getCurrentVersion().getWorldVersion()) {
            update.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        }
        return update;
    }

    public static int getVersion(CompoundTag compoundTag) {
        if (compoundTag.contains("DataVersion", 99)) {
            return compoundTag.getInt("DataVersion");
        }
        return -1;
    }

    @Nullable
    public CompoundTag read(ChunkPos chunkPos) throws IOException {
        return this.worker.load(chunkPos);
    }

    public void write(ChunkPos chunkPos, CompoundTag compoundTag) {
        this.worker.store(chunkPos, compoundTag);
        if (this.legacyStructureHandler != null) {
            this.legacyStructureHandler.removeIndex(chunkPos.toLong());
        }
    }

    public void flushWorker() {
        this.worker.synchronize().join();
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        this.worker.close();
    }
}
