package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/pattern/BlockPattern.class */
public class BlockPattern {
    private final Predicate<BlockInWorld>[][][] pattern;
    private final int depth;
    private final int height;
    private final int width;

    public BlockPattern(Predicate<BlockInWorld>[][][] predicateArr) {
        this.pattern = predicateArr;
        this.depth = predicateArr.length;
        if (this.depth > 0) {
            this.height = predicateArr[0].length;
            if (this.height > 0) {
                this.width = predicateArr[0][0].length;
                return;
            } else {
                this.width = 0;
                return;
            }
        }
        this.height = 0;
        this.width = 0;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    @Nullable
    private BlockPatternMatch matches(BlockPos blockPos, Direction direction, Direction direction2, LoadingCache<BlockPos, BlockInWorld> loadingCache) {
        for (int i = 0; i < this.width; i++) {
            for (int i2 = 0; i2 < this.height; i2++) {
                for (int i3 = 0; i3 < this.depth; i3++) {
                    if (!this.pattern[i3][i2][i].test(loadingCache.getUnchecked(translateAndRotate(blockPos, direction, direction2, i, i2, i3)))) {
                        return null;
                    }
                }
            }
        }
        return new BlockPatternMatch(blockPos, direction, direction2, loadingCache, this.width, this.height, this.depth);
    }

    @Nullable
    public BlockPatternMatch find(LevelReader levelReader, BlockPos blockPos) {
        BlockPatternMatch matches;
        LoadingCache<BlockPos, BlockInWorld> createLevelCache = createLevelCache(levelReader, false);
        int max = Math.max(Math.max(this.width, this.height), this.depth);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(max - 1, max - 1, max - 1))) {
            for (Direction direction : Direction.values()) {
                for (Direction direction2 : Direction.values()) {
                    if (direction2 != direction && direction2 != direction.getOpposite() && (matches = matches(blockPos2, direction, direction2, createLevelCache)) != null) {
                        return matches;
                    }
                }
            }
        }
        return null;
    }

    public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader levelReader, boolean z) {
        return CacheBuilder.newBuilder().build(new BlockCacheLoader(levelReader, z));
    }

    protected static BlockPos translateAndRotate(BlockPos blockPos, Direction direction, Direction direction2, int i, int i2, int i3) {
        if (direction == direction2 || direction == direction2.getOpposite()) {
            throw new IllegalArgumentException("Invalid forwards & up combination");
        }
        Vec3i vec3i = new Vec3i(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        Vec3i vec3i2 = new Vec3i(direction2.getStepX(), direction2.getStepY(), direction2.getStepZ());
        Vec3i cross = vec3i.cross(vec3i2);
        return blockPos.offset((vec3i2.getX() * (-i2)) + (cross.getX() * i) + (vec3i.getX() * i3), (vec3i2.getY() * (-i2)) + (cross.getY() * i) + (vec3i.getY() * i3), (vec3i2.getZ() * (-i2)) + (cross.getZ() * i) + (vec3i.getZ() * i3));
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/pattern/BlockPattern$BlockCacheLoader.class */
    static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld> {
        private final LevelReader level;
        private final boolean loadChunks;

        public BlockCacheLoader(LevelReader levelReader, boolean z) {
            this.level = levelReader;
            this.loadChunks = z;
        }

        public BlockInWorld load(BlockPos blockPos) throws Exception {
            return new BlockInWorld(this.level, blockPos, this.loadChunks);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch.class */
    public static class BlockPatternMatch {
        private final BlockPos frontTopLeft;
        private final Direction forwards;

        /* renamed from: up */
        private final Direction UP;
        private final LoadingCache<BlockPos, BlockInWorld> cache;
        private final int width;
        private final int height;
        private final int depth;

        public BlockPatternMatch(BlockPos blockPos, Direction direction, Direction direction2, LoadingCache<BlockPos, BlockInWorld> loadingCache, int i, int i2, int i3) {
            this.frontTopLeft = blockPos;
            this.forwards = direction;
            this.UP = direction2;
            this.cache = loadingCache;
            this.width = i;
            this.height = i2;
            this.depth = i3;
        }

        public BlockPos getFrontTopLeft() {
            return this.frontTopLeft;
        }

        public Direction getForwards() {
            return this.forwards;
        }

        public Direction getUp() {
            return this.UP;
        }

        public BlockInWorld getBlock(int i, int i2, int i3) {
            return (BlockInWorld) this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, getForwards(), getUp(), i, i2, i3));
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("up", this.UP).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
        }
    }
}
