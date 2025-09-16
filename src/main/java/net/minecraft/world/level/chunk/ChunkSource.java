package net.minecraft.world.level.chunk;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/ChunkSource.class */
public abstract class ChunkSource implements LightChunkGetter, AutoCloseable {
    @Nullable
    public abstract ChunkAccess getChunk(int i, int i2, ChunkStatus chunkStatus, boolean z);

    public abstract String gatherStats();

    public abstract LevelLightEngine getLightEngine();

    @Nullable
    public LevelChunk getChunk(int i, int i2, boolean z) {
        return (LevelChunk) getChunk(i, i2, ChunkStatus.FULL, z);
    }

    @Nullable
    public LevelChunk getChunkNow(int i, int i2) {
        return getChunk(i, i2, false);
    }

    @Override // net.minecraft.world.level.chunk.LightChunkGetter
    @Nullable
    public BlockGetter getChunkForLighting(int i, int i2) {
        return getChunk(i, i2, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int i, int i2) {
        return getChunk(i, i2, ChunkStatus.FULL, false) != null;
    }

    public void close() throws IOException {
    }

    public void setSpawnSettings(boolean z, boolean z2) {
    }

    public void updateChunkForced(ChunkPos chunkPos, boolean z) {
    }

    public boolean isEntityTickingChunk(Entity entity) {
        return true;
    }

    public boolean isEntityTickingChunk(ChunkPos chunkPos) {
        return true;
    }

    public boolean isTickingChunk(BlockPos blockPos) {
        return true;
    }
}
