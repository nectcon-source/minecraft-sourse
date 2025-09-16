package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/DarkOakFoliagePlacer.class */
public class DarkOakFoliagePlacer extends FoliagePlacer {
    public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return foliagePlacerParts(instance).apply(instance, DarkOakFoliagePlacer::new);
    });

    public DarkOakFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2) {
        super(uniformInt, uniformInt2);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        BlockPos above = foliageAttachment.foliagePos().above(i4);
        boolean doubleTrunk = foliageAttachment.doubleTrunk();
        if (doubleTrunk) {
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 + 2, set, -1, doubleTrunk, boundingBox);
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 + 3, set, 0, doubleTrunk, boundingBox);
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 + 2, set, 1, doubleTrunk, boundingBox);
            if (random.nextBoolean()) {
                placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3, set, 2, doubleTrunk, boundingBox);
                return;
            }
            return;
        }
        placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 + 2, set, -1, doubleTrunk, boundingBox);
        placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 + 1, set, 0, doubleTrunk, boundingBox);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return 4;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocationSigned(Random random, int i, int i2, int i3, int i4, boolean z) {
        if (i2 == 0 && z && ((i == (-i4) || i >= i4) && (i3 == (-i4) || i3 >= i4))) {
            return true;
        }
        return super.shouldSkipLocationSigned(random, i, i2, i3, i4, z);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return (i2 != -1 || z) ? i2 == 1 && i + i3 > (i4 * 2) - 2 : i == i4 && i3 == i4;
    }
}
