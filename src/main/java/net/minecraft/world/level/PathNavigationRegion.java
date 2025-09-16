package net.minecraft.world.level;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/PathNavigationRegion.class */
public class PathNavigationRegion implements BlockGetter, CollisionGetter {
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;

    public PathNavigationRegion(Level level, BlockPos blockPos, BlockPos blockPos2) {
        this.level = level;
        this.centerX = blockPos.getX() >> 4;
        this.centerZ = blockPos.getZ() >> 4;
        int x = blockPos2.getX() >> 4;
        int z = blockPos2.getZ() >> 4;
        this.chunks = new ChunkAccess[(x - this.centerX) + 1][(z - this.centerZ) + 1];
        ChunkSource chunkSource = level.getChunkSource();
        this.allEmpty = true;
        for (int i = this.centerX; i <= x; i++) {
            for (int i2 = this.centerZ; i2 <= z; i2++) {
                this.chunks[i - this.centerX][i2 - this.centerZ] = chunkSource.getChunkNow(i, i2);
            }
        }
        for (int x2 = blockPos.getX() >> 4; x2 <= (blockPos2.getX() >> 4); x2++) {
            for (int z2 = blockPos.getZ() >> 4; z2 <= (blockPos2.getZ() >> 4); z2++) {
                ChunkAccess chunkAccess = this.chunks[x2 - this.centerX][z2 - this.centerZ];
                if (chunkAccess != null && !chunkAccess.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) {
                    this.allEmpty = false;
                    return;
                }
            }
        }
    }

    private ChunkAccess getChunk(BlockPos blockPos) {
        return getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    private ChunkAccess getChunk(int i, int i2) {
        int i3 = i - this.centerX;
        int i4 = i2 - this.centerZ;
        if (i3 < 0 || i3 >= this.chunks.length || i4 < 0 || i4 >= this.chunks[i3].length) {
            return new EmptyLevelChunk(this.level, new ChunkPos(i, i2));
        }
        ChunkAccess chunkAccess = this.chunks[i3][i4];
        return chunkAccess != null ? chunkAccess : new EmptyLevelChunk(this.level, new ChunkPos(i, i2));
    }

    @Override // net.minecraft.world.level.CollisionGetter
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override // net.minecraft.world.level.CollisionGetter
    public BlockGetter getChunkForCollisions(int i, int i2) {
        return getChunk(i, i2);
    }

    @Override // net.minecraft.world.level.BlockGetter
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return getChunk(blockPos).getBlockEntity(blockPos);
    }

    @Override // net.minecraft.world.level.BlockGetter
    public BlockState getBlockState(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return getChunk(blockPos).getBlockState(blockPos);
    }

    @Override // net.minecraft.world.level.CollisionGetter
    public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb, Predicate<Entity> predicate) {
        return Stream.empty();
    }

    @Override // net.minecraft.world.level.CollisionGetter
    public Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aabb, Predicate<Entity> predicate) {
        return getBlockCollisions(entity, aabb);
    }

    @Override // net.minecraft.world.level.BlockGetter
    public FluidState getFluidState(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return getChunk(blockPos).getFluidState(blockPos);
    }
}
