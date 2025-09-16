package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
    protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, Items.AIR, var0 -> false) {
        @Override
        public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3) {
            return var3;
        }
    };
    private final boolean descending;
    private final double entityBottom;
    private final Item heldItem;
    private final Predicate<Fluid> canStandOnFluid;

    protected EntityCollisionContext(boolean var1, double var2, Item var4, Predicate<Fluid> var5) {
        this.descending = var1;
        this.entityBottom = var2;
        this.heldItem = var4;
        this.canStandOnFluid = var5;
    }

    @Deprecated
    protected EntityCollisionContext(Entity var1) {
        this(
                var1.isDescending(),
                var1.getY(),
                var1 instanceof LivingEntity ? ((LivingEntity)var1).getMainHandItem().getItem() : Items.AIR,
                var1 instanceof LivingEntity ? ((LivingEntity)var1)::canStandOnFluid : var0 -> false
        );
    }

    @Override
    public boolean isHoldingItem(Item var1) {
        return this.heldItem == var1;
    }


    @Override
    public boolean canStandOnFluid(FluidState var1, FlowingFluid var2) {
        return this.canStandOnFluid.test(var2) && !var1.getType().isSame(var2);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }


    @Override
    public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3) {
        return this.entityBottom > (double)var2.getY() + var1.max(Direction.Axis.Y) - (double)1.0E-5F;
    }
}
