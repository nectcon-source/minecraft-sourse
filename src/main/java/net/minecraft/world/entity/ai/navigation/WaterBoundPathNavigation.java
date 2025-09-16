package net.minecraft.world.entity.ai.navigation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/navigation/WaterBoundPathNavigation.class */
public class WaterBoundPathNavigation extends PathNavigation {
    private boolean allowBreaching;

    public WaterBoundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected PathFinder createPathFinder(int i) {
        this.allowBreaching = this.mob instanceof Dolphin;
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
        return new PathFinder(this.nodeEvaluator, i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected boolean canUpdatePath() {
        return this.allowBreaching || isInLiquid();
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5d), this.mob.getZ());
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            recomputePath();
        }
        if (isDone()) {
            return;
        }
        if (canUpdatePath()) {
            followThePath();
        } else if (this.path != null && !this.path.isDone()) {
            Vec3 nextEntityPos = this.path.getNextEntityPos(this.mob);
            if (Mth.floor(this.mob.getX()) == Mth.floor(nextEntityPos.x) && Mth.floor(this.mob.getY()) == Mth.floor(nextEntityPos.y) && Mth.floor(this.mob.getZ()) == Mth.floor(nextEntityPos.z)) {
                this.path.advance();
            }
        }
        DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
        if (isDone()) {
            return;
        }
        Vec3 nextEntityPos2 = this.path.getNextEntityPos(this.mob);
        this.mob.getMoveControl().setWantedPosition(nextEntityPos2.x, nextEntityPos2.y, nextEntityPos2.z, this.speedModifier);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected void followThePath() {
        if (this.path == null) {
            return;
        }
        Vec3 tempMobPos = getTempMobPos();
        float bbWidth = this.mob.getBbWidth();
        float f = bbWidth > 0.75f ? bbWidth / 2.0f : 0.75f - (bbWidth / 2.0f);
        Vec3 deltaMovement = this.mob.getDeltaMovement();
        if (Math.abs(deltaMovement.x) > 0.2d || Math.abs(deltaMovement.z) > 0.2d) {
            f = (float) (f * deltaMovement.length() * 6.0d);
        }
        Vec3 atBottomCenterOf = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        if (Math.abs(this.mob.getX() - atBottomCenterOf.x) < f && Math.abs(this.mob.getZ() - atBottomCenterOf.z) < f && Math.abs(this.mob.getY() - atBottomCenterOf.y) < f * 2.0f) {
            this.path.advance();
        }
        int min = Math.min(this.path.getNextNodeIndex() + 6, this.path.getNodeCount() - 1);
        while (true) {
            if (min <= this.path.getNextNodeIndex()) {
                break;
            }
            Vec3 entityPosAtNode = this.path.getEntityPosAtNode(this.mob, min);
            if (entityPosAtNode.distanceToSqr(tempMobPos) > 36.0d || !canMoveDirectly(tempMobPos, entityPosAtNode, 0, 0, 0)) {
                min--;
            } else {
                this.path.setNextNodeIndex(min);
                break;
            }
        }
        doStuckDetection(tempMobPos);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected void doStuckDetection(Vec3 vec3) {
        if (this.tick - this.lastStuckCheck > 100) {
            if (vec3.distanceToSqr(this.lastStuckCheckPos) < 2.25d) {
                stop();
            }
            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = vec3;
        }
        if (this.path != null && !this.path.isDone()) {
            Vec3i nextNodePos = this.path.getNextNodePos();
            if (nextNodePos.equals(this.timeoutCachedNode)) {
                this.timeoutTimer += Util.getMillis() - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = nextNodePos;
                this.timeoutLimit = this.mob.getSpeed() > 0.0f ? (vec3.distanceTo(Vec3.atCenterOf(this.timeoutCachedNode)) / this.mob.getSpeed()) * 100.0d : 0.0d;
            }
            if (this.timeoutLimit > 0.0d && this.timeoutTimer > this.timeoutLimit * 2.0d) {
                this.timeoutCachedNode = Vec3i.ZERO;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0d;
                stop();
            }
            this.lastTimeoutCheck = Util.getMillis();
        }
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int i2, int i3) {
        return this.level.clip(new ClipContext(vec3, new Vec3(vec32.x, vec32.y + (((double) this.mob.getBbHeight()) * 0.5d), vec32.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public boolean isStableDestination(BlockPos blockPos) {
        return !this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public void setCanFloat(boolean z) {
    }
}
