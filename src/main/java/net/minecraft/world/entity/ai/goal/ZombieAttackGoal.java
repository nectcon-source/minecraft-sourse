package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.monster.Zombie;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/ZombieAttackGoal.class */
public class ZombieAttackGoal extends MeleeAttackGoal {
    private final Zombie zombie;
    private int raiseArmTicks;

    public ZombieAttackGoal(Zombie zombie, double d, boolean z) {
        super(zombie, d, z);
        this.zombie = zombie;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        super.stop();
        this.zombie.setAggressive(false);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MeleeAttackGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        super.tick();
        this.raiseArmTicks++;
        if (this.raiseArmTicks >= 5 && getTicksUntilNextAttack() < getAttackInterval() / 2) {
            this.zombie.setAggressive(true);
        } else {
            this.zombie.setAggressive(false);
        }
    }
}
