package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FollowOwnerGoal.class */
public class FollowOwnerGoal extends Goal {
    private final TamableAnimal tamable;
    private LivingEntity owner;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowOwnerGoal(TamableAnimal tamableAnimal, double d, float f, float f2, boolean z) {
        this.tamable = tamableAnimal;
        this.level = tamableAnimal.level;
        this.speedModifier = d;
        this.navigation = tamableAnimal.getNavigation();
        this.startDistance = f;
        this.stopDistance = f2;
        this.canFly = z;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(tamableAnimal.getNavigation() instanceof GroundPathNavigation) && !(tamableAnimal.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        LivingEntity owner = this.tamable.getOwner();
        if (owner == null || owner.isSpectator() || this.tamable.isOrderedToSit() || this.tamable.distanceToSqr(owner) < this.startDistance * this.startDistance) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        if (this.navigation.isDone() || this.tamable.isOrderedToSit() || this.tamable.distanceToSqr(this.owner) <= this.stopDistance * this.stopDistance) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.tamable.getLookControl().setLookAt(this.owner, 10.0f, this.tamable.getMaxHeadXRot());
        int i = this.timeToRecalcPath - 1;
        this.timeToRecalcPath = i;
        if (i > 0) {
            return;
        }
        this.timeToRecalcPath = 10;
        if (this.tamable.isLeashed() || this.tamable.isPassenger()) {
            return;
        }
        if (this.tamable.distanceToSqr(this.owner) >= 144.0d) {
            teleportToOwner();
        } else {
            this.navigation.moveTo(this.owner, this.speedModifier);
        }
    }

    private void teleportToOwner() {
        BlockPos blockPosition = this.owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            if (maybeTeleportTo(blockPosition.getX() + randomIntInclusive(-3, 3), blockPosition.getY() + randomIntInclusive(-1, 1), blockPosition.getZ() + randomIntInclusive(-3, 3))) {
                return;
            }
        }
    }

    private boolean maybeTeleportTo(int i, int i2, int i3) {
        if ((Math.abs(i - this.owner.getX()) < 2.0d && Math.abs(i3 - this.owner.getZ()) < 2.0d) || !canTeleportTo(new BlockPos(i, i2, i3))) {
            return false;
        }
        this.tamable.moveTo(i + 0.5d, i2, i3 + 0.5d, this.tamable.yRot, this.tamable.xRot);
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos blockPos) {
        if (WalkNodeEvaluator.getBlockPathTypeStatic(this.level, blockPos.mutable()) != BlockPathTypes.WALKABLE) {
            return false;
        }
        BlockState blockState = this.level.getBlockState(blockPos.below());
        if (!this.canFly && (blockState.getBlock() instanceof LeavesBlock)) {
            return false;
        }
        if (!this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(blockPos.subtract(this.tamable.blockPosition())))) {
            return false;
        }
        return true;
    }

    private int randomIntInclusive(int i, int i2) {
        return this.tamable.getRandom().nextInt((i2 - i) + 1) + i;
    }
}
