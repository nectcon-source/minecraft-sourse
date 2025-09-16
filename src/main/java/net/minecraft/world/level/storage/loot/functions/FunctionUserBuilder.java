package net.minecraft.world.level.storage.loot.functions;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/FunctionUserBuilder.class */
public interface FunctionUserBuilder<T> {
    T apply(LootItemFunction.Builder builder);

    T unwrap();
}
