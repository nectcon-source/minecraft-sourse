package net.minecraft.world.level.storage.loot.entries;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootPoolEntry.class */
public interface LootPoolEntry {
    int getWeight(float f);

    void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext);
}
