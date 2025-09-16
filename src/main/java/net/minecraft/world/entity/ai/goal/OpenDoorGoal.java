package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/OpenDoorGoal.class */
public class OpenDoorGoal extends DoorInteractGoal {
    private final boolean closeDoor;
    private int forgetTime;

    public OpenDoorGoal(Mob mob, boolean z) {
        super(mob);
        this.mob = mob;
        this.closeDoor = z;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.closeDoor && this.forgetTime > 0 && super.canContinueToUse();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.forgetTime = 20;
        setOpen(true);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        setOpen(false);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.forgetTime--;
        super.tick();
    }
}
