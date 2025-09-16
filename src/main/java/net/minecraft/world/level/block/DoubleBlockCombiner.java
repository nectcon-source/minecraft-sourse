package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoubleBlockCombiner.class */
public class DoubleBlockCombiner {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoubleBlockCombiner$BlockType.class */
    public enum BlockType {
        SINGLE,
        FIRST,
        SECOND
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoubleBlockCombiner$Combiner.class */
    public interface Combiner<S, T> {
        T acceptDouble(S s, S s2);

        T acceptSingle(S s);

        T acceptNone();
    }

    public static <S extends BlockEntity> NeighborCombineResult<S> combineWithNeigbour(BlockEntityType<S> blockEntityType, Function<BlockState, BlockType> function, Function<BlockState, Direction> function2, DirectionProperty directionProperty, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BiPredicate<LevelAccessor, BlockPos> biPredicate) {
        S var8 = blockEntityType.getBlockEntity(levelAccessor, blockPos);
        if (var8 == null) {
            return Combiner::acceptNone;
        } else if (biPredicate.test(levelAccessor, blockPos)) {
            return Combiner::acceptNone;
        } else {
            BlockType var9 = (BlockType)function.apply(blockState);
            boolean var10 = var9 == DoubleBlockCombiner.BlockType.SINGLE;
            boolean var11 = var9 == DoubleBlockCombiner.BlockType.FIRST;
            if (var10) {
                return new NeighborCombineResult.Single<S>(var8);
            } else {
                BlockPos var12 = blockPos.relative((Direction)function2.apply(blockState));
                BlockState var13 = levelAccessor.getBlockState(var12);
                if (var13.is(blockState.getBlock())) {
                    BlockType var14 = (BlockType)function.apply(var13);
                    if (var14 != DoubleBlockCombiner.BlockType.SINGLE && var9 != var14 && var13.getValue(directionProperty) == blockState.getValue(directionProperty)) {
                        if (biPredicate.test(levelAccessor, var12)) {
                            return Combiner::acceptNone;
                        }

                        S var15 = blockEntityType.getBlockEntity(levelAccessor, var12);
                        if (var15 != null) {
                            S var16 = var11 ? var8 : var15;
                            S var17 = var11 ? var15 : var8;
                            return new NeighborCombineResult.Double<S>(var16, var17);
                        }
                    }
                }

                return new NeighborCombineResult.Single<S>(var8);
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoubleBlockCombiner$NeighborCombineResult.class */
    public interface NeighborCombineResult<S> {
        <T> T apply(Combiner<? super S, T> combiner);

        /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoubleBlockCombiner$NeighborCombineResult$Double.class */
        public static final class Double<S> implements NeighborCombineResult<S> {
            private final S first;
            private final S second;

            public Double(S s, S s2) {
                this.first = s;
                this.second = s2;
            }

            @Override // net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult
            public <T> T apply(Combiner<? super S, T> combiner) {
                return combiner.acceptDouble(this.first, this.second);
            }
        }

        /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DoubleBlockCombiner$NeighborCombineResult$Single.class */
        public static final class Single<S> implements NeighborCombineResult<S> {
            private final S single;

            public Single(S s) {
                this.single = s;
            }

            @Override // net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult
            public <T> T apply(Combiner<? super S, T> combiner) {
                return combiner.acceptSingle(this.single);
            }
        }
    }
}
