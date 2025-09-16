package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/EntitySelector.class */
public final class EntitySelector {
    public static final Predicate<Entity> ENTITY_STILL_ALIVE = (v0) -> {
        return v0.isAlive();
    };
    public static final Predicate<LivingEntity> LIVING_ENTITY_STILL_ALIVE = (v0) -> {
        return v0.isAlive();
    };
    public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = entity -> {
        return (!entity.isAlive() || entity.isVehicle() || entity.isPassenger()) ? false : true;
    };
    public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = entity -> {
        return (entity instanceof Container) && entity.isAlive();
    };
    public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = entity -> {
        return ((entity instanceof Player) && (entity.isSpectator() || ((Player) entity).isCreative())) ? false : true;
    };
    public static final Predicate<Entity> ATTACK_ALLOWED = entity -> {
        return ((entity instanceof Player) && (entity.isSpectator() || ((Player) entity).isCreative() || entity.level.getDifficulty() == Difficulty.PEACEFUL)) ? false : true;
    };
    public static final Predicate<Entity> NO_SPECTATORS = entity -> {
        return !entity.isSpectator();
    };

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/EntitySelector$MobCanWearArmorEntitySelector.class */
    public static class MobCanWearArmorEntitySelector implements Predicate<Entity> {
        private final ItemStack itemStack;

        public MobCanWearArmorEntitySelector(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override // java.util.function.Predicate
        public boolean test(@Nullable Entity entity) {
            if (!entity.isAlive() || !(entity instanceof LivingEntity)) {
                return false;
            }
            return ((LivingEntity) entity).canTakeItem(this.itemStack);
        }
    }

    public static Predicate<Entity> withinDistance(double d, double d2, double d3, double d4) {
        double d5 = d4 * d4;
        return entity -> {
            return entity != null && entity.distanceToSqr(d, d2, d3) <= d5;
        };
    }

    public static Predicate<Entity> pushableBy(Entity entity) {
        Team team = entity.getTeam();
        Team.CollisionRule collisionRule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        if (collisionRule == Team.CollisionRule.NEVER) {
            return Predicates.alwaysFalse();
        }
        return NO_SPECTATORS.and(entity2 -> {
            if (!entity2.isPushable()) {
                return false;
            }
            if (entity.level.isClientSide && (!(entity2 instanceof Player) || !((Player) entity2).isLocalPlayer())) {
                return false;
            }
            Team team2 = entity2.getTeam();
            Team.CollisionRule collisionRule2 = team2 == null ? Team.CollisionRule.ALWAYS : team2.getCollisionRule();
            if (collisionRule2 == Team.CollisionRule.NEVER) {
                return false;
            }
            boolean z = team != null && team.isAlliedTo(team2);
            if ((collisionRule == Team.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == Team.CollisionRule.PUSH_OWN_TEAM) && z) {
                return false;
            }
            if ((collisionRule == Team.CollisionRule.PUSH_OTHER_TEAMS || collisionRule2 == Team.CollisionRule.PUSH_OTHER_TEAMS) && !z) {
                return false;
            }
            return true;
        });
    }

    public static Predicate<Entity> notRiding(Entity entity) {
        return entity2 -> {
            while (entity2.isPassenger()) {
                entity2 = entity2.getVehicle();
                if (entity2 == entity) {
                    return false;
                }
            }
            return true;
        };
    }
}
