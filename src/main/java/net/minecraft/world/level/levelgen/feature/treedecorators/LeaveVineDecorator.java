package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/treedecorators/LeaveVineDecorator.class */
public class LeaveVineDecorator extends TreeDecorator {

    public static final Codec<LeaveVineDecorator> CODEC = Codec.unit(() -> LeaveVineDecorator.INSTANCE);

    public static final LeaveVineDecorator INSTANCE = new LeaveVineDecorator();

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.LEAVE_VINE;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        list2.forEach(blockPos -> {
            if (random.nextInt(4) == 0) {
                BlockPos west = blockPos.west();
                if (Feature.isAir(worldGenLevel, west)) {
                    addHangingVine(worldGenLevel, west, VineBlock.EAST, set, boundingBox);
                }
            }
            if (random.nextInt(4) == 0) {
                BlockPos east = blockPos.east();
                if (Feature.isAir(worldGenLevel, east)) {
                    addHangingVine(worldGenLevel, east, VineBlock.WEST, set, boundingBox);
                }
            }
            if (random.nextInt(4) == 0) {
                BlockPos north = blockPos.north();
                if (Feature.isAir(worldGenLevel, north)) {
                    addHangingVine(worldGenLevel, north, VineBlock.SOUTH, set, boundingBox);
                }
            }
            if (random.nextInt(4) == 0) {
                BlockPos south = blockPos.south();
                if (Feature.isAir(worldGenLevel, south)) {
                    addHangingVine(worldGenLevel, south, VineBlock.NORTH, set, boundingBox);
                }
            }
        });
    }

    private void addHangingVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty, Set<BlockPos> set, BoundingBox boundingBox) {
        placeVine(levelSimulatedRW, blockPos, booleanProperty, set, boundingBox);
        BlockPos below = blockPos.below();
        for (int i = 4; Feature.isAir(levelSimulatedRW, below) && i > 0; i--) {
            placeVine(levelSimulatedRW, below, booleanProperty, set, boundingBox);
            below = below.below();
        }
    }
}
