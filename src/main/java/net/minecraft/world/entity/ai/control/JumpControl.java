package net.minecraft.world.entity.ai.control;

import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/JumpControl.class */
public class JumpControl {
    private final Mob mob;
    protected boolean jump;

    public JumpControl(Mob mob) {
        this.mob = mob;
    }

    public void jump() {
        this.jump = true;
    }

    public void tick() {
        this.mob.setJumping(this.jump);
        this.jump = false;
    }
}
