package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/PathfinderMob.class */
public abstract class PathfinderMob extends Mob {
    protected PathfinderMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public float getWalkTargetValue(BlockPos blockPos) {
        return getWalkTargetValue(blockPos, this.level);
    }

    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0f;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType) {
        return getWalkTargetValue(blockPosition(), levelAccessor) >= 0.0f;
    }

    public boolean isPathFinding() {
        return !getNavigation().isDone();
    }

    @Override // net.minecraft.world.entity.Mob
    protected void tickLeash() {
        super.tickLeash();
        Entity leashHolder = getLeashHolder();
        if (leashHolder != null && leashHolder.level == this.level) {
            restrictTo(leashHolder.blockPosition(), 5);
            float distanceTo = distanceTo(leashHolder);
            if ((this instanceof TamableAnimal) && ((TamableAnimal) this).isInSittingPose()) {
                if (distanceTo > 10.0f) {
                    dropLeash(true, true);
                    return;
                }
                return;
            }
            onLeashDistance(distanceTo);
            if (distanceTo > 10.0f) {
                dropLeash(true, true);
                this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
            } else {
                if (distanceTo > 6.0f) {
                    double x = (leashHolder.getX() - getX()) / distanceTo;
                    double y = (leashHolder.getY() - getY()) / distanceTo;
                    double z = (leashHolder.getZ() - getZ()) / distanceTo;
                    setDeltaMovement(getDeltaMovement().add(Math.copySign(x * x * 0.4d, x), Math.copySign(y * y * 0.4d, y), Math.copySign(z * z * 0.4d, z)));
                    return;
                }
                this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
                Vec3 scale = new Vec3(leashHolder.getX() - getX(), leashHolder.getY() - getY(), leashHolder.getZ() - getZ()).normalize().scale(Math.max(distanceTo - 2.0f, 0.0f));
                getNavigation().moveTo(getX() + scale.x, getY() + scale.y, getZ() + scale.z, followLeashSpeed());
            }
        }
    }

    protected double followLeashSpeed() {
        return 1.0d;
    }

    protected void onLeashDistance(float f) {
    }
}
