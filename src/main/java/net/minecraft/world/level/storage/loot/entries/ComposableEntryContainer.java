package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;

/* JADX INFO: Access modifiers changed from: package-private */
@FunctionalInterface
/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/ComposableEntryContainer.class */
public interface ComposableEntryContainer {
    public static final ComposableEntryContainer ALWAYS_FALSE = (lootContext, consumer) -> {
        return false;
    };
    public static final ComposableEntryContainer ALWAYS_TRUE = (lootContext, consumer) -> {
        return true;
    };

    boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer);

    default ComposableEntryContainer and(ComposableEntryContainer composableEntryContainer) {
        Objects.requireNonNull(composableEntryContainer);
        return (lootContext, consumer) -> {
            return expand(lootContext, consumer) && composableEntryContainer.expand(lootContext, consumer);
        };
    }

    /* renamed from: or */
    default ComposableEntryContainer or(ComposableEntryContainer composableEntryContainer) {
        Objects.requireNonNull(composableEntryContainer);
        return (lootContext, consumer) -> {
            return expand(lootContext, consumer) || composableEntryContainer.expand(lootContext, consumer);
        };
    }
}
