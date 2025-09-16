package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/RegionFileStorage.class */
public final class RegionFileStorage implements AutoCloseable {
    private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
    private final File folder;
    private final boolean sync;

    RegionFileStorage(File file, boolean z) {
        this.folder = file;
        this.sync = z;
    }

    private RegionFile getRegionFile(ChunkPos chunkPos) throws IOException {
        long asLong = ChunkPos.asLong(chunkPos.getRegionX(), chunkPos.getRegionZ());
        RegionFile regionFile =  this.regionCache.getAndMoveToFirst(asLong);
        if (regionFile != null) {
            return regionFile;
        }
        if (this.regionCache.size() >= 256) {
             this.regionCache.removeLast().close();
        }
        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }
        RegionFile regionFile2 = new RegionFile(new File(this.folder, "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca"), this.folder, this.sync);
        this.regionCache.putAndMoveToFirst(asLong, regionFile2);
        return regionFile2;
    }

    @Nullable
    public CompoundTag read(ChunkPos chunkPos) throws IOException {
        DataInputStream chunkDataInputStream = getRegionFile(chunkPos).getChunkDataInputStream(chunkPos);
        Throwable th = null;
        if (chunkDataInputStream != null) {
            try {
                try {
                    CompoundTag read = NbtIo.read(chunkDataInputStream);
                    if (chunkDataInputStream != null) {
                        if (0 != 0) {
                            try {
                                chunkDataInputStream.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        } else {
                            chunkDataInputStream.close();
                        }
                    }
                    return read;
                } finally {
                }
            } catch (Throwable th3) {
                if (chunkDataInputStream != null) {
                    if (th != null) {
                        try {
                            chunkDataInputStream.close();
                        } catch (Throwable th4) {
                            th.addSuppressed(th4);
                        }
                    } else {
                        chunkDataInputStream.close();
                    }
                }
                throw th3;
            }
        }
        if (chunkDataInputStream != null) {
            if (0 != 0) {
                try {
                    chunkDataInputStream.close();
                } catch (Throwable th5) {
                    th.addSuppressed(th5);
                }
            } else {
                chunkDataInputStream.close();
            }
        }
        return null;
    }

    protected void write(ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        DataOutputStream chunkDataOutputStream = getRegionFile(chunkPos).getChunkDataOutputStream(chunkPos);
        Throwable th = null;
        try {
            try {
                NbtIo.write(compoundTag, chunkDataOutputStream);
                if (chunkDataOutputStream != null) {
                    if (0 != 0) {
                        try {
                            chunkDataOutputStream.close();
                            return;
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                            return;
                        }
                    }
                    chunkDataOutputStream.close();
                }
            } catch (Throwable th3) {
                th = th3;
                throw th3;
            }
        } catch (Throwable th4) {
            if (chunkDataOutputStream != null) {
                if (th != null) {
                    try {
                        chunkDataOutputStream.close();
                    } catch (Throwable th5) {
                        th.addSuppressed(th5);
                    }
                } else {
                    chunkDataOutputStream.close();
                }
            }
            throw th4;
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        ExceptionCollector<IOException> exceptionCollector = new ExceptionCollector<>();
        ObjectIterator it = this.regionCache.values().iterator();
        while (it.hasNext()) {
            try {
                ((RegionFile) it.next()).close();
            } catch (IOException e) {
                exceptionCollector.add(e);
            }
        }
        exceptionCollector.throwIfPresent();
    }

    public void flush() throws IOException {
        ObjectIterator it = this.regionCache.values().iterator();
        while (it.hasNext()) {
            ((RegionFile) it.next()).flush();
        }
    }
}
