package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/ChunkAccess.class */
public interface ChunkAccess extends BlockGetter, FeatureAccess {
    @Nullable
    BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean z);

    void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity);

    void addEntity(Entity entity);

    Set<BlockPos> getBlockEntitiesPos();

    LevelChunkSection[] getSections();

    Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps();

    void setHeightmap(Heightmap.Types types, long[] jArr);

    Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types);

    int getHeight(Heightmap.Types types, int i, int i2);

    ChunkPos getPos();

    void setLastSaveTime(long j);

    Map<StructureFeature<?>, StructureStart<?>> getAllStarts();

    void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map);

    @Nullable
    ChunkBiomeContainer getBiomes();

    void setUnsaved(boolean z);

    boolean isUnsaved();

    ChunkStatus getStatus();

    void removeBlockEntity(BlockPos blockPos);

    ShortList[] getPostProcessing();

    @Nullable
    CompoundTag getBlockEntityNbt(BlockPos blockPos);

    @Nullable
    CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos);

    Stream<BlockPos> getLights();

    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

    UpgradeData getUpgradeData();

    void setInhabitedTime(long j);

    long getInhabitedTime();

    boolean isLightCorrect();

    void setLightCorrect(boolean z);

    @Nullable
    default LevelChunkSection getHighestSection() {
        LevelChunkSection[] sections = getSections();
        for (int length = sections.length - 1; length >= 0; length--) {
            LevelChunkSection levelChunkSection = sections[length];
            if (!LevelChunkSection.isEmpty(levelChunkSection)) {
                return levelChunkSection;
            }
        }
        return null;
    }

    default int getHighestSectionPosition() {
        LevelChunkSection highestSection = getHighestSection();
        if (highestSection == null) {
            return 0;
        }
        return highestSection.bottomBlockY();
    }

    default boolean isYSpaceEmpty(int i, int i2) {
        if (i < 0) {
            i = 0;
        }
        if (i2 >= 256) {
            i2 = 255;
        }
        for (int i3 = i; i3 <= i2; i3 += 16) {
            if (!LevelChunkSection.isEmpty(getSections()[i3 >> 4])) {
                return false;
            }
        }
        return true;
    }

    default void markPosForPostprocessing(BlockPos blockPos) {
        LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", blockPos);
    }

    default void addPackedPostProcess(short s, int i) {
        getOrCreateOffsetList(getPostProcessing(), i).add(s);
    }

    default void setBlockEntityNbt(CompoundTag compoundTag) {
        LogManager.getLogger().warn("Trying to set a BlockEntity, but this operation is not supported.");
    }

    static ShortList getOrCreateOffsetList(ShortList[] shortListArr, int i) {
        if (shortListArr[i] == null) {
            shortListArr[i] = new ShortArrayList();
        }
        return shortListArr[i];
    }
}
