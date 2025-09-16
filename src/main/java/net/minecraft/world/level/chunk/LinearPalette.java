package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/LinearPalette.class */
public class LinearPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;
    private final T[] values;
    private final PaletteResize<T> resizeHandler;
    private final Function<CompoundTag, T> reader;
    private final int bits;
    private int size;

    public LinearPalette(IdMapper<T> idMapper, int i, PaletteResize<T> paletteResize, Function<CompoundTag, T> function) {
        this.registry = idMapper;
        this.values = (T[]) new Object[1 << i];
        this.bits = i;
        this.resizeHandler = paletteResize;
        this.reader = function;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public int idFor(T t) {
        for (int i = 0; i < this.size; i++) {
            if (this.values[i] == t) {
                return i;
            }
        }
        int i2 = this.size;
        if (i2 < this.values.length) {
            this.values[i2] = t;
            this.size++;
            return i2;
        }
        return this.resizeHandler.onResize(this.bits + 1, t);
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.size; i++) {
            if (predicate.test(this.values[i])) {
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    @Nullable
    public T valueFor(int i) {
        if (i >= 0 && i < this.size) {
            return this.values[i];
        }
        return null;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.size = friendlyByteBuf.readVarInt();
        for (int i = 0; i < this.size; i++) {
            this.values[i] = this.registry.byId(friendlyByteBuf.readVarInt());
        }
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.size);
        for (int i = 0; i < this.size; i++) {
            friendlyByteBuf.writeVarInt(this.registry.getId(this.values[i]));
        }
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public int getSerializedSize() {
        int varIntSize = FriendlyByteBuf.getVarIntSize(getSize());
        for (int i = 0; i < getSize(); i++) {
            varIntSize += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[i]));
        }
        return varIntSize;
    }

    public int getSize() {
        return this.size;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void read(ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            this.values[i] = this.reader.apply(listTag.getCompound(i));
        }
        this.size = listTag.size();
    }
}
