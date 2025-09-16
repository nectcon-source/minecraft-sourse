package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/treedecorators/TreeDecorator.class */
public abstract class TreeDecorator {
    public static final Codec<TreeDecorator> CODEC = Registry.TREE_DECORATOR_TYPES.dispatch((v0) -> {
        return v0.type();
    }, (v0) -> {
        return v0.codec();
    });

    protected abstract TreeDecoratorType<?> type();

    public abstract void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox);

    protected void placeVine(LevelWriter levelWriter, BlockPos blockPos, BooleanProperty booleanProperty, Set<BlockPos> set, BoundingBox boundingBox) {
        setBlock(levelWriter, blockPos, (BlockState) Blocks.VINE.defaultBlockState().setValue(booleanProperty, true), set, boundingBox);
    }

    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, Set<BlockPos> set, BoundingBox boundingBox) {
        levelWriter.setBlock(blockPos, blockState, 19);
        set.add(blockPos);
        boundingBox.expand(new BoundingBox(blockPos, blockPos));
    }
}
