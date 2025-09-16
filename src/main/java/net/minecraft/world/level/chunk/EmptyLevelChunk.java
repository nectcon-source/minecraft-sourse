package net.minecraft.world.level.chunk;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/EmptyLevelChunk.class */
public class EmptyLevelChunk extends LevelChunk {
    private static final Biome[] BIOMES = (Biome[]) Util.make(new Biome[ChunkBiomeContainer.BIOMES_SIZE], biomeArr -> {
        Arrays.fill(biomeArr, Biomes.PLAINS);
    });

    public EmptyLevelChunk(Level level, ChunkPos chunkPos) {
        super(level, chunkPos, new ChunkBiomeContainer(level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), BIOMES));
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.BlockGetter
    public BlockState getBlockState(BlockPos blockPos) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.chunk.ChunkAccess
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean z) {
        return null;
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.BlockGetter
    public FluidState getFluidState(BlockPos blockPos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    @Nullable
    public LevelLightEngine getLightEngine() {
        return null;
    }

    @Override // net.minecraft.world.level.BlockGetter
    public int getLightEmission(BlockPos blockPos) {
        return 0;
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void addEntity(Entity entity) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public void removeEntity(Entity entity) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public void removeEntity(Entity entity, int i) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos, LevelChunk.EntityCreationType entityCreationType) {
        return null;
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public void addBlockEntity(BlockEntity blockEntity) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void removeBlockEntity(BlockPos blockPos) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public void markUnsaved() {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public void getEntities(@Nullable Entity entity, AABB aabb, List<Entity> list, Predicate<? super Entity> predicate) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public <T extends Entity> void getEntitiesOfClass(Class<? extends T> cls, AABB aabb, List<T> list, Predicate<? super T> predicate) {
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public boolean isEmpty() {
        return true;
    }

    @Override // net.minecraft.world.level.chunk.ChunkAccess
    public boolean isYSpaceEmpty(int i, int i2) {
        return true;
    }

    @Override // net.minecraft.world.level.chunk.LevelChunk
    public ChunkHolder.FullChunkStatus getFullStatus() {
        return ChunkHolder.FullChunkStatus.BORDER;
    }
}
