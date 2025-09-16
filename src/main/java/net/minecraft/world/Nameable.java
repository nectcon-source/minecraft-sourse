package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/Nameable.class */
public interface Nameable {
    Component getName();

    default boolean hasCustomName() {
        return getCustomName() != null;
    }

    default Component getDisplayName() {
        return getName();
    }

    @Nullable
    default Component getCustomName() {
        return null;
    }
}
