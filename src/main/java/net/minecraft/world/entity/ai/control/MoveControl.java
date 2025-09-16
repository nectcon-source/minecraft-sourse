package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/MoveControl.class */
public class MoveControl {
    protected final Mob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected Operation operation;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/control/MoveControl$Operation.class */
    public enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING
    }

    public MoveControl(Mob mob) {
        this.operation = MoveControl.Operation.WAIT;
        this.mob = mob;
    }

    public boolean hasWanted() {
        return this.operation == Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public void setWantedPosition(double d, double d2, double d3, double d4) {
        this.wantedX = d;
        this.wantedY = d2;
        this.wantedZ = d3;
        this.speedModifier = d4;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }
    }

    public void strafe(float f, float f2) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = f;
        this.strafeRight = f2;
        this.speedModifier = 0.25d;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float attributeValue = ((float) this.speedModifier) * ((float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f = this.strafeForwards;
            float f2 = this.strafeRight;
            float sqrt = Mth.sqrt((f * f) + (f2 * f2));
            if (sqrt < 1.0f) {
                sqrt = 1.0f;
            }
            float f3 = attributeValue / sqrt;
            float f4 = f * f3;
            float f5 = f2 * f3;
            float sin = Mth.sin(this.mob.yRot * 0.017453292f);
            float cos = Mth.cos(this.mob.yRot * 0.017453292f);
            if (!isWalkable((f4 * cos) - (f5 * sin), (f5 * cos) + (f4 * sin))) {
                this.strafeForwards = 1.0f;
                this.strafeRight = 0.0f;
            }
            this.mob.setSpeed(attributeValue);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
            return;
        }
        if (this.operation != Operation.MOVE_TO) {
            if (this.operation == Operation.JUMPING) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.mob.isOnGround()) {
                    this.operation = Operation.WAIT;
                    return;
                }
                return;
            }
            this.mob.setZza(0.0f);
            return;
        }
        this.operation = Operation.WAIT;
        double x = this.wantedX - this.mob.getX();
        double z = this.wantedZ - this.mob.getZ();
        double y = this.wantedY - this.mob.getY();
        if ((x * x) + (y * y) + (z * z) < 2.500000277905201E-7d) {
            this.mob.setZza(0.0f);
            return;
        }
        this.mob.yRot = rotlerp(this.mob.yRot, ((float) (Mth.atan2(z, x) * 57.2957763671875d)) - 90.0f, 90.0f);
        this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        BlockPos blockPosition = this.mob.blockPosition();
        BlockState blockState = this.mob.level.getBlockState(blockPosition);
        Block block = blockState.getBlock();
        VoxelShape collisionShape = blockState.getCollisionShape(this.mob.level, blockPosition);
        if ((y > this.mob.maxUpStep && (x * x) + (z * z) < Math.max(1.0f, this.mob.getBbWidth())) || (!collisionShape.isEmpty() && this.mob.getY() < collisionShape.max(Direction.Axis.Y) + blockPosition.getY() && !block.is(BlockTags.DOORS) && !block.is(BlockTags.FENCES))) {
            this.mob.getJumpControl().jump();
            this.operation = Operation.JUMPING;
        }
    }

    private boolean isWalkable(float f, float f2) {
        NodeEvaluator nodeEvaluator;
        PathNavigation navigation = this.mob.getNavigation();
        if (navigation != null && (nodeEvaluator = navigation.getNodeEvaluator()) != null && nodeEvaluator.getBlockPathType(this.mob.level, Mth.floor(this.mob.getX() + f), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() + f2)) != BlockPathTypes.WALKABLE) {
            return false;
        }
        return true;
    }

    protected float rotlerp(float f, float f2, float f3) {
        float wrapDegrees = Mth.wrapDegrees(f2 - f);
        if (wrapDegrees > f3) {
            wrapDegrees = f3;
        }
        if (wrapDegrees < (-f3)) {
            wrapDegrees = -f3;
        }
        float f4 = f + wrapDegrees;
        if (f4 < 0.0f) {
            f4 += 360.0f;
        } else if (f4 > 360.0f) {
            f4 -= 360.0f;
        }
        return f4;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }
}
