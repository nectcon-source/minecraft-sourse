package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/GiantTrunkPlacer.class */
public class GiantTrunkPlacer extends TrunkPlacer {
    public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return trunkPlacerParts(instance).apply(instance, (v1, v2, v3) -> {
            return new GiantTrunkPlacer(v1, v2, v3);
        });
    });

    public GiantTrunkPlacer(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        BlockPos below = blockPos.below();
        setDirtAt(levelSimulatedRW, below);
        setDirtAt(levelSimulatedRW, below.east());
        setDirtAt(levelSimulatedRW, below.south());
        setDirtAt(levelSimulatedRW, below.south().east());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i2 = 0; i2 < i; i2++) {
            placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 0, i2, 0);
            if (i2 < i - 1) {
                placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 1, i2, 0);
                placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 1, i2, 1);
                placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 0, i2, 1);
            }
        }
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, true));
    }

    private static void placeLogIfFreeWithOffset(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos.MutableBlockPos mutableBlockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, int i2, int i3) {
        mutableBlockPos.setWithOffset(blockPos, i, i2, i3);
        placeLogIfFree(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration);
    }
}
