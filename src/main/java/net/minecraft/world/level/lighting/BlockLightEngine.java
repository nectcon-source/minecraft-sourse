package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import org.apache.commons.lang3.mutable.MutableInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/BlockLightEngine.class */
public final class BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockPos.MutableBlockPos pos;

    public BlockLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.BLOCK, new BlockLightSectionStorage(lightChunkGetter));
        this.pos = new BlockPos.MutableBlockPos();
    }

    private int getLightEmission(long j) {
        int x = BlockPos.getX(j);
        int y = BlockPos.getY(j);
        int z = BlockPos.getZ(j);
        BlockGetter chunkForLighting = this.chunkSource.getChunkForLighting(x >> 4, z >> 4);
        if (chunkForLighting != null) {
            return chunkForLighting.getLightEmission(this.pos.set(x, y, z));
        }
        return 0;
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine, net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int computeLevelFromNeighbor(long j, long j2, int i) {
        if (j2 == Long.MAX_VALUE) {
            return 15;
        }
        if (j == Long.MAX_VALUE) {
            return (i + 15) - getLightEmission(j2);
        }
        if (i >= 15) {
            return i;
        }
        Direction fromNormal = Direction.fromNormal(Integer.signum(BlockPos.getX(j2) - BlockPos.getX(j)), Integer.signum(BlockPos.getY(j2) - BlockPos.getY(j)), Integer.signum(BlockPos.getZ(j2) - BlockPos.getZ(j)));
        if (fromNormal == null) {
            return 15;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState stateAndOpacity = getStateAndOpacity(j2, mutableInt);
        if (mutableInt.getValue().intValue() >= 15 || Shapes.faceShapeOccludes(getShape(getStateAndOpacity(j, null), j, fromNormal), getShape(stateAndOpacity, j2, fromNormal.getOpposite()))) {
            return 15;
        }
        return i + Math.max(1, mutableInt.getValue().intValue());
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected void checkNeighborsAfterUpdate(long j, int i, boolean z) {
        long blockToSection = SectionPos.blockToSection(j);
        for (Direction direction : DIRECTIONS) {
            long offset = BlockPos.offset(j, direction);
            long blockToSection2 = SectionPos.blockToSection(offset);
            if (blockToSection == blockToSection2 || ((BlockLightSectionStorage) this.storage).storingLightForSection(blockToSection2)) {
                checkNeighbor(j, offset, i, z);
            }
        }
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine, net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int getComputedLevel(long j, long j2, int i) {
        DataLayer dataLayer;
        int i2 = i;
        if (Long.MAX_VALUE != j2) {
            int computeLevelFromNeighbor = computeLevelFromNeighbor(Long.MAX_VALUE, j, 0);
            if (i2 > computeLevelFromNeighbor) {
                i2 = computeLevelFromNeighbor;
            }
            if (i2 == 0) {
                return i2;
            }
        }
        long blockToSection = SectionPos.blockToSection(j);
        DataLayer dataLayer2 = ((BlockLightSectionStorage) this.storage).getDataLayer(blockToSection, true);
        for (Direction direction : DIRECTIONS) {
            long offset = BlockPos.offset(j, direction);
            if (offset != j2) {
                long blockToSection2 = SectionPos.blockToSection(offset);
                if (blockToSection == blockToSection2) {
                    dataLayer = dataLayer2;
                } else {
                    dataLayer = ((BlockLightSectionStorage) this.storage).getDataLayer(blockToSection2, true);
                }
                if (dataLayer == null) {
                    continue;
                } else {
                    int computeLevelFromNeighbor2 = computeLevelFromNeighbor(offset, j, getLevel(dataLayer, offset));
                    if (i2 > computeLevelFromNeighbor2) {
                        i2 = computeLevelFromNeighbor2;
                    }
                    if (i2 == 0) {
                        return i2;
                    }
                }
            }
        }
        return i2;
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine
    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
         this.storage.runAllUpdates();
        checkEdge(Long.MAX_VALUE, blockPos.asLong(), 15 - i, true);
    }
}
