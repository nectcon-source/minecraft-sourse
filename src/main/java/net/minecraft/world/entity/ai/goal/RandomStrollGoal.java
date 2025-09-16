package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RandomStrollGoal.class */
public class RandomStrollGoal extends Goal {
    protected final PathfinderMob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected final double speedModifier;
    protected int interval;
    protected boolean forceTrigger;
    private boolean checkNoActionTime;

    public RandomStrollGoal(PathfinderMob pathfinderMob, double d) {
        this(pathfinderMob, d, 120);
    }

    public RandomStrollGoal(PathfinderMob pathfinderMob, double d, int i) {
        this(pathfinderMob, d, i, true);
    }

    public RandomStrollGoal(PathfinderMob pathfinderMob, double d, int i, boolean z) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.interval = i;
        this.checkNoActionTime = z;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        Vec3 position;
        if (this.mob.isVehicle()) {
            return false;
        }
        if ((!this.forceTrigger && ((this.checkNoActionTime && this.mob.getNoActionTime() >= 100) || this.mob.getRandom().nextInt(this.interval) != 0)) || (position = getPosition()) == null) {
            return false;
        }
        this.wantedX = position.x;
        this.wantedY = position.y;
        this.wantedZ = position.z;
        this.forceTrigger = false;
        return true;
    }

    @Nullable
    protected Vec3 getPosition() {
        return RandomPos.getPos(this.mob, 10, 7);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return (this.mob.getNavigation().isDone() || this.mob.isVehicle()) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.mob.getNavigation().stop();
        super.stop();
    }

    public void trigger() {
        this.forceTrigger = true;
    }

    public void setInterval(int i) {
        this.interval = i;
    }
}
