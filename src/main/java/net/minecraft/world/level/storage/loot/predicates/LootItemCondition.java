package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemCondition.class */
public interface LootItemCondition extends LootContextUser, Predicate<LootContext> {
    LootItemConditionType getType();

    @FunctionalInterface
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/predicates/LootItemCondition$Builder.class */
    public interface Builder {
        LootItemCondition build();

        default Builder invert() {
            return InvertedLootItemCondition.invert(this);
        }

        /* renamed from: or */
        default AlternativeLootItemCondition.Builder or(Builder builder) {
            return AlternativeLootItemCondition.alternative(this, builder);
        }
    }
}
