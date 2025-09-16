package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootItem.class */
public class LootItem extends LootPoolSingletonContainer {
    private final Item item;

    private LootItem(Item item, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
        super(i, i2, lootItemConditionArr, lootItemFunctionArr);
        this.item = item;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.ITEM;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        consumer.accept(new ItemStack(this.item));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike itemLike) {
        return simpleBuilder((i, i2, lootItemConditionArr, lootItemFunctionArr) -> {
            return new LootItem(itemLike.asItem(), i, i2, lootItemConditionArr, lootItemFunctionArr);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/LootItem$Serializer.class */
    public static class Serializer extends LootPoolSingletonContainer.Serializer<LootItem> {
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
        public void serializeCustom(JsonObject jsonObject, LootItem lootItem, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject,  lootItem, jsonSerializationContext);
            ResourceLocation key = Registry.ITEM.getKey(lootItem.item);
            if (key == null) {
                throw new IllegalArgumentException("Can't serialize unknown item " + lootItem.item);
            }
            jsonObject.addProperty("name", key.toString());
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer
        public LootItem deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
            return new LootItem(GsonHelper.getAsItem(jsonObject, "name"), i, i2, lootItemConditionArr, lootItemFunctionArr);
        }
    }
}
