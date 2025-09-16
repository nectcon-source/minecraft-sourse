package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/phys/shapes/CollisionContext.class */
public interface CollisionContext {
    boolean isDescending();

    boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean z);

    boolean isHoldingItem(Item item);

    boolean canStandOnFluid(FluidState fluidState, FlowingFluid flowingFluid);

    static CollisionContext empty() {
        return EntityCollisionContext.EMPTY;
    }

    /* renamed from: of */
    static CollisionContext of(Entity entity) {
        return new EntityCollisionContext(entity);
    }
}
