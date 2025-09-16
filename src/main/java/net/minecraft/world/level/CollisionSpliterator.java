package net.minecraft.world.level;

import java.util.Objects;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/CollisionSpliterator.class */
public class CollisionSpliterator extends Spliterators.AbstractSpliterator<VoxelShape> {

    @Nullable
    private final Entity source;
    private final AABB box;
    private final CollisionContext context;
    private final Cursor3D cursor;
    private final BlockPos.MutableBlockPos pos;
    private final VoxelShape entityShape;
    private final CollisionGetter collisionGetter;
    private boolean needsBorderCheck;
    private final BiPredicate<BlockState, BlockPos> predicate;

    public CollisionSpliterator(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aabb) {
        this(collisionGetter, entity, aabb, (blockState, blockPos) -> {
            return true;
        });
    }

    public CollisionSpliterator(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aabb, BiPredicate<BlockState, BlockPos> biPredicate) {
        super(Long.MAX_VALUE, 1280);
        this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        this.pos = new BlockPos.MutableBlockPos();
        this.entityShape = Shapes.create(aabb);
        this.collisionGetter = collisionGetter;
        this.needsBorderCheck = entity != null;
        this.source = entity;
        this.box = aabb;
        this.predicate = biPredicate;
        this.cursor = new Cursor3D(Mth.floor(aabb.minX - 1.0E-7d) - 1, Mth.floor(aabb.minY - 1.0E-7d) - 1, Mth.floor(aabb.minZ - 1.0E-7d) - 1, Mth.floor(aabb.maxX + 1.0E-7d) + 1, Mth.floor(aabb.maxY + 1.0E-7d) + 1, Mth.floor(aabb.maxZ + 1.0E-7d) + 1);
    }

    @Override // java.util.Spliterator
    public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
        return (this.needsBorderCheck && worldBorderCheck(consumer)) || collisionCheck(consumer);
    }

    boolean collisionCheck(Consumer<? super VoxelShape> consumer) {
        BlockGetter chunk;
        while (this.cursor.advance()) {
            int nextX = this.cursor.nextX();
            int nextY = this.cursor.nextY();
            int nextZ = this.cursor.nextZ();
            int nextType = this.cursor.getNextType();
            if (nextType != 3 && (chunk = getChunk(nextX, nextZ)) != null) {
                this.pos.set(nextX, nextY, nextZ);
                BlockState blockState = chunk.getBlockState(this.pos);
                if (this.predicate.test(blockState, this.pos) && (nextType != 1 || blockState.hasLargeCollisionShape())) {
                    if (nextType != 2 || blockState.is(Blocks.MOVING_PISTON)) {
                        VoxelShape collisionShape = blockState.getCollisionShape(this.collisionGetter, this.pos, this.context);
                        if (collisionShape == Shapes.block()) {
                            if (this.box.intersects(nextX, nextY, nextZ, nextX + 1.0d, nextY + 1.0d, nextZ + 1.0d)) {
                                consumer.accept(collisionShape.move(nextX, nextY, nextZ));
                                return true;
                            }
                        } else {
                            VoxelShape move = collisionShape.move(nextX, nextY, nextZ);
                            if (Shapes.joinIsNotEmpty(move, this.entityShape, BooleanOp.AND)) {
                                consumer.accept(move);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private BlockGetter getChunk(int i, int i2) {
        return this.collisionGetter.getChunkForCollisions(i >> 4, i2 >> 4);
    }

    boolean worldBorderCheck(Consumer<? super VoxelShape> consumer) {
        Objects.requireNonNull(this.source);
        this.needsBorderCheck = false;
        WorldBorder worldBorder = this.collisionGetter.getWorldBorder();
        AABB boundingBox = this.source.getBoundingBox();
        if (!isBoxFullyWithinWorldBorder(worldBorder, boundingBox)) {
            VoxelShape collisionShape = worldBorder.getCollisionShape();
            if (!isOutsideBorder(collisionShape, boundingBox) && isCloseToBorder(collisionShape, boundingBox)) {
                consumer.accept(collisionShape);
                return true;
            }
            return false;
        }
        return false;
    }

    private static boolean isCloseToBorder(VoxelShape voxelShape, AABB aabb) {
        return Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aabb.inflate(1.0E-7d)), BooleanOp.AND);
    }

    private static boolean isOutsideBorder(VoxelShape voxelShape, AABB aabb) {
        return Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aabb.deflate(1.0E-7d)), BooleanOp.AND);
    }

    public static boolean isBoxFullyWithinWorldBorder(WorldBorder worldBorder, AABB aabb) {
        double floor = Mth.floor(worldBorder.getMinX());
        double floor2 = Mth.floor(worldBorder.getMinZ());
        double ceil = Mth.ceil(worldBorder.getMaxX());
        double ceil2 = Mth.ceil(worldBorder.getMaxZ());
        return aabb.minX > floor && aabb.minX < ceil && aabb.minZ > floor2 && aabb.minZ < ceil2 && aabb.maxX > floor && aabb.maxX < ceil && aabb.maxZ > floor2 && aabb.maxZ < ceil2;
    }
}
