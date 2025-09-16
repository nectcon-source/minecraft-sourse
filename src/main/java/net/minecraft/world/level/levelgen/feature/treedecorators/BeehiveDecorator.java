package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/treedecorators/BeehiveDecorator.class */
public class BeehiveDecorator extends TreeDecorator {
    public static final Codec<BeehiveDecorator> CODEC = Codec.floatRange(0.0f, 1.0f).fieldOf("probability").xmap((v1) -> {
        return new BeehiveDecorator(v1);
    }, beehiveDecorator -> {
        return Float.valueOf(beehiveDecorator.probability);
    }).codec();
    private final float probability;

    public BeehiveDecorator(float f) {
        this.probability = f;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override // net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        if (random.nextFloat() >= this.probability) {
            return;
        }
        Direction randomOffset = BeehiveBlock.getRandomOffset(random);
        int max = !list2.isEmpty() ? Math.max(list2.get(0).getY() - 1, list.get(0).getY()) : Math.min(list.get(0).getY() + 1 + random.nextInt(3), list.get(list.size() - 1).getY());
        List<BlockPos> list3 =  list.stream().filter(blockPos -> {
            return blockPos.getY() == max;
        }).collect(Collectors.toList());
        if (list3.isEmpty()) {
            return;
        }
        BlockPos relative = list3.get(random.nextInt(list3.size())).relative(randomOffset);
        if (!Feature.isAir(worldGenLevel, relative) || !Feature.isAir(worldGenLevel, relative.relative(Direction.SOUTH))) {
            return;
        }
        setBlock(worldGenLevel, relative,  Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH), set, boundingBox);
        BlockEntity blockEntity = worldGenLevel.getBlockEntity(relative);
        if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) blockEntity;
            int nextInt = 2 + random.nextInt(2);
            for (int i = 0; i < nextInt; i++) {
                beehiveBlockEntity.addOccupantWithPresetTicks(new Bee(EntityType.BEE, worldGenLevel.getLevel()), false, random.nextInt(599));
            }
        }
    }
}
