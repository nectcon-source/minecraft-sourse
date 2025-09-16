package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/EmptyTickList.class */
public class EmptyTickList<T> implements TickList<T> {
    private static final EmptyTickList<Object> INSTANCE = new EmptyTickList<>();

    public static <T> EmptyTickList<T> empty() {
        return (EmptyTickList<T>) INSTANCE;
    }

    @Override // net.minecraft.world.level.TickList
    public boolean hasScheduledTick(BlockPos blockPos, T t) {
        return false;
    }

    @Override // net.minecraft.world.level.TickList
    public void scheduleTick(BlockPos blockPos, T t, int i) {
    }

    @Override // net.minecraft.world.level.TickList
    public void scheduleTick(BlockPos blockPos, T t, int i, TickPriority tickPriority) {
    }

    @Override // net.minecraft.world.level.TickList
    public boolean willTickThisTick(BlockPos blockPos, T t) {
        return false;
    }
}
