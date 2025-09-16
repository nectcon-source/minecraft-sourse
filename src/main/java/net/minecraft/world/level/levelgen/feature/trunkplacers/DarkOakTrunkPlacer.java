package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/DarkOakTrunkPlacer.class */
public class DarkOakTrunkPlacer extends TrunkPlacer {
    public static final Codec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return trunkPlacerParts(instance).apply(instance, (v1, v2, v3) -> {
            return new DarkOakTrunkPlacer(v1, v2, v3);
        });
    });

    public DarkOakTrunkPlacer(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        List<FoliagePlacer.FoliageAttachment> newArrayList = Lists.newArrayList();
        BlockPos below = blockPos.below();
        setDirtAt(levelSimulatedRW, below);
        setDirtAt(levelSimulatedRW, below.east());
        setDirtAt(levelSimulatedRW, below.south());
        setDirtAt(levelSimulatedRW, below.south().east());
        Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int nextInt = i - random.nextInt(4);
        int nextInt2 = 2 - random.nextInt(3);
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        int i2 = x;
        int i3 = z;
        int i4 = (y + i) - 1;
        for (int i5 = 0; i5 < i; i5++) {
            if (i5 >= nextInt && nextInt2 > 0) {
                i2 += randomDirection.getStepX();
                i3 += randomDirection.getStepZ();
                nextInt2--;
            }
            BlockPos blockPos2 = new BlockPos(i2, y + i5, i3);
            if (TreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos2)) {
                placeLog(levelSimulatedRW, random, blockPos2, set, boundingBox, treeConfiguration);
                placeLog(levelSimulatedRW, random, blockPos2.east(), set, boundingBox, treeConfiguration);
                placeLog(levelSimulatedRW, random, blockPos2.south(), set, boundingBox, treeConfiguration);
                placeLog(levelSimulatedRW, random, blockPos2.east().south(), set, boundingBox, treeConfiguration);
            }
        }
        newArrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(i2, i4, i3), 0, true));
        for (int i6 = -1; i6 <= 2; i6++) {
            for (int i7 = -1; i7 <= 2; i7++) {
                if ((i6 < 0 || i6 > 1 || i7 < 0 || i7 > 1) && random.nextInt(3) <= 0) {
                    int nextInt3 = random.nextInt(3) + 2;
                    for (int i8 = 0; i8 < nextInt3; i8++) {
                        placeLog(levelSimulatedRW, random, new BlockPos(x + i6, (i4 - i8) - 1, z + i7), set, boundingBox, treeConfiguration);
                    }
                    newArrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(i2 + i6, i4, i3 + i7), 0, false));
                }
            }
        }
        return newArrayList;
    }
}
