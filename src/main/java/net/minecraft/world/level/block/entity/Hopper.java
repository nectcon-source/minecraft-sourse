package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/Hopper.class */
public interface Hopper extends Container {
    public static final VoxelShape INSIDE = Block.box(2.0d, 11.0d, 2.0d, 14.0d, 16.0d, 14.0d);
    public static final VoxelShape ABOVE = Block.box(0.0d, 16.0d, 0.0d, 16.0d, 32.0d, 16.0d);
    public static final VoxelShape SUCK = Shapes.or(INSIDE, ABOVE);

    @Nullable
    Level getLevel();

    double getLevelX();

    double getLevelY();

    double getLevelZ();

    default VoxelShape getSuckShape() {
        return SUCK;
    }
}
