package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/navigation/PathNavigation.class */
public abstract class PathNavigation {
    protected final Mob mob;
    protected final Level level;

    @Nullable
    protected Path path;
    protected double speedModifier;
    protected int tick;
    protected int lastStuckCheck;
    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    protected NodeEvaluator nodeEvaluator;
    private BlockPos targetPos;
    private int reachRange;
    private final PathFinder pathFinder;
    private boolean isStuck;
    protected Vec3 lastStuckCheckPos = Vec3.ZERO;
    protected Vec3i timeoutCachedNode = Vec3i.ZERO;
    protected float maxDistanceToWaypoint = 0.5f;
    private float maxVisitedNodesMultiplier = 1.0f;

    protected abstract PathFinder createPathFinder(int i);

    protected abstract Vec3 getTempMobPos();

    protected abstract boolean canUpdatePath();

    protected abstract boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int i2, int i3);

    public PathNavigation(Mob mob, Level level) {
        this.mob = mob;
        this.level = level;
        this.pathFinder = createPathFinder(Mth.floor(mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0d));
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0f;
    }

    public void setMaxVisitedNodesMultiplier(float f) {
        this.maxVisitedNodesMultiplier = f;
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    public void setSpeedModifier(double d) {
        this.speedModifier = d;
    }

    public boolean hasDelayedRecomputation() {
        return this.hasDelayedRecomputation;
    }

    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
                return;
            }
            return;
        }
        this.hasDelayedRecomputation = true;
    }

    @Nullable
    public final Path createPath(double d, double d2, double d3, int i) {
        return createPath(new BlockPos(d, d2, d3), i);
    }

    @Nullable
    public Path createPath(Stream<BlockPos> stream, int i) {
        return createPath((Set<BlockPos>) stream.collect(Collectors.toSet()), 8, false, i);
    }

    @Nullable
    public Path createPath(Set<BlockPos> set, int i) {
        return createPath(set, 8, false, i);
    }

    @Nullable
    public Path createPath(BlockPos blockPos, int i) {
        return createPath((Set<BlockPos>) ImmutableSet.of(blockPos), 8, false, i);
    }

    @Nullable
    public Path createPath(Entity entity, int i) {
        return createPath((Set<BlockPos>) ImmutableSet.of(entity.blockPosition()), 16, true, i);
    }

    @Nullable
    protected Path createPath(Set<BlockPos> set, int i, boolean z, int i2) {
        if (set.isEmpty() || this.mob.getY() < 0.0d || !canUpdatePath()) {
            return null;
        }
        if (this.path != null && !this.path.isDone() && set.contains(this.targetPos)) {
            return this.path;
        }
        this.level.getProfiler().push("pathfind");
        float attributeValue = (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        BlockPos above = z ? this.mob.blockPosition().above() : this.mob.blockPosition();
        int i3 = (int) (attributeValue + i);
        Path findPath = this.pathFinder.findPath(new PathNavigationRegion(this.level, above.offset(-i3, -i3, -i3), above.offset(i3, i3, i3)), this.mob, set, attributeValue, i2, this.maxVisitedNodesMultiplier);
        this.level.getProfiler().pop();
        if (findPath != null && findPath.getTarget() != null) {
            this.targetPos = findPath.getTarget();
            this.reachRange = i2;
            resetStuckTimeout();
        }
        return findPath;
    }

    public boolean moveTo(double d, double d2, double d3, double d4) {
        return moveTo(createPath(d, d2, d3, 1), d4);
    }

    public boolean moveTo(Entity entity, double d) {
        Path createPath = createPath(entity, 1);
        return createPath != null && moveTo(createPath, d);
    }

    public boolean moveTo(@Nullable Path path, double d) {
        if (path == null) {
            this.path = null;
            return false;
        }
        if (!path.sameAs(this.path)) {
            this.path = path;
        }
        if (isDone()) {
            return false;
        }
        trimPath();
        if (this.path.getNodeCount() <= 0) {
            return false;
        }
        this.speedModifier = d;
        Vec3 tempMobPos = getTempMobPos();
        this.lastStuckCheck = this.tick;
        this.lastStuckCheckPos = tempMobPos;
        return true;
    }

    @Nullable
    public Path getPath() {
        return this.path;
    }

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
            Vec3 tempMobPos = getTempMobPos();
            Vec3 nextEntityPos = this.path.getNextEntityPos(this.mob);
            if (tempMobPos.y > nextEntityPos.y && !this.mob.isOnGround() && Mth.floor(tempMobPos.x) == Mth.floor(nextEntityPos.x) && Mth.floor(tempMobPos.z) == Mth.floor(nextEntityPos.z)) {
                this.path.advance();
            }
        }
        DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
        if (isDone()) {
            return;
        }
        Vec3 nextEntityPos2 = this.path.getNextEntityPos(this.mob);
        BlockPos blockPos = new BlockPos(nextEntityPos2);
        this.mob.getMoveControl().setWantedPosition(nextEntityPos2.x, this.level.getBlockState(blockPos.below()).isAir() ? nextEntityPos2.y : WalkNodeEvaluator.getFloorLevel(this.level, blockPos), nextEntityPos2.z, this.speedModifier);
    }

    protected void followThePath() {
        Vec3 tempMobPos = getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75f ? this.mob.getBbWidth() / 2.0f : 0.75f - (this.mob.getBbWidth() / 2.0f);
        Vec3i nextNodePos = this.path.getNextNodePos();
        if ((Math.abs(this.mob.getX() - (((double) nextNodePos.getX()) + 0.5d)) < ((double) this.maxDistanceToWaypoint) && Math.abs(this.mob.getZ() - (((double) nextNodePos.getZ()) + 0.5d)) < ((double) this.maxDistanceToWaypoint) && Math.abs(this.mob.getY() - ((double) nextNodePos.getY())) < 1.0d) || (this.mob.canCutCorner(this.path.getNextNode().type) && shouldTargetNextNodeInDirection(tempMobPos))) {
            this.path.advance();
        }
        doStuckDetection(tempMobPos);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 vec3) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        }
        Vec3 atBottomCenterOf = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        return vec3.closerThan(atBottomCenterOf, 2.0d) && Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1)).subtract(atBottomCenterOf).dot(vec3.subtract(atBottomCenterOf)) > 0.0d;
    }

    protected void doStuckDetection(Vec3 vec3) {
        if (this.tick - this.lastStuckCheck > 100) {
            if (vec3.distanceToSqr(this.lastStuckCheckPos) < 2.25d) {
                this.isStuck = true;
                stop();
            } else {
                this.isStuck = false;
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
                this.timeoutLimit = this.mob.getSpeed() > 0.0f ? (vec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode)) / this.mob.getSpeed()) * 1000.0d : 0.0d;
            }
            if (this.timeoutLimit > 0.0d && this.timeoutTimer > this.timeoutLimit * 3.0d) {
                timeoutPath();
            }
            this.lastTimeoutCheck = Util.getMillis();
        }
    }

    private void timeoutPath() {
        resetStuckTimeout();
        stop();
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0d;
        this.isStuck = false;
    }

    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress() {
        return !isDone();
    }

    public void stop() {
        this.path = null;
    }

    protected boolean isInLiquid() {
        return this.mob.isInWaterOrBubble() || this.mob.isInLava();
    }

    protected void trimPath() {
        if (this.path == null) {
            return;
        }
        for (int i = 0; i < this.path.getNodeCount(); i++) {
            Node node = this.path.getNode(i);
            Node node2 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
            if (this.level.getBlockState(new BlockPos(node.x, node.y, node.z)).is(Blocks.CAULDRON)) {
                this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
                if (node2 != null && node.y >= node2.y) {
                    this.path.replaceNode(i + 1, node.cloneAndMove(node2.x, node.y + 1, node2.z));
                }
            }
        }
    }

    public boolean isStableDestination(BlockPos blockPos) {
        BlockPos below = blockPos.below();
        return this.level.getBlockState(below).isSolidRender(this.level, below);
    }

    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean z) {
        this.nodeEvaluator.setCanFloat(z);
    }

    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    public void recomputePath(BlockPos blockPos) {
        if (this.path == null || this.path.isDone() || this.path.getNodeCount() == 0) {
            return;
        }
        Node endNode = this.path.getEndNode();
        if (blockPos.closerThan(new Vec3((endNode.x + this.mob.getX()) / 2.0d, (endNode.y + this.mob.getY()) / 2.0d, (endNode.z + this.mob.getZ()) / 2.0d), this.path.getNodeCount() - this.path.getNextNodeIndex())) {
            recomputePath();
        }
    }

    public boolean isStuck() {
        return this.isStuck;
    }
}
