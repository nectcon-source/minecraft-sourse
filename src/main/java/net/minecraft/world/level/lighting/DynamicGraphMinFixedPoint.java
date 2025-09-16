package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/DynamicGraphMinFixedPoint.class */
public abstract class DynamicGraphMinFixedPoint {
    private final int levelCount;
    private final LongLinkedOpenHashSet[] queues;
    private final Long2ByteMap computedLevels;
    private int firstQueuedLevel;
    private volatile boolean hasWork;

    protected abstract boolean isSource(long j);

    protected abstract int getComputedLevel(long j, long j2, int i);

    protected abstract void checkNeighborsAfterUpdate(long j, int i, boolean z);

    protected abstract int getLevel(long j);

    protected abstract void setLevel(long j, int i);

    protected abstract int computeLevelFromNeighbor(long j, long j2, int i);

    protected DynamicGraphMinFixedPoint(int i, final int i2, final int i3) {
        if (i >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        }
        this.levelCount = i;
        this.queues = new LongLinkedOpenHashSet[i];
        for (int i4 = 0; i4 < i; i4++) {
            this.queues[i4] = new LongLinkedOpenHashSet(i2, 0.5f) { // from class: net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint.1
                protected void rehash(int i5) {
                    if (i5 > i2) {
                        super.rehash(i5);
                    }
                }
            };
        }
        this.computedLevels = new Long2ByteOpenHashMap(i3, 0.5f) { // from class: net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint.2
            protected void rehash(int i5) {
                if (i5 > i3) {
                    super.rehash(i5);
                }
            }
        };
        this.computedLevels.defaultReturnValue((byte) -1);
        this.firstQueuedLevel = i;
    }

    private int getKey(int i, int i2) {
        int i3 = i;
        if (i3 > i2) {
            i3 = i2;
        }
        if (i3 > this.levelCount - 1) {
            i3 = this.levelCount - 1;
        }
        return i3;
    }

    private void checkFirstQueuedLevel(int i) {
        int i2 = this.firstQueuedLevel;
        this.firstQueuedLevel = i;
        for (int i3 = i2 + 1; i3 < i; i3++) {
            if (!this.queues[i3].isEmpty()) {
                this.firstQueuedLevel = i3;
                return;
            }
        }
    }

    protected void removeFromQueue(long j) {
        int i = this.computedLevels.get(j) & 255;
        if (i == 255) {
            return;
        }
        dequeue(j, getKey(getLevel(j), i), this.levelCount, true);
        this.hasWork = this.firstQueuedLevel < this.levelCount;
    }

    public void removeIf(LongPredicate longPredicate) {
        LongArrayList longArrayList = new LongArrayList();
        this.computedLevels.keySet().forEach((LongConsumer) j -> {
            if (longPredicate.test(j)) {
                longArrayList.add(j);
            }
        });
        longArrayList.forEach((LongConsumer) this::removeFromQueue);
    }

    private void dequeue(long j, int i, int i2, boolean z) {
        if (z) {
            this.computedLevels.remove(j);
        }
        this.queues[i].remove(j);
        if (this.queues[i].isEmpty() && this.firstQueuedLevel == i) {
            checkFirstQueuedLevel(i2);
        }
    }

    private void enqueue(long j, int i, int i2) {
        this.computedLevels.put(j, (byte) i);
        this.queues[i2].add(j);
        if (this.firstQueuedLevel > i2) {
            this.firstQueuedLevel = i2;
        }
    }

    protected void checkNode(long j) {
        checkEdge(j, j, this.levelCount - 1, false);
    }

    protected void checkEdge(long j, long j2, int i, boolean z) {
        checkEdge(j, j2, i, getLevel(j2), this.computedLevels.get(j2) & 255, z);
        this.hasWork = this.firstQueuedLevel < this.levelCount;
    }

    private void checkEdge(long j, long j2, int i, int i2, int i3, boolean z) {
        boolean z2;
        int clamp;
        if (isSource(j2)) {
            return;
        }
        int clamp2 = Mth.clamp(i, 0, this.levelCount - 1);
        int clamp3 = Mth.clamp(i2, 0, this.levelCount - 1);
        if (i3 == 255) {
            z2 = true;
            i3 = clamp3;
        } else {
            z2 = false;
        }
        if (z) {
            clamp = Math.min(i3, clamp2);
        } else {
            clamp = Mth.clamp(getComputedLevel(j2, j, clamp2), 0, this.levelCount - 1);
        }
        int key = getKey(clamp3, i3);
        if (clamp3 == clamp) {
            if (!z2) {
                dequeue(j2, key, this.levelCount, true);
            }
        } else {
            int key2 = getKey(clamp3, clamp);
            if (key != key2 && !z2) {
                dequeue(j2, key, key2, false);
            }
            enqueue(j2, clamp, key2);
        }
    }

    protected final void checkNeighbor(long j, long j2, int i, boolean z) {
        int i2;
        boolean z2;
        int i3 = this.computedLevels.get(j2) & 255;
        int clamp = Mth.clamp(computeLevelFromNeighbor(j, j2, i), 0, this.levelCount - 1);
        if (z) {
            checkEdge(j, j2, clamp, getLevel(j2), i3, true);
            return;
        }
        if (i3 == 255) {
            z2 = true;
            i2 = Mth.clamp(getLevel(j2), 0, this.levelCount - 1);
        } else {
            i2 = i3;
            z2 = false;
        }
        if (clamp == i2) {
            checkEdge(j, j2, this.levelCount - 1, z2 ? i2 : getLevel(j2), i3, false);
        }
    }

    protected final boolean hasWork() {
        return this.hasWork;
    }

    protected final int runUpdates(int i) {
        if (this.firstQueuedLevel >= this.levelCount) {
            return i;
        }
        while (this.firstQueuedLevel < this.levelCount && i > 0) {
            i--;
            LongLinkedOpenHashSet longLinkedOpenHashSet = this.queues[this.firstQueuedLevel];
            long removeFirstLong = longLinkedOpenHashSet.removeFirstLong();
            int clamp = Mth.clamp(getLevel(removeFirstLong), 0, this.levelCount - 1);
            if (longLinkedOpenHashSet.isEmpty()) {
                checkFirstQueuedLevel(this.levelCount);
            }
            int remove = this.computedLevels.remove(removeFirstLong) & 255;
            if (remove < clamp) {
                setLevel(removeFirstLong, remove);
                checkNeighborsAfterUpdate(removeFirstLong, remove, true);
            } else if (remove > clamp) {
                enqueue(removeFirstLong, remove, getKey(this.levelCount - 1, remove));
                setLevel(removeFirstLong, this.levelCount - 1);
                checkNeighborsAfterUpdate(removeFirstLong, clamp, false);
            }
        }
        this.hasWork = this.firstQueuedLevel < this.levelCount;
        return i;
    }

    public int getQueueSize() {
        return this.computedLevels.size();
    }
}
