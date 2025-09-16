package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetStewEffectFunction.class */
public class SetStewEffectFunction extends LootItemConditionalFunction {
    private final Map<MobEffect, RandomValueBounds> effectDurationMap;

    private SetStewEffectFunction(LootItemCondition[] lootItemConditionArr, Map<MobEffect, RandomValueBounds> map) {
        super(lootItemConditionArr);
        this.effectDurationMap = ImmutableMap.copyOf(map);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() != Items.SUSPICIOUS_STEW || this.effectDurationMap.isEmpty()) {
            return itemStack;
        }
        Random random = lootContext.getRandom();
        Map.Entry<MobEffect, RandomValueBounds> entry = (Map.Entry) Iterables.get(this.effectDurationMap.entrySet(), random.nextInt(this.effectDurationMap.size()));
        MobEffect key = entry.getKey();
        int i = entry.getValue().getInt(random);
        if (!key.isInstantenous()) {
            i *= 20;
        }
        SuspiciousStewItem.saveMobEffect(itemStack, key, i);
        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetStewEffectFunction$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final Map<MobEffect, RandomValueBounds> effectDurationMap = Maps.newHashMap();

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        public Builder withEffect(MobEffect mobEffect, RandomValueBounds randomValueBounds) {
            this.effectDurationMap.put(mobEffect, randomValueBounds);
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new SetStewEffectFunction(getConditions(), this.effectDurationMap);
        }
    }

    public static Builder stewEffect() {
        return new Builder();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/SetStewEffectFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetStewEffectFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, SetStewEffectFunction setStewEffectFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject,  setStewEffectFunction, jsonSerializationContext);
            if (!setStewEffectFunction.effectDurationMap.isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (MobEffect mobEffect : setStewEffectFunction.effectDurationMap.keySet()) {
                    JsonObject jsonObject2 = new JsonObject();
                    ResourceLocation key = Registry.MOB_EFFECT.getKey(mobEffect);
                    if (key == null) {
                        throw new IllegalArgumentException("Don't know how to serialize mob effect " + mobEffect);
                    }
                    jsonObject2.add("type", new JsonPrimitive(key.toString()));
                    jsonObject2.add("duration", jsonSerializationContext.serialize(setStewEffectFunction.effectDurationMap.get(mobEffect)));
                    jsonArray.add(jsonObject2);
                }
                jsonObject.add("effects", jsonArray);
            }
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public SetStewEffectFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            Map<MobEffect, RandomValueBounds> newHashMap = Maps.newHashMap();
            if (jsonObject.has("effects")) {
                Iterator it = GsonHelper.getAsJsonArray(jsonObject, "effects").iterator();
                while (it.hasNext()) {
                    JsonElement jsonElement = (JsonElement) it.next();
                    String asString = GsonHelper.getAsString(jsonElement.getAsJsonObject(), "type");
                    newHashMap.put(Registry.MOB_EFFECT.getOptional(new ResourceLocation(asString)).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown mob effect '" + asString + "'");
                    }), (RandomValueBounds) GsonHelper.getAsObject(jsonElement.getAsJsonObject(), "duration", jsonDeserializationContext, RandomValueBounds.class));
                }
            }
            return new SetStewEffectFunction(lootItemConditionArr, newHashMap);
        }
    }
}
