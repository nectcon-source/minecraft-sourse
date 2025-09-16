package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetItemCountFunction.class */
public class SetItemCountFunction extends LootItemConditionalFunction {
    private final RandomIntGenerator value;

    private SetItemCountFunction(LootItemCondition[] lootItemConditionArr, RandomIntGenerator randomIntGenerator) {
        super(lootItemConditionArr);
        this.value = randomIntGenerator;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.setCount(this.value.getInt(lootContext.getRandom()));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(RandomIntGenerator randomIntGenerator) {
        return simpleBuilder(lootItemConditionArr -> {
            return new SetItemCountFunction(lootItemConditionArr, randomIntGenerator);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetItemCountFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetItemCountFunction setItemCountFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setItemCountFunction, jsonSerializationContext);
            jsonObject.add("count", RandomIntGenerators.serialize(setItemCountFunction.value, jsonSerializationContext));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetItemCountFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new SetItemCountFunction(lootItemConditionArr, RandomIntGenerators.deserialize(jsonObject.get("count"), jsonDeserializationContext));
        }
    }
}
