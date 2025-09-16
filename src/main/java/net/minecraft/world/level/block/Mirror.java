package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.Direction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/Mirror.class */
public enum Mirror {
    NONE(OctahedralGroup.IDENTITY),
    LEFT_RIGHT(OctahedralGroup.INVERT_Z),
    FRONT_BACK(OctahedralGroup.INVERT_X);

    private final OctahedralGroup rotation;

    Mirror(OctahedralGroup octahedralGroup) {
        this.rotation = octahedralGroup;
    }

    public int mirror(int i, int i2) {
        int i3 = i2 / 2;
        int i4 = i > i3 ? i - i2 : i;
        switch (this) {
            case FRONT_BACK:
                return (i2 - i4) % i2;
            case LEFT_RIGHT:
                return ((i3 - i4) + i2) % i2;
            default:
                return i;
        }
    }

    public Rotation getRotation(Direction direction) {
        Direction.Axis axis = direction.getAxis();
        return ((this == LEFT_RIGHT && axis == Direction.Axis.Z) || (this == FRONT_BACK && axis == Direction.Axis.X)) ? Rotation.CLOCKWISE_180 : Rotation.NONE;
    }

    public Direction mirror(Direction direction) {
        if (this == FRONT_BACK && direction.getAxis() == Direction.Axis.X) {
            return direction.getOpposite();
        }
        if (this == LEFT_RIGHT && direction.getAxis() == Direction.Axis.Z) {
            return direction.getOpposite();
        }
        return direction;
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }
}
