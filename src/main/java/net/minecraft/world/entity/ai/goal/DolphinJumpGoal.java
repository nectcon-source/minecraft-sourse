package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/DolphinJumpGoal.class */
public class DolphinJumpGoal extends JumpGoal {
    private static final int[] STEPS_TO_CHECK = {0, 1, 4, 5, 6, 7};
    private final Dolphin dolphin;
    private final int interval;
    private boolean breached;

    public DolphinJumpGoal(Dolphin dolphin, int i) {
        this.dolphin = dolphin;
        this.interval = i;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        Direction motionDirection = this.dolphin.getMotionDirection();
        int stepX = motionDirection.getStepX();
        int stepZ = motionDirection.getStepZ();
        BlockPos blockPosition = this.dolphin.blockPosition();
        for (int i : STEPS_TO_CHECK) {
            if (!waterIsClear(blockPosition, stepX, stepZ, i) || !surfaceIsClear(blockPosition, stepX, stepZ, i)) {
                return false;
            }
        }
        return true;
    }

    private boolean waterIsClear(BlockPos blockPos, int i, int i2, int i3) {
        BlockPos offset = blockPos.offset(i * i3, 0, i2 * i3);
        return this.dolphin.level.getFluidState(offset).is(FluidTags.WATER) && !this.dolphin.level.getBlockState(offset).getMaterial().blocksMotion();
    }

    private boolean surfaceIsClear(BlockPos blockPos, int i, int i2, int i3) {
        return this.dolphin.level.getBlockState(blockPos.offset(i * i3, 1, i2 * i3)).isAir() && this.dolphin.level.getBlockState(blockPos.offset(i * i3, 2, i2 * i3)).isAir();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        double d = this.dolphin.getDeltaMovement().y;
        return (d * d >= 0.029999999329447746d || this.dolphin.xRot == 0.0f || Math.abs(this.dolphin.xRot) >= 10.0f || !this.dolphin.isInWater()) && !this.dolphin.isOnGround();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean isInterruptable() {
        return false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        Direction motionDirection = this.dolphin.getMotionDirection();
        this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add(motionDirection.getStepX() * 0.6d, 0.7d, motionDirection.getStepZ() * 0.6d));
        this.dolphin.getNavigation().stop();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.dolphin.xRot = 0.0f;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        boolean z = this.breached;
        if (!z) {
            this.breached = this.dolphin.level.getFluidState(this.dolphin.blockPosition()).is(FluidTags.WATER);
        }
        if (this.breached && !z) {
            this.dolphin.playSound(SoundEvents.DOLPHIN_JUMP, 1.0f, 1.0f);
        }
        Vec3 deltaMovement = this.dolphin.getDeltaMovement();
        if (deltaMovement.y * deltaMovement.y < 0.029999999329447746d && this.dolphin.xRot != 0.0f) {
            this.dolphin.xRot = Mth.rotlerp(this.dolphin.xRot, 0.0f, 0.2f);
        } else {
            this.dolphin.xRot = (float) (Math.signum(-deltaMovement.y) * Math.acos(Math.sqrt(Entity.getHorizontalDistanceSqr(deltaMovement)) / deltaMovement.length()) * 57.2957763671875d);
        }
    }
}
