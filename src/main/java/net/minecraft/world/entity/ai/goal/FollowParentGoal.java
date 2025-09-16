package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.animal.Animal;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/FollowParentGoal.class */
public class FollowParentGoal extends Goal {
    private final Animal animal;
    private Animal parent;
    private final double speedModifier;
    private int timeToRecalcPath;

    public FollowParentGoal(Animal animal, double d) {
        this.animal = animal;
        this.speedModifier = d;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.animal.getAge() >= 0) {
            return false;
        }
        Animal animal = null;
        double d = Double.MAX_VALUE;
        for (Animal animal2 : this.animal.level.getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(8.0d, 4.0d, 8.0d))) {
            if (animal2.getAge() >= 0) {
                double distanceToSqr = this.animal.distanceToSqr(animal2);
                if (distanceToSqr <= d) {
                    d = distanceToSqr;
                    animal = animal2;
                }
            }
        }
        if (animal == null || d < 9.0d) {
            return false;
        }
        this.parent = animal;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        if (this.animal.getAge() >= 0 || !this.parent.isAlive()) {
            return false;
        }
        double distanceToSqr = this.animal.distanceToSqr(this.parent);
        if (distanceToSqr < 9.0d || distanceToSqr > 256.0d) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.parent = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        int i = this.timeToRecalcPath - 1;
        this.timeToRecalcPath = i;
        if (i > 0) {
            return;
        }
        this.timeToRecalcPath = 10;
        this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
    }
}
