package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/MegaJungleTrunkPlacer.class */
public class MegaJungleTrunkPlacer extends GiantTrunkPlacer {
    public static final Codec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return trunkPlacerParts(instance).apply(instance, (v1, v2, v3) -> {
            return new MegaJungleTrunkPlacer(v1, v2, v3);
        });
    });

    public MegaJungleTrunkPlacer(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer, net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer, net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        List<FoliagePlacer.FoliageAttachment> newArrayList = Lists.newArrayList();
        newArrayList.addAll(super.placeTrunk(levelSimulatedRW, random, i, blockPos, set, boundingBox, treeConfiguration));
        int i2 = i - 2;
        int nextInt = random.nextInt(4);
        while (true) {
            int i3 = i2 - nextInt;
            if (i3 > i / 2) {
                float nextFloat = random.nextFloat() * 6.2831855f;
                int i4 = 0;
                int i5 = 0;
                for (int i6 = 0; i6 < 5; i6++) {
                    i4 = (int) (1.5f + (Mth.cos(nextFloat) * i6));
                    i5 = (int) (1.5f + (Mth.sin(nextFloat) * i6));
                    placeLog(levelSimulatedRW, random, blockPos.offset(i4, (i3 - 3) + (i6 / 2), i5), set, boundingBox, treeConfiguration);
                }
                newArrayList.add(new FoliagePlacer.FoliageAttachment(blockPos.offset(i4, i3, i5), -2, false));
                i2 = i3;
                nextInt = 2 + random.nextInt(4);
            } else {
                return newArrayList;
            }
        }
    }
}
