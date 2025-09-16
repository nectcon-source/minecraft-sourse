package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LimitCount.class */
public class LimitCount extends LootItemConditionalFunction {
    private final IntLimiter limiter;

    private LimitCount(LootItemCondition[] lootItemConditionArr, IntLimiter intLimiter) {
        super(lootItemConditionArr);
        this.limiter = intLimiter;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.LIMIT_COUNT;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.setCount(this.limiter.applyAsInt(itemStack.getCount()));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntLimiter intLimiter) {
        return simpleBuilder(lootItemConditionArr -> {
            return new LimitCount(lootItemConditionArr, intLimiter);
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LimitCount$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, LimitCount limitCount, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  limitCount, jsonSerializationContext);
            jsonObject.add("limit", jsonSerializationContext.serialize(limitCount.limiter));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public LimitCount deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new LimitCount(lootItemConditionArr, (IntLimiter) GsonHelper.getAsObject(jsonObject, "limit", jsonDeserializationContext, IntLimiter.class));
        }
    }
}
