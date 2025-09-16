package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/BreathAirGoal.class */
public class BreathAirGoal extends Goal {
    private final PathfinderMob mob;

    public BreathAirGoal(PathfinderMob pathfinderMob) {
        this.mob = pathfinderMob;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.mob.getAirSupply() < 140;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean isInterruptable() {
        return false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        findAirPosition();
    }

    private void findAirPosition() {
        BlockPos blockPos = null;
        Iterator<BlockPos> it = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 1.0d), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() - 1.0d), Mth.floor(this.mob.getX() + 1.0d), Mth.floor(this.mob.getY() + 8.0d), Mth.floor(this.mob.getZ() + 1.0d)).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            BlockPos next = it.next();
            if (givesAir(this.mob.level, next)) {
                blockPos = next;
                break;
            }
        }
        if (blockPos == null) {
            blockPos = new BlockPos(this.mob.getX(), this.mob.getY() + 8.0d, this.mob.getZ());
        }
        this.mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), 1.0d);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        findAirPosition();
        this.mob.moveRelative(0.02f, new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
    }

    private boolean givesAir(LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState = levelReader.getBlockState(blockPos);
        return (levelReader.getFluidState(blockPos).isEmpty() || blockState.is(Blocks.BUBBLE_COLUMN)) && blockState.isPathfindable(levelReader, blockPos, PathComputationType.LAND);
    }
}
