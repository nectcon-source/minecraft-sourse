package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/treedecorators/CocoaDecorator.class */
public class CocoaDecorator extends TreeDecorator {
    public static final Codec<CocoaDecorator> CODEC = Codec.floatRange(0.0f, 1.0f).fieldOf("probability").xmap((v1) -> {
        return new CocoaDecorator(v1);
    }, cocoaDecorator -> {
        return Float.valueOf(cocoaDecorator.probability);
    }).codec();
    private final float probability;

    public CocoaDecorator(float f) {
        this.probability = f;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        if (random.nextFloat() >= this.probability) {
            return;
        }
        int y = list.get(0).getY();
        list.stream().filter(blockPos -> {
            return blockPos.getY() - y <= 2;
        }).forEach(blockPos2 -> {
            Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
            while (it.hasNext()) {
                Direction next = it.next();
                if (random.nextFloat() <= 0.25f) {
                    Direction opposite = next.getOpposite();
                    BlockPos offset = blockPos2.offset(opposite.getStepX(), 0, opposite.getStepZ());
                    if (Feature.isAir(worldGenLevel, offset)) {
                        setBlock(worldGenLevel, offset, (BlockState) ((BlockState) Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, Integer.valueOf(random.nextInt(3)))).setValue(CocoaBlock.FACING, next), set, boundingBox);
                    }
                }
            }
        });
    }
}
