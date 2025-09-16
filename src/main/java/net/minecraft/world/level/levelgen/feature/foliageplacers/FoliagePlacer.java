package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer.class */
public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.dispatch((v0) -> {
        return v0.type();
    }, (v0) -> {
        return v0.codec();
    });
    protected final UniformInt radius;
    protected final UniformInt offset;

    protected abstract FoliagePlacerType<?> type();

    protected abstract void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, int i4, BoundingBox boundingBox);

    public abstract int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration);

    protected abstract boolean shouldSkipLocation(Random random, int i, int i2, int i3, int i4, boolean z);

    protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, UniformInt, UniformInt> foliagePlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(UniformInt.codec(0, 8, 8).fieldOf("radius").forGetter(foliagePlacer -> {
            return foliagePlacer.radius;
        }), UniformInt.codec(0, 8, 8).fieldOf("offset").forGetter(foliagePlacer2 -> {
            return foliagePlacer2.offset;
        }));
    }

    public FoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2) {
        this.radius = uniformInt;
        this.offset = uniformInt2;
    }

    public void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int i2, int i3, Set<BlockPos> set, BoundingBox boundingBox) {
        createFoliage(levelSimulatedRW, random, treeConfiguration, i, foliageAttachment, i2, i3, set, offset(random), boundingBox);
    }

    public int foliageRadius(Random random, int i) {
        return this.radius.sample(random);
    }

    private int offset(Random random) {
        return this.offset.sample(random);
    }

    protected boolean shouldSkipLocationSigned(Random random, int i, int i2, int i3, int i4, boolean z) {
        int abs;
        int abs2;
        if (z) {
            abs = Math.min(Math.abs(i), Math.abs(i - 1));
            abs2 = Math.min(Math.abs(i3), Math.abs(i3 - 1));
        } else {
            abs = Math.abs(i);
            abs2 = Math.abs(i3);
        }
        return shouldSkipLocation(random, abs, i2, abs2, i4, z);
    }

    protected void placeLeavesRow(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, Set<BlockPos> set, int i2, boolean z, BoundingBox boundingBox) {
        int i3 = z ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i4 = -i; i4 <= i + i3; i4++) {
            for (int i5 = -i; i5 <= i + i3; i5++) {
                if (!shouldSkipLocationSigned(random, i4, i2, i5, i, z)) {
                    mutableBlockPos.setWithOffset(blockPos, i4, i2, i5);
                    if (TreeFeature.validTreePos(levelSimulatedRW, mutableBlockPos)) {
                        levelSimulatedRW.setBlock(mutableBlockPos, treeConfiguration.leavesProvider.getState(random, mutableBlockPos), 19);
                        boundingBox.expand(new BoundingBox(mutableBlockPos, mutableBlockPos));
                        set.add(mutableBlockPos.immutable());
                    }
                }
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageAttachment.class */
    public static final class FoliageAttachment {
        private final BlockPos foliagePos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos blockPos, int i, boolean z) {
            this.foliagePos = blockPos;
            this.radiusOffset = i;
            this.doubleTrunk = z;
        }

        public BlockPos foliagePos() {
            return this.foliagePos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }
}
