package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FleeSunGoal.class */
public class FleeSunGoal extends Goal {
    protected final PathfinderMob mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final Level level;

    public FleeSunGoal(PathfinderMob pathfinderMob, double d) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.level = pathfinderMob.level;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.getTarget() != null || !this.level.isDay() || !this.mob.isOnFire() || !this.level.canSeeSky(this.mob.blockPosition()) || !this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return false;
        }
        return setWantedPos();
    }

    protected boolean setWantedPos() {
        Vec3 hidePos = getHidePos();
        if (hidePos == null) {
            return false;
        }
        this.wantedX = hidePos.x;
        this.wantedY = hidePos.y;
        this.wantedZ = hidePos.z;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Nullable
    protected Vec3 getHidePos() {
        Random random = this.mob.getRandom();
        BlockPos blockPosition = this.mob.blockPosition();
        for (int i = 0; i < 10; i++) {
            BlockPos offset = blockPosition.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.level.canSeeSky(offset) && this.mob.getWalkTargetValue(offset) < 0.0f) {
                return Vec3.atBottomCenterOf(offset);
            }
        }
        return null;
    }
}
