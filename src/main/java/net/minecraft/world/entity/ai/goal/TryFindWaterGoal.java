package net.minecraft.world.entity.ai.goal;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/TryFindWaterGoal.class */
public class TryFindWaterGoal extends Goal {
    private final PathfinderMob mob;

    public TryFindWaterGoal(PathfinderMob pathfinderMob) {
        this.mob = pathfinderMob;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.mob.isOnGround() && !this.mob.level.getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        BlockPos blockPos = null;
        Iterator<BlockPos> it = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 2.0d), Mth.floor(this.mob.getY() - 2.0d), Mth.floor(this.mob.getZ() - 2.0d), Mth.floor(this.mob.getX() + 2.0d), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() + 2.0d)).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            BlockPos next = it.next();
            if (this.mob.level.getFluidState(next).is(FluidTags.WATER)) {
                blockPos = next;
                break;
            }
        }
        if (blockPos != null) {
            this.mob.getMoveControl().setWantedPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0d);
        }
    }
}
