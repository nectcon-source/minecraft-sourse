package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/TickList.class */
public interface TickList<T> {
    boolean hasScheduledTick(BlockPos blockPos, T t);

    void scheduleTick(BlockPos blockPos, T t, int i, TickPriority tickPriority);

    boolean willTickThisTick(BlockPos blockPos, T t);

    default void scheduleTick(BlockPos blockPos, T t, int i) {
        scheduleTick(blockPos, t, i, TickPriority.NORMAL);
    }
}
