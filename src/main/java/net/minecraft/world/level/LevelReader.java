package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/LevelReader.class */
public interface LevelReader extends BlockAndTintGetter, CollisionGetter, BiomeManager.NoiseBiomeSource {
    @Nullable
    ChunkAccess getChunk(int i, int i2, ChunkStatus chunkStatus, boolean z);

    @Deprecated
    boolean hasChunk(int i, int i2);

    int getHeight(Heightmap.Types types, int i, int i2);

    int getSkyDarken();

    BiomeManager getBiomeManager();

    Biome getUncachedNoiseBiome(int i, int i2, int i3);

    boolean isClientSide();

    @Deprecated
    int getSeaLevel();

    DimensionType dimensionType();

    default Biome getBiome(BlockPos blockPos) {
        return getBiomeManager().getBiome(blockPos);
    }

    default Stream<BlockState> getBlockStatesIfLoaded(AABB aabb) {
        int floor = Mth.floor(aabb.minX);
        int floor2 = Mth.floor(aabb.maxX);
        if (hasChunksAt(floor, Mth.floor(aabb.minY), Mth.floor(aabb.minZ), floor2, Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            return getBlockStates(aabb);
        }
        return Stream.empty();
    }

    @Override // net.minecraft.world.level.BlockAndTintGetter
    default int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return colorResolver.getColor(getBiome(blockPos), blockPos.getX(), blockPos.getZ());
    }

    @Override // net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource
    default Biome getNoiseBiome(int i, int i2, int i3) {
        ChunkAccess chunk = getChunk(i >> 2, i3 >> 2, ChunkStatus.BIOMES, false);
        if (chunk != null && chunk.getBiomes() != null) {
            return chunk.getBiomes().getNoiseBiome(i, i2, i3);
        }
        return getUncachedNoiseBiome(i, i2, i3);
    }

    default BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
        return new BlockPos(blockPos.getX(), getHeight(types, blockPos.getX(), blockPos.getZ()), blockPos.getZ());
    }

    default boolean isEmptyBlock(BlockPos blockPos) {
        return getBlockState(blockPos).isAir();
    }

    default boolean canSeeSkyFromBelowWater(BlockPos blockPos) {
        if (blockPos.getY() >= getSeaLevel()) {
            return canSeeSky(blockPos);
        }
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), getSeaLevel(), blockPos.getZ());
        if (!canSeeSky(blockPos2)) {
            return false;
        }
        BlockPos below = blockPos2.below();
        while (true) {
            BlockPos blockPos3 = below;
            if (blockPos3.getY() > blockPos.getY()) {
                BlockState blockState = getBlockState(blockPos3);
                if (blockState.getLightBlock(this, blockPos3) > 0 && !blockState.getMaterial().isLiquid()) {
                    return false;
                }
                below = blockPos3.below();
            } else {
                return true;
            }
        }
    }

    @Deprecated
    default float getBrightness(BlockPos blockPos) {
        return dimensionType().brightness(getMaxLocalRawBrightness(blockPos));
    }

    default int getDirectSignal(BlockPos blockPos, Direction direction) {
        return getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
    }

    default ChunkAccess getChunk(BlockPos blockPos) {
        return getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    default ChunkAccess getChunk(int i, int i2) {
        return getChunk(i, i2, ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk(int i, int i2, ChunkStatus chunkStatus) {
        return getChunk(i, i2, chunkStatus, true);
    }

    @Nullable
    default BlockGetter getChunkForCollisions(int i, int i2) {
        return getChunk(i, i2, ChunkStatus.EMPTY, false);
    }

    default boolean isWaterAt(BlockPos blockPos) {
        return getFluidState(blockPos).is(FluidTags.WATER);
    }

    default boolean containsAnyLiquid(AABB aabb) {
        int floor = Mth.floor(aabb.minX);
        int ceil = Mth.ceil(aabb.maxX);
        int floor2 = Mth.floor(aabb.minY);
        int ceil2 = Mth.ceil(aabb.maxY);
        int floor3 = Mth.floor(aabb.minZ);
        int ceil3 = Mth.ceil(aabb.maxZ);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = floor; i < ceil; i++) {
            for (int i2 = floor2; i2 < ceil2; i2++) {
                for (int i3 = floor3; i3 < ceil3; i3++) {
                    if (!getBlockState(mutableBlockPos.set(i, i2, i3)).getFluidState().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    default int getMaxLocalRawBrightness(BlockPos blockPos) {
        return getMaxLocalRawBrightness(blockPos, getSkyDarken());
    }

    default int getMaxLocalRawBrightness(BlockPos blockPos, int i) {
        if (blockPos.getX() < -30000000 || blockPos.getZ() < -30000000 || blockPos.getX() >= 30000000 || blockPos.getZ() >= 30000000) {
            return 15;
        }
        return getRawBrightness(blockPos, i);
    }

    @Deprecated
    default boolean hasChunkAt(BlockPos blockPos) {
        return hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    @Deprecated
    default boolean hasChunksAt(BlockPos blockPos, BlockPos blockPos2) {
        return hasChunksAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
    }

    @Deprecated
    default boolean hasChunksAt(int i, int i2, int i3, int i4, int i5, int i6) {
        if (i5 < 0 || i2 >= 256) {
            return false;
        }
        int i7 = i3 >> 4;
        int i8 = i4 >> 4;
        int i9 = i6 >> 4;
        for (int i10 = i >> 4; i10 <= i8; i10++) {
            for (int i11 = i7; i11 <= i9; i11++) {
                if (!hasChunk(i10, i11)) {
                    return false;
                }
            }
        }
        return true;
    }
}
