package net.minecraft.world;

import javax.annotation.Nullable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/Clearable.class */
public interface Clearable {
    void clearContent();

    static void tryClear(@Nullable Object obj) {
        if (obj instanceof Clearable) {
            ((Clearable) obj).clearContent();
        }
    }
}
