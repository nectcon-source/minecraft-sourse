package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/treedecorators/AlterGroundDecorator.class */
public class AlterGroundDecorator extends TreeDecorator {
    public static final Codec<AlterGroundDecorator> CODEC = BlockStateProvider.CODEC.fieldOf("provider").xmap(AlterGroundDecorator::new, alterGroundDecorator -> {
        return alterGroundDecorator.provider;
    }).codec();
    private final BlockStateProvider provider;

    public AlterGroundDecorator(BlockStateProvider blockStateProvider) {
        this.provider = blockStateProvider;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ALTER_GROUND;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        int y = list.get(0).getY();
        list.stream().filter(blockPos -> {
            return blockPos.getY() == y;
        }).forEach(blockPos2 -> {
            placeCircle(worldGenLevel, random, blockPos2.west().north());
            placeCircle(worldGenLevel, random, blockPos2.east(2).north());
            placeCircle(worldGenLevel, random, blockPos2.west().south(2));
            placeCircle(worldGenLevel, random, blockPos2.east(2).south(2));
            for (int i = 0; i < 5; i++) {
                int nextInt = random.nextInt(64);
                int i2 = nextInt % 8;
                int i3 = nextInt / 8;
                if (i2 == 0 || i2 == 7 || i3 == 0 || i3 == 7) {
                    placeCircle(worldGenLevel, random, blockPos2.offset((-3) + i2, 0, (-3) + i3));
                }
            }
        });
    }

    private void placeCircle(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
        for (int i = -2; i <= 2; i++) {
            for (int i2 = -2; i2 <= 2; i2++) {
                if (Math.abs(i) != 2 || Math.abs(i2) != 2) {
                    placeBlockAt(levelSimulatedRW, random, blockPos.offset(i, 0, i2));
                }
            }
        }
    }

    private void placeBlockAt(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
        for (int i = 2; i >= -3; i--) {
            BlockPos above = blockPos.above(i);
            if (Feature.isGrassOrDirt(levelSimulatedRW, above)) {
                levelSimulatedRW.setBlock(above, this.provider.getState(random, blockPos), 19);
                return;
            } else {
                if (!Feature.isAir(levelSimulatedRW, above) && i < 0) {
                    return;
                }
            }
        }
    }
}
