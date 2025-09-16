package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/HashMapPalette.class */
public class HashMapPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;
    private final CrudeIncrementalIntIdentityHashBiMap<T> values;
    private final PaletteResize<T> resizeHandler;
    private final Function<CompoundTag, T> reader;
    private final Function<T, CompoundTag> writer;
    private final int bits;

    public HashMapPalette(IdMapper<T> idMapper, int i, PaletteResize<T> paletteResize, Function<CompoundTag, T> function, Function<T, CompoundTag> function2) {
        this.registry = idMapper;
        this.bits = i;
        this.resizeHandler = paletteResize;
        this.reader = function;
        this.writer = function2;
        this.values = new CrudeIncrementalIntIdentityHashBiMap<>(1 << i);
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public int idFor(T t) {
        int id = this.values.getId(t);
        if (id == -1) {
            id = this.values.add(t);
            if (id >= (1 << this.bits)) {
                id = this.resizeHandler.onResize(this.bits + 1, t);
            }
        }
        return id;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < getSize(); i++) {
            if (predicate.test(this.values.byId(i))) {
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    @Nullable
    public T valueFor(int i) {
        return this.values.byId(i);
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.values.clear();
        int readVarInt = friendlyByteBuf.readVarInt();
        for (int i = 0; i < readVarInt; i++) {
            this.values.add(this.registry.byId(friendlyByteBuf.readVarInt()));
        }
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void write(FriendlyByteBuf friendlyByteBuf) {
        int size = getSize();
        friendlyByteBuf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            friendlyByteBuf.writeVarInt(this.registry.getId(this.values.byId(i)));
        }
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public int getSerializedSize() {
        int varIntSize = FriendlyByteBuf.getVarIntSize(getSize());
        for (int i = 0; i < getSize(); i++) {
            varIntSize += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(i)));
        }
        return varIntSize;
    }

    public int getSize() {
        return this.values.size();
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void read(ListTag listTag) {
        this.values.clear();
        for (int i = 0; i < listTag.size(); i++) {
            this.values.add(this.reader.apply(listTag.getCompound(i)));
        }
    }

    public void write(ListTag listTag) {
        for (int i = 0; i < getSize(); i++) {
            listTag.add(this.writer.apply(this.values.byId(i)));
        }
    }
}
