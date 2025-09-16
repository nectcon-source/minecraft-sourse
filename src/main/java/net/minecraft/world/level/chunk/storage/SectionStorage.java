package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/SectionStorage.class */
public class SectionStorage<R> implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IOWorker worker;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap();
    private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
    private final Function<Runnable, Codec<R>> codec;
    private final Function<Runnable, R> factory;
    private final DataFixer fixerUpper;
    private final DataFixTypes type;

    public SectionStorage(File file, Function<Runnable, Codec<R>> function, Function<Runnable, R> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean z) {
        this.codec = function;
        this.factory = function2;
        this.fixerUpper = dataFixer;
        this.type = dataFixTypes;
        this.worker = new IOWorker(file, z, file.getName());
    }

    protected void tick(BooleanSupplier booleanSupplier) {
        while (!this.dirty.isEmpty() && booleanSupplier.getAsBoolean()) {
            writeColumn(SectionPos.of(this.dirty.firstLong()).chunk());
        }
    }

    @Nullable
    protected Optional<R> get(long j) {
        return  this.storage.get(j);
    }

    protected Optional<R> getOrLoad(long j) {
        SectionPos m39of = SectionPos.of(j);
        if (outsideStoredRange(m39of)) {
            return Optional.empty();
        }
        Optional<R> optional = get(j);
        if (optional != null) {
            return optional;
        }
        readColumn(m39of.chunk());
        Optional<R> optional2 = get(j);
        if (optional2 == null) {
            throw ((IllegalStateException) Util.pauseInIde(new IllegalStateException()));
        }
        return optional2;
    }

    protected boolean outsideStoredRange(SectionPos sectionPos) {
        return Level.isOutsideBuildHeight(SectionPos.sectionToBlockCoord(sectionPos.y()));
    }

    protected R getOrCreate(long j) {
        Optional<R> orLoad = getOrLoad(j);
        if (orLoad.isPresent()) {
            return orLoad.get();
        }
        R apply = this.factory.apply(() -> {
            setDirty(j);
        });
        this.storage.put(j, Optional.of(apply));
        return apply;
    }

    private void readColumn(ChunkPos chunkPos) {
        readColumn(chunkPos, NbtOps.INSTANCE, tryRead(chunkPos));
    }

    @Nullable
    private CompoundTag tryRead(ChunkPos chunkPos) {
        try {
            return this.worker.load(chunkPos);
        } catch (IOException e) {
            LOGGER.error("Error reading chunk {} data from disk", chunkPos, e);
            return null;
        }
    }

    private <T> void readColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps, @Nullable T t) {
        if (t == null) {
            for (int i = 0; i < 16; i++) {
                this.storage.put(SectionPos.of(chunkPos, i).asLong(), Optional.empty());
            }
            return;
        }
        Dynamic<T> dynamic = new Dynamic<>(dynamicOps, t);
        int version = getVersion(dynamic);
        int worldVersion = SharedConstants.getCurrentVersion().getWorldVersion();
        boolean z = version != worldVersion;
        OptionalDynamic<T> optionalDynamic = this.fixerUpper.update(this.type.getType(), dynamic, version, worldVersion).get("Sections");
        for (int i2 = 0; i2 < 16; i2++) {
            long asLong = SectionPos.of(chunkPos, i2).asLong();
            Optional<R> flatMap = optionalDynamic.get(Integer.toString(i2)).result().flatMap(dynamic2 -> {
                DataResult parse = this.codec.apply(() -> {
                    setDirty(asLong);
                }).parse(dynamic2);
                Logger logger = LOGGER;
                logger.getClass();
                return parse.resultOrPartial(logger::error);
            });
            this.storage.put(asLong, flatMap);
            flatMap.ifPresent(obj -> {
                onSectionLoad(asLong);
                if (z) {
                    setDirty(asLong);
                }
            });
        }
    }

    private void writeColumn(ChunkPos chunkPos) {
        Tag tag = (Tag) writeColumn(chunkPos, NbtOps.INSTANCE).getValue();
        if (tag instanceof CompoundTag) {
            this.worker.store(chunkPos, (CompoundTag) tag);
        } else {
            LOGGER.error("Expected compound tag, got {}", tag);
        }
    }

    private <T> Dynamic<T> writeColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
        Map<T, T> newHashMap = Maps.newHashMap();
        for (int i = 0; i < 16; i++) {
            long asLong = SectionPos.of(chunkPos, i).asLong();
            this.dirty.remove(asLong);
            Optional<R> optional = (Optional) this.storage.get(asLong);
            if (optional != null && optional.isPresent()) {
                DataResult<T> encodeStart = this.codec.apply(() -> {
                    setDirty(asLong);
                }).encodeStart(dynamicOps, optional.get());
                String num = Integer.toString(i);
                Logger logger = LOGGER;
                logger.getClass();
                encodeStart.resultOrPartial(logger::error).ifPresent(obj -> {
                    newHashMap.put(dynamicOps.createString(num), obj);
                });
            }
        }
        return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Sections"), dynamicOps.createMap(newHashMap), dynamicOps.createString("DataVersion"), dynamicOps.createInt(SharedConstants.getCurrentVersion().getWorldVersion()))));
    }

    protected void onSectionLoad(long j) {
    }

    protected void setDirty(long j) {
        Optional<R> optional = (Optional) this.storage.get(j);
        if (optional == null || !optional.isPresent()) {
            LOGGER.warn("No data for position: {}", SectionPos.of(j));
        } else {
            this.dirty.add(j);
        }
    }

    private static int getVersion(Dynamic<?> dynamic) {
        return dynamic.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkPos chunkPos) {
        if (!this.dirty.isEmpty()) {
            for (int i = 0; i < 16; i++) {
                if (this.dirty.contains(SectionPos.of(chunkPos, i).asLong())) {
                    writeColumn(chunkPos);
                    return;
                }
            }
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        this.worker.close();
    }
}
