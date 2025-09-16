package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BucketPickup.class */
public interface BucketPickup {
    Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState);
}
