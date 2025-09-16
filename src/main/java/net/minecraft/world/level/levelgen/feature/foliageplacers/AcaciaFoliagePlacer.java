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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/AcaciaFoliagePlacer.class */
public class AcaciaFoliagePlacer extends FoliagePlacer {
    public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return foliagePlacerParts(instance).apply(instance, AcaciaFoliagePlacer::new);
    });

    public AcaciaFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2) {
        super(uniformInt, uniformInt2);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        boolean doubleTrunk = foliageAttachment.doubleTrunk();
        BlockPos above = foliageAttachment.foliagePos().above(i4);
        placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 + foliageAttachment.radiusOffset(), set, (-1) - i2, doubleTrunk, boundingBox);
        placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, i3 - 1, set, -i2, doubleTrunk, boundingBox);
        placeLeavesRow(levelSimulatedRW, random, treeConfiguration, above, (i3 + foliageAttachment.radiusOffset()) - 1, set, 0, doubleTrunk, boundingBox);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return 0;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return i2 == 0 ? ((i <= 1 && i3 <= 1) || i == 0 || i3 == 0) ? false : true : i == i4 && i3 == i4 && i4 > 0;
    }
}
