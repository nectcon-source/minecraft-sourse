package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/PanicGoal.class */
public class PanicGoal extends Goal {
    protected final PathfinderMob mob;
    protected final double speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected boolean isRunning;

    public PanicGoal(PathfinderMob pathfinderMob, double d) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        BlockPos lookForWater;
        if (this.mob.getLastHurtByMob() == null && !this.mob.isOnFire()) {
            return false;
        }
        if (this.mob.isOnFire() && (lookForWater = lookForWater(this.mob.level, this.mob, 5, 4)) != null) {
            this.posX = lookForWater.getX();
            this.posY = lookForWater.getY();
            this.posZ = lookForWater.getZ();
            return true;
        }
        return findRandomPosition();
    }

    protected boolean findRandomPosition() {
        Vec3 pos = RandomPos.getPos(this.mob, 5, 4);
        if (pos == null) {
            return false;
        }
        this.posX = pos.x;
        this.posY = pos.y;
        this.posZ = pos.z;
        return true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
        this.isRunning = true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.isRunning = false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected BlockPos lookForWater(BlockGetter blockGetter, Entity entity, int i, int i2) {
        BlockPos blockPosition = entity.blockPosition();
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();
        float f = i * i * i2 * 2;
        BlockPos blockPos = null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i3 = x - i; i3 <= x + i; i3++) {
            for (int i4 = y - i2; i4 <= y + i2; i4++) {
                for (int i5 = z - i; i5 <= z + i; i5++) {
                    mutableBlockPos.set(i3, i4, i5);
                    if (blockGetter.getFluidState(mutableBlockPos).is(FluidTags.WATER)) {
                        float f2 = ((i3 - x) * (i3 - x)) + ((i4 - y) * (i4 - y)) + ((i5 - z) * (i5 - z));
                        if (f2 < f) {
                            f = f2;
                            blockPos = new BlockPos(mutableBlockPos);
                        }
                    }
                }
            }
        }
        return blockPos;
    }
}
