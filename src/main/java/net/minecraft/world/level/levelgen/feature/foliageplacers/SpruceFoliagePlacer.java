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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/SpruceFoliagePlacer.class */
public class SpruceFoliagePlacer extends FoliagePlacer {
    public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> {
        return foliagePlacerParts(instance).and(UniformInt.codec(0, 16, 8).fieldOf("trunk_height").forGetter(spruceFoliagePlacer -> {
            return spruceFoliagePlacer.trunkHeight;
        })).apply(instance, SpruceFoliagePlacer::new);
    });
    private final UniformInt trunkHeight;

    public SpruceFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3) {
        super(uniformInt, uniformInt2);
        this.trunkHeight = uniformInt3;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox) {
        BlockPos foliagePos = foliageAttachment.foliagePos();
        int nextInt = random.nextInt(2);
        int i5 = 1;
        int i6 = 0;
        for (int i7 = i4; i7 >= (-i2); i7--) {
            placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliagePos, nextInt, set, i7, foliageAttachment.doubleTrunk(), boundingBox);
            if (nextInt >= i5) {
                nextInt = i6;
                i6 = 1;
                i5 = Math.min(i5 + 1, i3 + foliageAttachment.radiusOffset());
            } else {
                nextInt++;
            }
        }
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return Math.max(4, i - this.trunkHeight.sample(random));
    }

    @Override // net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
    protected boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z) {
        return i == i4 && i3 == i4 && i4 > 0;
    }
}
