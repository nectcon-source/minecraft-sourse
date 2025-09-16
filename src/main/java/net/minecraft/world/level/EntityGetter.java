package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/EntityGetter.class */
public interface EntityGetter {
    List<Entity> getEntities(@Nullable Entity entity, AABB aabb, @Nullable Predicate<? super Entity> predicate);

    <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> cls, AABB aabb, @Nullable Predicate<? super T> predicate);

    List<? extends Player> players();

    default <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> cls, AABB aabb, @Nullable Predicate<? super T> predicate) {
        return getEntitiesOfClass(cls, aabb, predicate);
    }

    default List<Entity> getEntities(@Nullable Entity entity, AABB aabb) {
        return getEntities(entity, aabb, EntitySelector.NO_SPECTATORS);
    }

    default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        if (voxelShape.isEmpty()) {
            return true;
        }
        for (Entity entity2 : getEntities(entity, voxelShape.bounds())) {
            if (!entity2.removed && entity2.blocksBuilding && (entity == null || !entity2.isPassengerOfSameVehicle(entity))) {
                if (Shapes.joinIsNotEmpty(voxelShape, Shapes.create(entity2.getBoundingBox()), BooleanOp.AND)) {
                    return false;
                }
            }
        }
        return true;
    }

    default <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> cls, AABB aabb) {
        return getEntitiesOfClass(cls, aabb, EntitySelector.NO_SPECTATORS);
    }

    default <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> cls, AABB aabb) {
        return getLoadedEntitiesOfClass(cls, aabb, EntitySelector.NO_SPECTATORS);
    }

    default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb, Predicate<Entity> predicate) {
        if (aabb.getSize() < 1.0E-7d) {
            return Stream.empty();
        }
        AABB inflate = aabb.inflate(1.0E-7d);
        return getEntities(entity, inflate, predicate.and(entity2 -> {
            return entity2.getBoundingBox().intersects(inflate) && (entity != null ? entity.canCollideWith(entity2) : entity2.canBeCollidedWith());
        })).stream().map((v0) -> {
            return v0.getBoundingBox();
        }).map(Shapes::create);
    }

    @Nullable
    default Player getNearestPlayer(double d, double d2, double d3, double d4, @Nullable Predicate<Entity> predicate) {
        double d5 = -1.0d;
        Player player = null;
        for (Player player2 : players()) {
            if (predicate == null || predicate.test(player2)) {
                double distanceToSqr = player2.distanceToSqr(d, d2, d3);
                if (d4 < 0.0d || distanceToSqr < d4 * d4) {
                    if (d5 == -1.0d || distanceToSqr < d5) {
                        d5 = distanceToSqr;
                        player = player2;
                    }
                }
            }
        }
        return player;
    }

    @Nullable
    default Player getNearestPlayer(Entity entity, double d) {
        return getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), d, false);
    }

    @Nullable
    default Player getNearestPlayer(double d, double d2, double d3, double d4, boolean z) {
        return getNearestPlayer(d, d2, d3, d4, z ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS);
    }

    default boolean hasNearbyAlivePlayer(double d, double d2, double d3, double d4) {
        for (Player player : players()) {
            if (EntitySelector.NO_SPECTATORS.test(player) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player)) {
                double distanceToSqr = player.distanceToSqr(d, d2, d3);
                if (d4 < 0.0d || distanceToSqr < d4 * d4) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    default Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity) {
        return (Player) getNearestEntity(players(), targetingConditions, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
    }

    @Nullable
    default Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity, double d, double d2, double d3) {
        return (Player) getNearestEntity(players(), targetingConditions, livingEntity, d, d2, d3);
    }

    @Nullable
    default Player getNearestPlayer(TargetingConditions targetingConditions, double d, double d2, double d3) {
        return (Player) getNearestEntity(players(), targetingConditions, null, d, d2, d3);
    }

    @Nullable
    default <T extends LivingEntity> T getNearestEntity(Class<? extends T> cls, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double d2, double d3, AABB aabb) {
        return (T) getNearestEntity(getEntitiesOfClass(cls, aabb, null), targetingConditions, livingEntity, d, d2, d3);
    }

    @Nullable
    default <T extends LivingEntity> T getNearestLoadedEntity(Class<? extends T> cls, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double d2, double d3, AABB aabb) {
        return (T) getNearestEntity(getLoadedEntitiesOfClass(cls, aabb, null), targetingConditions, livingEntity, d, d2, d3);
    }

    @Nullable
    default <T extends LivingEntity> T getNearestEntity(List<? extends T> list, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double d2, double d3) {
        double d4 = -1.0d;
        T t = null;
        for (T t2 : list) {
            if (targetingConditions.test(livingEntity, t2)) {
                double distanceToSqr = t2.distanceToSqr(d, d2, d3);
                if (d4 == -1.0d || distanceToSqr < d4) {
                    d4 = distanceToSqr;
                    t = t2;
                }
            }
        }
        return t;
    }

    default List<Player> getNearbyPlayers(TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aabb) {
        List<Player> newArrayList = Lists.newArrayList();
        for (Player player : players()) {
            if (aabb.contains(player.getX(), player.getY(), player.getZ()) && targetingConditions.test(livingEntity, player)) {
                newArrayList.add(player);
            }
        }
        return newArrayList;
    }

    default <T extends LivingEntity> List<T> getNearbyEntities(Class<? extends T> cls, TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aabb) {
        List<T> entitiesOfClass = getEntitiesOfClass(cls, aabb, null);
        List<T> newArrayList = Lists.newArrayList();
        for (T t : entitiesOfClass) {
            if (targetingConditions.test(livingEntity, t)) {
                newArrayList.add(t);
            }
        }
        return newArrayList;
    }

    @Nullable
    default Player getPlayerByUUID(UUID uuid) {
        for (int i = 0; i < players().size(); i++) {
            Player player = players().get(i);
            if (uuid.equals(player.getUUID())) {
                return player;
            }
        }
        return null;
    }
}
