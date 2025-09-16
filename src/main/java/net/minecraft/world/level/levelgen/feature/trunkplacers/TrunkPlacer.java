package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/trunkplacers/TrunkPlacer.class */
public abstract class TrunkPlacer {
    public static final Codec<TrunkPlacer> CODEC = Registry.TRUNK_PLACER_TYPES.dispatch((v0) -> {
        return v0.type();
    }, (v0) -> {
        return v0.codec();
    });
    protected final int baseHeight;
    protected final int heightRandA;
    protected final int heightRandB;

    protected abstract TrunkPlacerType<?> type();

    public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration);

    protected static <P extends TrunkPlacer> Products.P3<RecordCodecBuilder.Mu<P>, Integer, Integer, Integer> trunkPlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(Codec.intRange(0, 32).fieldOf("base_height").forGetter(trunkPlacer -> {
            return Integer.valueOf(trunkPlacer.baseHeight);
        }), Codec.intRange(0, 24).fieldOf("height_rand_a").forGetter(trunkPlacer2 -> {
            return Integer.valueOf(trunkPlacer2.heightRandA);
        }), Codec.intRange(0, 24).fieldOf("height_rand_b").forGetter(trunkPlacer3 -> {
            return Integer.valueOf(trunkPlacer3.heightRandB);
        }));
    }

    public TrunkPlacer(int i, int i2, int i3) {
        this.baseHeight = i;
        this.heightRandA = i2;
        this.heightRandB = i3;
    }

    public int getTreeHeight(Random random) {
        return this.baseHeight + random.nextInt(this.heightRandA + 1) + random.nextInt(this.heightRandB + 1);
    }

    protected static void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, BoundingBox boundingBox) {
        TreeFeature.setBlockKnownShape(levelWriter, blockPos, blockState);
        boundingBox.expand(new BoundingBox(blockPos, blockPos));
    }

    private static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            return (!Feature.isDirt(blockState.getBlock()) || blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) ? false : true;
        });
    }

    protected static void setDirtAt(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
        if (!isDirt(levelSimulatedRW, blockPos)) {
            TreeFeature.setBlockKnownShape(levelSimulatedRW, blockPos, Blocks.DIRT.defaultBlockState());
        }
    }

    protected static boolean placeLog(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        if (TreeFeature.validTreePos(levelSimulatedRW, blockPos)) {
            setBlock(levelSimulatedRW, blockPos, treeConfiguration.trunkProvider.getState(random, blockPos), boundingBox);
            set.add(blockPos.immutable());
            return true;
        }
        return false;
    }

    protected static void placeLogIfFree(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos.MutableBlockPos mutableBlockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        if (TreeFeature.isFree(levelSimulatedRW, mutableBlockPos)) {
            placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration);
        }
    }
}
