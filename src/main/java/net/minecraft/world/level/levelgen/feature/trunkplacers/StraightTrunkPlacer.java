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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/StraightTrunkPlacer.class */
public class StraightTrunkPlacer extends TrunkPlacer {
    public static final Codec<StraightTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return trunkPlacerParts(instance).apply(instance, (v1, v2, v3) -> {
            return new StraightTrunkPlacer(v1, v2, v3);
        });
    });

    public StraightTrunkPlacer(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        setDirtAt(levelSimulatedRW, blockPos.below());
        for (int i2 = 0; i2 < i; i2++) {
            placeLog(levelSimulatedRW, random, blockPos.above(i2), set, boundingBox, treeConfiguration);
        }
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, false));
    }
}
