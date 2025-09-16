package net.minecraft.world.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/TickNextTickData.class */
public class TickNextTickData<T> {
    private static long counter;
    private final T type;
    public final BlockPos pos;
    public final long triggerTick;
    public final TickPriority priority;

    /* renamed from: c */
    private final long c;

    public TickNextTickData(BlockPos blockPos, T t) {
        this(blockPos, t, 0L, TickPriority.NORMAL);
    }

    public TickNextTickData(BlockPos blockPos, T t, long j, TickPriority tickPriority) {
        long j2 = counter;
        counter = j2 + 1;
        this.c = j2;
        this.pos = blockPos.immutable();
        this.type = t;
        this.triggerTick = j;
        this.priority = tickPriority;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TickNextTickData) {
            TickNextTickData<?> tickNextTickData = (TickNextTickData) obj;
            return this.pos.equals(tickNextTickData.pos) && this.type == tickNextTickData.type;
        }
        return false;
    }

    public int hashCode() {
        return this.pos.hashCode();
    }

    public static <T> Comparator<TickNextTickData<T>> createTimeComparator() {
        return Comparator.<TickNextTickData<T>>comparingLong(var0 -> var0.triggerTick).thenComparing(var0 -> var0.priority).thenComparingLong(var0 -> var0.c);
    }

    public String toString() {
        return this.type + ": " + this.pos + ", " + this.triggerTick + ", " + this.priority + ", " + this.c;
    }

    public T getType() {
        return this.type;
    }
}
