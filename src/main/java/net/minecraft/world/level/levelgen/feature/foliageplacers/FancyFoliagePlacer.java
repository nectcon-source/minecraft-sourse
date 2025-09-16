package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/FancyFoliagePlacer.class */
public class FancyFoliagePlacer extends BlobFoliagePlacer {
    public static final Codec<FancyFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return blobParts(instance).apply(instance, (v1, v2, v3) -> {
            return new FancyFoliagePlacer(v1, v2, v3);
        });
    });

    public FancyFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, int i) {
        super(uniformInt, uniformInt2, i);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer, net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer, net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        int i5 = i4;
        while (i5 >= i4 - i2) {
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), i3 + ((i5 == i4 || i5 == i4 - i2) ? 0 : 1), set, i5, foliageAttachment.doubleTrunk(), boundingBox);
            i5--;
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer, net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return Mth.square(((float) i) + 0.5f) + Mth.square(((float) i3) + 0.5f) > ((float) (i4 * i4));
    }
}
