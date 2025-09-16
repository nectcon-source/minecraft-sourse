package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/Palette.class */
public interface Palette<T> {
    int idFor(T t);

    boolean maybeHas(Predicate<T> predicate);

    @Nullable
    T valueFor(int i);

    void read(FriendlyByteBuf friendlyByteBuf);

    void write(FriendlyByteBuf friendlyByteBuf);

    int getSerializedSize();

    void read(ListTag listTag);
}
