package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/SwimNodeEvaluator.class */
public class SwimNodeEvaluator extends NodeEvaluator {
    private final boolean allowBreaching;

    public SwimNodeEvaluator(boolean z) {
        this.allowBreaching = z;
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public Node getStart() {
        return super.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5d), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public Target getGoal(double d, double d2, double d3) {
        return new Target(super.getNode(Mth.floor(d - (this.mob.getBbWidth() / 2.0f)), Mth.floor(d2 + 0.5d), Mth.floor(d3 - (this.mob.getBbWidth() / 2.0f))));
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public int getNeighbors(Node[] nodeArr, Node node) {
        int i = 0;
        for (Direction direction : Direction.values()) {
            Node waterNode = getWaterNode(node.x + direction.getStepX(), node.y + direction.getStepY(), node.z + direction.getStepZ());
            if (waterNode != null && !waterNode.closed) {
                int i2 = i;
                i++;
                nodeArr[i2] = waterNode;
            }
        }
        return i;
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3, Mob mob, int i4, int i5, int i6, boolean z, boolean z2) {
        return getBlockPathType(blockGetter, i, i2, i3);
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3) {
        BlockPos blockPos = new BlockPos(i, i2, i3);
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (fluidState.isEmpty() && blockState.isPathfindable(blockGetter, blockPos.below(), PathComputationType.WATER) && blockState.isAir()) {
            return BlockPathTypes.BREACH;
        }
        if (!fluidState.is(FluidTags.WATER) || !blockState.isPathfindable(blockGetter, blockPos, PathComputationType.WATER)) {
            return BlockPathTypes.BLOCKED;
        }
        return BlockPathTypes.WATER;
    }

    @Nullable
    private Node getWaterNode(int i, int i2, int i3) {
        BlockPathTypes isFree = isFree(i, i2, i3);
        if ((this.allowBreaching && isFree == BlockPathTypes.BREACH) || isFree == BlockPathTypes.WATER) {
            return getNode(i, i2, i3);
        }
        return null;
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    @Nullable
    protected Node getNode(int i, int i2, int i3) {
        Node node = null;
        BlockPathTypes blockPathType = getBlockPathType(this.mob.level, i, i2, i3);
        float pathfindingMalus = this.mob.getPathfindingMalus(blockPathType);
        if (pathfindingMalus >= 0.0f) {
            node = super.getNode(i, i2, i3);
            node.type = blockPathType;
            node.costMalus = Math.max(node.costMalus, pathfindingMalus);
            if (this.level.getFluidState(new BlockPos(i, i2, i3)).isEmpty()) {
                node.costMalus += 8.0f;
            }
        }
        if (blockPathType == BlockPathTypes.OPEN) {
            return node;
        }
        return node;
    }

    private BlockPathTypes isFree(int i, int i2, int i3) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i4 = i; i4 < i + this.entityWidth; i4++) {
            for (int i5 = i2; i5 < i2 + this.entityHeight; i5++) {
                for (int i6 = i3; i6 < i3 + this.entityDepth; i6++) {
                    FluidState fluidState = this.level.getFluidState(mutableBlockPos.set(i4, i5, i6));
                    BlockState blockState = this.level.getBlockState(mutableBlockPos.set(i4, i5, i6));
                    if (fluidState.isEmpty() && blockState.isPathfindable(this.level, mutableBlockPos.below(), PathComputationType.WATER) && blockState.isAir()) {
                        return BlockPathTypes.BREACH;
                    }
                    if (!fluidState.is(FluidTags.WATER)) {
                        return BlockPathTypes.BLOCKED;
                    }
                }
            }
        }
        if (this.level.getBlockState(mutableBlockPos).isPathfindable(this.level, mutableBlockPos, PathComputationType.WATER)) {
            return BlockPathTypes.WATER;
        }
        return BlockPathTypes.BLOCKED;
    }
}
