package net.minecraft.world.level.block.piston;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/piston/PistonMath.class */
public class PistonMath {
    public static AABB getMovementArea(AABB aabb, Direction direction, double d) {
        double step = d * direction.getAxisDirection().getStep();
        double min = Math.min(step, 0.0d);
        double max = Math.max(step, 0.0d);
        switch (direction) {
            case WEST:
                return new AABB(aabb.minX + min, aabb.minY, aabb.minZ, aabb.minX + max, aabb.maxY, aabb.maxZ);
            case EAST:
                return new AABB(aabb.maxX + min, aabb.minY, aabb.minZ, aabb.maxX + max, aabb.maxY, aabb.maxZ);
            case DOWN:
                return new AABB(aabb.minX, aabb.minY + min, aabb.minZ, aabb.maxX, aabb.minY + max, aabb.maxZ);
            case UP:
            default:
                return new AABB(aabb.minX, aabb.maxY + min, aabb.minZ, aabb.maxX, aabb.maxY + max, aabb.maxZ);
            case NORTH:
                return new AABB(aabb.minX, aabb.minY, aabb.minZ + min, aabb.maxX, aabb.maxY, aabb.minZ + max);
            case SOUTH:
                return new AABB(aabb.minX, aabb.minY, aabb.maxZ + min, aabb.maxX, aabb.maxY, aabb.maxZ + max);
        }
    }
}
