package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import org.apache.commons.lang3.mutable.MutableInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/SkyLightEngine.class */
public final class SkyLightEngine extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public SkyLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.SKY, new SkyLightSectionStorage(lightChunkGetter));
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine, net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int computeLevelFromNeighbor(long j, long j2, int i) {
        Direction fromNormal;
        if (j2 == Long.MAX_VALUE) {
            return 15;
        }
        if (j == Long.MAX_VALUE) {
            if (((SkyLightSectionStorage) this.storage).hasLightSource(j2)) {
                i = 0;
            } else {
                return 15;
            }
        }
        if (i >= 15) {
            return i;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState stateAndOpacity = getStateAndOpacity(j2, mutableInt);
        if (mutableInt.getValue().intValue() >= 15) {
            return 15;
        }
        int x = BlockPos.getX(j);
        int y = BlockPos.getY(j);
        int z = BlockPos.getZ(j);
        int x2 = BlockPos.getX(j2);
        int y2 = BlockPos.getY(j2);
        int z2 = BlockPos.getZ(j2);
        boolean z3 = x == x2 && z == z2;
        int signum = Integer.signum(x2 - x);
        int signum2 = Integer.signum(y2 - y);
        int signum3 = Integer.signum(z2 - z);
        if (j == Long.MAX_VALUE) {
            fromNormal = Direction.DOWN;
        } else {
            fromNormal = Direction.fromNormal(signum, signum2, signum3);
        }
        BlockState stateAndOpacity2 = getStateAndOpacity(j, null);
        if (fromNormal != null) {
            if (Shapes.faceShapeOccludes(getShape(stateAndOpacity2, j, fromNormal), getShape(stateAndOpacity, j2, fromNormal.getOpposite()))) {
                return 15;
            }
        } else {
            if (Shapes.faceShapeOccludes(getShape(stateAndOpacity2, j, Direction.DOWN), Shapes.empty())) {
                return 15;
            }
            Direction fromNormal2 = Direction.fromNormal(signum, z3 ? -1 : 0, signum3);
            if (fromNormal2 == null) {
                return 15;
            }
            if (Shapes.faceShapeOccludes(Shapes.empty(), getShape(stateAndOpacity, j2, fromNormal2.getOpposite()))) {
                return 15;
            }
        }
        if ((j == Long.MAX_VALUE || (z3 && y > y2)) && i == 0 && mutableInt.getValue().intValue() == 0) {
            return 0;
        }
        return i + Math.max(1, mutableInt.getValue().intValue());
    }

    @Override // net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected void checkNeighborsAfterUpdate(long j, int i, boolean z) {
        int i2;
        long blockToSection = SectionPos.blockToSection(j);
        int y = BlockPos.getY(j);
        int sectionRelative = SectionPos.sectionRelative(y);
        int blockToSectionCoord = SectionPos.blockToSectionCoord(y);
        if (sectionRelative != 0) {
            i2 = 0;
        } else {
            int i3 = 0;
            while (!((SkyLightSectionStorage) this.storage).storingLightForSection(SectionPos.offset(blockToSection, 0, (-i3) - 1, 0)) && ((SkyLightSectionStorage) this.storage).hasSectionsBelow((blockToSectionCoord - i3) - 1)) {
                i3++;
            }
            i2 = i3;
        }
        long offset = BlockPos.offset(j, 0, (-1) - (i2 * 16), 0);
        long blockToSection2 = SectionPos.blockToSection(offset);
        if (blockToSection == blockToSection2 || ((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection2)) {
            checkNeighbor(j, offset, i, z);
        }
        long offset2 = BlockPos.offset(j, Direction.UP);
        long blockToSection3 = SectionPos.blockToSection(offset2);
        if (blockToSection == blockToSection3 || ((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection3)) {
            checkNeighbor(j, offset2, i, z);
        }
        for (Direction direction : HORIZONTALS) {
            int i4 = 0;
            while (true) {
                long offset3 = BlockPos.offset(j, direction.getStepX(), -i4, direction.getStepZ());
                long blockToSection4 = SectionPos.blockToSection(offset3);
                if (blockToSection == blockToSection4) {
                    checkNeighbor(j, offset3, i, z);
                    break;
                }
                if (((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection4)) {
                    checkNeighbor(j, offset3, i, z);
                }
                i4++;
                if (i4 > i2 * 16) {
                    break;
                }
            }
        }
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine, net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected int getComputedLevel(long j, long j2, int i) {
        DataLayer dataLayer;
        long j3;
        int i2;
        int i3 = i;
        if (Long.MAX_VALUE != j2) {
            int computeLevelFromNeighbor = computeLevelFromNeighbor(Long.MAX_VALUE, j, 0);
            if (i3 > computeLevelFromNeighbor) {
                i3 = computeLevelFromNeighbor;
            }
            if (i3 == 0) {
                return i3;
            }
        }
        long blockToSection = SectionPos.blockToSection(j);
        DataLayer dataLayer2 = ((SkyLightSectionStorage) this.storage).getDataLayer(blockToSection, true);
        for (Direction direction : DIRECTIONS) {
            long offset = BlockPos.offset(j, direction);
            long blockToSection2 = SectionPos.blockToSection(offset);
            if (blockToSection == blockToSection2) {
                dataLayer = dataLayer2;
            } else {
                dataLayer = ((SkyLightSectionStorage) this.storage).getDataLayer(blockToSection2, true);
            }
            if (dataLayer != null) {
                if (offset == j2) {
                    continue;
                } else {
                    int computeLevelFromNeighbor2 = computeLevelFromNeighbor(offset, j, getLevel(dataLayer, offset));
                    if (i3 > computeLevelFromNeighbor2) {
                        i3 = computeLevelFromNeighbor2;
                    }
                    if (i3 == 0) {
                        return i3;
                    }
                }
            } else if (direction != Direction.DOWN) {
                long flatIndex = BlockPos.getFlatIndex(offset);
                while (true) {
                    j3 = flatIndex;
                    if (((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection2) || ((SkyLightSectionStorage) this.storage).isAboveData(blockToSection2)) {
                        break;
                    }
                    blockToSection2 = SectionPos.offset(blockToSection2, Direction.UP);
                    flatIndex = BlockPos.offset(j3, 0, 16, 0);
                }
                DataLayer dataLayer3 = ((SkyLightSectionStorage) this.storage).getDataLayer(blockToSection2, true);
                if (j3 == j2) {
                    continue;
                } else {
                    if (dataLayer3 != null) {
                        i2 = computeLevelFromNeighbor(j3, j, getLevel(dataLayer3, j3));
                    } else {
                        i2 = ((SkyLightSectionStorage) this.storage).lightOnInSection(blockToSection2) ? 0 : 15;
                    }
                    if (i3 > i2) {
                        i3 = i2;
                    }
                    if (i3 == 0) {
                        return i3;
                    }
                }
            } else {
                continue;
            }
        }
        return i3;
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine, net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint
    protected void checkNode(long j) {
        long j2;
        ((SkyLightSectionStorage) this.storage).runAllUpdates();
        long blockToSection = SectionPos.blockToSection(j);
        if (((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection)) {
            super.checkNode(j);
            return;
        }
        long flatIndex = BlockPos.getFlatIndex(j);
        while (true) {
            j2 = flatIndex;
            if (((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection) || ((SkyLightSectionStorage) this.storage).isAboveData(blockToSection)) {
                break;
            }
            blockToSection = SectionPos.offset(blockToSection, Direction.UP);
            flatIndex = BlockPos.offset(j2, 0, 16, 0);
        }
        if (((SkyLightSectionStorage) this.storage).storingLightForSection(blockToSection)) {
            super.checkNode(j2);
        }
    }

    @Override // net.minecraft.world.level.lighting.LayerLightEngine
    public String getDebugData(long j) {
        return super.getDebugData(j) + (((SkyLightSectionStorage) this.storage).isAboveData(j) ? "*" : "");
    }
}
