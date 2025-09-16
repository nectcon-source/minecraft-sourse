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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/MegaJungleFoliagePlacer.class */
public class MegaJungleFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter(megaJungleFoliagePlacer -> {
            return Integer.valueOf(megaJungleFoliagePlacer.height);
        })).apply(instance, (v1, v2, v3) -> {
            return new MegaJungleFoliagePlacer(v1, v2, v3);
        });
    });
    protected final int height;

    public MegaJungleFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, int i) {
        super(uniformInt, uniformInt2);
        this.height = i;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        int nextInt = foliageAttachment.doubleTrunk() ? i2 : 1 + random.nextInt(2);
        for (int i5 = i4; i5 >= i4 - nextInt; i5--) {
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), ((i3 + foliageAttachment.radiusOffset()) + 1) - i5, set, i5, foliageAttachment.doubleTrunk(), boundingBox);
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return this.height;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return i + i3 >= 7 || (i * i) + (i3 * i3) > i4 * i4;
    }
}
