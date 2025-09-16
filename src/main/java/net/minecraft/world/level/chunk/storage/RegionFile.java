package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/RegionFile.class */
public class RegionFile implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ByteBuffer PADDING_BUFFER = ByteBuffer.allocateDirect(1);
    private final FileChannel file;
    private final Path externalFileDir;
    private final RegionFileVersion version;
    private final ByteBuffer header;
    private final IntBuffer offsets;
    private final IntBuffer timestamps;

    @VisibleForTesting
    protected final RegionBitmap usedSectors;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/RegionFile$CommitOp.class */
    interface CommitOp {
        void run() throws IOException;
    }

    public RegionFile(File file, File file2, boolean z) throws IOException {
        this(file.toPath(), file2.toPath(), RegionFileVersion.VERSION_DEFLATE, z);
    }

    public RegionFile(Path path, Path path2, RegionFileVersion regionFileVersion, boolean z) throws IOException {
        this.header = ByteBuffer.allocateDirect(8192);
        this.usedSectors = new RegionBitmap();
        this.version = regionFileVersion;
        if (!Files.isDirectory(path2, new LinkOption[0])) {
            throw new IllegalArgumentException("Expected directory, got " + path2.toAbsolutePath());
        }
        this.externalFileDir = path2;
        this.offsets = this.header.asIntBuffer();
        this.offsets.limit(1024);
        this.header.position(4096);
        this.timestamps = this.header.asIntBuffer();
        if (z) {
            this.file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
        } else {
            this.file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        }
        this.usedSectors.force(0, 2);
        this.header.position(0);
        int read = this.file.read(this.header, 0L);
        if (read != -1) {
            if (read != 8192) {
                LOGGER.warn("Region file {} has truncated header: {}", path, Integer.valueOf(read));
            }
            long size = Files.size(path);
            for (int i = 0; i < 1024; i++) {
                int i2 = this.offsets.get(i);
                if (i2 != 0) {
                    int sectorNumber = getSectorNumber(i2);
                    int numSectors = getNumSectors(i2);
                    if (sectorNumber < 2) {
                        LOGGER.warn("Region file {} has invalid sector at index: {}; sector {} overlaps with header", path, Integer.valueOf(i), Integer.valueOf(sectorNumber));
                        this.offsets.put(i, 0);
                    } else if (numSectors == 0) {
                        LOGGER.warn("Region file {} has an invalid sector at index: {}; size has to be > 0", path, Integer.valueOf(i));
                        this.offsets.put(i, 0);
                    } else if (sectorNumber * 4096 > size) {
                        LOGGER.warn("Region file {} has an invalid sector at index: {}; sector {} is out of bounds", path, Integer.valueOf(i), Integer.valueOf(sectorNumber));
                        this.offsets.put(i, 0);
                    } else {
                        this.usedSectors.force(sectorNumber, numSectors);
                    }
                }
            }
        }
    }

    private Path getExternalChunkPath(ChunkPos chunkPos) {
        return this.externalFileDir.resolve("c." + chunkPos.x + "." + chunkPos.z + ".mcc");
    }

    @Nullable
    public synchronized DataInputStream getChunkDataInputStream(ChunkPos chunkPos) throws IOException {
        int offset = getOffset(chunkPos);
        if (offset == 0) {
            return null;
        }
        int sectorNumber = getSectorNumber(offset);
        int numSectors = getNumSectors(offset) * 4096;
        ByteBuffer allocate = ByteBuffer.allocate(numSectors);
        this.file.read(allocate, sectorNumber * 4096);
        allocate.flip();
        if (allocate.remaining() < 5) {
            LOGGER.error("Chunk {} header is truncated: expected {} but read {}", chunkPos, Integer.valueOf(numSectors), Integer.valueOf(allocate.remaining()));
            return null;
        }
        int i = allocate.getInt();
        byte b = allocate.get();
        if (i == 0) {
            LOGGER.warn("Chunk {} is allocated, but stream is missing", chunkPos);
            return null;
        }
        int i2 = i - 1;
        if (isExternalStreamChunk(b)) {
            if (i2 != 0) {
                LOGGER.warn("Chunk has both internal and external streams");
            }
            return createExternalChunkInputStream(chunkPos, getExternalChunkVersion(b));
        }
        if (i2 > allocate.remaining()) {
            LOGGER.error("Chunk {} stream is truncated: expected {} but read {}", chunkPos, Integer.valueOf(i2), Integer.valueOf(allocate.remaining()));
            return null;
        }
        if (i2 < 0) {
            LOGGER.error("Declared size {} of chunk {} is negative", Integer.valueOf(i), chunkPos);
            return null;
        }
        return createChunkInputStream(chunkPos, b, createStream(allocate, i2));
    }

    private static boolean isExternalStreamChunk(byte b) {
        return (b & 128) != 0;
    }

    private static byte getExternalChunkVersion(byte b) {
        return (byte) (b & (-129));
    }

    @Nullable
    private DataInputStream createChunkInputStream(ChunkPos chunkPos, byte b, InputStream inputStream) throws IOException {
        RegionFileVersion fromId = RegionFileVersion.fromId(b);
        if (fromId == null) {
            LOGGER.error("Chunk {} has invalid chunk stream version {}", chunkPos, Byte.valueOf(b));
            return null;
        }
        return new DataInputStream(new BufferedInputStream(fromId.wrap(inputStream)));
    }

    @Nullable
    private DataInputStream createExternalChunkInputStream(ChunkPos chunkPos, byte b) throws IOException {
        Path externalChunkPath = getExternalChunkPath(chunkPos);
        if (!Files.isRegularFile(externalChunkPath, new LinkOption[0])) {
            LOGGER.error("External chunk path {} is not file", externalChunkPath);
            return null;
        }
        return createChunkInputStream(chunkPos, b, Files.newInputStream(externalChunkPath, new OpenOption[0]));
    }

    private static ByteArrayInputStream createStream(ByteBuffer byteBuffer, int i) {
        return new ByteArrayInputStream(byteBuffer.array(), byteBuffer.position(), i);
    }

    private int packSectorOffset(int i, int i2) {
        return (i << 8) | i2;
    }

    private static int getNumSectors(int i) {
        return i & 255;
    }

    private static int getSectorNumber(int i) {
        return (i >> 8) & 16777215;
    }

    private static int sizeToSectors(int i) {
        return ((i + 4096) - 1) / 4096;
    }

    public boolean doesChunkExist(ChunkPos chunkPos) {
        int i;
        int offset = getOffset(chunkPos);
        if (offset == 0) {
            return false;
        }
        int sectorNumber = getSectorNumber(offset);
        int numSectors = getNumSectors(offset);
        ByteBuffer allocate = ByteBuffer.allocate(5);
        try {
            this.file.read(allocate, sectorNumber * 4096);
            allocate.flip();
            if (allocate.remaining() != 5) {
                return false;
            }
            int i2 = allocate.getInt();
            byte b = allocate.get();
            if (isExternalStreamChunk(b)) {
                if (!RegionFileVersion.isValidVersion(getExternalChunkVersion(b)) || !Files.isRegularFile(getExternalChunkPath(chunkPos), new LinkOption[0])) {
                    return false;
                }
                return true;
            }
            if (!RegionFileVersion.isValidVersion(b) || i2 == 0 || (i = i2 - 1) < 0 || i > 4096 * numSectors) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public DataOutputStream getChunkDataOutputStream(ChunkPos chunkPos) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(this.version.wrap(new ChunkBuffer(chunkPos))));
    }

    public void flush() throws IOException {
        this.file.force(true);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/RegionFile$ChunkBuffer.class */
    class ChunkBuffer extends ByteArrayOutputStream {
        private final ChunkPos pos;

        public ChunkBuffer(ChunkPos chunkPos) {
            super(8096);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(RegionFile.this.version.getId());
            this.pos = chunkPos;
        }

        @Override // java.io.ByteArrayOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            ByteBuffer wrap = ByteBuffer.wrap(this.buf, 0, this.count);
            wrap.putInt(0, (this.count - 5) + 1);
            RegionFile.this.write(this.pos, wrap);
        }
    }

    protected synchronized void write(ChunkPos chunkPos, ByteBuffer byteBuffer) throws IOException {
        int allocate;
        CommitOp commitOp;
        int offsetIndex = getOffsetIndex(chunkPos);
        int i = this.offsets.get(offsetIndex);
        int sectorNumber = getSectorNumber(i);
        int numSectors = getNumSectors(i);
        int remaining = byteBuffer.remaining();
        int sizeToSectors = sizeToSectors(remaining);
        if (sizeToSectors >= 256) {
            Path externalChunkPath = getExternalChunkPath(chunkPos);
            LOGGER.warn("Saving oversized chunk {} ({} bytes} to external file {}", chunkPos, Integer.valueOf(remaining), externalChunkPath);
            sizeToSectors = 1;
            allocate = this.usedSectors.allocate(1);
            commitOp = writeToExternalFile(externalChunkPath, byteBuffer);
            this.file.write(createExternalStub(), allocate * 4096);
        } else {
            allocate = this.usedSectors.allocate(sizeToSectors);
            commitOp = () -> {
                Files.deleteIfExists(getExternalChunkPath(chunkPos));
            };
            this.file.write(byteBuffer, allocate * 4096);
        }
        int epochMillis = (int) (Util.getEpochMillis() / 1000);
        this.offsets.put(offsetIndex, packSectorOffset(allocate, sizeToSectors));
        this.timestamps.put(offsetIndex, epochMillis);
        writeHeader();
        commitOp.run();
        if (sectorNumber != 0) {
            this.usedSectors.free(sectorNumber, numSectors);
        }
    }

    private ByteBuffer createExternalStub() {
        ByteBuffer allocate = ByteBuffer.allocate(5);
        allocate.putInt(1);
        allocate.put((byte) (this.version.getId() | 128));
        allocate.flip();
        return allocate;
    }

    private CommitOp writeToExternalFile(Path path, ByteBuffer byteBuffer) throws IOException {
        Path createTempFile = Files.createTempFile(this.externalFileDir, "tmp", null, new FileAttribute[0]);
        FileChannel open = FileChannel.open(createTempFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Throwable th = null;
        try {
            try {
                byteBuffer.position(5);
                open.write(byteBuffer);
                if (open != null) {
                    if (0 != 0) {
                        try {
                            open.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    } else {
                        open.close();
                    }
                }
                return () -> {
                    Files.move(createTempFile, path, StandardCopyOption.REPLACE_EXISTING);
                };
            } finally {
            }
        } catch (Throwable th3) {
            if (open != null) {
                if (th != null) {
                    try {
                        open.close();
                    } catch (Throwable th4) {
                        th.addSuppressed(th4);
                    }
                } else {
                    open.close();
                }
            }
            throw th3;
        }
    }

    private void writeHeader() throws IOException {
        this.header.position(0);
        this.file.write(this.header, 0L);
    }

    private int getOffset(ChunkPos chunkPos) {
        return this.offsets.get(getOffsetIndex(chunkPos));
    }

    public boolean hasChunk(ChunkPos chunkPos) {
        return getOffset(chunkPos) != 0;
    }

    private static int getOffsetIndex(ChunkPos chunkPos) {
        return chunkPos.getRegionLocalX() + (chunkPos.getRegionLocalZ() * 32);
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            padToFullSector();
            try {
                this.file.force(true);
            } finally {
            }
        } catch (Throwable th) {
            try {
                this.file.force(true);
                throw th;
            } finally {
            }
        }
    }

    private void padToFullSector() throws IOException {
        int var1 = (int)this.file.size();
        int var2 = sizeToSectors(var1) * 4096;
        if (var1 != var2) {
            ByteBuffer var3 = PADDING_BUFFER.duplicate();
            var3.position(0);
            this.file.write(var3, (long)(var2 - 1));
        }
    }
}
