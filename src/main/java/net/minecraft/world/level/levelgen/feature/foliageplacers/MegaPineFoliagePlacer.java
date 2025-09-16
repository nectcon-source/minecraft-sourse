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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/MegaPineFoliagePlacer.class */
public class MegaPineFoliagePlacer extends FoliagePlacer {
    public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return foliagePlacerParts(instance).and(UniformInt.codec(0, 16, 8).fieldOf("crown_height").forGetter(megaPineFoliagePlacer -> {
            return megaPineFoliagePlacer.crownHeight;
        })).apply(instance, MegaPineFoliagePlacer::new);
    });
    private final UniformInt crownHeight;

    public MegaPineFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3) {
        super(uniformInt, uniformInt2);
        this.crownHeight = uniformInt3;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        int i5;
        BlockPos foliagePos = foliageAttachment.foliagePos();
        int i6 = 0;
        for (int y = (foliagePos.getY() - i2) + i4; y <= foliagePos.getY() + i4; y++) {
            int y2 = foliagePos.getY() - y;
            int radiusOffset = i3 + foliageAttachment.radiusOffset() + Mth.floor((y2 / i2) * 3.5f);
            if (y2 > 0 && radiusOffset == i6 && (y & 1) == 0) {
                i5 = radiusOffset + 1;
            } else {
                i5 = radiusOffset;
            }
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, new BlockPos(foliagePos.getX(), y, foliagePos.getZ()), i5, set, 0, foliageAttachment.doubleTrunk(), boundingBox);
            i6 = radiusOffset;
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return this.crownHeight.sample(random);
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return i + i3 >= 7 || (i * i) + (i3 * i3) > i4 * i4;
    }
}
