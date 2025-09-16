package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/NoiseColumn.class */
public final class NoiseColumn implements BlockGetter {
    private final BlockState[] column;

    public NoiseColumn(BlockState[] blockStateArr) {
        this.column = blockStateArr;
    }

    @Override // net.minecraft.world.level.BlockGetter
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override // net.minecraft.world.level.BlockGetter
    public BlockState getBlockState(BlockPos blockPos) {
        int y = blockPos.getY();
        if (y < 0 || y >= this.column.length) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.column[y];
    }

    @Override // net.minecraft.world.level.BlockGetter
    public FluidState getFluidState(BlockPos blockPos) {
        return getBlockState(blockPos).getFluidState();
    }
}
