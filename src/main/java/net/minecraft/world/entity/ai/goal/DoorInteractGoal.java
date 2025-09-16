package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/DoorInteractGoal.class */
public abstract class DoorInteractGoal extends Goal {
    protected Mob mob;
    protected BlockPos doorPos = BlockPos.ZERO;
    protected boolean hasDoor;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public DoorInteractGoal(Mob mob) {
        this.mob = mob;
        if (!GoalUtils.hasGroundPathNavigation(mob)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    protected boolean isOpen() {
        if (!this.hasDoor) {
            return false;
        }
        BlockState blockState = this.mob.level.getBlockState(this.doorPos);
        if (!(blockState.getBlock() instanceof DoorBlock)) {
            this.hasDoor = false;
            return false;
        }
        return ((Boolean) blockState.getValue(DoorBlock.OPEN)).booleanValue();
    }

    protected void setOpen(boolean z) {
        if (this.hasDoor) {
            BlockState blockState = this.mob.level.getBlockState(this.doorPos);
            if (blockState.getBlock() instanceof DoorBlock) {
                ((DoorBlock) blockState.getBlock()).setOpen(this.mob.level, blockState, this.doorPos, z);
            }
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        GroundPathNavigation groundPathNavigation;
        Path path;
        if (!GoalUtils.hasGroundPathNavigation(this.mob) || !this.mob.horizontalCollision || (path = (groundPathNavigation = (GroundPathNavigation) this.mob.getNavigation()).getPath()) == null || path.isDone() || !groundPathNavigation.canOpenDoors()) {
            return false;
        }
        for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
            Node node = path.getNode(i);
            this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
            if (this.mob.distanceToSqr(this.doorPos.getX(), this.mob.getY(), this.doorPos.getZ()) <= 2.25d) {
                this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
                if (this.hasDoor) {
                    return true;
                }
            }
        }
        this.doorPos = this.mob.blockPosition().above();
        this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
        return this.hasDoor;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return !this.passed;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.passed = false;
        this.doorOpenDirX = (float) ((this.doorPos.getX() + 0.5d) - this.mob.getX());
        this.doorOpenDirZ = (float) ((this.doorPos.getZ() + 0.5d) - this.mob.getZ());
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if ((this.doorOpenDirX * ((float) ((this.doorPos.getX() + 0.5d) - this.mob.getX()))) + (this.doorOpenDirZ * ((float) ((this.doorPos.getZ() + 0.5d) - this.mob.getZ()))) < 0.0f) {
            this.passed = true;
        }
    }
}
