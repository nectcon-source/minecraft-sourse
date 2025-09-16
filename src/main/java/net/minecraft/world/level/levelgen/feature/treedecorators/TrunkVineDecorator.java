package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/treedecorators/TrunkVineDecorator.class */
public class TrunkVineDecorator extends TreeDecorator {

    public static final Codec<TrunkVineDecorator> CODEC = Codec.unit(() -> TrunkVineDecorator.INSTANCE);
    public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.TRUNK_VINE;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        list.forEach(blockPos -> {
            if (random.nextInt(3) > 0) {
                BlockPos west = blockPos.west();
                if (Feature.isAir(worldGenLevel, west)) {
                    placeVine(worldGenLevel, west, VineBlock.EAST, set, boundingBox);
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos east = blockPos.east();
                if (Feature.isAir(worldGenLevel, east)) {
                    placeVine(worldGenLevel, east, VineBlock.WEST, set, boundingBox);
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos north = blockPos.north();
                if (Feature.isAir(worldGenLevel, north)) {
                    placeVine(worldGenLevel, north, VineBlock.SOUTH, set, boundingBox);
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos south = blockPos.south();
                if (Feature.isAir(worldGenLevel, south)) {
                    placeVine(worldGenLevel, south, VineBlock.NORTH, set, boundingBox);
                }
            }
        });
    }
}
