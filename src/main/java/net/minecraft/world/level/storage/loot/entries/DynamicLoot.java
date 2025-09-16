package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/DynamicLoot.class */
public class DynamicLoot extends LootPoolSingletonContainer {
    private final ResourceLocation name;

    private DynamicLoot(ResourceLocation resourceLocation, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
        super(i, i2, lootItemConditionArr, lootItemFunctionArr);
        this.name = resourceLocation;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.DYNAMIC;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        lootContext.addDynamicDrops(this.name, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation resourceLocation) {
        return simpleBuilder((i, i2, lootItemConditionArr, lootItemFunctionArr) -> {
            return new DynamicLoot(resourceLocation, i, i2, lootItemConditionArr, lootItemFunctionArr);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/DynamicLoot$Serializer.class */
    public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
        public void serializeCustom(JsonObject jsonObject, DynamicLoot dynamicLoot, JsonSerializationContext jsonSerializationContext) {
            super.serializeCustom(jsonObject,  dynamicLoot, jsonSerializationContext);
            jsonObject.addProperty("name", dynamicLoot.name.toString());
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Serializer
        public DynamicLoot deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int i2, LootItemCondition[] lootItemConditionArr, LootItemFunction[] lootItemFunctionArr) {
            return new DynamicLoot(new ResourceLocation(GsonHelper.getAsString(jsonObject, "name")), i, i2, lootItemConditionArr, lootItemFunctionArr);
        }
    }
}
