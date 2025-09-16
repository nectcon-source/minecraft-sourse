package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/LevelWriter.class */
public interface LevelWriter {
    boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int i2);

    boolean removeBlock(BlockPos blockPos, boolean z);

    boolean destroyBlock(BlockPos blockPos, boolean z, @Nullable Entity entity, int i);

    default boolean setBlock(BlockPos blockPos, BlockState blockState, int i) {
        return setBlock(blockPos, blockState, i, 512);
    }

    default boolean destroyBlock(BlockPos blockPos, boolean z) {
        return destroyBlock(blockPos, z, null);
    }

    default boolean destroyBlock(BlockPos blockPos, boolean z, @Nullable Entity entity) {
        return destroyBlock(blockPos, z, entity, 512);
    }

    default boolean addFreshEntity(Entity entity) {
        return false;
    }
}
