package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/ImposterProtoChunk.class */
public class ImposterProtoChunk extends ProtoChunk {
    private final LevelChunk wrapped;

    public ImposterProtoChunk(LevelChunk levelChunk) {
        super(levelChunk.getPos(), UpgradeData.EMPTY);
        this.wrapped = levelChunk;
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.BlockGetter
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.wrapped.getBlockEntity(blockPos);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.BlockGetter
    @Nullable
    public BlockState getBlockState(BlockPos blockPos) {
        return this.wrapped.getBlockState(blockPos);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.BlockGetter
    public FluidState getFluidState(BlockPos blockPos) {
        return this.wrapped.getFluidState(blockPos);
    }

    @Override // net.minecraft.world.level.BlockGetter
    public int getMaxLightLevel() {
        return this.wrapped.getMaxLightLevel();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean z) {
        return null;
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void addEntity(Entity entity) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk
    public void setStatus(ChunkStatus chunkStatus) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public LevelChunkSection[] getSections() {
        return this.wrapped.getSections();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk
    @Nullable
    public LevelLightEngine getLightEngine() {
        return this.wrapped.getLightEngine();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setHeightmap(Heightmap.Types types, long[] jArr) {
    }

    private Heightmap.Types fixType(Heightmap.Types types) {
        if (types == Heightmap.Types.WORLD_SURFACE_WG) {
            return Heightmap.Types.WORLD_SURFACE;
        }
        if (types == Heightmap.Types.OCEAN_FLOOR_WG) {
            return Heightmap.Types.OCEAN_FLOOR;
        }
        return types;
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public int getHeight(Heightmap.Types types, int i, int i2) {
        return this.wrapped.getHeight(fixType(types), i, i2);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public ChunkPos getPos() {
        return this.wrapped.getPos();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setLastSaveTime(long j) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.FeatureAccess
    @Nullable
    public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
        return this.wrapped.getStartForFeature(structureFeature);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.FeatureAccess
    public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return this.wrapped.getAllStarts();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.FeatureAccess
    public LongSet getReferencesForFeature(StructureFeature<?> structureFeature) {
        return this.wrapped.getReferencesForFeature(structureFeature);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.FeatureAccess
    public void addReferenceForFeature(StructureFeature<?> structureFeature, long j) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.FeatureAccess
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return this.wrapped.getAllReferences();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.FeatureAccess
    public void setAllReferences(Map<StructureFeature<?>, LongSet> map) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public ChunkBiomeContainer getBiomes() {
        return this.wrapped.getBiomes();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setUnsaved(boolean z) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public boolean isUnsaved() {
        return false;
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public ChunkStatus getStatus() {
        return this.wrapped.getStatus();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void removeBlockEntity(BlockPos blockPos) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void markPosForPostprocessing(BlockPos blockPos) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setBlockEntityNbt(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.wrapped.getBlockEntityNbt(blockPos);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
        return this.wrapped.getBlockEntityNbtForSaving(blockPos);
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk
    public void setBiomes(ChunkBiomeContainer chunkBiomeContainer) {
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public Stream<BlockPos> getLights() {
        return this.wrapped.getLights();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public ProtoTickList<Block> getBlockTicks() {
        return new ProtoTickList<>(block -> {
            return block.defaultBlockState().isAir();
        }, getPos());
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public ProtoTickList<Fluid> getLiquidTicks() {
        return new ProtoTickList<>(fluid -> {
            return fluid == Fluids.EMPTY;
        }, getPos());
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk
    public BitSet getCarvingMask(GenerationStep.Carving carving) {
        throw ((UnsupportedOperationException) Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context")));
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk
    public BitSet getOrCreateCarvingMask(GenerationStep.Carving carving) {
        throw ((UnsupportedOperationException) Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context")));
    }

    public LevelChunk getWrapped() {
        return this.wrapped;
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public boolean isLightCorrect() {
        return this.wrapped.isLightCorrect();
    }

    @Override // net.minecraft.world.level.chunk.ProtoChunk, net.minecraft.world.level.chunk.ChunkAccess
    public void setLightCorrect(boolean z) {
        this.wrapped.setLightCorrect(z);
    }
}
