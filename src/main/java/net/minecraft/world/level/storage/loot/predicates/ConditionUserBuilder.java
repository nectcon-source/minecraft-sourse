package net.minecraft.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/ConditionUserBuilder.class */
public interface ConditionUserBuilder<T> {
    T when(LootItemCondition.Builder builder);

    T unwrap();
}
