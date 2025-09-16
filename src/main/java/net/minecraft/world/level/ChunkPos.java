package net.minecraft.world.level;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ChunkPos.class */
public class ChunkPos {
    public static final long INVALID_CHUNK_POS = asLong(1875016, 1875016);

    /* renamed from: x */
    public final int x;

    /* renamed from: z */
    public final int z;

    public ChunkPos(int i, int i2) {
        this.x = i;
        this.z = i2;
    }

    public ChunkPos(BlockPos blockPos) {
        this.x = blockPos.getX() >> 4;
        this.z = blockPos.getZ() >> 4;
    }

    public ChunkPos(long j) {
        this.x = (int) j;
        this.z = (int) (j >> 32);
    }

    public long toLong() {
        return asLong(this.x, this.z);
    }

    public static long asLong(int i, int i2) {
        return (i & 4294967295L) | ((i2 & 4294967295L) << 32);
    }

    public static int getX(long j) {
        return (int) (j & 4294967295L);
    }

    public static int getZ(long j) {
        return (int) ((j >>> 32) & 4294967295L);
    }

    public int hashCode() {
        return ((1664525 * this.x) + 1013904223) ^ ((1664525 * (this.z ^ (-559038737))) + 1013904223);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChunkPos) {
            ChunkPos chunkPos = (ChunkPos) obj;
            return this.x == chunkPos.x && this.z == chunkPos.z;
        }
        return false;
    }

    public int getMinBlockX() {
        return this.x << 4;
    }

    public int getMinBlockZ() {
        return this.z << 4;
    }

    public int getMaxBlockX() {
        return (this.x << 4) + 15;
    }

    public int getMaxBlockZ() {
        return (this.z << 4) + 15;
    }

    public int getRegionX() {
        return this.x >> 5;
    }

    public int getRegionZ() {
        return this.z >> 5;
    }

    public int getRegionLocalX() {
        return this.x & 31;
    }

    public int getRegionLocalZ() {
        return this.z & 31;
    }

    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    public BlockPos getWorldPosition() {
        return new BlockPos(getMinBlockX(), 0, getMinBlockZ());
    }

    public int getChessboardDistance(ChunkPos chunkPos) {
        return Math.max(Math.abs(this.x - chunkPos.x), Math.abs(this.z - chunkPos.z));
    }

    public static Stream<ChunkPos> rangeClosed(ChunkPos chunkPos, int i) {
        return rangeClosed(new ChunkPos(chunkPos.x - i, chunkPos.z - i), new ChunkPos(chunkPos.x + i, chunkPos.z + i));
    }

    public static Stream<ChunkPos> rangeClosed(final ChunkPos chunkPos, final ChunkPos chunkPos2) {
        int abs = Math.abs(chunkPos.x - chunkPos2.x) + 1;
        int abs2 = Math.abs(chunkPos.z - chunkPos2.z) + 1;
        final int i = chunkPos.x < chunkPos2.x ? 1 : -1;
        final int i2 = chunkPos.z < chunkPos2.z ? 1 : -1;
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<ChunkPos>(abs * abs2, 64) { // from class: net.minecraft.world.level.ChunkPos.1

            @Nullable
            private ChunkPos pos;

            @Override // java.util.Spliterator
            public boolean tryAdvance(Consumer<? super ChunkPos> consumer) {
                if (this.pos == null) {
                    this.pos = chunkPos;
                } else {
                    int i3 = this.pos.x;
                    int i4 = this.pos.z;
                    if (i3 == chunkPos2.x) {
                        if (i4 == chunkPos2.z) {
                            return false;
                        }
                        this.pos = new ChunkPos(chunkPos.x, i4 + i2);
                    } else {
                        this.pos = new ChunkPos(i3 + i, i4);
                    }
                }
                consumer.accept(this.pos);
                return true;
            }
        }, false);
    }
}
