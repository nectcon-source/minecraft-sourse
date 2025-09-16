package net.minecraft.world.level;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/CollisionGetter.class */
public interface CollisionGetter extends BlockGetter {
    WorldBorder getWorldBorder();

    @Nullable
    BlockGetter getChunkForCollisions(int i, int i2);

    Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb, Predicate<Entity> predicate);

    default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        return true;
    }

    default boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape collisionShape = blockState.getCollisionShape(this, blockPos, collisionContext);
        return collisionShape.isEmpty() || isUnobstructed(null, collisionShape.move((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ()));
    }

    default boolean isUnobstructed(Entity entity) {
        return isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
    }

    default boolean noCollision(AABB aabb) {
        return noCollision(null, aabb, entity -> {
            return true;
        });
    }

    default boolean noCollision(Entity entity) {
        return noCollision(entity, entity.getBoundingBox(), entity2 -> {
            return true;
        });
    }

    default boolean noCollision(Entity entity, AABB aabb) {
        return noCollision(entity, aabb, entity2 -> {
            return true;
        });
    }

    default boolean noCollision(@Nullable Entity entity, AABB aabb, Predicate<Entity> predicate) {
        return getCollisions(entity, aabb, predicate).allMatch((v0) -> {
            return v0.isEmpty();
        });
    }

    default Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aabb, Predicate<Entity> predicate) {
        return Stream.concat(getBlockCollisions(entity, aabb), getEntityCollisions(entity, aabb, predicate));
    }

    default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aabb) {
        return StreamSupport.stream(new CollisionSpliterator(this, entity, aabb), false);
    }

    default boolean noBlockCollision(@Nullable Entity entity, AABB aabb, BiPredicate<BlockState, BlockPos> biPredicate) {
        return getBlockCollisions(entity, aabb, biPredicate).allMatch((v0) -> {
            return v0.isEmpty();
        });
    }

    default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aabb, BiPredicate<BlockState, BlockPos> biPredicate) {
        return StreamSupport.stream(new CollisionSpliterator(this, entity, aabb, biPredicate), false);
    }
}
