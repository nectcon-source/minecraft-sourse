package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyExplosionDecay.class */
public class ApplyExplosionDecay extends LootItemConditionalFunction {
    private ApplyExplosionDecay(LootItemCondition[] lootItemConditionArr) {
        super(lootItemConditionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.EXPLOSION_DECAY;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Float f = (Float) lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (f != null) {
            Random random = lootContext.getRandom();
            float floatValue = 1.0f / f.floatValue();
            int count = itemStack.getCount();
            int i = 0;
            for (int i2 = 0; i2 < count; i2++) {
                if (random.nextFloat() <= floatValue) {
                    i++;
                }
            }
            itemStack.setCount(i);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> explosionDecay() {
        return simpleBuilder(ApplyExplosionDecay::new);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/ApplyExplosionDecay$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyExplosionDecay> {
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public ApplyExplosionDecay deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            return new ApplyExplosionDecay(lootItemConditionArr);
        }
    }
}
