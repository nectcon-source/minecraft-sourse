package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ChunkTickList.class */
public class ChunkTickList<T> implements TickList<T> {
    private final List<ScheduledTick<T>> ticks;
    private final Function<T, ResourceLocation> toId;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ChunkTickList$ScheduledTick.class */
    static class ScheduledTick<T> {
        private final T type;
        public final BlockPos pos;
        public final int delay;
        public final TickPriority priority;

        private ScheduledTick(T t, BlockPos blockPos, int i, TickPriority tickPriority) {
            this.type = t;
            this.pos = blockPos;
            this.delay = i;
            this.priority = tickPriority;
        }

        public String toString() {
            return this.type + ": " + this.pos + ", " + this.delay + ", " + this.priority;
        }
    }

    public ChunkTickList(Function<T, ResourceLocation> function, List<TickNextTickData<T>> list, long j) {
//        this(function, list.stream().map(tickNextTickData -> {
//            return new ScheduledTick(tickNextTickData.getType(), tickNextTickData.pos, (int) (tickNextTickData.triggerTick - j), tickNextTickData.priority);
//        }).collect(Collectors.toList()));
        this(
                function,
                list.stream()
                        .<ScheduledTick<T>>map(tickNextTickData ->
                                new ScheduledTick<>(
                                        tickNextTickData.getType(),
                                        tickNextTickData.pos,
                                        (int)(tickNextTickData.triggerTick - j),
                                        tickNextTickData.priority
                                )
                        )
                        .collect(Collectors.toList())
        );
    }

    private ChunkTickList(Function<T, ResourceLocation> function, List<ScheduledTick<T>> list) {
        this.ticks = list;
        this.toId = function;
    }

    @Override // net.minecraft.world.level.TickList
    public boolean hasScheduledTick(BlockPos blockPos, T t) {
        return false;
    }

    @Override // net.minecraft.world.level.TickList
    public void scheduleTick(BlockPos blockPos, T t, int i, TickPriority tickPriority) {
        this.ticks.add(new ScheduledTick<>(t, blockPos, i, tickPriority));
    }

    @Override // net.minecraft.world.level.TickList
    public boolean willTickThisTick(BlockPos blockPos, T t) {
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public ListTag save() {
        ListTag listTag = new ListTag();
        for (ScheduledTick<T> scheduledTick : this.ticks) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("i", ((ResourceLocation) this.toId.apply((scheduledTick).type)).toString());
            compoundTag.putInt("x", scheduledTick.pos.getX());
            compoundTag.putInt("y", scheduledTick.pos.getY());
            compoundTag.putInt("z", scheduledTick.pos.getZ());
            compoundTag.putInt("t", scheduledTick.delay);
            compoundTag.putInt("p", scheduledTick.priority.getValue());
            listTag.add(compoundTag);
        }
        return listTag;
    }

    public static <T> ChunkTickList<T> create(ListTag listTag, Function<T, ResourceLocation> function, Function<ResourceLocation, T> function2) {
        List<ScheduledTick<T>> newArrayList = Lists.newArrayList();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            T apply = function2.apply(new ResourceLocation(compound.getString("i")));
            if (apply != null) {
                newArrayList.add(new ScheduledTick<>(apply, new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z")), compound.getInt("t"), TickPriority.byValue(compound.getInt("p"))));
            }
        }
        return new ChunkTickList<>(function, newArrayList);
    }

    public void copyOut(TickList<T> tickList) {
        this.ticks.forEach(scheduledTick -> {
            tickList.scheduleTick(scheduledTick.pos, scheduledTick.type, scheduledTick.delay, scheduledTick.priority);
        });
    }
}
