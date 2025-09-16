package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/ShulkerSharedHelper.class */
public class ShulkerSharedHelper {
    public static AABB openBoundingBox(BlockPos blockPos, Direction direction) {
        return Shapes.block().bounds().expandTowards(0.5f * direction.getStepX(), 0.5f * direction.getStepY(), 0.5f * direction.getStepZ()).contract(direction.getStepX(), direction.getStepY(), direction.getStepZ()).move(blockPos.relative(direction));
    }
}
