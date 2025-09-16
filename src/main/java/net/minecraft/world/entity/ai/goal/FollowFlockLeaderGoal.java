package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FollowFlockLeaderGoal.class */
public class FollowFlockLeaderGoal extends Goal {
    private final AbstractSchoolingFish mob;
    private int timeToRecalcPath;
    private int nextStartTick;

    public FollowFlockLeaderGoal(AbstractSchoolingFish abstractSchoolingFish) {
        this.mob = abstractSchoolingFish;
        this.nextStartTick = nextStartTick(abstractSchoolingFish);
    }

    protected int nextStartTick(AbstractSchoolingFish abstractSchoolingFish) {
        return 200 + (abstractSchoolingFish.getRandom().nextInt(200) % 20);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.hasFollowers()) {
            return false;
        }
        if (this.mob.isFollower()) {
            return true;
        }
        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        }
        this.nextStartTick = nextStartTick(this.mob);
        List<AbstractSchoolingFish> entitiesOfClass = this.mob.level.getEntitiesOfClass(this.mob.getClass(), this.mob.getBoundingBox().inflate(8.0d, 8.0d, 8.0d), abstractSchoolingFish -> {
            return abstractSchoolingFish.canBeFollowed() || !abstractSchoolingFish.isFollower();
        });
        entitiesOfClass.stream().filter((v0) -> {
            return v0.canBeFollowed();
        }).findAny().orElse(this.mob).addFollowers(entitiesOfClass.stream().filter(abstractSchoolingFish2 -> {
            return !abstractSchoolingFish2.isFollower();
        }));
        return this.mob.isFollower();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.mob.isFollower() && this.mob.inRangeOfLeader();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.mob.stopFollowing();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        int i = this.timeToRecalcPath - 1;
        this.timeToRecalcPath = i;
        if (i > 0) {
            return;
        }
        this.timeToRecalcPath = 10;
        this.mob.pathToLeader();
    }
}
