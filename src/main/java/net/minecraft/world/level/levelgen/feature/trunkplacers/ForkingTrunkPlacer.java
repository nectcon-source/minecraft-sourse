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
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/ForkingTrunkPlacer.class */
public class ForkingTrunkPlacer extends TrunkPlacer {
    public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return trunkPlacerParts(instance).apply(instance, (v1, v2, v3) -> {
            return new ForkingTrunkPlacer(v1, v2, v3);
        });
    });

    public ForkingTrunkPlacer(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FORKING_TRUNK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        setDirtAt(levelSimulatedRW, blockPos.below());
        List<FoliagePlacer.FoliageAttachment> newArrayList = Lists.newArrayList();
        Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int nextInt = (i - random.nextInt(4)) - 1;
        int nextInt2 = 3 - random.nextInt(3);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int x = blockPos.getX();
        int z = blockPos.getZ();
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            int y = blockPos.getY() + i3;
            if (i3 >= nextInt && nextInt2 > 0) {
                x += randomDirection.getStepX();
                z += randomDirection.getStepZ();
                nextInt2--;
            }
            if (placeLog(levelSimulatedRW, random, mutableBlockPos.set(x, y, z), set, boundingBox, treeConfiguration)) {
                i2 = y + 1;
            }
        }
        newArrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(x, i2, z), 1, false));
        int x2 = blockPos.getX();
        int z2 = blockPos.getZ();
        Direction randomDirection2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (randomDirection2 != randomDirection) {
            int nextInt3 = (nextInt - random.nextInt(2)) - 1;
            int i4 = 0;
            int i5 = nextInt3;
            for (int nextInt4 = 1 + random.nextInt(3); i5 < i && nextInt4 > 0; nextInt4--) {
                if (i5 >= 1) {
                    int y2 = blockPos.getY() + i5;
                    x2 += randomDirection2.getStepX();
                    z2 += randomDirection2.getStepZ();
                    if (placeLog(levelSimulatedRW, random, mutableBlockPos.set(x2, y2, z2), set, boundingBox, treeConfiguration)) {
                        i4 = y2 + 1;
                    }
                }
                i5++;
            }
            if (i4 > 1) {
                newArrayList.add(new FoliagePlacer.FoliageAttachment(new BlockPos(x2, i4, z2), 0, false));
            }
        }
        return newArrayList;
    }
}
