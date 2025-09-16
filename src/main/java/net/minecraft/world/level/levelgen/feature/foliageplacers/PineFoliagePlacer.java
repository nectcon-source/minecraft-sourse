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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/PineFoliagePlacer.class */
public class PineFoliagePlacer extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return foliagePlacerParts(instance).and(UniformInt.codec(0, 16, 8).fieldOf("height").forGetter(pineFoliagePlacer -> {
            return pineFoliagePlacer.height;
        })).apply(instance, PineFoliagePlacer::new);
    });
    private final UniformInt height;

    public PineFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3) {
        super(uniformInt, uniformInt2);
        this.height = uniformInt3;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.PINE_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        int i5 = 0;
        for (int i6 = i4; i6 >= i4 - i2; i6--) {
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), i5, set, i6, foliageAttachment.doubleTrunk(), boundingBox);
            if (i5 >= 1 && i6 == (i4 - i2) + 1) {
                i5--;
            } else if (i5 < i3 + foliageAttachment.radiusOffset()) {
                i5++;
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageRadius(Random random, int i) {
        return super.foliageRadius(random, i) + random.nextInt(i + 1);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return this.height.sample(random);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return i == i4 && i3 == i4 && i4 > 0;
    }
}
