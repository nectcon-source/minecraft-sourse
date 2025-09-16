package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/FlyingMoveControl.class */
public class FlyingMoveControl extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;

    public FlyingMoveControl(Mob mob, int i, boolean z) {
        super(mob);
        this.maxTurn = i;
        this.hoversInPlace = z;
    }

    @Override // net.minecraft.world.entity.p000ai.control.MoveControl
    public void tick() {
        float attributeValue;
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            this.mob.setNoGravity(true);
            double x = this.wantedX - this.mob.getX();
            double y = this.wantedY - this.mob.getY();
            double z = this.wantedZ - this.mob.getZ();
            if ((x * x) + (y * y) + (z * z) < 2.500000277905201E-7d) {
                this.mob.setYya(0.0f);
                this.mob.setZza(0.0f);
                return;
            }
            this.mob.yRot = rotlerp(this.mob.yRot, ((float) (Mth.atan2(z, x) * 57.2957763671875d)) - 90.0f, 90.0f);
            if (this.mob.isOnGround()) {
                attributeValue = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                attributeValue = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
            }
            this.mob.setSpeed(attributeValue);
            this.mob.xRot = rotlerp(this.mob.xRot, (float) (-(Mth.atan2(y, Mth.sqrt((x * x) + (z * z))) * 57.2957763671875d)), this.maxTurn);
            this.mob.setYya(y > 0.0d ? attributeValue : -attributeValue);
            return;
        }
        if (!this.hoversInPlace) {
            this.mob.setNoGravity(false);
        }
        this.mob.setYya(0.0f);
        this.mob.setZza(0.0f);
    }
}
