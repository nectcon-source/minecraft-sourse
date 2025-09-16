package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/DolphinLookControl.class */
public class DolphinLookControl extends LookControl {
    private final int maxYRotFromCenter;

    public DolphinLookControl(Mob mob, int i) {
        super(mob);
        this.maxYRotFromCenter = i;
    }

    @Override // net.minecraft.world.entity.p000ai.control.LookControl
    public void tick() {
        if (this.hasWanted) {
            this.hasWanted = false;
            this.mob.yHeadRot = rotateTowards(this.mob.yHeadRot, getYRotD() + 20.0f, this.yMaxRotSpeed);
            this.mob.xRot = rotateTowards(this.mob.xRot, getXRotD() + 10.0f, this.xMaxRotAngle);
        } else {
            if (this.mob.getNavigation().isDone()) {
                this.mob.xRot = rotateTowards(this.mob.xRot, 0.0f, 5.0f);
            }
            this.mob.yHeadRot = rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, this.yMaxRotSpeed);
        }
        float wrapDegrees = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);
        if (wrapDegrees < (-this.maxYRotFromCenter)) {
            this.mob.yBodyRot -= 4.0f;
        } else if (wrapDegrees > this.maxYRotFromCenter) {
            this.mob.yBodyRot += 4.0f;
        }
    }
}
