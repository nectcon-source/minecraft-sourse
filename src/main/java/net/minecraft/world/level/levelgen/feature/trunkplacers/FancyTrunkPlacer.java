package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/FancyTrunkPlacer.class */
public class FancyTrunkPlacer extends TrunkPlacer {
    public static final Codec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return trunkPlacerParts(instance).apply(instance, (v1, v2, v3) -> {
            return new FancyTrunkPlacer(v1, v2, v3);
        });
    });

    public FancyTrunkPlacer(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        int i2 = i + 2;
        int floor = Mth.floor(i2 * 0.618d);
        if (!treeConfiguration.fromSapling) {
            setDirtAt(levelSimulatedRW, blockPos.below());
        }
        int min = Math.min(1, Mth.floor(1.382d + Math.pow((1.0d * i2) / 13.0d, 2.0d)));
        int y = blockPos.getY() + floor;
        int i3 = i2 - 5;
        List<FoliageCoords> newArrayList = Lists.newArrayList();
        newArrayList.add(new FoliageCoords(blockPos.above(i3), y));
        while (i3 >= 0) {
            float treeShape = treeShape(i2, i3);
            if (treeShape >= 0.0f) {
                for (int i4 = 0; i4 < min; i4++) {
                    double nextFloat = 1.0d * treeShape * (random.nextFloat() + 0.328d);
                    double nextFloat2 = random.nextFloat() * 2.0f * 3.141592653589793d;
                    BlockPos offset = blockPos.offset((nextFloat * Math.sin(nextFloat2)) + 0.5d, i3 - 1, (nextFloat * Math.cos(nextFloat2)) + 0.5d);
                    if (makeLimb(levelSimulatedRW, random, offset, offset.above(5), false, set, boundingBox, treeConfiguration)) {
                        int x = blockPos.getX() - offset.getX();
                        int z = blockPos.getZ() - offset.getZ();
                        double y2 = offset.getY() - (Math.sqrt((x * x) + (z * z)) * 0.381d);
                        BlockPos blockPos2 = new BlockPos(blockPos.getX(), y2 > ((double) y) ? y : (int) y2, blockPos.getZ());
                        if (makeLimb(levelSimulatedRW, random, blockPos2, offset, false, set, boundingBox, treeConfiguration)) {
                            newArrayList.add(new FoliageCoords(offset, blockPos2.getY()));
                        }
                    }
                }
            }
            i3--;
        }
        makeLimb(levelSimulatedRW, random, blockPos, blockPos.above(floor), true, set, boundingBox, treeConfiguration);
        makeBranches(levelSimulatedRW, random, i2, blockPos, newArrayList, set, boundingBox, treeConfiguration);
        List<FoliagePlacer.FoliageAttachment> newArrayList2 = Lists.newArrayList();
        for (FoliageCoords foliageCoords : newArrayList) {
            if (trimBranches(i2, foliageCoords.getBranchBase() - blockPos.getY())) {
                newArrayList2.add(foliageCoords.attachment);
            }
        }
        return newArrayList2;
    }

    private boolean makeLimb(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BlockPos blockPos2, boolean z, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        if (!z && Objects.equals(blockPos, blockPos2)) {
            return true;
        } else {
            BlockPos var9 = blockPos2.offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
            int var10 = this.getSteps(var9);
            float var11 = (float)var9.getX() / (float)var10;
            float var12 = (float)var9.getY() / (float)var10;
            float var13 = (float)var9.getZ() / (float)var10;

            for(int var14 = 0; var14 <= var10; ++var14) {
                BlockPos var15 = blockPos.offset((double)(0.5F + (float)var14 * var11), (double)(0.5F + (float)var14 * var12), (double)(0.5F + (float)var14 * var13));
                if (z) {
                    setBlock(levelSimulatedRW, var15, (BlockState)treeConfiguration.trunkProvider.getState(random, var15).setValue(RotatedPillarBlock.AXIS, this.getLogAxis(blockPos, var15)), boundingBox);
                    set.add(var15.immutable());
                } else if (!TreeFeature.isFree(levelSimulatedRW, var15)) {
                    return false;
                }
            }

            return true;
        }
    }

    private int getSteps(BlockPos blockPos) {
        return Math.max(Mth.abs(blockPos.getX()), Math.max(Mth.abs(blockPos.getY()), Mth.abs(blockPos.getZ())));
    }

    private Direction.Axis getLogAxis(BlockPos blockPos, BlockPos blockPos2) {
        Direction.Axis axis = Direction.Axis.Y;
        int abs = Math.abs(blockPos2.getX() - blockPos.getX());
        int max = Math.max(abs, Math.abs(blockPos2.getZ() - blockPos.getZ()));
        if (max > 0) {
            if (abs == max) {
                axis = Direction.Axis.X;
            } else {
                axis = Direction.Axis.Z;
            }
        }
        return axis;
    }

    private boolean trimBranches(int i, int i2) {
        return ((double) i2) >= ((double) i) * 0.2d;
    }

    private void makeBranches(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, List<FoliageCoords> list, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        for (FoliageCoords foliageCoords : list) {
            int branchBase = foliageCoords.getBranchBase();
            BlockPos blockPos2 = new BlockPos(blockPos.getX(), branchBase, blockPos.getZ());
            if (!blockPos2.equals(foliageCoords.attachment.foliagePos()) && trimBranches(i, branchBase - blockPos.getY())) {
                makeLimb(levelSimulatedRW, random, blockPos2, foliageCoords.attachment.foliagePos(), true, set, boundingBox, treeConfiguration);
            }
        }
    }

    private float treeShape(int i, int i2) {
        if (i2 < i * 0.3f) {
            return -1.0f;
        }
        float f = i / 2.0f;
        float f2 = f - i2;
        float sqrt = Mth.sqrt((f * f) - (f2 * f2));
        if (f2 == 0.0f) {
            sqrt = f;
        } else if (Math.abs(f2) >= f) {
            return 0.0f;
        }
        return sqrt * 0.5f;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/FancyTrunkPlacer$FoliageCoords.class */
    static class FoliageCoords {
        private final FoliagePlacer.FoliageAttachment attachment;
        private final int branchBase;

        public FoliageCoords(BlockPos blockPos, int i) {
            this.attachment = new FoliagePlacer.FoliageAttachment(blockPos, 0, false);
            this.branchBase = i;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}
