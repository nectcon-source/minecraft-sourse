package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/GlobalPalette.class */
public class GlobalPalette<T> implements Palette<T> {
    private final IdMapper<T> registry;
    private final T defaultValue;

    public GlobalPalette(IdMapper<T> idMapper, T t) {
        this.registry = idMapper;
        this.defaultValue = t;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public int idFor(T t) {
        int id = this.registry.getId(t);
        if (id == -1) {
            return 0;
        }
        return id;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public boolean maybeHas(Predicate<T> predicate) {
        return true;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public T valueFor(int i) {
        T byId = this.registry.byId(i);
        return byId == null ? this.defaultValue : byId;
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void read(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void write(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public int getSerializedSize() {
        return FriendlyByteBuf.getVarIntSize(0);
    }

    @Override // net.minecraft.world.level.chunk.Palette
    public void read(ListTag listTag) {
    }
}
