package net.minecraft.world.level.lighting;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/LayerLightEngine.class */
public abstract class LayerLightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>> extends DynamicGraphMinFixedPoint implements LayerLightEventListener {
    private static final Direction[] DIRECTIONS = Direction.values();
    protected final LightChunkGetter chunkSource;
    protected final LightLayer layer;
    protected final S storage;
    private boolean runningLightUpdates;
    protected final BlockPos.MutableBlockPos pos;
    private final long[] lastChunkPos;
    private final BlockGetter[] lastChunk;

    public LayerLightEngine(LightChunkGetter lightChunkGetter, LightLayer lightLayer, S s) {
        super(16, 256, 8192);
        this.pos = new BlockPos.MutableBlockPos();
        this.lastChunkPos = new long[2];
        this.lastChunk = new BlockGetter[2];
        this.chunkSource = lightChunkGetter;
        this.layer = lightLayer;
        this.storage = s;
        clearCache();
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected void checkNode(long j) {
        this.storage.runAllUpdates();
        if (this.storage.storingLightForSection(SectionPos.blockToSection(j))) {
            super.checkNode(j);
        }
    }

    @Nullable
    private BlockGetter getChunk(int i, int i2) {
        long asLong = ChunkPos.asLong(i, i2);
        for (int i3 = 0; i3 < 2; i3++) {
            if (asLong == this.lastChunkPos[i3]) {
                return this.lastChunk[i3];
            }
        }
        BlockGetter chunkForLighting = this.chunkSource.getChunkForLighting(i, i2);
        for (int i4 = 1; i4 > 0; i4--) {
            this.lastChunkPos[i4] = this.lastChunkPos[i4 - 1];
            this.lastChunk[i4] = this.lastChunk[i4 - 1];
        }
        this.lastChunkPos[0] = asLong;
        this.lastChunk[0] = chunkForLighting;
        return chunkForLighting;
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk,  null);
    }

    protected BlockState getStateAndOpacity(long j, @Nullable MutableInt mutableInt) {
        if (j == Long.MAX_VALUE) {
            if (mutableInt != null) {
                mutableInt.setValue(0);
            }
            return Blocks.AIR.defaultBlockState();
        }
        BlockGetter chunk = getChunk(SectionPos.blockToSectionCoord(BlockPos.getX(j)), SectionPos.blockToSectionCoord(BlockPos.getZ(j)));
        if (chunk == null) {
            if (mutableInt != null) {
                mutableInt.setValue(16);
            }
            return Blocks.BEDROCK.defaultBlockState();
        }
        this.pos.set(j);
        BlockState blockState = chunk.getBlockState(this.pos);
        boolean z = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
        if (mutableInt != null) {
            mutableInt.setValue(blockState.getLightBlock(this.chunkSource.getLevel(), this.pos));
        }
        return z ? blockState : Blocks.AIR.defaultBlockState();
    }

    protected VoxelShape getShape(BlockState blockState, long j, Direction direction) {
        return blockState.canOcclude() ? blockState.getFaceOcclusionShape(this.chunkSource.getLevel(), this.pos.set(j), direction) : Shapes.empty();
    }

    public static int getLightBlockInto(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, BlockState blockState2, BlockPos blockPos2, Direction direction, int i) {
        boolean z = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
        boolean z2 = blockState2.canOcclude() && blockState2.useShapeForLightOcclusion();
        if (!z && !z2) {
            return i;
        }
        if (Shapes.mergedFaceOccludes(z ? blockState.getOcclusionShape(blockGetter, blockPos) : Shapes.empty(), z2 ? blockState2.getOcclusionShape(blockGetter, blockPos2) : Shapes.empty(), direction)) {
            return 16;
        }
        return i;
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected boolean isSource(long j) {
        return j == Long.MAX_VALUE;
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int getComputedLevel(long j, long j2, int i) {
        return 0;
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int getLevel(long j) {
        if (j == Long.MAX_VALUE) {
            return 0;
        }
        return 15 - this.storage.getStoredLevel(j);
    }

    protected int getLevel(DataLayer dataLayer, long j) {
        return 15 - dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(j)), SectionPos.sectionRelative(BlockPos.getY(j)), SectionPos.sectionRelative(BlockPos.getZ(j)));
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected void setLevel(long j, int i) {
        this.storage.setStoredLevel(j, Math.min(15, 15 - i));
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int computeLevelFromNeighbor(long j, long j2, int i) {
        return 0;
    }

    public boolean hasLightWork() {
        return hasWork() || this.storage.hasWork() || this.storage.hasInconsistencies();
    }

    public int runUpdates(int i, boolean z, boolean z2) {
        if (!this.runningLightUpdates) {
            if (this.storage.hasWork()) {
                i = this.storage.runUpdates(i);
                if (i == 0) {
                    return i;
                }
            }
            this.storage.markNewInconsistencies(this, z, z2);
        }
        this.runningLightUpdates = true;
        if (hasWork()) {
            i = runUpdates(i);
            clearCache();
            if (i == 0) {
                return i;
            }
        }
        this.runningLightUpdates = false;
        this.storage.swapSectionMap();
        return i;
    }

    protected void queueSectionData(long j, @Nullable DataLayer dataLayer, boolean z) {
        this.storage.queueSectionData(j, dataLayer, z);
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEventListener
    @Nullable
    public DataLayer getDataLayerData(SectionPos sectionPos) {
        return this.storage.getDataLayerData(sectionPos.asLong());
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEventListener
    public int getLightValue(BlockPos blockPos) {
        return this.storage.getLightValue(blockPos.asLong());
    }

    public String getDebugData(long j) {
        return "" + this.storage.getLevel(j);
    }

    public void checkBlock(BlockPos blockPos) {
        long asLong = blockPos.asLong();
        checkNode(asLong);
        for (Direction direction : DIRECTIONS) {
            checkNode(BlockPos.offset(asLong, direction));
        }
    }

    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
    }

    @Override // net.minecraft.world.level.lighting.LightEventListener
    public void updateSectionStatus(SectionPos sectionPos, boolean z) {
        this.storage.updateSectionStatus(sectionPos.asLong(), z);
    }

    public void enableLightSources(ChunkPos chunkPos, boolean z) {
        this.storage.enableLightSources(SectionPos.getZeroNode(SectionPos.asLong(chunkPos.x, 0, chunkPos.z)), z);
    }

    public void retainData(ChunkPos chunkPos, boolean z) {
        this.storage.retainData(SectionPos.getZeroNode(SectionPos.asLong(chunkPos.x, 0, chunkPos.z)), z);
    }
}
