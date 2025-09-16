package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/ServerTickList.class */
public class ServerTickList<T> implements TickList<T> {
    protected final Predicate<T> ignore;
    private final Function<T, ResourceLocation> toId;
    private final ServerLevel level;
    private final Consumer<TickNextTickData<T>> ticker;
    private final Set<TickNextTickData<T>> tickNextTickSet = Sets.newHashSet();
    private final TreeSet<TickNextTickData<T>> tickNextTickList = Sets.newTreeSet(TickNextTickData.createTimeComparator());
    private final Queue<TickNextTickData<T>> currentlyTicking = Queues.newArrayDeque();
    private final List<TickNextTickData<T>> alreadyTicked = Lists.newArrayList();

    public ServerTickList(ServerLevel serverLevel, Predicate<T> predicate, Function<T, ResourceLocation> function, Consumer<TickNextTickData<T>> consumer) {
        this.ignore = predicate;
        this.toId = function;
        this.level = serverLevel;
        this.ticker = consumer;
    }

    public void tick() {
        int size = this.tickNextTickList.size();
        if (size != this.tickNextTickSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        if (size > 65536) {
            size = 65536;
        }
        ServerChunkCache chunkSource = this.level.getChunkSource();
        Iterator<TickNextTickData<T>> it = this.tickNextTickList.iterator();
        this.level.getProfiler().push("cleaning");
        while (size > 0 && it.hasNext()) {
            TickNextTickData<T> next = it.next();
            if (next.triggerTick > this.level.getGameTime()) {
                break;
            }
            if (chunkSource.isTickingChunk(next.pos)) {
                it.remove();
                this.tickNextTickSet.remove(next);
                this.currentlyTicking.add(next);
                size--;
            }
        }
        this.level.getProfiler().popPush("ticking");
        while (true) {
            TickNextTickData<T> poll = this.currentlyTicking.poll();
            if (poll != null) {
                if (chunkSource.isTickingChunk(poll.pos)) {
                    try {
                        this.alreadyTicked.add(poll);
                        this.ticker.accept(poll);
                    } catch (Throwable th) {
                        CrashReport forThrowable = CrashReport.forThrowable(th, "Exception while ticking");
                        CrashReportCategory.populateBlockDetails(forThrowable.addCategory("Block being ticked"), poll.pos, null);
                        throw new ReportedException(forThrowable);
                    }
                } else {
                    scheduleTick(poll.pos, poll.getType(), 0);
                }
            } else {
                this.level.getProfiler().pop();
                this.alreadyTicked.clear();
                this.currentlyTicking.clear();
                return;
            }
        }
    }

    @Override // net.minecraft.world.level.TickList
    public boolean willTickThisTick(BlockPos blockPos, T t) {
        return this.currentlyTicking.contains(new TickNextTickData(blockPos, t));
    }

    public List<TickNextTickData<T>> fetchTicksInChunk(ChunkPos chunkPos, boolean z, boolean z2) {
        int i = (chunkPos.x << 4) - 2;
        int i2 = i + 16 + 2;
        int i3 = (chunkPos.z << 4) - 2;
        return fetchTicksInArea(new BoundingBox(i, 0, i3, i2, 256, i3 + 16 + 2), z, z2);
    }

    public List<TickNextTickData<T>> fetchTicksInArea(BoundingBox boundingBox, boolean z, boolean z2) {
        List<TickNextTickData<T>> fetchTicksInArea = fetchTicksInArea(null, this.tickNextTickList, boundingBox, z);
        if (z && fetchTicksInArea != null) {
            this.tickNextTickSet.removeAll(fetchTicksInArea);
        }
        List<TickNextTickData<T>> fetchTicksInArea2 = fetchTicksInArea(fetchTicksInArea, this.currentlyTicking, boundingBox, z);
        if (!z2) {
            fetchTicksInArea2 = fetchTicksInArea(fetchTicksInArea2, this.alreadyTicked, boundingBox, z);
        }
        return fetchTicksInArea2 == null ? Collections.emptyList() : fetchTicksInArea2;
    }

    @Nullable
    private List<TickNextTickData<T>> fetchTicksInArea(@Nullable List<TickNextTickData<T>> list, Collection<TickNextTickData<T>> collection, BoundingBox boundingBox, boolean z) {
        Iterator<TickNextTickData<T>> it = collection.iterator();
        while (it.hasNext()) {
            TickNextTickData<T> next = it.next();
            BlockPos blockPos = next.pos;
            if (blockPos.getX() >= boundingBox.x0 && blockPos.getX() < boundingBox.x1 && blockPos.getZ() >= boundingBox.z0 && blockPos.getZ() < boundingBox.z1) {
                if (z) {
                    it.remove();
                }
                if (list == null) {
                    list = Lists.newArrayList();
                }
                list.add(next);
            }
        }
        return list;
    }

    public void copy(BoundingBox boundingBox, BlockPos blockPos) {
        for (TickNextTickData<T> tickNextTickData : fetchTicksInArea(boundingBox, false, false)) {
            if (boundingBox.isInside(tickNextTickData.pos)) {
                addTickData(new TickNextTickData<>(tickNextTickData.pos.offset(blockPos), tickNextTickData.getType(), tickNextTickData.triggerTick, tickNextTickData.priority));
            }
        }
    }

    public ListTag save(ChunkPos chunkPos) {
        return saveTickList(this.toId, fetchTicksInChunk(chunkPos, false, true), this.level.getGameTime());
    }

    private static <T> ListTag saveTickList(Function<T, ResourceLocation> function, Iterable<TickNextTickData<T>> iterable, long j) {
        ListTag listTag = new ListTag();
        for (TickNextTickData<T> tickNextTickData : iterable) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("i", function.apply(tickNextTickData.getType()).toString());
            compoundTag.putInt("x", tickNextTickData.pos.getX());
            compoundTag.putInt("y", tickNextTickData.pos.getY());
            compoundTag.putInt("z", tickNextTickData.pos.getZ());
            compoundTag.putInt("t", (int) (tickNextTickData.triggerTick - j));
            compoundTag.putInt("p", tickNextTickData.priority.getValue());
            listTag.add(compoundTag);
        }
        return listTag;
    }

    @Override // net.minecraft.world.level.TickList
    public boolean hasScheduledTick(BlockPos blockPos, T t) {
        return this.tickNextTickSet.contains(new TickNextTickData(blockPos, t));
    }

    @Override // net.minecraft.world.level.TickList
    public void scheduleTick(BlockPos blockPos, T t, int i, TickPriority tickPriority) {
        if (!this.ignore.test(t)) {
            addTickData(new TickNextTickData<>(blockPos, t, i + this.level.getGameTime(), tickPriority));
        }
    }

    private void addTickData(TickNextTickData<T> tickNextTickData) {
        if (!this.tickNextTickSet.contains(tickNextTickData)) {
            this.tickNextTickSet.add(tickNextTickData);
            this.tickNextTickList.add(tickNextTickData);
        }
    }

    public int size() {
        return this.tickNextTickSet.size();
    }
}
