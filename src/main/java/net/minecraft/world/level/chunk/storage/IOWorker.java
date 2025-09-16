package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/IOWorker.class */
public class IOWorker implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
    private final RegionFileStorage storage;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final Map<ChunkPos, PendingStore> pendingWrites = Maps.newLinkedHashMap();

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/IOWorker$Priority.class */
    enum Priority {
        HIGH,
        LOW
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/IOWorker$PendingStore.class */
    static class PendingStore {
        private CompoundTag data;
        private final CompletableFuture<Void> result = new CompletableFuture<>();

        public PendingStore(CompoundTag compoundTag) {
            this.data = compoundTag;
        }
    }

    protected IOWorker(File file, boolean z, String str) {
        this.storage = new RegionFileStorage(file, z);
        this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(Priority.values().length), Util.ioPool(), "IOWorker-" + str);
    }

    public CompletableFuture<Void> store(ChunkPos chunkPos, CompoundTag compoundTag) {
        return submitTask(() -> {
            PendingStore computeIfAbsent = this.pendingWrites.computeIfAbsent(chunkPos, chunkPos2 -> {
                return new PendingStore(compoundTag);
            });
            computeIfAbsent.data = compoundTag;
            return Either.left(computeIfAbsent.result);
        }).thenCompose(Function.identity());
    }

    @Nullable
    public CompoundTag load(ChunkPos chunkPos) throws IOException {
        try {
            return submitTask(() -> {
                PendingStore pendingStore = this.pendingWrites.get(chunkPos);
                if (pendingStore != null) {
                    return Either.left(pendingStore.data);
                }
                try {
                    return Either.left(this.storage.read(chunkPos));
                } catch (Exception e) {
                    LOGGER.warn("Failed to read chunk {}", chunkPos, e);
                    return Either.right(e);
                }
            }).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException) {
                throw ((IOException) e.getCause());
            }
            throw e;
        }
    }

    public CompletableFuture<Void> synchronize() {
        CompletableFuture<Void> var1 = this.submitTask(() -> Either.left(CompletableFuture.allOf((CompletableFuture[])this.pendingWrites.values().stream().map((var0) -> var0.result).toArray((var0) -> new CompletableFuture[var0])))).thenCompose(Function.identity());
        return var1.thenCompose((var1x) -> this.submitTask(() -> {
            try {
                this.storage.flush();
                return Either.left(null);
            } catch (Exception var1_1) {
                LOGGER.warn("Failed to synchronized chunks", var1_1);
                return Either.right(var1_1);
            }
        }));
    }

    private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier) {
        return this.mailbox.askEither((var2) -> new StrictQueue.IntRunnable(IOWorker.Priority.HIGH.ordinal(), () -> {
            if (!this.shutdownRequested.get()) {
                var2.tell(supplier.get());
            }

            this.tellStorePending();
        }));
    }

    private void storePendingChunk() {
        Iterator<Map.Entry<ChunkPos, PendingStore>> it = this.pendingWrites.entrySet().iterator();
        if (!it.hasNext()) {
            return;
        }
        Map.Entry<ChunkPos, PendingStore> next = it.next();
        it.remove();
        runStore(next.getKey(), next.getValue());
        tellStorePending();
    }

    private void tellStorePending() {
        this.mailbox.tell(new StrictQueue.IntRunnable(Priority.LOW.ordinal(), this::storePendingChunk));
    }

    private void runStore(ChunkPos chunkPos, PendingStore pendingStore) {
        try {
            this.storage.write(chunkPos, pendingStore.data);
            pendingStore.result.complete(null);
        } catch (Exception e) {
            LOGGER.error("Failed to store chunk {}", chunkPos, e);
            pendingStore.result.completeExceptionally(e);
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        if (!this.shutdownRequested.compareAndSet(false, true)) {
            return;
        }
        try {
            this.mailbox.ask(processorHandle -> {
                return new StrictQueue.IntRunnable(Priority.HIGH.ordinal(), () -> {
                    processorHandle.tell(Unit.INSTANCE);
                });
            }).join();
            this.mailbox.close();
            this.pendingWrites.forEach(this::runStore);
            this.pendingWrites.clear();
            try {
                this.storage.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close storage", e);
            }
        } catch (CompletionException e2) {
            if (e2.getCause() instanceof IOException) {
                throw ((IOException) e2.getCause());
            }
            throw e2;
        }
    }
}
