package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/EmptyLootItem.class */
public class EmptyLootItem extends LootPoolSingletonContainer {
    private EmptyLootItem(int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
        super(i, i2, lootItemConditionArr, lootItemFunctionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.EMPTY;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
    }

    public static LootPoolSingletonContainer.Builder<?> emptyItem() {
        return simpleBuilder(EmptyLootItem::new);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/EmptyLootItem$Serializer.class */
    public static class Serializer extends LootPoolSingletonContainer.Serializer<EmptyLootItem> {
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer
        public EmptyLootItem deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
            return new EmptyLootItem(i, i2, lootItemConditionArr, lootItemFunctionArr);
        }
    }
}
